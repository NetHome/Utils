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

/**
 * The ProtocolDecoder interface is used  between a pulse length
 * detecting class and protocol decoders. The pulse decoder feeds the interface with
 * detected pulses and the implementation of the interface tries to detect protocol
 * messages. Detected messages are reported back via the supplied {@link ProtocolDecoderSink} interface.
 * This is the interface a protocol decoder has to implement to be used in the framework.
 * 
 * @author Stefan
 */
public interface ProtocolDecoder {

    /**
	 * This method is called when a new pulse is detected, it feeds the internal
	 * state machine in the decoder.
     * The function shall return an integer representation of the resulting state in the decoder where 0 should
     * represent the idle state where the decoder has not found the start of a message.
	 * 
	 * @param pulseLength Length of the detected pulse in uS.
	 * @param state False if this is a low flank (Space) true if it is a 
	 * high flank (Mark).
     * @return The resulting state in the decoder (0 - 255) where 0 should indicate idle
	 */
	int parse(double pulseLength, boolean state);
	
	/**
	 * Shall return a general specification of the current decoder.
	 * @return Description/specification of the decoder
	 */
	ProtocolInfo getInfo();

    /**
     * Sets the sink which the protocol decoder use to report back decoded messages. The implementer of the
     * protocol decoder can assume that this method will be called before {@link ProtocolDecoder#parse(double, boolean)}
     * is called.
     * @param sink The ProtocolDecoderSink that can be used to report decoded messages.
     */
    void setTarget(ProtocolDecoderSink sink);
}