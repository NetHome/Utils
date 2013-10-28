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
 * This is the interface for a raw sampler. It is fed the raw samples from the
 * data source. 
 * 
 * @author Stefan
 *
 */
public interface ProtocolSampler {
	/**
	 * Add a new data sample.
	 * @param sample 0 - 255
	 */
	public abstract void addSample(int sample);

	/**
	 * This method is used to inform the sampler of which sample rate the data
	 * is sampled.
	 * @param frequency In Hz
	 */
	public abstract void setSampleRate(int frequency);
}