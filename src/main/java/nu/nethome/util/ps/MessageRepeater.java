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
* A helper class to generate complete pulse series for repeated messages from a protocol encoder
*/
public class MessageRepeater {
    /**
     * Encode a complete pulse sequence for the specified message using the specified encoder and repeated
     * specified number of times.
     *
     * @param encoder To encode the message
     * @param message Message to encode
     * @param repeat Number of times to repeat the message
     * @return The total encoded sequence
     * @throws BadMessageException
     */
    public static int[] repeat(ProtocolEncoder encoder, Message message, int repeat) throws BadMessageException {
        int preamble[] = encoder.encode(message, ProtocolEncoder.Phase.FIRST);
        int repeated[] = encoder.encode(message, ProtocolEncoder.Phase.REPEATED);
        int result[] = new int[preamble.length + repeated.length * repeat];
        System.arraycopy(preamble, 0, result, 0, preamble.length);
        for (int i = 0; i < repeat; i++) {
            System.arraycopy(repeated, 0, result, preamble.length + repeated.length * i, repeated.length);
        }
        return result;
    }
}
