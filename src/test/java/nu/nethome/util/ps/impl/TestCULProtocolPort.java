/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
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

import nu.nethome.util.ps.impl.util.MocCULProtocolPort;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for CULProtocolPort
 * @author Stefan
 *
 */
public class TestCULProtocolPort {

	private MocCULProtocolPort m_Port;
	private ProtocolDecoderGroup m_FakeDecoder; 

	@Before
	public void setUp() throws Exception {
		m_FakeDecoder = new ProtocolDecoderGroup();
		m_Port = new MocCULProtocolPort(m_FakeDecoder);
	}

	@After
	public void tearDown() throws Exception {
		m_Port = null;
	}

	@Test
	public void testSetBandwitdthOrdinal() {
		// Verify default value
		assertEquals(10, m_Port.getBandwidthOrdinal());
		
		// Verify that values are written
		m_Port.setBandwidthOrdinal(0);
		assertEquals(0, m_Port.getBandwidthOrdinal());

		// Verify that correct values are written to the registers, test a few of
		// the alternatives
		m_Port.setBandwidthOrdinal(10); // Test 325KHz
		assertEquals(10, m_Port.getBandwidthOrdinal());
		assertEquals(0x55, m_Port.m_Registers[MocCULProtocolPort.MDMCFG4]);
		assertEquals(0x08, m_Port.m_Registers[MocCULProtocolPort.FSCTRL1]);
				
		m_Port.setBandwidthOrdinal(15);// Test 812KHz
		assertEquals(15, m_Port.getBandwidthOrdinal());
		assertEquals(0x05, m_Port.m_Registers[MocCULProtocolPort.MDMCFG4]);
		assertEquals(0x0E, m_Port.m_Registers[MocCULProtocolPort.FSCTRL1]);

		m_Port.setBandwidthOrdinal(0);// Test 58KHz
		assertEquals(0, m_Port.getBandwidthOrdinal());
		assertEquals(0xF5, m_Port.m_Registers[MocCULProtocolPort.MDMCFG4]);
		assertEquals(0x06, m_Port.m_Registers[MocCULProtocolPort.FSCTRL1]);

		// Verify that bad values does not get written
		m_Port.setBandwidthOrdinal(16);
		assertEquals(0, m_Port.getBandwidthOrdinal());
		m_Port.setBandwidthOrdinal(-1);
		assertEquals(0, m_Port.getBandwidthOrdinal());
	}
	
	@Test
	public void testSetAGCSettings() {
		// Verify default value
		assertEquals(0x040091, m_Port.getAGCSettings());
		
		m_Port.setAGCSettings(0x050092);
		assertEquals(0x050092, m_Port.getAGCSettings());
		assertEquals(0x05, m_Port.m_Registers[MocCULProtocolPort.AGCCTRL2]);
		assertEquals(0x00, m_Port.m_Registers[MocCULProtocolPort.AGCCTRL1]);
		assertEquals(0x92, m_Port.m_Registers[MocCULProtocolPort.AGCCTRL0]);
	}
}
