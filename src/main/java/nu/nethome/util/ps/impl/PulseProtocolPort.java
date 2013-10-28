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

/**
 * The interface for pulse protocol ports. Pulse protocols are "slow" pulse length based
 * protocols such as IR-Remote protocols and RF-protocols for remote switches.
 * The implementation of this interface feeds the supplied ProtocolDecoders with pulses as
 * they are received from the source (for example an IR-decoder hardware) and the ProtocolDecoders
 * Interpret their protocols from the pulse trains.
 * 
 * @author Stefan Str�mberg
 */
public interface PulseProtocolPort {

//	/**
//	 * If the port receives raw samples between the pulses, they are fed to the ProtocolSampler
//	 * if one is provided via this method.
//	 * @param d
//	 */
//	public abstract void setSampler(ProtocolSampler d);
//
//	/**
//	 * Add a decoder to the port. Multiple decoders are supported
//	 * @param d
//	 */
//	public abstract void addDecoder(ProtocolDecoder d);

	/**
	 * Open the port and start sending pulses to the decoders.
	 * @return
	 */
	public abstract int open();

	/**
	 * Close the port and stop sending pulses to the decoders.
	 */
	public abstract void close();

	/**
	 * Check if the port is currently open.
	 * @return True if the port is currently open
	 */
	public abstract boolean isOpen();
}