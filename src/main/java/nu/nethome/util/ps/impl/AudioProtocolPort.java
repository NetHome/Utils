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

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The AudioProtocolPort listens to a specified microphone input and
 * interprets the signal as square wave pulses. The pulse lengths are fed 
 * to the added protocol decoders which will try to decode their protocols.
 * With simple hardware this will allow the microphone input to be used as 
 * interface for detecting IR or RF-protocols.
 * 
 * @author Stefan
 *
 */
public class AudioProtocolPort implements PulseProtocolPort {
	
	private static final int TIME_TO_DEEM_INACTIVE = 5000; 	// 5 seconds
	private static final int CLOSE_LOOP_WAIT = 100; 		// 100 ms
	private static final int CLOSE_WAIT_LOOP_COUNT = 30; 	// 30 times
	private static final float STANDARD_SAMPLE_FREQUENCY = 44100.0F;

	/**
	 * TheAuStream works as a sink for the sampled data, and also reads off the .au-header
	 * from the beginning of the data stream before the data is fed to the AudioProtocolPort.
	 * @author Stefan
	 */
	protected class AuStream extends OutputStream {
		int m_HeaderCounter = 0;
		int m_Header[] = new int[6];

		@Override
		public void write(int b) throws IOException {
			// Read off the .AU header of the data stream. .AU-header consists of 6 words:
			// <0x2e736e64> <Data Offset> <Data Size> <Encoding> <Sample Rate> <Channels>
			if (m_HeaderCounter < 24) {
				b = (b < 0) ? 256 + b : b;
				if (((m_HeaderCounter % 4) == 0)) {
					m_Header[m_HeaderCounter >> 2] = 0;
				}
				// Build up 32 bit words from four big endian ordered bytes each
				m_Header[m_HeaderCounter >> 2] += b << ((3 -(m_HeaderCounter % 4)) * 8);
				m_HeaderCounter++;
				// If this was the last header byte, use the header info
				if (m_HeaderCounter == 24) {
					setSampleRate(m_Header[4]);
				}
			} else {
				addRawSample(b);
			}
		}
	}
	
	public enum Channel {MONO, RIGHT, LEFT};
	protected AuStream m_outputStream = null;
	protected AudioFileFormat.Type m_targetType;
	protected AudioInputStream m_audioInputStream;
	protected TargetDataLine m_TargetDataLine = null;
	protected ProtocolSampler m_Sampler = null;
	protected volatile int m_SourceNumber = 0;
	protected volatile Mixer.Info m_Mixers[];
	protected Channel m_Channel = Channel.MONO;
	volatile boolean m_IsLeft = true;
	private float m_SampleRate = STANDARD_SAMPLE_FREQUENCY;
	volatile boolean m_IsOpen = false;
	private static Logger logger = Logger.getLogger(AudioProtocolPort.class.getName());
	private volatile long m_LastReceivedTime = 0;
	private Mixer m_Mixer; 

	/**
	 * This method is called internally when a new data sample is received from
	 * the audio source. This method reads out the correct Left/Right samples
	 * and forwards the correct samples to the Sampler.
	 * 
	 * @param sample
	 */
	protected void addRawSample(int sample) {
		m_IsLeft = !m_IsLeft;
		if ((m_Channel != Channel.MONO) && (m_IsLeft == (m_Channel == Channel.LEFT))) return;
		m_Sampler.addSample(sample);
		m_LastReceivedTime = System.currentTimeMillis();
	}
	
	public float getSampleRate() {
		return m_SampleRate;
	}

	public void setSampleRate(float sampleRate) {
		// Really ugly temporary fix for problems in OS X. For some reason I seem to get
		// only half the sample rate I ask for
		String os = System.getProperty( "os.name" );
		logger.config("OS: " + os);
		if (os.equals("Mac OS X")) {
			sampleRate = STANDARD_SAMPLE_FREQUENCY / 2;
			logger.info("Adjusting sample rate for MAC OS");
		}
		m_SampleRate = sampleRate;
		m_Sampler.setSampleRate((int)sampleRate);
		logger.info("Sample Rate:" + Float.toString(sampleRate));
	}

	public AudioProtocolPort(ProtocolSampler sampler) {
		findMixers();
		m_Sampler = sampler;		
	}
	
