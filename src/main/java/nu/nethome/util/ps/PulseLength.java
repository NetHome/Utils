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

/**
 * Represents a pulse length including tolerance limits when receiving
 * the pulse. This is used by protocol decoders to
 * @author Stefan
 */
public class PulseLength {

	private int m_CenterLength;
	private int m_UpperLimit;
	private int m_LowerLimit;
	private String m_BaseName;
	
	/**
	 * Create the pulse length
	 * @param decoder The decoder that uses this pulse. The class name is used for the name of 
	 * the system parameter 
	 * @param name Name of this parameter
	 * @param length The actual pulse length in micro seconds
	 * @param upperLimit The highest acceptable pulse length in micro seconds
	 * @param lowerLimit  The lowest acceptable pulse length in micro seconds
	 */
	@SuppressWarnings("unchecked")
	public PulseLength(Class decoder, String name, int length, 
			int lowerLimit, int upperLimit) {
		createPulseLength(decoder, name, length, lowerLimit, upperLimit);
	}

	/**
	 * Create the pulse length
	 * @param decoder The decoder that uses this pulse. The class name is used for the name of 
	 * the system parameter 
	 * @param name Name of this parameter
	 * @param length The actual pulse length in micro seconds
	 * @param tolerance The acceptable pulse length tolerance in micro seconds
	 */
	@SuppressWarnings("unchecked")
	public PulseLength(Class decoder, String name, int length, 
			int tolerance) {
		createPulseLength(decoder, name, length, length - tolerance, length + tolerance);
	}
	
	/**
	 * Set parameters for the pulse length
	 * @param decoder The decoder that uses this pulse. The class name is used for the name of 
	 * the system parameter 
	 * @param name Name of this parameter
	 * @param length The actual pulse length in micro seconds
	 * @param upperLimit The highest acceptable pulse length in micro seconds
	 * @param lowerLimit  The lowest acceptable pulse length in micro seconds
	 */
	private void createPulseLength(Class<ProtocolDecoder> decoder, String name, int length, 
			int lowerLimit, int upperLimit) {
		m_BaseName = decoder.getName() + "." + name;
		
		// Set the values, and use configurations from system properties
		// if available
		m_CenterLength = getModifiedLength("Length", length);
		m_UpperLimit = getModifiedLength("Upper", upperLimit);
		m_LowerLimit = getModifiedLength("Lower", lowerLimit);
	}
	
	/**
	 * Get the standard length of the pulse
	 * @return pulse length
	 */
	public int length() {
		return m_CenterLength;
	}
	
	/**
	 * Verify if the supplied length is within this pulse's tolerance
	 * @param pulse Pulse length to compare with
	 * @return true if the supplied pulse length is within tolerance
	 */
	public boolean matches(double pulse) {
		return ((pulse >= m_LowerLimit) && (pulse <= m_UpperLimit));
	}
	
	/**
	 * Check if the supplied pulse length has been overridden by a system
	 * property, and return the new value in that case.
	 * @param name name of the length type
	 * @param defaultValue the default value of the length
	 * @return the modified value
	 */
	private int getModifiedLength(String name, int defaultValue) {
		return Integer.parseInt(System.getProperty(m_BaseName + "." + name, 
				Integer.toString(defaultValue)));
	}
}
