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

package nu.nethome.util.ps;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The RawProtocolMessage is a ProtocolMessage which does not know any
 * specific protocol. It collects all sample data of a protocol message. This is used
 * to be able to present the raw data of an unknown protocol message for analysis.
 * @author Stefan
 */
public class RawProtocolMessage extends ProtocolMessage {
	static final long serialVersionUID = 0;
	public List<Integer> m_PulseList;
	public List<Integer> m_Samples;
	public int m_SampleFrequency = 22000;
	public LinkedList<Double> m_PulseLengths;

	public RawProtocolMessage(List<Integer> pulsePositions, List<Integer> samples, int frequency, LinkedList<Double> pulseLengths) { 
		super("Raw", pulsePositions.size(), 0, 1);
		m_PulseList = pulsePositions;
		m_Samples = samples;
		m_SampleFrequency = frequency;
		m_PulseLengths = pulseLengths;
	}
	@Override
	public String toString() {
		String result = "Raw: ";
		Iterator<Integer> i = m_PulseList.iterator();
		while(i.hasNext()) {
			result += Long.toString(Math.round(i.next() / 100) * 100) + ",";
		}
		return result;
	}
}