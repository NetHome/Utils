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

import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended for testing Encoder/Decoder-pairs for pulse protocols.
 * It will take a pulse sequence (from an Encoder) and play it for a Decoder
 * and collect all messages parsed by the decoder. It basically simulates an
 * AudioPulsePlayer followed by an RF-Transmitter followed by an RF-Receiver
 * followed by an AudioProtocolPort. 
 * 
 * @author Stefan
 *
 */
public class PulseTestPlayer implements ProtocolDecoderSink {

	protected ProtocolDecoder m_Decoder;
	protected ArrayList<ProtocolMessage> m_Messages = new ArrayList<ProtocolMessage>();
	protected int m_PartialllyParsed = 0;
	protected int m_ReportedLevel = 0;
	protected int m_PulseWidthModification = 0;

	public boolean playMessage(int message[]) {
		boolean state = true;
		m_Decoder.parse(10000.0, false);
		for (int pulse : message) {
			if ((pulse < 0) || (pulse > 100000)) {
				return false;
			}
			float adjustedPulse = state ? pulse + m_PulseWidthModification : 
				pulse - m_PulseWidthModification;
			m_Decoder.parse(adjustedPulse, state);
			state = !state;
		}
		return true;
	}
	
	public void setDecoder(ProtocolDecoder decoder) {
		m_Decoder = decoder;
	}
	public void parsedMessage(ProtocolMessage message) {
		m_Messages.add(message);
	}

	public void partiallyParsedMessage(String protocol, int bits) {
		m_PartialllyParsed ++;
		System.out.println("Partially parsed " + protocol + " " + Integer.toString(bits));
	}

	public void reportLevel(int level) {
		m_ReportedLevel  = level;
	}

	public ProtocolMessage[] getMessages() {
		ProtocolMessage result[] = new ProtocolMessage[m_Messages.size()];
		return m_Messages.toArray(result);
	}

	public int getPartialllyParsed() {
		return m_PartialllyParsed;
	}

	public int getReportedLevel() {
		return m_ReportedLevel;
	}
	
	public int getMessageCount() {
		return m_Messages.size();
	}
	
	public int getMessageField(int messageNumber, String fieldName) {
		if (messageNumber >= m_Messages.size()) {
			return -1;
		}
		List<FieldValue> fields = m_Messages.get(messageNumber).getFields();
		for (FieldValue field : fields) {
			if (fieldName.equals(field.getName())) {
				return field.getValue();
			}
		}
		return -1;
	}

	public String getMessageFieldString(int messageNumber, String fieldName) {
		if (messageNumber >= m_Messages.size()) {
			return "";
		}
		List<FieldValue> fields = m_Messages.get(messageNumber).getFields();
		for (FieldValue field : fields) {
			if (fieldName.equals(field.getName()) && (field.getStringValue() != null)) {
				return field.getStringValue();
			}
		}
		return "";
	}

	/**
	 * @return Length of pulse modification in uS.
	 */
	public int getPulseWidthModification() {
		return m_PulseWidthModification;
	}

	/**
	 * A real hardware RF transmitter and receiver will affect the pulse lengths
	 * of the signal which is transmitted and then received. A rough simplification
	 * is that the mark-pulses are stretched by 60uS and the space pulses are
	 * shortened by the same amount. This time may be modified with this method. 
	 * By setting it to 0, no pulse modification is performed. Default value is 60.
	 * 
	 * @param pulseWidthModification Length of pulse modification in uS.
	 */
	public void setPulseWidthModification(int pulseWidthModification) {
		m_PulseWidthModification = pulseWidthModification;
	}

}
