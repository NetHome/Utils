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

import nu.nethome.util.ps.ProtocolDecoder;

/**
 * A Flank Detector analyzes a stream of analog sample values and tries to find digital
 * pulses in the signal. The detected pulses are sent to the attached ProtocolDecoder.
 * This flank detector that uses a fairly simple algorithm which just measures
 * rate of change by comparing the current sample with a sample a couple samples back.
 * It also uses a FIR-filter for smoothing noisy signals.
 * 
 * @author Stefan
 */
public class SimpleFlankDetector implements ProtocolSampler {
	
	final static double PUSH_PERIOD = 200e-3;

	protected ProtocolDecoder m_ProtocolDecoder;
	
	// Sample Parameters
	protected int m_FlankSwing = 50; // 70;
	protected int m_FlankHoldoff = 5;
	protected int m_FlankLength = 3;
	protected int m_SampleRate;
	
	protected int m_Last[] = new int[6];
	protected boolean m_CurrentState = false;
	protected int m_CurrentStateCounter = 0;
	protected int m_SampleCounter = 0;
	protected int m_PushCount = 0;
	protected boolean m_HasPushed = false;
	protected int m_LastFlankDirection = 0;
	protected int m_PulseWidthCompensation = 0;
	
	public void addSample(int sample) {

		// Detect if there is a new flank in the data stream
		int flankDirection  = 0;
		if (Math.abs(m_Last[m_FlankLength] - sample) > m_FlankSwing) {
			flankDirection = (int) Math.signum(m_Last[m_FlankLength] - sample);
		}
		if ((flankDirection != 0) && (flankDirection != m_LastFlankDirection ) && (m_CurrentStateCounter > m_FlankHoldoff))
		{
			// Yes, there is a flank marking the end of the current pulse and starting a new. 
			// Send the length of the current pulse to the ProtocolDecoder.
			//
			// If we have pushed a fake pulse to the state machines before, we cannot really trust
			// the current pulse value. To not risk fooling the parsers that there was a really short
			// idle period, we send "almost" the push period which is the least the idle period were.
			double value = m_HasPushed ? PUSH_PERIOD * 1000000 - 1000 : Math.rint(((double)m_CurrentStateCounter / m_SampleRate) * 1000000.0);
			
			if (value > 10000) m_CurrentState = false;
			
			// Give the adjusted pulse to the ProtocolDecoder including info of mark or space pulse
			m_ProtocolDecoder.parse(m_CurrentState ? value + m_PulseWidthCompensation : value - m_PulseWidthCompensation, 
					m_CurrentState);
			
			// Update state
			m_CurrentState = !m_CurrentState;
			m_CurrentStateCounter = 0;
			m_HasPushed = false;
		}
		else if (m_CurrentStateCounter > m_PushCount) {
			// If there has been no pulse for PUSH_PERIOD, we send the current idle pulse plus a fake 0us 
			// pulse through the state machines to push them out of any dark corners they may be stuck 
			// in since last pulse train.
			// This makes it possible for the state machines to detect "end of signal" even when there are no
			// new real pulses.
			
			// First send the idle pulse we have seen so far
			m_ProtocolDecoder.parse(PUSH_PERIOD * 1000000, false);
			// Then send a fake 0us mark pulse
			m_ProtocolDecoder.parse(0.0, true);
			m_HasPushed = true;
			m_CurrentStateCounter = m_FlankHoldoff; // Not zero, so we don't risk missing a real state swing
			m_CurrentState = false; // Ok, this long pulse we assume is space
		}
		m_LastFlankDirection = flankDirection;
		m_Last[5] = m_Last[4];
		m_Last[4] = m_Last[3];
		m_Last[3] = m_Last[2];
		m_Last[2] = m_Last[1];
		m_Last[1] = m_Last[0];
		m_Last[0] = sample;
		m_CurrentStateCounter++;
	}

	public void setProtocolDecoder(ProtocolDecoder decoder) {
		m_ProtocolDecoder = decoder;
	}

	public int getSampleRate() {
		return m_SampleRate;
	}

	/* (non-Javadoc)
	 * @see ProtocolSampler
	 */
	public void setSampleRate(int sampleRate) {
		m_SampleRate = sampleRate;
		m_PushCount = (int)(sampleRate * PUSH_PERIOD + 0.5);
	}

	/**
	 * See setFlankSwing
	 * @return flank swing
	 */
	public int getFlankSwing() {
		return m_FlankSwing;
	}

	/**
	 * Set the change needed in the signal to be detected as a flank. The change is
	 * detected over a number of samples, see setFlankLenth
	 * @param flankSwing
	 */
	public void setFlankSwing(int flankSwing) {
		m_FlankSwing = flankSwing;
	}

	/**
	 * See setFlankHoldoff
	 * @return flank holdoff
	 */
	public int getFlankHoldoff() {
		return m_FlankHoldoff;
	}

	/**
	 * Set the number of samples to ignore flanks after a flank has been detected.
	 * This is used to avoid detecting large flanks or ringing as multiple flanks.
	 * @param flankHoldoff number of samples to ignore new flanks
	 */
	public void setFlankHoldoff(int flankHoldoff) {
		m_FlankHoldoff = flankHoldoff;
	}

	/**
	 * See setFlankLength
	 * @return flankLength
	 */
	public int getFlankLength() {
		return m_FlankLength;
	}

	/**
	 * Set the number of samples which is used to measure the flankSwing. For a 
	 * "slow" signal, for example using the FIR-filter, a higher value is needed
	 * 
	 * @param flankLength 1 - 5
	 */
	public void setFlankLength(int flankLength) {
		if ((flankLength < 1) || (flankLength > 5)) return;
		m_FlankLength = flankLength;
	}

	/**
	 * See setPulseWidthCompensation
	 * @return pulseWidthCompensation in micro seconds
	 */
	public int getPulseWidthCompensation() {
		return m_PulseWidthCompensation;
	}

	/**
	 * Set the amount of time (in micro seconds) which shall be added to mark pulses
	 * and subtracted from space pulses to compensate for the receiver hardware
	 * @param pulseWidthCompensation in micro seconds
	 */
	public void setPulseWidthCompensation(int pulseWidthCompensation) {
		m_PulseWidthCompensation = pulseWidthCompensation;
	}
}
