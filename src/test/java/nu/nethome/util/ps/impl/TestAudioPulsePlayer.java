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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sound.sampled.Mixer;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Module tests for AudioPulsePlayer
 * @author Stefan
 *
 */
public class TestAudioPulsePlayer {

	public AudioPulsePlayer m_Player;
	
	/**
	 * Set up Test environment
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		m_Player = new AudioPulsePlayer();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		m_Player.closeLine();
		m_Player = null;
	}

	/**
	 * Test method for {@link nu.nethome.util.ps.impl.AudioPulsePlayer#AudioPulsePlayer()}.
	 */
	@Ignore("Dependant on local hardware")
	public void testAudioPulsePlayer() {
		assertTrue(m_Player.getSourceList().length > 0);
		assertEquals(0, m_Player.getSource());
	}

	/**
	 * Test method for {@link nu.nethome.util.ps.impl.AudioPulsePlayer#setSource(int)}.
	 */
	@Ignore
	public void testSetSource() {
		m_Player.setSource(1);
		assertEquals(1, m_Player.getSource());
		m_Player.setSource(100);
		assertEquals(1, m_Player.getSource());
		m_Player.setSource(0);
		assertEquals(0, m_Player.getSource());
	}

	/**
	 * Test method for {@link nu.nethome.util.ps.impl.AudioPulsePlayer#getSourceList()}.
	 */
	@Test
	public void testGetSourceList() {
		Mixer.Info mixers[] = m_Player.getSourceList();
		for (int i = 0; i < mixers.length; i++) {
			assertTrue(mixers[i].getName().length() > 0);
		}
	}
	
	@Ignore
	public void testOpenAndClose() {
		assertTrue(m_Player.openLine());
		assertTrue(m_Player.isOpen());
		m_Player.closeLine();
		assertFalse(m_Player.isOpen());
	}

	@Test
	public void testGetSetSwing() {
		m_Player.setSwing(127);
		assertEquals(127, m_Player.getSwing());
		m_Player.setSwing(-128);
		assertEquals(-128, m_Player.getSwing());
		m_Player.setSwing(128);
		assertEquals(-128, m_Player.getSwing());
		m_Player.setSwing(-129);
		assertEquals(-128, m_Player.getSwing());
	}
	
	@Test
	public void testGetEmptyBuffer() {
		byte res[] = m_Player.getDataBuffer();
		assertEquals(0, res.length);
	}

	@Ignore
	public void testPlayData() {
		final int message[] = {250, 500, 500};
		final byte expectedResult[] = 
		{0, 0, -2, -2, -4, -4, -6, -6, -8, -8, 10, 10, 10, 10, -10, -10, -10, -10, 
				-10, -10, -10, -10, 10, 10, 10, 10, 10, 10, 10, 10};
		m_Player.setSwing(10);
		m_Player.setSampleRate(8000.0F);
		assertTrue(m_Player.openLine());
		assertTrue(m_Player.playMessage(message));
		byte result[] = m_Player.getDataBuffer();
		assertArrayEquals(expectedResult, result);
		m_Player.closeLine();
	}
	
	@Ignore
	public void testPlayTime() {
		// Message that should take 50mS to play
		final int message[] = {10000, 10000, 10000, 10000, 10000};
		m_Player.setSwing(10);
		m_Player.setSampleRate(8000.0F);
		assertTrue(m_Player.openLine());
		long startTime = new Date().getTime();
		assertTrue(m_Player.playMessage(message));
		long elapsedTime = new Date().getTime() - startTime;
		assertTrue(elapsedTime > 50);
		assertTrue(elapsedTime < 200); // Hard to tell really
		m_Player.closeLine();
	}
	
}
