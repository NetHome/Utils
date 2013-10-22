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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RFBitBangerPort {
    public static final String DEFAULT_DEVICE_NAME = "/dev/rfbb";

    private static final long LIRC_MODE2_SPACE = 0x00000000L;
    private static final long LIRC_MODE2_PULSE = 0x01000000L;
    private static final long LIRC_VALUE_MASK = 0x00FFFFFF;
    private Logger logger = Logger.getLogger(RFBitBangerPort.class.getName());
    private String deviceName = DEFAULT_DEVICE_NAME;

    public RFBitBangerPort(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Transmit the list of pulses (in microseconds)
     *
     * @param message      List of pulse lengths in microseconds, beginning with a mark pulse
     * @param repeat       Number of times to repeat message
     * @param repeatOffset Number pulses into the message the repeat sequence should begin
     * @return True if successful
     */
    public boolean playMessage(int message[], int repeat, int repeatOffset) {
        byte[] messageBytes = new byte[(repeatOffset + (message.length - repeatOffset) * repeat) * 4];
        int writePosition = 0;
        boolean state = true;

        for (int i = 0; i < repeatOffset; i++) {
            writePosition = writePulse(state, message[i], messageBytes, writePosition);
            state = !state;
        }
        for (int repeatCounter = 0; repeatCounter < repeat; repeatCounter++) {
            for (int i = repeatOffset; i < message.length; i++) {
                writePosition = writePulse(state, message[i], messageBytes, writePosition);
                state = !state;
            }
        }
        FileOutputStream device = null;
        boolean result = false;
        try {
            device = new FileOutputStream(deviceName);
            device.write(messageBytes);
            result = true;
        } catch (IOException e) {
            String errorMessage = device == null ? "Could not open rf-bitbanger device" : "Could not write to rf-bitbanger device";
            logger.log(Level.WARNING, errorMessage, e);
        } finally {
            if (device != null) {
                try {
                    device.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return result;
    }

    private int writePulse(boolean state, int pulse, byte[] messageBytes, int writePosition) {
        long value = pulse & LIRC_VALUE_MASK;
        value |= (state ? LIRC_MODE2_PULSE : 0L);
        messageBytes[writePosition] = (byte) (value & 0xFF);
        messageBytes[writePosition + 1] = (byte) ((value >> 8) & 0xFF);
        messageBytes[writePosition + 2] = (byte) ((value >> 16) & 0xFF);
        messageBytes[writePosition + 3] = (byte) ((value >> 24) & 0xFF);
        return writePosition + 4;
    }
}
