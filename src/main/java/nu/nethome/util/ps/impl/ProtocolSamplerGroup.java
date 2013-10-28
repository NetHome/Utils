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

import java.util.LinkedList;

/**
 * A ProtocolSampler which just is a group of many ProtocolSamplers. Similar to the Composite-pattern.
 * All operations are just delegated down to all members of the group. This is used when you want
 * more than one ProtocolSamplers receive data from a data source.
 * 
 * @author Stefan
 */
public class ProtocolSamplerGroup extends LinkedList<ProtocolSampler> implements
		ProtocolSampler {

	public void addSample(int sample) {
		for (ProtocolSampler sampler : this) {
			sampler.addSample(sample);
		}
	}

	public void setSampleRate(int frequency) {
		for (ProtocolSampler sampler : this) {
			sampler.setSampleRate(frequency);
		}
	}
}
