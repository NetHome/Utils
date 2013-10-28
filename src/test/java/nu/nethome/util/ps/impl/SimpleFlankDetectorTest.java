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

/**
 * 
 */
package nu.nethome.util.ps.impl;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

/**
 * Test the simple flank detector
 * @author Stefan
 *
 */
public class SimpleFlankDetectorTest {
	
	class TestProtocolDecoder implements ProtocolDecoder {

		public LinkedList<Double> pulses = new LinkedList<Double>();
		public boolean getActive() {
			// TODO Auto-generated method stub
			return false;
		}

        public void setTarget(ProtocolDecoderSink sink) {
            // Do Dinada
        }

        public ProtocolInfo getInfo() {
			return null;
		}

		public int parse(double pulseLength, boolean state) {
			pulses.add(pulseLength);
            return 0;
		}

		public void setActive(boolean active) {
            // Do Dinada
		}
		
	}
	SimpleFlankDetector testItem;
	TestProtocolDecoder decoder;

	protected int testDataSimple[] = {0, 0, 0, 0, 51, 51, 51, 51, 51, 0, 0, 0, 0, 0, 0, 101, 101, 101, 101, 101, 101, 101, 0, 0, 0, 0};

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testItem = new SimpleFlankDetector();
		decoder = new TestProtocolDecoder();
		testItem.setProtocolDecoder(decoder);
		
		testItem.setFlankHoldoff(1);
		testItem.setFlankLength(3);
		testItem.setFlankSwing(50);
		testItem.setSampleRate(10000);
		testItem.setPulseWidthCompensation(0);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link SimpleFlankDetector#addSample(int)}.
	 */
	@Test
	public void testFindSimpleFlank1() {
		
		// Parse test data
		for (int sample : testDataSimple) {
			testItem.addSample(sample);
		}
		// Verify result
		assertEquals((Double)400.0, decoder.pulses.get(0));
		assertEquals((Double)500.0, decoder.pulses.get(1));
		assertEquals((Double)600.0, decoder.pulses.get(2));
		assertEquals((Double)700.0, decoder.pulses.get(3));
	}

	/**
	 * Test method for {@link SimpleFlankDetector#setSampleRate(int)}.
	 */
	@Test
	public void testSetSampleRate() {
		testItem.setSampleRate(10000);
		assertEquals(10000, testItem.getSampleRate());

		testItem.setSampleRate(20000);
		assertEquals(20000, testItem.getSampleRate());
		
		// Parse test data
		for (int sample : testDataSimple) {
			testItem.addSample(sample);
		}
		// Verify result
		assertEquals((Double)200.0, decoder.pulses.get(0));
		assertEquals((Double)250.0, decoder.pulses.get(1));
		assertEquals((Double)300.0, decoder.pulses.get(2));
		assertEquals((Double)350.0, decoder.pulses.get(3));

	}

	/**
	 * Test method for {@link SimpleFlankDetector#setFlankSwing(int)}.
	 */
	@Test
	public void testSetFlankSwing() {
		testItem.setFlankSwing(20);
		assertEquals(20, testItem.getFlankSwing());

		testItem.setFlankSwing(100);
		assertEquals(100, testItem.getFlankSwing());
		
		// Parse test data
		for (int sample : testDataSimple) {
			testItem.addSample(sample);
		}
		// Verify result
		assertEquals((Double)1500.0, decoder.pulses.get(0));
		assertEquals((Double)700.0, decoder.pulses.get(1));
	}

	/**
	 * Test method for {@link SimpleFlankDetector#setFlankHoldoff(int)}.
	 */
	@Test
	public void testSetFlankLength() {
		testItem.setFlankLength(1);
		assertEquals(1, testItem.getFlankLength());

		testItem.setFlankLength(5);
		assertEquals(5, testItem.getFlankLength());

		testItem.setFlankLength(6);
		assertEquals(5, testItem.getFlankLength());

		testItem.setFlankLength(0);
		assertEquals(5, testItem.getFlankLength());
}

	/**
	 * Test method for {@link SimpleFlankDetector#setFlankLength(int)}.
	 */
	@Test
	public void testSetFlankHoldoff() {
		testItem.setFlankHoldoff(2);
		assertEquals(2, testItem.getFlankHoldoff());

		testItem.setFlankHoldoff(20);
		assertEquals(20, testItem.getFlankHoldoff());
	}

	/**
	 * Test method 
	 */
	@Test
	public void testSetPulseWidthCompensation() {
		testItem.setPulseWidthCompensation(2);
		assertEquals(2, testItem.getPulseWidthCompensation());

		testItem.setPulseWidthCompensation(20);
		assertEquals(20, testItem.getPulseWidthCompensation());
	}

}
