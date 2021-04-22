package celsior.component;

public class Register {
    
    public Register(int _id) {
        id = _id;
    }
    
    public void setVal(byte newVal) {
        val = newVal;
    }
    
    public void setVal(int newVal) {
        val = (byte) newVal;
    }
    
    public byte getVal() {
        return val;
    }
    
    public int getBit(int bit) {
        if(bit < 0 || bit > 7)
            throw new IndexOutOfBoundsException("Bit " + bit + " is out of bounds!");
        return (val >>> bit) & 1;
    }
    
    @Override
    public String toString() {
        return "ID: " + id + ", val: " + Byte.toUnsignedInt(val);
    }
    
    private byte val;
    int id;
}
