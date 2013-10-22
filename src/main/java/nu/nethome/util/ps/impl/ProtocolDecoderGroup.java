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
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * A ProtocolDecoder which just is a group of many ProtocolDecoders. Similar to the Composite-pattern.
 * All operations are just delegated down to all members of the group. This is used when you want
 * more than one ProtocolDecoder receive data from a data source.
 * 
 * @author Stefan
 */

public class ProtocolDecoderGroup implements
		ProtocolDecoder {

    private final LinkedList<ProtocolDecoder> activeDecoders = new LinkedList<ProtocolDecoder>();
    private final LinkedList<ProtocolDecoder> passiveDecoders = new LinkedList<ProtocolDecoder>();

    public boolean isActive(ProtocolDecoder decoder) {
		return activeDecoders.contains(decoder);
	}

    public void setTarget(ProtocolDecoderSink sink) {
        for (ProtocolDecoder decoder : activeDecoders) {
            decoder.setTarget(sink);
        }
        for (ProtocolDecoder decoder : passiveDecoders) {
            decoder.setTarget(sink);
        }
    }

    public ProtocolInfo getInfo() {
		// No point in returning any value here
		return null;
	}

	public int parse(double pulseLength, boolean state) {
		// Let all decoders in the group parse this
        int result = 0;
		for (ProtocolDecoder decoder : activeDecoders) {
			result = decoder.parse(pulseLength, state);
		}
        return result;
	}

	public void setActive(ProtocolDecoder decoder, boolean active) {
        if (active) {
            passiveDecoders.remove(decoder);
            if (!activeDecoders.contains(decoder)) {
                activeDecoders.add(decoder);
            }
        } else {
            activeDecoders.remove(decoder);
            if (!passiveDecoders.contains(decoder)) {
                passiveDecoders.add(decoder);
            }
        }
	}

    public boolean remove(ProtocolDecoder o) {
        return activeDecoders.remove(o) || passiveDecoders.remove(o);
    }

    public boolean add(ProtocolDecoder protocolDecoder) {
        return activeDecoders.add(protocolDecoder);
    }

    public List<ProtocolDecoder> getAllDecoders() {
        List<ProtocolDecoder> result = new LinkedList<ProtocolDecoder>();
        for (ProtocolDecoder d : activeDecoders) {
            result.add(d);
        }
        for (ProtocolDecoder d : passiveDecoders) {
            result.add(d);
        }
        return result;
    }
}
