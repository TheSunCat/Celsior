package celsior;

import static celsior.MathUtils.composeByte;

public class ALU {
    public Register r0, r1, r2;
    
    public ALU() {
        r0 = new Register(11);
        r1 = new Register(12);
        r2 = new Register(13);
    }
    
    /** ALU Compare | 00000000 | unused, unused, not equal, less or equal, less, greater equal, greater, equal */
    public void auc() {
        aue();
        boolean equal = r2.getVal() == 1;
        
        aug();
        boolean greater = r2.getVal() == 1;
        
        aul();
        boolean less = r2.getVal() == 1;
        
        age();
        boolean greater_equal = r2.getVal() == 1;
        
        ale();
        boolean less_equal = r2.getVal() == 1;
        
        boolean not_equal = !equal;
        
        byte b = composeByte(equal, greater, less, greater_equal, less_equal, not_equal, false, false);
        
        r2.setVal(b);
    }
    
    // ALU Add
    public void aua() {
        r2.setVal(r0.getVal() + r1.getVal());
    }
    
    // ALU Subtract
    public void aus() {
        r2.setVal(r0.getVal() - r1.getVal());
    }
    
    // ALU Multiply
    public void aum() {
        r2.setVal(r0.getVal() * r1.getVal());
    }
    
    // ALU XOR
    public void aux() {
        r2.setVal(r0.getVal() ^ r1.getVal());
    }
    
    // ALU OR
    public void aur() {
        r2.setVal(r0.getVal() | r1.getVal());
    }
    
    // ALU AND
    public void aud() {
        r2.setVal(r0.getVal() & r1.getVal());
    }
    
    // ALU NOT
    public void aun() {
        r2.setVal(~r0.getVal());
    }
    
    // ALU RSFT
    public void rsft() {
        r2.setVal(r0.getVal() >>> r1.getVal());
    }
    
    // ALU Equals
    private void aue() {
        if(r0.getVal() == r1.getVal())
            r2.setVal(1);
        else
            r2.setVal(0);
    }
    
    // ALU Greater Than
    private void aug() {
        if(r0.getVal() > r1.getVal())
            r2.setVal(1);
        else
            r2.setVal(0);
    }
    
    // ALU Less Than
    public void aul() {
        if(r0.getVal() < r1.getVal())
            r2.setVal(1);
        else
            r2.setVal(0);
    }
    
    // ALU Greater Than Or Equal To
    public void age() {
        if(r0.getVal() >= r1.getVal())
            r2.setVal(1);
        else
            r2.setVal(0);
    }
    
    // ALU Less Than Or Equal To
    public void ale() {
        if(r0.getVal() <= r1.getVal())
            r2.setVal(1);
        else
            r2.setVal(0);
    }
}
