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

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;
import nu.nethome.util.ps.RawProtocolMessage;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * The RawDecoder acts as both a ProtocolDecoder and a ProtocolSampler. It is used to
 * save the raw sample data when a signal is detected in the sample stream. 
 * When flanks are detected (the parse-method is called) the RawDecoder starts saving
 * all raw samples until the protocol message stops. The raw samples are then 
 * reported as a special ProtocolMessage which contains all raw sample data.
 * 
 * @author Stefan
 *
 */
public class RawDecoder  implements ProtocolDecoder, ProtocolSampler {
	
	protected static final int IDLE = 0;
	protected static final int READING_MESSAGE = 1;
	/** Maximum size of a raw sampled message in milliseconds */
	protected static final int MAX_MESSAGE_LENGTH_MS = 800;
	protected static final int RAW_MESSAGE_END_GAP = 29000; // 11000
	protected static final int LEVELTIMETOZERO = 1; // X Seconds without signal before level drops to zero
	protected static final int REPORTSPERSECOND = 10; // Number of levelreports per second

	protected int m_State = IDLE;
	protected LinkedList<Integer> m_Pulses;
	protected boolean m_IsSampling = false;
	protected ArrayList<Integer> m_Samples;
	protected int m_SampleCount = 0;
	protected int m_SampleCountAtLastPulse = 0;
	protected int m_PulseCount;
	protected double m_LastPulse = 0;
	protected ProtocolDecoderSink m_Sink = null;
	protected int m_SampleFrequency;
	protected int m_MaxMessageLength;
	protected boolean m_FreeSampling = false;
	protected int m_FreeSampleCount = 0;
	protected int m_Level = 0;
	protected int m_LevelReportCount = 10;
	protected LinkedList<Double> m_PulseLengths;
    private int maxSampleLength;

    public void setTarget(ProtocolDecoderSink sink) {
		m_Sink = sink;
	}

	public RawDecoder() {
		setSampleRate(22000);
	}

	public ProtocolInfo getInfo() {
		return new ProtocolInfo("Raw", "Flank Length", "-", 0, 5);
	}

	public void setSampleRate(int frequency) {
		m_SampleFrequency = frequency;
		m_MaxMessageLength = (int)(m_SampleFrequency * MAX_MESSAGE_LENGTH_MS * 0.001);
	}

	public int getSampleRate() {
		return m_SampleFrequency;
	}

	/**
	 * Forces the class to start saving raw data for the specified number of samples.
	 * When the samples are collected they are reported as usual via the
	 * ProtocolDecoderSink.
	 * @param samples Number of samples to collect.
	 */
	public void startFreeSampling(int samples) {
		m_FreeSampleCount = samples;
        m_FreeSampling = true;
        restartSampler(samples);
	}

    private void restartSampler(int samples) {
        m_SampleCount = 0;
        m_Pulses = new LinkedList<Integer>();
        m_Samples = new ArrayList<Integer>(samples+ 1);
        m_PulseLengths = new LinkedList<Double>();
        maxSampleLength = samples;
    }

	public void addSample(int sample) {
        calculateSignalLevel(sample);
		if (m_IsSampling || m_FreeSampling) {
			m_Samples.add(sample);
			m_SampleCount++;
            if (m_SampleCount >= maxSampleLength) {
                endMessage(!m_FreeSampling);
            }
		}
	}

    private void calculateSignalLevel(int sample) {
        int absSample = Math.abs(sample);
        if (m_Level < absSample) m_Level = absSample;
        m_LevelReportCount--;
        if (m_LevelReportCount <= 0){
            m_Sink.reportLevel(m_Level);
            m_Level -= 127 / (REPORTSPERSECOND * LEVELTIMETOZERO);
            m_LevelReportCount = m_SampleFrequency/REPORTSPERSECOND;
        }
    }

    public int parse(double pulse, boolean state) {
		switch (m_State) {
			case IDLE: {
				if ((pulse > 0.0) && (pulse < 200000.0) && !state){
                    if (!m_FreeSampling) {
                        restartSampler(m_MaxMessageLength);
                    }
					m_PulseCount = 1;
					m_IsSampling = true;
					m_Pulses.add(m_SampleCount);
					m_PulseLengths.add(pulse);
					m_State = READING_MESSAGE;
				}
				break;
			}
			case READING_MESSAGE: {
				if ((pulse > 0) && ((pulse < RAW_MESSAGE_END_GAP) || m_FreeSampling)) {
					m_Pulses.add(m_SampleCount);
					m_PulseLengths.add(pulse);
					m_PulseCount++;
				}
				else if (m_PulseCount > 1){
					// It has been a long space, so we got our message.
					endMessage(true);
				}
				break;
			}
		}
		m_LastPulse = pulse;
		m_SampleCountAtLastPulse = m_SampleCount;
        return m_State;
	}
	
	/**
	 * End sampling of a raw message and report the collected message
     * @param trimEnd
     */
	protected void endMessage(boolean trimEnd) {
		// First we trim  off the space from the samples
		for (int i = m_Samples.size() - 1; trimEnd && (i > m_SampleCountAtLastPulse); i--){
			m_Samples.remove(i);
		}
		RawProtocolMessage message = new RawProtocolMessage(m_Pulses, m_Samples, m_SampleFrequency, m_PulseLengths);
		// Report the parsed message
		m_Sink.parsedMessage(message);
		m_IsSampling = false;
        m_FreeSampling = false;
        m_FreeSampleCount = 0;
		m_State = IDLE;
		System.out.println("Raw sample with " + Integer.toString(m_SampleCount) + " samples");
	}
}
