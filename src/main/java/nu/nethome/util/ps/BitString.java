package nu.nethome.util.ps;

import org.apache.commons.lang3.Validate;

/**
 * Represents a string of bits.
 */
public class BitString {
    private static final int MAX_LENGTH = 64;
    private int length;
    private long bits;

    public BitString(long bits, int length) {
        this.bits = bits;
        this.length = length;
    }

    public BitString(int length) {
        Validate.inclusiveBetween(0, MAX_LENGTH, length);
        this.length = length;
    }

    public void setValue(BitString from) {
        length = from.length;
        bits = from.bits;
    }

    public BitString() {
        length = 0;
    }

    public int length() {
        return length;
    }

    public void clear() {
        length = 0;
        bits = 0;
    }

    public boolean getBit(int position) {
        Validate.inclusiveBetween(0, length - 1, position);
        return ((bits >> position) & 1) == 1;
    }

    public void setBit(int position, boolean value) {
        Validate.inclusiveBetween(0, MAX_LENGTH, length);
        if (position >= length) {
            length = position + 1;
        }
        long bitMask = 1L << position;
        if (value) {
            bits |= bitMask;
        } else {
            bits &= ~bitMask;
        }
    }

    public void addMsb(boolean msb) {
        Validate.isTrue(length < MAX_LENGTH);

        setBit(length, msb);
    }

    public void addLsb(boolean lsb) {
        Validate.isTrue(length < MAX_LENGTH);

        bits <<= 1;
        bits |= lsb ? 1 : 0;
        length++;
    }

    public boolean shiftRight(int positions) {
        long original = bits;
        bits >>= positions;
        return (original & 1) != 0;
    }

    public int extractInt(Field position) {
        Validate.isTrue(position.startBit < MAX_LENGTH);
        Validate.isTrue(position.startBit + position.length < MAX_LENGTH);
        long result = bits;
        result >>= position.startBit;
        result &= ((1L << position.length) - 1L);
        return (int)result;
    }

    public int extractSignedInt(Field position) {
        int rawInt = extractInt(position);
        if ((rawInt & (1L << (position.length - 1))) != 0) {
            int mask = 0xFFFFFFFF & ~((1 << position.length) - 1);
        }
        Validate.isTrue(position.startBit + position.length < MAX_LENGTH);
        long result = bits;
        result >>= position.startBit;
        result &= ((1L << position.length) - 1L);
        return (int)result;
    }

    public void insert(Field position, int value) {
        Validate.isTrue(position.startBit + position.length < MAX_LENGTH);
        if (position.startBit + position.length > length) {
            length = position.startBit + position.length;
        }
        long clearMask = ((1L << position.length) - 1);
        long valueMask = value & clearMask;

        valueMask <<= position.startBit;
        clearMask <<= position.startBit;
        clearMask = ~clearMask;
        bits &= clearMask;
        bits |= valueMask;
    }

    public int[] toByteInts() {
        int noBytes = (length + 7) / 8;
        int[] result = new int[noBytes];

        for (int i = 0; i < noBytes; i++) {
            result[i] = extractInt(new Field(i * 8, 8));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BitString bitString = (BitString) o;

        if (length != bitString.length) return false;
        return bits == bitString.bits;
    }

    @Override
    public int hashCode() {
        int result = length;
        result = 31 * result + (int) (bits ^ (bits >>> 32));
        return result;
    }

    /**
     * Represents a field of bits within a bit string. The field is defined as start bit position (inclusive) and
     * a length of the field.
     */
    public static class Field {
        public final int startBit;
        public final int length;

        public Field(int startBit, int length) {
            Validate.isTrue(startBit >= 0);
            Validate.isTrue(length > 0);
            this.startBit = startBit;
            this.length = length;
        }
    }

}
