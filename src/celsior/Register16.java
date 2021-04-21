package celsior;

public class Register16 {
    public Register16(int _id) {
        val = 0x00;
        id = _id;
    }
    
    public void setVal(char newVal) {
        val = newVal;
    }
    
    public char getVal() {
        return val;
    }
    
    private char val;
    int id;
}
