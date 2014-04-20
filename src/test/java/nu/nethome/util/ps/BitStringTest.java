package nu.nethome.util.ps;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class BitStringTest {

    BitString bitString;

    @Before
    public void setUp() throws Exception {

    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorValidatesUpper() throws Exception {
        bitString = new BitString(65);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorValidatesLower() throws Exception {
        bitString = new BitString(-1);
    }

    @Test
    public void constructorSetsAllZero() throws Exception {
        bitString = new BitString(64);

        assertThat(bitString.length(), is(64));
        for (int i = 0; i < 64; i++) {
            assertThat(bitString.getBit(i), is(false));
        }
    }

    @Test
    public void setsBit() throws Exception {
        bitString = new BitString(2);

        bitString.setBit(1, true);
        assertThat(bitString.getBit(0), is(false));
        assertThat(bitString.getBit(1), is(true));
        bitString.setBit(1, false);
        assertThat(bitString.getBit(0), is(false));
        assertThat(bitString.getBit(1), is(false));
    }

    @Test
    public void setBitGrowsString() throws Exception {
        bitString = new BitString(0);

        assertThat(bitString.length(), is(0));
        bitString.setBit(17, false);
        assertThat(bitString.length(), is(18));
    }

    @Test
    public void addMsb() throws Exception {
        bitString = new BitString(1);

        bitString.addMsb(true);
        assertThat(bitString.getBit(0), is(false));
        assertThat(bitString.getBit(1), is(true));
        assertThat(bitString.length(), is(2));
    }

    @Test
    public void addLsb() throws Exception {
        bitString = new BitString(2);
        bitString.setBit(1, true);
        bitString.addLsb(true);
        assertThat(bitString.length(), is(3));
        assertThat(bitString.getBit(0), is(true));
        assertThat(bitString.getBit(1), is(false));
        assertThat(bitString.getBit(2), is(true));
    }

    @Test
    public void extractInteger() throws Exception {
        bitString = new BitString(40);
        bitString.setBit(35, true);

        assertThat(bitString.extractInt(new BitString.Field(35, 5)), is(1));

        bitString.setBit(37, true);
        bitString.setBit(38, true);
        assertThat(bitString.extractInt(new BitString.Field(30, 6)), is(32));
    }

    @Test
    public void insertInteger() throws Exception {
        bitString = new BitString(50);
        bitString.insert(new BitString.Field(45, 5), 5);

        assertThat(bitString.extractInt(new BitString.Field(45, 5)), is(5));
        assertThat(bitString.extractInt(new BitString.Field(40, 10)), is( 5 * 32));
    }

    @Test
    public void insertIntegerGrowsString() throws Exception {
        bitString = new BitString(1);
        bitString.insert(new BitString.Field(5, 5), 5);

        assertThat(bitString.length(), is(10));
        assertThat(bitString.extractInt(new BitString.Field(5, 5)), is(5));
    }
}