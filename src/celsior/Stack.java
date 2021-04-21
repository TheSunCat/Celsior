package celsior;

public class Stack implements Cloneable {
    byte[] stack;
    int stackPtr = -1;
    
    public Stack(int size) {
        stack = new byte[size];
    }
    
    public int size() {
        return stack.length;
    }
    
    public void push(byte val) {
        if(stackPtr >= stack.length - 1)
            throw new IndexOutOfBoundsException("Stack of size " + stack.length + "overflowed while pushing "
                    + Byte.toUnsignedInt(val) + "(0x" + Integer.toHexString(Byte.toUnsignedInt(val)).toUpperCase() + ").");
        stack[++stackPtr] = val;
    }
    
    public byte pop() {
        if(stackPtr <= -1)
            throw new IndexOutOfBoundsException("Stack of size " + stack.length + "underflowed while popping.");
        
        return stack[stackPtr--];
    }
    
    public byte getElementAt(int pointer) {
        return stack[pointer];
    }
}
