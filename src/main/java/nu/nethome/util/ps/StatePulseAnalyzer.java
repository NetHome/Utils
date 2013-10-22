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

package nu.nethome.util.ps;

import java.util.HashMap;

/**
 * The StatePulseAnalyzer is a utility class used for measuring statistics 
 * about the pulse lengths received by a ProtocolDecoder in different states
 * to help tuning the pulse length constants.
 * 
 * @author Stefan
 *
 */
public class StatePulseAnalyzer {

	public class PulseLength implements Comparable<PulseLength>{
		private String state;
		private double sum = 0;
		private int count = 0;
		private double max = 0;
		private double min = 100000;
		static final double TOLERANCE = 0.1;
		
		public PulseLength(String state) {
			this.state = state;
		}
		
		public boolean accept(double length_us) {
			sum += length_us;
			count++;
			max = Math.max(max, length_us);
			min = Math.min(min, length_us);
			return true;
		}
		
		public int compareTo(PulseLength o) {
			PulseLength other = (PulseLength)o;
			return (count == other.count) ? 0 : ((count > other.count) ? -1 : 1); // Reverse order
		}
		
		public double getAverage() {
			return Math.round((sum * 10) / count) / 10;
		}

		public double getMiddle() {
			return Math.round(((max + min) / 2) * 10) / 10;
		}

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public double getSum() {
            return sum;
        }

        public void setSum(double sum) {
            this.sum = sum;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }
    }
	
	HashMap<String,PulseLength> pulses = new HashMap<String,PulseLength>();
	
	public void addPulse(String state, double length_us) {
		if (length_us < 1.0) return; // Dont accept too short pulses
		PulseLength pulse;
		if (!pulses.containsKey(state)) {
			pulse = new PulseLength(state);
			pulses.put(state, pulse);
		} else {
			pulse = pulses.get(state);
		}
		pulse.accept(length_us);
	}
	
	public HashMap<String,PulseLength> getPulses() {	
		return pulses;
	}
	
	public void printPulses() {
		System.out.println("Stats:");
		for (PulseLength p : pulses.values()) {
			System.out.println(p.state + ": Avg:" + Double.toString(p.getAverage()) +
					" Mid: " + Double.toString(p.getMiddle()) +
					" Max: " + Double.toString(p.max) +
					" Min: " + Double.toString(p.min) +
					" Count: " + Double.toString(p.count));
		}
	}
}
