package celsior.component;

import java.util.Arrays;

public class Memory {
    
    public Memory(int size) {
        m = new byte[size];
    }
    
    public void putByte(char address, byte b) {
        m[address] = b;
    }
    
    public void putByte(int address, int b) {
        putByte((char) address, (byte) b);
    }
    
    public void putBytes(int startAddress, byte[] bytes) {
        System.arraycopy(bytes, 0x10, m, startAddress, bytes.length - 0x10);
    }
    
    public byte getByte(char address) {
        return m[address];
    }
    
    public byte getByte(int address) {
        return getByte((char) address);
    }
    
    public byte[] getBytes(char from, char to) {
        return Arrays.copyOfRange(m, from, to);
    }
    
    public int size() {
        return m.length;
    }
    
    private final byte[] m;
}
