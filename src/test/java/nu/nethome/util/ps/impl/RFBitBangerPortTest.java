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

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RFBitBangerPortTest {

    RFBitBangerPort port;
    File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();

    }

    @Test
    public void playSimpleMessage() throws Exception {
        port = new RFBitBangerPort(tempFile.getAbsolutePath());
        int[] message = {10, 20};
        assertThat(port.playMessage(message, 1, 0), is(true));
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(tempFile));
        assertThat(br.read(), is(10));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x01));

        assertThat(br.read(), is(20));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
    }

    @Test
    public void falseWhenFailToOpenDevice() throws Exception {
        port = new RFBitBangerPort("/foo/fie");
        int[] message = {10, 20};
        assertThat(port.playMessage(message, 1, 0), is(false));
    }

    @Test
    public void playSimpleMessageWithLargePulses() throws Exception {
        port = new RFBitBangerPort(tempFile.getAbsolutePath());
        int[] message = {0x123456, 0x789011};
        assertThat(port.playMessage(message, 1, 0), is(true));
        BufferedReader br = null;
        tempFile.length();
        byte [] buffer = new byte[(int) tempFile.length()];
        InputStream ios = null;
        ios = new FileInputStream(tempFile);
        ios.read(buffer);

        assertThat(buffer[3], is((byte)0x01));
        assertThat(buffer[2], is((byte)0x12));
        assertThat(buffer[1], is((byte)0x34));
        assertThat(buffer[0], is((byte)0x56));

        assertThat(buffer[7], is((byte)0x00));
        assertThat(buffer[6], is((byte)0x78));
        assertThat(buffer[5], is((byte)0x90));
        assertThat(buffer[4], is((byte)0x11));
    }

    @Test
    public void playRepeatedMessage() throws Exception {
        port = new RFBitBangerPort(tempFile.getAbsolutePath());
        int[] message = {10, 20};
        assertThat(port.playMessage(message, 2, 0), is(true));
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(tempFile));
        assertThat(br.read(), is(10));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x01));

        assertThat(br.read(), is(20));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));

        assertThat(br.read(), is(10));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x01));

        assertThat(br.read(), is(20));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
        assertThat(br.read(), is(0x00));
    }

    @Test
    public void playRepeatedMessageWithHeader() throws Exception {
        port = new RFBitBangerPort(tempFile.getAbsolutePath());
        int[] message = {0x11, 0x22, 0x33, 0x44};
        assertThat(port.playMessage(message, 2, 2), is(true));
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(tempFile));
        assertThat(readLong(br), is(0x01000011L));
        assertThat(readLong(br), is(0x00000022L));

        assertThat(readLong(br), is(0x01000033L));
        assertThat(readLong(br), is(0x00000044L));

        assertThat(readLong(br), is(0x01000033L));
        assertThat(readLong(br), is(0x00000044L));
    }

    @Test
    public void canPlayEmptyMessage() throws Exception {
        port = new RFBitBangerPort(tempFile.getAbsolutePath());
        int[] message = {};
        assertThat(port.playMessage(message, 1, 0), is(true));
        assertThat(tempFile.length(), is(0L));
    }

    @Test
    public void canPlayEmptyMessageWithNoRepeats() throws Exception {
        port = new RFBitBangerPort(tempFile.getAbsolutePath());
        int[] message = {};
        assertThat(port.playMessage(message, 0, 0), is(true));
        assertThat(tempFile.length(), is(0L));
    }

    private long readLong(BufferedReader br) throws IOException {
        long result = br.read();
        result |= (br.read() << 8);
        result |= (br.read() << 16);
        result |= br.read() << 24;
        return result;
    }
}
