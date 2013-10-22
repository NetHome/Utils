/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome.
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.util.ps.impl;

import gnu.io.*;
import nu.nethome.util.ps.ProtocolDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * The CULProtocolPort interfaces with an USB radio transceiver and sends and receives data
 * in form of pulse trains to/from the transceiver. The CUL transceiver can be found at:
 * {@see http://busware.de/tiki-index.php?page=CUL}. Special firmware from NetHome is
 * needed on the CUL.
 * 
 * @author Stefan
 *
 */
public class CULProtocolPort implements SerialPortEventListener, Runnable, PulseProtocolPort {

	// Addresses of the CC1101 control registers
	protected static final int IOCFG2     = 0x00;
	protected static final int IOCFG1     = 0x01;
	protected static final int IOCFG0     = 0x02;
	protected static final int FIFOTHR    = 0x03;
	protected static final int SYNC1      = 0x04;
	protected static final int SYNC0      = 0x05;
	protected static final int PKTLEN     = 0x06;
	protected static final int PKTCTRL1   = 0x07;
	protected static final int PKTCTRL0   = 0x08;
	protected static final int ADDR       = 0x09;
	protected static final int CHANNR     = 0x0A;
	protected static final int FSCTRL1    = 0x0B;
	protected static final int FSCTRL0    = 0x0C;
	protected static final int FREQ2      = 0x0D;
	protected static final int FREQ1      = 0x0E;
	protected static final int FREQ0      = 0x0F;
	protected static final int MDMCFG4    = 0x10;
	protected static final int MDMCFG3    = 0x11;
	protected static final int MDMCFG2    = 0x12;
	protected static final int MDMCFG1    = 0x13;
	protected static final int MDMCFG0    = 0x14;
	protected static final int DEVIATN    = 0x15;
	protected static final int MCSM2      = 0x16;
	protected static final int MCSM1      = 0x17;
	protected static final int MCSM0      = 0x18;
	protected static final int FOCCFG     = 0x19;
	protected static final int BSCFG      = 0x1A;
	protected static final int AGCCTRL2   = 0x1B;
	protected static final int AGCCTRL1   = 0x1C;
	protected static final int AGCCTRL0   = 0x1D;
	protected static final int WOREVT1    = 0x1E;
	protected static final int WOREVT0    = 0x1F;
	protected static final int WORCTRL    = 0x20;
	protected static final int FREND1     = 0x21;
	protected static final int FREND0     = 0x22;
	protected static final int FSCAL3     = 0x23;
	protected static final int FSCAL2     = 0x24;
	protected static final int FSCAL1     = 0x25;
	protected static final int FSCAL0     = 0x26;
	protected static final int RCCTRL1    = 0x27;
	protected static final int RCCTRL0    = 0x28;

	// Available receiver bandwidths in frequency order. 
	// bandwidth ordinal refers to the index in this table.
	protected static final double bandwidths[] = {
		58000,  68000,  81000,  102000, 
		116000, 135000, 162000, 203000,
		232000, 270000, 325000, 406000, 
		464000, 541000, 650000, 812000
	};
	
	// CC1101 register settings for the MDMCFG4 7:4 register
	// corresponding to the bandwidths in the bandwidths-table.
	protected static final int bandwidthSettingsMDMCFG4[] = {
		0xF0, 0xE0, 0xD0, 0xC0, 
		0xB0, 0xA0, 0x90, 0x80,
		0x70, 0x60, 0x50, 0x40, 
		0x30, 0x20, 0x10, 0x00
	};

	// CC1101 register settings for the FSCTRL1 4:0 register
	// corresponding to the bandwidths in the bandwidths-table.
	protected static final int bandwidthSettingsFSCTRL1[] = {
		0x06, 0x06, 0x06, 0x06, 
		0x08, 0x08, 0x08, 0x08,
		0x08, 0x08, 0x08, 0x0C, 
		0x0C, 0x0C, 0x0E, 0x0E
	};

		
	protected static final int bandwidthvalues[] = {
	};
	
	private static final int READ_BUFFER_SIZE = 40;
	private static final double CRYSTAL_FREQUENCY = 26000000;
	protected int m_ReadBufferPointer = 0;
	String m_ComPort = "COM4";
    static Enumeration<CommPortIdentifier> portList;
    protected InputStream m_InputStream;

	protected OutputStream m_OutputStream;
    protected SerialPort m_SerialPort;
    protected CommPortIdentifier portId = null;
	protected boolean m_IsOpen = false;
    protected byte[] m_ReadBuffer = new byte[READ_BUFFER_SIZE];
	private ProtocolDecoder m_Decoder;
	private char m_LastCommand = 'x';
	private double m_AddForward = 0;
	private int m_Dupicates;
	private double m_NextLength;
	private char m_NextCommand;
	private double m_PulseLengthCompensation = 0;
	private int m_Spikes = 0;
	private int m_Mode = 0;
	
	// Radio Settings
	private double m_RadioFrequency = 433920000.0;
	private int m_BandwidthOrdinal = 10;
	private int m_AGCSettings = 0x040091;
	private int m_OutputPowerOrdinal = 0;
	private int m_ModulationOnPeriod = 0;
	private int m_ModulationOffPeriod = 0;
	
	public CULProtocolPort(ProtocolDecoder decoder) {
		m_Decoder = decoder;
		// In order for RxTx to recognize CUL as a serial port on Linux, we have
		// to add this system property. We make it possible to override by checking if the
		// property has already been set.
		if ((System.getProperty("os.name").toUpperCase().indexOf("LINUX") != -1 ) && 
				(System.getProperty("gnu.io.rxtx.SerialPorts") == null)){
			System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:" + "" +
					"/dev/ttyUSB0:/dev/ttyUSB1:/dev/ttyUSB2:" + 
					"/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2");
		}
	}

    public int open() {
        portList = CommPortIdentifier.getPortIdentifiers();

        boolean foundPort = false;
        
        /* Find the configured serial port */
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(m_ComPort)) {
                	// Ok, found 
                	foundPort = true;
                	break;
                }
            }
        }
        if (!foundPort) {
			System.out.print("Failed to find serial Port: " + m_ComPort);
			return 1;
        }
        
        /* Try to open the serial port */
        try {
            m_SerialPort = (SerialPort) portId.open("SNAPPort", 2000);
        } catch (PortInUseException e) {
        	System.out.print("COM Port " + m_ComPort + " is already in use" );
			return 2;
        }
        try {
            m_InputStream = m_SerialPort.getInputStream();
            m_OutputStream = m_SerialPort.getOutputStream();
        } catch (IOException e) {
        	System.out.print("COM Port " + m_ComPort + " could not be read " + e);
			return 3;        	
        }
        try {
            m_SerialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
        	System.out.print("COM Port " + m_ComPort + " has too many listeners" + e);
			return 4;        	
        }
        m_SerialPort.notifyOnDataAvailable(true);
        
        // Configure serial port parameters
        // Not doing this any more, since it does not work properly on Linux
