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
 * The ProtocolDecoderSink is the interface between a ProtocolDecoder and the
 * receiver of decoded messages. When the decoder has decoded a message, it
 * should report this via this interface.
 * 
 * @author Stefan
 *
 */
public interface ProtocolDecoderSink {
	/**
	 * This method is called by the ProtocolDecoder when a complete message 
	 * is decoded.
	 * @param message The decoded message
	 */
	public void parsedMessage(ProtocolMessage message);

	/**
	 * This method is called by the ProtocolDecoder when a parts of a message
	 * is decoded, but decoding was aborted due to error in the signal. It is not required that the decoder
     * uses this method and if it is used, it should only be called if the decoder has already successfully
     * decoded a number of bits of a message.
	 * 
	 * @param protocol Name of the protocol that was partially decoded.
	 * @param bits How many bits of the message was decoded.
	 */
	public void partiallyParsedMessage(String protocol, int bits);

	/**
	 * This method may be called by the protocol decoder to report the current
	 * signal strength of the incoming analog signal. The interpretation of the
	 * level is up to the decoder, normally it is the heith of the detected flanks.
     *
     * This method is only used by special raw decoders with access to the original analog signal. 
	 * 
	 * @param level Reported signal level
	 */
	public void reportLevel(int level);
}
