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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test the MessageRepeater
 */
public class MessageRepeaterTest {

    ProtocolEncoder encoder;
    Message mess;

    @Before
    public void setUp() throws Exception {
        mess = mock(Message.class);
        encoder = mock(ProtocolEncoder.class);
        int header[] = {1};
        int message[] = {10,11,12,13,14,15,16,17,18,19};
        when(encoder.encode(mess, ProtocolEncoder.Phase.FIRST)).thenReturn(header);
        when(encoder.encode(mess, ProtocolEncoder.Phase.REPEATED)).thenReturn(message);
    }

    @Test
    public void standard() throws BadMessageException {
        int result[];
        result = MessageRepeater.repeat(encoder, mess, 2);
        assertThat(result.length, is(21));
        assertThat(result[0], is(1));
        assertThat(result[1], is(10));
        assertThat(result[10], is(19));
        assertThat(result[11], is(10));
    }

    @Test
    public void emptyPreamble() throws BadMessageException {
        when(encoder.encode(mess, ProtocolEncoder.Phase.FIRST)).thenReturn(new int[0]);
        int result[];
        result = MessageRepeater.repeat(encoder, mess, 2);
        assertThat(result.length, is(20));
        assertThat(result[0], is(10));
        assertThat(result[9], is(19));
        assertThat(result[10], is(10));
    }
}
