package celsior;

public class MathUtils {
    public static char bytesToShort(byte hi, byte low) { // TODO seems faulty
        return (char) (((hi & 0xFF) << 8) | (low & 0xFF));
    }
    
    // Gets left-side byte from char (byte 0)
    public static byte getLeftByte(char c) {
        return (byte) ((c >>> 8) & 0xff);
    }
    
    // Gets right-side byte from char (byte 1)
    public static byte getRightByte(char c) {
        return (byte) (c & 0xff);
    }
    
    /** Create a byte from the given bits. {@code bit1} is the right-most bit, and {@code bit8} is the left-most. */
    public static byte composeByte(boolean bit1, boolean bit2, boolean bit3, boolean bit4, boolean bit5, boolean bit6, boolean bit7, boolean bit8) {
        return composeByte((bit1) ? 1 : 0, (bit2) ? 1 : 0, (bit3) ? 1 : 0, (bit4) ? 1 : 0,
                            (bit5) ? 1 : 0, (bit6) ? 1 : 0, (bit7) ? 1 : 0, (bit8) ? 1 : 0);
    }
    
    /** Create a byte from the given bits (0 = cleared, 1 = set). {@code bit1} is the right-most bit, and {@code bit8} is the left-most. */
    public static byte composeByte(int bit1, int bit2, int bit3, int bit4, int bit5, int bit6, int bit7, int bit8) {
        int[] bits = new int[8];
        byte output = 0x00;
        
        bits[0] = bit1;
        bits[1] = bit2;
        bits[2] = bit3;
        bits[3] = bit4;
        bits[4] = bit5;
        bits[5] = bit6;
        bits[6] = bit7;
        bits[7] = bit8;
        
        for (int i = 0; i < 8; i++)
            output |= bits[i] << i;
        
        return output;
    }
    
    /** Get the bit at the given index in the given byte. {@code index = 0} is the right-most bit, and {@code index = 7} is the left-most bit. */
    public static boolean bitAt(byte val, int index) {
        return ((val >> index) & 0x1) == 1;
    }
}
