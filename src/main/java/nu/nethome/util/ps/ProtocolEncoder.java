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
 * This is a generic interface for encoders of simple pulse based RF/IR protocols. The encode-method uses a
 * generic message class for the messages to send. It is recommended that implementers of this interface
 * also supplies factory methods to create Message-objects which are guaranteed to be correct for the protocol.
 */
public interface ProtocolEncoder {

    public enum Phase {FIRST, REPEATED}

    /**
     * Shall return a general specification of the current decoder.
     * @return Description/specification of the decoder
     */
    ProtocolInfo getInfo();

    /**
     * Encodes the given Message according to the protocol. The result is a list of pulse lengths given in micro
     * seconds. The first value is the length of the first mark MARK flank of the first pulse, the second value
     * is the length of the SPACE between the first and the second pulse, the third value is the length of the
     * second MARK pulse and so on.
     * Most of these kind of protocols introduce redundancy by repeating the messages for as long as the user holds
     * the button on the remote. In some protocols there is a difference between the first message in a repeated
     * sequence and the repeated messages. Often the first message is preceded by some sort of preamble, usually one
     * long pulse for the receiver to adjust gain and then a long space. The encoder must be able to generate the
     * preamble and the repeat message separately. The phase parameter specifies which phase should be generated.
     * To get a complete message, the user of this interface must first call the encode message with phase set to
     * FIRST and then call the encode method with phase set to REPEATED and then concatenate the results and repeat the
     * result from REPEATED the required number of times. The helper class {@link MessageRepeater} can do just that.
     * If the protocol has no separate preamble, the method should return an empty array (not null).
     *
     * @param message The protocol message to encode
     * @param phase The repeat phase this message is in
     * @return The encoded signal in form of pulse lengths in micro seconds.
     * @throws BadMessageException Is thrown when the Message object violates the protocol restrictions or lack
     * needed fields
     */
    int[] encode(Message message, Phase phase) throws BadMessageException;

    /**
     * Most IR-protocols are modulated with a frequency, usually around 40KHz. For some protocols (Pronto for example)
     * the modulation frequency is embedded in the message itself. An encoder must be able to report the requested
     * modulation frequency given a specific message. If the protocol does not support modulation the encoder should
     * return 0 (zero)
     * @param message Protocol message that optionally may be used to determine the modulation frequency
     * @return Modulation frequency in Hz or 0 if modulation is not applicable
     */
    int modulationFrequency(Message message);
}
