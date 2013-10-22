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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The PulseLengthAnalyzer is used to group pulses with similar lengths and
 * collect statistics for each discovered group.
 * 
 * @author Stefan
 *
 */
public class PulseLengthAnalyzer {

	/**
	 * This class represents a group of pulses with similar length. 
	 * Similar in this case means that the pulses are within the
	 * TOLERANCE.
	 * 
	 * @author Stefan
	 */
	public class PulseLengthGroup implements Comparable<PulseLengthGroup>{
		protected double m_Sum;
		protected int m_Count;
		public double m_Max = 0.0;
		public double m_Min = 100000.0;
		public boolean m_IsMark;
		static final double TOLERANCE = 0.1;
		
		protected PulseLengthGroup() {}
		
		public PulseLengthGroup(double length_us, boolean isMark) {
			m_Max = length_us;
			m_Min = length_us;
			m_Sum = length_us;
			m_Count = 1;
			m_IsMark = isMark;
		}
		
		/**
		 * Tries to get the group to accept a new pulse. If the pulse is within
		 * the TOLERANCE of the group, the pulse is added and it returns true, 
		 * otherwise it returns false and no data is added.
		 * 
		 * @param length_us Length of the pulse in uS
		 * @param isMark True if it is a mark pulse, False if it is a space pulse
		 * @return True if the pulse was accepted
		 */
		public boolean accept(double length_us, boolean isMark, boolean isPrimer) {
			if ((isMark == m_IsMark) && (Math.abs(1 - (length_us / (m_Sum / m_Count))) < TOLERANCE )) {
				m_Sum += length_us;
				m_Count++;
				m_Max = Math.max(m_Max, length_us);
				m_Min = Math.min(m_Min, length_us);
				return true;
			}
			return false;
		}
		
		public int compareTo(PulseLengthGroup o) {
			PulseLengthGroup other = (PulseLengthGroup)o;
			return (m_Count == other.m_Count) ? 0 : ((m_Count > other.m_Count) ? -1 : 1); // Reverse order
		}
		
		/**
		 * Get the average value of the pulse lengths in the group
		 * @return
		 */
		public double getAvarage() {
			return m_Sum / m_Count;
		}
		
		public int getCount() {
			return m_Count;
		}
	}
	
	public class PulseLengthPrimerGroup extends PulseLengthGroup {
		protected double m_PrimerValue = 0;
	
		public PulseLengthPrimerGroup(double length_us, boolean isMark) {
			m_PrimerValue = length_us;
			m_IsMark = isMark;
		}

		@Override
		public boolean accept(double length_us, boolean isMark, boolean isPrimer) {
			if ((isMark == m_IsMark) && (Math.abs(1 - (length_us / m_PrimerValue)) < TOLERANCE )) {
				if (isPrimer) {
					return true;
				}
				m_Sum += length_us;
				m_Count++;
				m_Max = Math.max(m_Max, length_us);
				m_Min = Math.min(m_Min, length_us);
				return true;
			}
			return false;
		}

		@Override
		public double getAvarage() {
			if (m_Count == 0) {
				return m_PrimerValue;
			}
			return super.getAvarage();
		}

	
	}
	
	
	LinkedList<PulseLengthGroup> m_Pulses = new LinkedList<PulseLengthGroup>();
	
	/**
	 * Adds a new pulse for analysis. The pulse is added to an existing group or
	 * a new group is created for it.
	 * 
	 * @param length_us
	 * @param isMark true if this is a mark pulse
	 */
	public void addPulse(double length_us, boolean isMark) {
		if (length_us < 1.0) return; // Don't accept too short pulses
		Iterator<PulseLengthGroup> pulses = m_Pulses.iterator();
		while (pulses.hasNext()) {
			if (pulses.next().accept(length_us, isMark, false)) return;
		}
		PulseLengthGroup newPulse = new PulseLengthGroup(length_us, isMark);
		m_Pulses.add(newPulse);
	}

	/**
	 * Adds a new primer pulse for analysis. A primer pulse is not really a pulse, it is a place holder
	 * where there is determined that the center of a group is to make sure that the groups are 
	 * centered on the correct places. 
	 * 
	 * @param length_us
	 */
	public void addPrimePulse(double length_us, boolean isMark) {
		if (length_us < 1.0) return; // Don't accept too short pulses
		Iterator<PulseLengthGroup> pulses = m_Pulses.iterator();
		while (pulses.hasNext()) {
			if (pulses.next().accept(length_us, isMark, true)) return;
		}
		PulseLengthGroup newPulse = new PulseLengthPrimerGroup(length_us, isMark);
		m_Pulses.add(newPulse);
	}

	
	/**
	 * Returns all found pulse groups and their statistics.
	 * @return List of all found pulse groups
	 */
	public List<PulseLengthGroup> getPulses() {
		
		// Sort the groups based on number of pulses
		Collections.sort(m_Pulses);
		
		// Remove the empty groups
		while ((m_Pulses.size() != 0) && (m_Pulses.getLast().m_Count == 0)) {
			m_Pulses.removeLast();
		}
		return m_Pulses;
	}
}