	/**
	 * Find all mixers which fulfill our specs for data input
	 */
	public void findMixers() {
		// Specify the audio format
		AudioFormat	audioFormat = new AudioFormat(
				m_SampleRate,
                8,
                (m_Channel == Channel.MONO) ? 1 : 2,	// Channels
                false,
                false);
		// Create a corresponding DataLine.Info
		DataLine.Info	info = new DataLine.Info(TargetDataLine.class, audioFormat);

		ArrayList<Mixer.Info> resultMixers = new ArrayList<Mixer.Info>();
		Line.Info[] sources;
		
		// Get info on all available mixers
		Mixer.Info mixers[] = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixers.length; i++) {
			// Find which mixers can supply at least one target data line of requester sort
			try {
				sources = AudioSystem.getMixer(mixers[i]).getTargetLineInfo(info);
				if (sources.length > 0) {
					resultMixers.add(mixers[i]);
				}
			}
			catch (IllegalArgumentException e) {
				logger.warning("Could not open mixer in find: " + e.getMessage());
			}
		}
		m_Mixers = resultMixers.toArray(new Mixer.Info[resultMixers.size()]);
	}
	
	
	/* (non-Javadoc)
	 * @see PulseProtocolPort#open()
	 */
	public synchronized int open(){

		// Create the sample stream which will receive the data
		m_outputStream = new AuStream();
		
		// Specify the audio format
		AudioFormat	audioFormat = new AudioFormat(
				m_SampleRate,
                8,
                (m_Channel == Channel.MONO) ? 1 : 2,
                false,
                false);

		// Make sure we have the mixers
		if (m_Mixers == null) findMixers();
		
		// Sanity check on source number
		if (m_SourceNumber >= m_Mixers.length) m_SourceNumber = 0;
		if (m_Mixers.length < 1) return 1;

		// Specify audio format to AU
		m_targetType = AudioFileFormat.Type.AU;

		DataLine.Info	info = new DataLine.Info(TargetDataLine.class, audioFormat);
		logger.finer("Opening mixer: " + m_Mixers[m_SourceNumber].getName());
		try
		{
			// Try to get a TargetDataLine. It is used to read audio data
			m_Mixer = AudioSystem.getMixer(m_Mixers[m_SourceNumber]);
			m_TargetDataLine = (TargetDataLine) m_Mixer.getLine(info);
			// Create the source stream which will read from the line
			m_audioInputStream = new AudioInputStream(m_TargetDataLine);
			// Open the TargetDataLine
			m_TargetDataLine.open(audioFormat);
			// Start the TargetDataLine. It tells the line that
			// we now want to read data from it. According to info on the net, this has to be done close
			// to the open statement, otherwise a second open during a program session may fail(!)
			m_TargetDataLine.start();
		}
		catch (LineUnavailableException e)
		{
			logger.warning("unable to get a recording line: " + e.getMessage());
			return 1;
		}
		catch (IllegalArgumentException e) {
			logger.warning("unable to get a recording line: " + e.getMessage());
			return 1;
		}
		
		m_IsOpen = true;
		m_IsLeft = true;
		
		// Start the thread that will actually read data from the line
		Thread thread = new Thread("Sampler") {public void run() {readData();}};
		thread.start();
		
		// Set the LastReceivedTime, so the port is counted as active if we ask immediately after open
		// even if samples have not had time to arrive yet.
		m_LastReceivedTime = System.currentTimeMillis();
		return 0;
	}
		
	/* (non-Javadoc)
	 * @see PulseProtocolPort#close()
	 */
	public synchronized void close() {
		if (!this.isOpen()) return;
		
		m_IsOpen = false;

		logger.info("Closing TargetDataLine wo stop and flush, buffer:" + Integer.toString(m_TargetDataLine.available()));

		// Temporarily removed the stop() and flush() calls to try to get around the problems on WindowsVista
		
		// Stop the sampling TargetDataLine
		// m_TargetDataLine.stop();

		// Flush any remaining samples. This is added to avoid problems on OS X
		// m_TargetDataLine.flush();
		
		// Finally, close the TargetDataLine. Is this asynchronous?
		m_TargetDataLine.close();
		
		// Wait until the line is really closed. Sort of busy wait, a bit ugly
		int count = 0;
		do {
			try {
				Thread.sleep(CLOSE_LOOP_WAIT);
			} catch (InterruptedException e) {
				// Do Dinada
			}
			count++;
		} while (m_TargetDataLine.isOpen() && (count < CLOSE_WAIT_LOOP_COUNT));
		
		logger.info("Close wait count: " + Integer.toString(count));
		if (count == CLOSE_WAIT_LOOP_COUNT) {
			logger.warning("TargetDataLine Close failed");
		}
		
		// Close any other open target lines in the mixer
		Line otherLines[] = m_Mixer.getTargetLines();
		logger.info("Closing " + Integer.toString(otherLines.length) + " additional lines in the mixer");
		for (Line line : otherLines) {
			line.close();
		}
		
		String closeMixer = System.getProperty( "nu.nethome.util.ps.AudioProtocolPort.CloseMixer" );
		if ((closeMixer != null) && closeMixer.equalsIgnoreCase("yes")) {
			// Close the Mixer used
			m_Mixer.close();
		}
		
		// Close the input stream
		try {
			m_audioInputStream.close();
		}
		catch (IOException e) {
			logger.warning("Error closing audio input stream used to sample:" + e.getMessage());
		}		

		// This is just an attempt to get around problems with reopening on Vista
		m_Mixer = null;
		m_TargetDataLine = null;
		m_outputStream = null;
		m_audioInputStream = null;
		m_Mixers = null;
		// Now force the system to Garbage Collect. Hopefully this will free any audio
		// resources held by the corresponding Java objects.
		System.gc();
		
		// Just wait a little while for things to settle
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Do Dinada
		}
	}
		
	/**
	 * This method is used by the separate read thread which reads data from
	 * the audio source 
	 */
	protected void readData() {
		try
		{
			AudioSystem.write(
					m_audioInputStream,
					m_targetType,
					m_outputStream);
		}
		catch (IOException e)
		{
			logger.warning("Failed sampling data: " + e.getMessage());
		}
		logger.info("Sampler Thread Exiting");
	}

	public void setSource(int sourceNumber) {
		m_SourceNumber = sourceNumber;
	}

	public int getSource() {
		return m_SourceNumber;
	}

	public Mixer.Info[] getSourceList() {
		if (m_Mixers == null) findMixers();
		return m_Mixers;
	}

	public Channel getChannel() {
		return m_Channel;
	}

	public void setChannel(Channel channel) {
		m_Channel = channel;
	}
	
	/* (non-Javadoc)
	 * @see PulseProtocolPort#isOpen()
	 */
	public boolean isOpen() {
		return m_IsOpen;
	}
	
	/**
	 * An audio port may become inactive even if it is still open
	 * @return True is the connection is currently active
	 */
	public boolean isActive() {
		// If it was more that X seconds since we received a sample, the line must be inactive
		long timeSinceUpdate = System.currentTimeMillis() - m_LastReceivedTime;
		return timeSinceUpdate < TIME_TO_DEEM_INACTIVE;
	}
}