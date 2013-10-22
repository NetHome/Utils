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

package nu.nethome.util.ps.impl.util;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.impl.CULProtocolPort;

import java.util.LinkedList;
import java.util.List;

/**
 * Simulates the serial port and part of the CUL-Stick for unit testing of CULProtocolPort
 * @author Stefan Str�mberg
 *
 */
public class MocCULProtocolPort extends CULProtocolPort {
	
	// Addresses of the CC1101 control registers, made public for tests
	public static final int IOCFG2     = 0x00;
	public static final int IOCFG1     = 0x01;
	public static final int IOCFG0     = 0x02;
	public static final int FIFOTHR    = 0x03;
	public static final int SYNC1      = 0x04;
	public static final int SYNC0      = 0x05;
	public static final int PKTLEN     = 0x06;
	public static final int PKTCTRL1   = 0x07;
	public static final int PKTCTRL0   = 0x08;
	public static final int ADDR       = 0x09;
	public static final int CHANNR     = 0x0A;
	public static final int FSCTRL1    = 0x0B;
	public static final int FSCTRL0    = 0x0C;
	public static final int FREQ2      = 0x0D;
	public static final int FREQ1      = 0x0E;
	public static final int FREQ0      = 0x0F;
	public static final int MDMCFG4    = 0x10;
	public static final int MDMCFG3    = 0x11;
	public static final int MDMCFG2    = 0x12;
	public static final int MDMCFG1    = 0x13;
	public static final int MDMCFG0    = 0x14;
	public static final int DEVIATN    = 0x15;
	public static final int MCSM2      = 0x16;
	public static final int MCSM1      = 0x17;
	public static final int MCSM0      = 0x18;
	public static final int FOCCFG     = 0x19;
	public static final int BSCFG      = 0x1A;
	public static final int AGCCTRL2   = 0x1B;
	public static final int AGCCTRL1   = 0x1C;
	public static final int AGCCTRL0   = 0x1D;
	public static final int WOREVT1    = 0x1E;
	public static final int WOREVT0    = 0x1F;
	public static final int WORCTRL    = 0x20;
	public static final int FREND1     = 0x21;
	public static final int FREND0     = 0x22;
	public static final int FSCAL3     = 0x23;
	public static final int FSCAL2     = 0x24;
	public static final int FSCAL1     = 0x25;
	public static final int FSCAL0     = 0x26;
	public static final int RCCTRL1    = 0x27;
	public static final int RCCTRL0    = 0x28;

	
	
	
	public int m_Registers[];
	public List<String> m_Commands = new LinkedList<String>();

	public MocCULProtocolPort(ProtocolDecoder decoder) {
		super(decoder);
		m_Registers = new int[0x29];
		for (int i = 0; i < m_Registers.length; i++) {
			m_Registers[i] = 0;
		}
		m_IsOpen = true;
	}
	
	/**
	 * Override writeLine method, save and interpret the string
	 * @see nu.nethome.util.ps.impl.CULProtocolPort#writeLine(java.lang.String)
	 */
	@Override
	protected void writeLine(String line) {
		m_Commands.add(line);
		if (line.charAt(0) == 'O') {
			int address = Integer.parseInt(line.substring(1, 3), 16);
			int data = Integer.parseInt(line.substring(3, 5), 16);
			m_Registers[address] = data;
		}
	}
}
