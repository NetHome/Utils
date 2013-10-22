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
import nu.nethome.util.ps.ProtocolMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * User: Stefan
 * Date: 2011-06-13
 * Time: 19:47
 */
public class ProtocolDecoderGroupTest {


    class TestDecoder implements ProtocolDecoder {

        public int calls = 0;
        public ProtocolDecoderSink sink = null;

        public int parse(double pulseLength, boolean state) {
            return calls++;
        }

        public ProtocolInfo getInfo() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setTarget(ProtocolDecoderSink sink) {
            this.sink = sink;
        }
    }

    class TestProtocolDecoderSink implements ProtocolDecoderSink {
        public void parsedMessage(ProtocolMessage message) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void partiallyParsedMessage(String protocol, int bits) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void reportLevel(int level) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    ProtocolDecoderGroup testSubject;
    ProtocolDecoderSink testSink;
    TestDecoder decoder1;
    TestDecoder decoder2;
    TestDecoder decoder3;

    @Before
    public void setUp() throws Exception {
        testSubject = new ProtocolDecoderGroup();
        testSink = new TestProtocolDecoderSink();
        decoder1 = new TestDecoder();
        decoder2 = new TestDecoder();
        decoder3 = new TestDecoder();
        testSubject.add(decoder1);
        testSubject.add(decoder2);
    }

    @After
    public void tearDown() throws Exception {
        testSubject = null;
        decoder1 = null;
        decoder2 = null;
        decoder3 = null;
    }

    @Test
    public void testIsActive() throws Exception {
        testSubject.setActive(decoder2, false);

        assertTrue(testSubject.isActive(decoder1));
        assertFalse(testSubject.isActive(decoder2));
    }

    @Test
    public void testSetTarget() throws Exception {
        testSubject.setActive(decoder2, false);
        testSubject.setTarget(testSink);

        assertThat(decoder1.sink, is(testSink));
        assertThat(decoder2.sink, is(testSink));
    }

    @Test
    public void testGetInfo() throws Exception {
        assertNull(testSubject.getInfo());
    }

    @Test
    public void testParse() throws Exception {
        testSubject.add(decoder3);
        testSubject.setActive(decoder3, false);
        testSubject.parse(1L, true);
        assertThat(decoder1.calls, is(1));
        assertThat(decoder2.calls, is(1));
        assertThat(decoder3.calls, is(0));
    }

    @Test
    public void testSetActive() throws Exception {
        testSubject.add(decoder3);
        testSubject.setActive(decoder3, false);

        testSubject.parse(1L, true);

        assertThat(decoder1.calls, is(1));
        assertThat(decoder2.calls, is(1));
        assertThat(decoder3.calls, is(0));

        testSubject.setActive(decoder1, false);
        testSubject.setActive(decoder3, true);

        testSubject.parse(1L, true);

        assertThat(decoder1.calls, is(1));
        assertThat(decoder2.calls, is(2));
        assertThat(decoder3.calls, is(1));
        assertThat(testSubject.getAllDecoders().size(), is(3));
    }

    @Test
    public void testRemove() throws Exception {
        testSubject.parse(1L, true);

        assertThat(decoder1.calls, is(1));
        assertThat(decoder2.calls, is(1));

        testSubject.remove(decoder2);
        testSubject.parse(1L, true);

        assertThat(decoder1.calls, is(2));
        assertThat(decoder2.calls, is(1));
    }

    @Test
    public void testRemoveNonActive() throws Exception {

        testSubject.setActive(decoder2, false);
        assertThat(testSubject.getAllDecoders(), hasItem((ProtocolDecoder) decoder2));

        assertThat(testSubject.remove(decoder2), is(true));
        assertThat(testSubject.getAllDecoders(), not(hasItem((ProtocolDecoder) decoder2)));
        assertThat(testSubject.remove(decoder2), is(false));
    }

    @Test
    public void testAdd() throws Exception {
        assertThat(testSubject.getAllDecoders().size(), is(2));
        testSubject.add(decoder3);
        assertThat(testSubject.getAllDecoders().size(), is(3));
    }

    @Test
    public void testGetAllDecoders() throws Exception {
        testSubject.setActive(decoder2, false);
        List<ProtocolDecoder> list = testSubject.getAllDecoders();
        assertThat(list.contains(decoder1), is(true));
        assertThat(list.contains(decoder2), is(true));
        assertThat(list.size(), is(2));
    }
}