//         try {
//            m_SerialPort.setSerialPortParams(256000,
//                SerialPort.DATABITS_8,
//                SerialPort.STOPBITS_1,
//                SerialPort.PARITY_NONE);
//        } catch (UnsupportedCommOperationException e) {
//        	System.out.print("Could not set parameters on " + m_ComPort + " " + e);
//			return 5;        	        	
//        }
 
        m_IsOpen = true;
		
        // Set output power
		writeLine("x04");
        
		// Switch on reception
		if (m_Mode == 1) {
			writeLine("X03");
		} else {
			writeLine("X01");			
		}

		// apply settings
		setBandwidthOrdinal(getBandwidthOrdinal());
		setAGCSettings(getAGCSettings());
		setRadioFrequency(m_RadioFrequency);

		return 0;
    }
    
    public void close() {
    	m_IsOpen = false;
    	if (m_SerialPort != null) {
    		m_SerialPort.close();
    		m_InputStream = null;
    		m_OutputStream = null;
    		m_SerialPort = null;
    	}
    }
    
	public boolean isOpen() {
		return m_IsOpen;
	}


	public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
        	// logger.warning("Failed in event reception " + e);
        }
	}

    public synchronized void serialEvent(SerialPortEvent event) {
        switch(event.getEventType()) {
        case SerialPortEvent.BI:
        case SerialPortEvent.OE:
        case SerialPortEvent.FE:
        case SerialPortEvent.PE:
        case SerialPortEvent.CD:
        case SerialPortEvent.CTS:
        case SerialPortEvent.DSR:
        case SerialPortEvent.RI:
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;
        case SerialPortEvent.DATA_AVAILABLE:

        	try {
        		byte[] readBuffer = new byte[400];

        		while (m_InputStream.available() > 0) {
        			int numBytes = m_InputStream.read(readBuffer);
        			for (int i = 0; i < numBytes; i++){
        				m_ReadBuffer[m_ReadBufferPointer++] = readBuffer[i];
        				if ((readBuffer[i] == 0x0A) || (m_ReadBufferPointer == READ_BUFFER_SIZE - 1)){
        					m_ReadBuffer[m_ReadBufferPointer] = 0;
        					String result = new String(m_ReadBuffer, 0, m_ReadBufferPointer - 2);
        					//System.out.println(result);
        					if (m_ReadBufferPointer == READ_BUFFER_SIZE - 1) {
        						System.out.println("Overflow!");
        					}
        					m_ReadBufferPointer = 0;
        					try {
        						analyzeReceivedCommand(result);
        					}
        					catch (Exception o) {
        						// Problem down in the decoders!
        						o.printStackTrace();
        					}
        				}
        				// NYI - Process received bytes
        			}
        		}
        	} catch (IOException e){
        		System.out.print("Error reading data from serial port " + e);
        	}
        	break;
        }
    }

	/**
	 * Analyze a command/event received from the CUL device
	 * @param commandString Command string to analyze
	 */
    private void analyzeReceivedCommand(String commandString) {
    	if (commandString.length() < 1) return; // Make sure string is not empty
    	char command = commandString.charAt(0);
    	
    	// Check if it is acknowledgment of command
    	if (command == 'o') {
    		acknowledgeCommand(commandString);
    		return;
    	}

    	// Check if this is valid pulse data
    	if (((command != 'm') && (command != 's')) || (commandString.length() != 5)) {
    		System.out.println("Error - unknown command: " + commandString);
    		return;
    	}

    	double pulseLength = Integer.parseInt(commandString.substring(1), 16);
    	// System.out.println(Character.toString(command) + Double.toString(pulseLength) + " us");

    	// Temporary - currently device signals overflow with this specific pulse value
    	if (pulseLength == 32767) {
    		System.out.println("Error - overflow in device");
    		return;
    	}

    	// Detect two pulses of same type in a row. This is probably due to a very short "ringing" spike
    	// after a transition, so the duplicate vale is added to the next pulse instead. 
    	if (command == m_LastCommand) {
    		m_Dupicates++;
    		System.out.println("Error - duplicate command " + Integer.toString(m_Dupicates));
    		m_AddForward = pulseLength;
    		return;
    	}

    	pulseLength += m_AddForward;
      	m_AddForward = 0.0;
          	if (pulseLength > 33000) {
    		System.out.println("Error - Too long pulse");
    	}
    	
     	if (pulseLength < 100.0) {
     		m_Spikes++;
     		System.out.println("Error - Spike" + Double.toString(pulseLength) + "(" + Integer.toHexString(m_Spikes) + ")");
    	}
     	
//     	if ((command == 'm') && (pulseLength < 500) && (pulseLength > 200)) {
//     		m_AddForward = 60;
//     		pulseLength -= m_AddForward;
//     	}

    	parsePulse(pulseLength, command == 'm');
    	m_LastCommand = command;
    }
    
    /**
     * Received ack on a command. Currently does nothing
     * @param commandString
     */
    private void acknowledgeCommand(String commandString) {
		
	}

	/**
     * Send a processed pulse to the decoders
     * @param pulseLength
     * @param isMark
     */
    private void parsePulse(double pulseLength, boolean isMark) {
    	
    	pulseLength += isMark ? m_PulseLengthCompensation  : -m_PulseLengthCompensation;
    	
		// Give the pulse to the decoder
		m_Decoder.parse(pulseLength, isMark);
    }
    
    public String[] getPortNames() {
    	ArrayList<String> result = new ArrayList<String>();
        /* Find the serial ports */
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            	result.add(portId.getName());
            }
        }
        return result.toArray(new String[result.size()]);
    }

	public String getSerialPort() {
		return m_ComPort;
	}

	public void setSerialPort(String serialPort) {
		m_ComPort = serialPort;
	}
	
	/**
	 * Transmit the list of pulses (in microseconds) via the CUL device
	 * @param message List of pulse lengths in microseconds, beginning with a mark pulse
	 * @param repeat Number of times to repeat message
	 * @param repeatOffset Number pulses into the message the repeat sequence should begin
	 * @return True if successful 
	 */
	public boolean playMessage(int message[], int repeat, int repeatOffset) {

		// Reset the transmit buffer
		writeLine("E");
		
		// Loop through the flanks in the message
		for (int i = 0; i < message.length; i++) {
			int mark = message[i++];
			// Fill with 0 if uneven number of flanks
			int space = i < message.length ? message[i] : 0; 
			String command = String.format("A%04X%04X", mark, space);
			// Add the pulse to the transmit buffer
			writeLine(command);
		}
		// Transmit the message, check if it should be modulated
		if ((m_ModulationOnPeriod > 0) || (repeatOffset > 0)) {
			// Yes, also write modulation parameters
			writeLine(String.format("S%02X%02X%02X%02X", repeat, m_ModulationOnPeriod, m_ModulationOffPeriod, repeatOffset));
		} else {
			// No modulation, just specify repeat
			writeLine(String.format("S%02X", repeat));
		}
		
		// NYI - Wait for confirmation
		
		return true;
	}

	/**
	 * Write a text line to the serial port. EOL-characters are added to the string
	 * @param line Line to be written
	 */
	protected void writeLine(String line) {
		String totalLine = line + "\r\n";
		try {
			m_OutputStream.write(totalLine.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a value to the CC1101 register
	 * @param address Register address
	 * @param value Register value
	 */
	protected void writeCC1101Register(int address, int value) {
		if (isOpen()) {
			writeLine(String.format("O%02X%02X", address, value));
		}
	}
	
    public double getRadioFrequency() {
		return m_RadioFrequency;
	}

	public void setRadioFrequency(double radioFrequency) {
		m_RadioFrequency = radioFrequency;
		
		// Convert frequency to register setting
		long registerFrequency = (long)(radioFrequency * (1 << 16) / CRYSTAL_FREQUENCY);
		writeCC1101Register(FREQ0, (int)(registerFrequency & 0x0FF));
		writeCC1101Register(FREQ1, (int)((registerFrequency >> 8) & 0x0FF));
		writeCC1101Register(FREQ2, (int)((registerFrequency >> 16) & 0x0FF));
	}

	/**
	 * Get a list of the bandwidth alternatives that are available with the CUL hardware.
	 * The index in this list corresponds to the BandwidthOrdinal.
	 * @return Array with the available bandwidths 
	 */
	public double[] getBandwidths() {
		return bandwidths.clone();
	}

	/**
	 * Get current bandwidth setting. The value is really an index in a fixed list of
	 * available values which can be seen with getBandwidths(). Note that this method
	 * does not get the value from the CUL-device, it gets the last set value.
	 * @return Bandwidth setting
	 */
	public int getBandwidthOrdinal() {
		return m_BandwidthOrdinal;
	}

	/**
	 * Set current bandwidth. The value is really an index in a fixed list of
	 * available values which can be seen with getBandwidths().
	 */
	public void setBandwidthOrdinal(int bandwidthOrdinal) {
		
		if ((bandwidthOrdinal < 0) || (bandwidthOrdinal > 15)) return;
		
		m_BandwidthOrdinal = bandwidthOrdinal;

		// This is cheating, I am assuming the current default setting for the low nibble
		// This is the exponent of the data rate speed. This needs to be set to approx
		// 40KBaud to handle out 40KHz modulation for IR sending
		int MDMCFG4LowNibble = 0x0A;
		// Temporary fix. For reception the high data rate setting does not work (for some reason)
		// Set it low.
		if (m_Mode == 0) {
			//MDMCFG4LowNibble = 0x05;
			MDMCFG4LowNibble = 0x05;
		}
		
		// Apply the setting for the MDMCFG4 register bits 7:4 which controls bandwidth setting
		writeCC1101Register(MDMCFG4, bandwidthSettingsMDMCFG4[m_BandwidthOrdinal] | MDMCFG4LowNibble);
		
		// Temp for setting data rate
		writeCC1101Register(MDMCFG3, 0x43);

		// Apply the setting for the FSCTRL1 register bits 4:0 which controls Internal frequence (IF)
		// setting compatible with the bandwith
		writeCC1101Register(FSCTRL1, bandwidthSettingsFSCTRL1[m_BandwidthOrdinal]);
	}

	public int getAGCSettings() {
		return m_AGCSettings;
	}

	public void setAGCSettings(int settings) {
		
		if ((settings < 0) || (settings > 0x00FFFFFF)) return;
		
		m_AGCSettings = settings;
		
		// Apply the setting for the AGCCTRL2
		writeCC1101Register(AGCCTRL2, m_AGCSettings >> 16);
		// Apply the setting for the AGCCTRL1
		writeCC1101Register(AGCCTRL1, (m_AGCSettings >> 8) & 0xFF);
		// Apply the setting for the AGCCTRL2
		writeCC1101Register(AGCCTRL0, m_AGCSettings & 0xFF);		
	}

	/**
	 * See setModulationOnPeriod
	 * @return ModulationOnPeriod
	 */
	public int getModulationOnPeriod() {
		return m_ModulationOnPeriod;
	}

	/**
	 * The mark pulses may be modulated. This parameter specifies the on period
	 * of this modulation. The time is specified in increments of 375nS. If on
	 * and off periods are set to the same value the resulting modulation frequency
	 * will be 10E9/(OnPeriod * 375 * 2).
	 * Setting the period to 0 turns off the mark modulation.
	 * @param modulationOnPeriod 0 - 255.
	 */
	public void setModulationOnPeriod(int modulationOnPeriod) {
		m_ModulationOnPeriod = modulationOnPeriod;
	}

	/**
	 * See setModulationOffPeriod
	 * @return ModulationOffPeriod
	 */
	public int getModulationOffPeriod() {
		return m_ModulationOffPeriod;
	}

	/**
	 * Set the off period of mark pulse modulation. The time is specified in 
	 * increments of 375nS. See setModulationOnPeriod for details.
	 * @param modulationOffPeriod
	 */
	public void setModulationOffPeriod(int modulationOffPeriod) {
		m_ModulationOffPeriod = modulationOffPeriod;
	}

	/**
	 * See setOutputPowerOrdinal
	 * @return Power level
	 */
	public int getOutputPowerOrdinal() {
		return m_OutputPowerOrdinal;
	}

	/**
	 * Set the output power used for transmission. There are five power levels
	 * available:  0 = -30 dBm, 1 = -20 dBm, 2 = -15 dBm, 3 = -10 dBm, 4 = 0 dBm,
	 * 5 = 5 dBm, 6 = 7 dBm, 7 = 10 dBm
	 * @param outputPowerOrdinal Power level
	 */
	public void setOutputPowerOrdinal(int outputPowerOrdinal) {
		if ((outputPowerOrdinal >= 0) && (outputPowerOrdinal <= 7)) {
			m_OutputPowerOrdinal = outputPowerOrdinal;
		}
	}

	public int getMode() {
		return m_Mode;
	}

	/**
	 * Temporary fix. I have to have different settings in the CUL-stick depending on
	 * if I receive or transmit. 0 = Reception mode, 1 = transmission mode
	 * @param mode
	 */
	public void setMode(int mode) {
		m_Mode = mode;
	}
	

}
