package celsior;

import static celsior.MathUtils.*;

public class CPU {
    public Memory m;
    public ALU alu;
    
    public Memory h0, h1;
    public Stack stack;
    
    private Register r0, r1, r2, r3,
            r4, r5, r6, r7;
    
    private Register input;
    
    private Register pc0, pc1, flags;
    
    private Register secret0, secret1;
    
    private Register16 abr;
    
    public int progCounter = 0x00;
    
    protected byte dataBus = 0x00;
    private int addressBus = 0x00;
    
    public CPU() {
        reset(true);
    }
    
    public void reset(boolean clearMem) {
        r0 = new Register(0);
        r1 = new Register(1);
        r2 = new Register(2);
        r3 = new Register(3);
        r4 = new Register(4);
        r5 = new Register(5);
        r6 = new Register(6);
        r7 = new Register(7);
        
        pc0 = new Register(8);
        pc1 = new Register(9);
        
        flags = new Register(10);
        
        abr = new Register16(14);
        
        secret0 = new Register(15);
        secret1 = new Register(16);
        
        input = new Register(17); // INPUT
        
        alu = new ALU();
        
        progCounter = 0x00;
        dataBus = 0x00;
        addressBus = 0x00;
        
        if(clearMem) {
            m = new Memory(65536); // 64kb
            h0 = new Memory(256);
            h1 = new Memory(256);
            
            stack = new Stack(8);
        }
        
        log("Reset CPU", false);
    }
    
    public void clock() {
        byte b = getByte();
        switch(b) {
            case 0x00:
                abort("Encountered NOP");
                break;
            case 0x01:
                instruction("ADD");
                add(getByte(), getByte(), getByte());
                break;
            case 0x02:
                instruction("SUB");
                sub(getByte(), getByte(), getByte());
                break;
            case 0x03:
                instruction("MUL");
                mul(getByte(), getByte(), getByte());
                break;
            case 0x04:
                instruction("RGT");
                rgt(getByte(), getByte(), getByte());
                break;
            case 0x05:
                instruction("LFT");
                lft(getByte(), getByte(), getByte());
                break;
            case 0x06:
                instruction("LBL");
                lbl(getByte(), getByte(), getByte());
                break;
            case 0x07:
                instruction("JMP");
                jmp(getByte());
                break;
            case 0x08:
                instruction("JIF");
                jif(getByte(), getByte());
                break;
            case 0x09:
                instruction("MOV");
                mov(getByte());
                break;
            case 0xA:
                instruction("PUSH");
                push(getByte());
                break;
            case 0x0B:
                instruction("RTR");
                rtr(getByte(), getByte());
                break;
            case 0x0C:
                instruction("MTR");
                mtr(getByte(), getByte(), getByte());
                break;
            case 0x0D:
                instruction("RTM");
                rtm(getByte(), getByte(), getByte());
                break;
            case 0x0E:
                instruction("MTM");
                mtm(getByte(), getByte(), getByte(), getByte());
                break;
            case 0x0F:
                instruction("VTR");
                vtr(getByte(), getByte());
                break;
            case 0x10:
                instruction("RTV");
                rtv(getByte(), getByte());
                break;
            case 0x11:
                instruction("FTR");
                ftr(getByte());
                break;
            case 0x12:
                instruction("CMP");
                cmp(getByte(), getByte());
                break;
            case 0x13:
                instruction("AND");
                and(getByte(), getByte(), getByte());
                break;
            case 0x14:
                instruction("NOT");
                not(getByte(), getByte());
                break;
            case 0x15:
                instruction("OR");
                or(getByte(), getByte(), getByte());
                break;
            case 0x16:
                instruction("XOR");
                xor(getByte(), getByte(), getByte());
                break;
            case 0x50:
                instruction("PXL");
                Celsior.gpu.pxl(getByte(), getByte(), getByte());
                break;
            case 0x51:
                instruction("LINE");
                Celsior.gpu.line(getByte(), getByte(), getByte(), getByte(), getByte());
                break;
            case 0x52:
                instruction("PRT");
                Celsior.gpu.prt(getByte(), getByte(), getByte());
                break;
            case 0x53:
                instruction("GMT");
                Celsior.gpu.gmt();
                break;
            default:
                abort("Unknown instruction " + Integer.toHexString(b));
        }
        
        if(Celsior.debugging) {
            updateDebug();
        }
    }
    
    public void abort(String errorMessage) {
        log(errorMessage + " at 0x" + Integer.toHexString(progCounter - 1).toUpperCase() + ".", true);
        
        Celsior.instance.pause();
    }
    
    public void instruction(String instructionName) {
        log("Executed " + instructionName + " at 0x" + Integer.toHexString(progCounter - 1).toUpperCase() + ".", false);
    }
    
    public void log(String message, boolean error) {
        String type;
        
        if(error)
            type = "[ERROR] ";
        else
            type = "[DEBUG] ";
        
        System.out.println(type + message);
        
        if(error)
            throw new IllegalArgumentException(type + message);
    }
    
    /**
     * Gets the byte stored in memory at progCounter. Then, progCounter is incremented.
     * @return current byte in mem at progCounter + 1
     */
    public byte getByte() {
        byte ret = m.getByte(progCounter);
        //System.out.println(Byte.toUnsignedInt(ret));
        pca();
        return ret;
    }
    
    /**
     * >>>>>>>>
     * ASSEMBLY
     * >>>>>>>>
    */
    
    // ARITHMETIC
    
    /**
     * Add registers and output to regOut.
     * @param reg1 First register
     * @param reg2 Second register
     * @param regOut Register to output to
     */
    public void add(byte reg1, byte reg2, byte regOut) {
        rtu(reg1, (byte) 0);
        rtu(reg2, (byte) 1);
        alu.aua();
        auo();
        callRw(regOut);
    }
    
    /**
     * Subtract reg2 from reg1 and output to regOut.
     * @param reg1 First register
     * @param reg2 Second register
     * @param regOut Register to output to
     */
    public void sub(byte reg1, byte reg2, byte regOut) {
        rtu(reg1, (byte) 0);
        rtu(reg2, (byte) 1);
        alu.aus();
        auo();
        callRw(regOut);
    }
    
    /**
     * Multiply reg1 by reg2 and output to regOut.
     * @param reg1 First register
     * @param reg2 Second register
     * @param regOut Register to output to
     */
    public void mul(byte reg1, byte reg2, byte regOut) {
        rtu(reg1, (byte) 0);
        rtu(reg2, (byte) 1);
        alu.aum();
        auo();
        callRw(regOut);
    }
    
    /**
     * Right shift reg by num, output to regOut.
     * @param reg
     * @param num
     * @param regOut Register to output to
     */
    public void rgt(byte reg, byte num, byte regOut) {
        rtu(reg, (byte) 0);
        dataBus = num;
        au1();
        alu.rsft();
        auo();
        callRw(regOut);
    }
    
    /**
     * Left shift reg1 by num, output to regOut.
     * @param reg Register in
     * @param num 
     * @param regOut Register to output to
     */
    public void lft(byte reg, byte num, byte regOut) {
        dataBus = 8;
        au0();
        dataBus = num;
        au1();
        alu.aus();
        auo();
        au1();
        rtu(reg, (byte) 0);
        alu.rsft();
        auo();
        callRw(regOut);
    }
    
    // JUMPING
    
    /**
     * Saves the current program counter position to the header RAM at {@code index}.
     * @param index the label index
     */
    public void lbl(byte index, byte mem0, byte mem1) {
        dataBus = mem0;
        ard0();
        
        dataBus = mem1;
        ard1();
        
        rta();
        
        dataBus = index;
        ath(); // adress bus to header at dataBus index
    }
    
    /**
     * Sets the program counter to the location located in the Header RAM at {@code index}.
     * @param index index of address stored in Header RAM.
     */
    public void jmp(byte index) {
        dataBus = index;
        hta();
        
        rta();
        
        atp();
        
        pca();
    }
    
    /**
     * Jumps to {@code index} if the {@code compareType} is set in the flags register.
     * @param index index of address stored in Header RAM.
     * @param compareType 0 = equal, 1 = greater, 2 = less, 3 = greater_e, 4 = less_e, 5 = not_equal
     */
    public void jif(byte index, byte compareType) {
        if(bitAt(flags.getVal(), compareType))
            jmp(index);
    }
    
    // REGISTERS
    
    /**
     * Set register to value.
     * @param regTo register to set
     */
    public void mov(byte regTo) {
        pta();
        pca();
        me();
        callRw(regTo);
    }
    
    /**
     * Push register to stack.
     * @param regFrom register to push
     */
    public void push(byte regFrom) {
        callRe(regFrom);
        stw();
    }

    /**
     * Register Copy
     * @param rFrom register from
     * @param rTo register to
     */
    public void rtr(byte rFrom, byte rTo) {
        callRe(rFrom);
        callRw(rTo);
    }
    
    /**
     * Memory write to register.
     * @param mem1 first byte of mem instructions
     * @param mem2 second byte of mem instructions
     * @param rTo register to copy to
     */
    public void mtr(byte mem1, byte mem2, byte rTo) {
        dataBus = mem1;
        ard0();
        dataBus = mem2;
        ard1();
        rta();
        me();
        callRw(rTo);
    }
    
    /**
     * Register Write to Memory.
     * @param rFrom register from
     * @param mem1 first byte of mem address
     * @param mem2 second byte of mem address
     */
    public void rtm(byte rFrom, byte mem1, byte mem2) {
        // load address into address bus
        dataBus = mem1;
        ard0();
        dataBus = mem2;
        ard1();
        rta();
        
        // load register into data bus
        callRe(rFrom);
        
        // write data bus to memory
        mw();
    }
    
    /**
     * Memory Copy
     * @param mem1_a first byte of mem address
     * @param mem2_a second byte of mem address
     * @param mem1_b first byte of second mem address
     * @param mem2_b second byte of second mem address
     */
    public void mtm(byte mem1_a, byte mem2_a, byte mem1_b, byte mem2_b) {
        // mem_a to s0
        dataBus = mem1_a;
        ard0();
        dataBus = mem2_a;
        ard1();
        rta();
        me();
        s0w();
        
        // s0 to mem_b
        dataBus = mem1_b;
        ard0();
        dataBus = mem2_a;
        ard1();
        rta();
        s0e();
        mw();
    }
    
    /**
     * Variable Copy to Register
     * @param vFrom var id from
     * @param rTo register to
     */
    public void vtr(byte vFrom, byte rTo) {
        // get var value into dataBus
        dataBus = (byte) 255;
        ard0();
        dataBus = (byte) 255;
        ard1();
        rta();
        dab(vFrom);
        me();
        
        // copy var value to register
        callRw(rTo);
    }
    
    /**
     * Register Copy to Variable
     * @param rFrom register from
     * @param vTo variable id to
     */
    public void rtv(byte rFrom, byte vTo) {
        // get var address into address bus
        dataBus = (byte) 255;
        ard0();
        dataBus = (byte) 255;
        ard1();
        rta();
        dab(vTo);
        
        // load register value into data bus
        callRe(rFrom);
        
        // copy data bus to memory
        mw();
    }
    
    /**
     * Flags Register Copy to Register
     */
    public void ftr(byte rTo) {
        fe();
        callRw(rTo);
    }    
    // COMPARISON
    
    /**
     * Sets flags register according to results of comparison. <br>
     * Register is structured like so:<br>
     * unused, unused, not equal, less or equal, less, greater equal, greater, equal
     * @param reg1 first register to compare
     * @param reg2 second register to compare
     */
    public void cmp(byte reg1, byte reg2) {
        callRe(reg1);
        au0();
        callRe(reg2);
        au1();
        alu.auc();
        auo();
        flags.setVal(dataBus);
    }
    
    /**
     * AND reg1 and reg2, output to regOut.
     * @param reg1 First register
     * @param reg2 Second register
     * @param regOut Register to output to
     */
    public void and(byte reg1, byte reg2, byte regOut) {
        rtu(reg1, (byte) 0);
        rtu(reg2, (byte) 1);
        alu.aud();
        auo();
        callRw(regOut);
    }
    
    /**
     * NOT reg, output to regOut.
     * @param reg Register in
     * @param regOut Register to output to
     */
    public void not(byte reg, byte regOut) {
        rtu(reg, (byte) 0);
        alu.aun();
        auo();
        callRw(regOut);
    }
    
    /**
     * OR reg1 and reg2, output to regOut.
     * @param reg1 First register
     * @param reg2 Second register
     * @param regOut Register to output to
     */
    public void or(byte reg1, byte reg2, byte regOut) {
        rtu(reg1, (byte) 0);
        rtu(reg2, (byte) 1);
        alu.aur();
        auo();
        callRw(regOut);
    }
    
    /**
     * XOR reg1 and reg2, output to regOut.
     * @param reg1 First register
     * @param reg2 Second register
     * @param regOut Register to output to
     */
    public void xor(byte reg1, byte reg2, byte regOut) {
        rtu(reg1, (byte) 0);
        rtu(reg2, (byte) 1);
        alu.aux();
        auo();
        callRw(regOut);
    }
    
    // GPU
    
    /**
     * Display a color pixel on the screen at the specified coordinates.
     * @param rX the register containing the X position
     * @param rY the register containing the X position
     * @param rCol the register containing the color
     */
//    private void pxl(byte rX, byte rY, byte rCol) {
//        callRe(rX);
//        byte x = dataBus;
//        
//        callRe(rY);
//        byte y = dataBus;
//        
//        callRe(rCol);
//        byte color = dataBus;
//        
//        Celsior.gpu.pxl(x, y, color);
//    }
    
    /**
     * >>>>>>>>
     * MIDDLEASSEMBLY..?
     * >>>>>>>>
    */
    
    /**
     * Register Copy to ALU
     * @param rFrom register from
     * @param aluTo register to
     */
    private void rtu(byte rFrom, byte aluTo) {
        callRe(rFrom);
        switch (aluTo) {
            case 0:
                au0();
                break;
            case 1:
                au1();
                break;
            default:
                throw new IllegalArgumentException("copying to nonexistent ALU register! " + aluTo);
        }
    }
    
    /**
     * Compare value to value
     * @param num1 Number 1
     * @param num2 Number 2
     */
    private void cmv(byte num1, byte num2) {
        dataBus = num1;
        au0();
        dataBus = num2;
        au1();
        alu.auc();
        auo();
        flags.setVal(dataBus);
    }
    
    /**
     * Decrement address bus by value, forces CPU to dab
     * @param n Value to decrement by
     */
    private void dab(byte n) {
        addressBus -= n;
    }
    
    /**
     * >>>>>>>>
     * SUBASSEMBLY
     * >>>>>>>>
    */
    
    /** Program Counter Advance */
    private void pca() {
        progCounter++;
    }
    
    /** Program Counter Subtract */
    private void pcs() {
        progCounter--;
    }
    
    /** Program Counter 0 (write databus to reg) */
    private void pc0() {
        pc0.setVal(dataBus);
    }
    
    /** Program Counter 1 (write databus to reg) */
    private void pc1() {
        pc1.setVal(dataBus);
    }
    
    /** Program Counter Enable (write pc regs to programcounter short) */
    private void pce() {
        progCounter = bytesToShort((byte) pc0.getVal(), (byte) pc1.getVal());
    }
    
    /** Memory Write - set memory at addressBus's pointer to contents of dataBus */
    private void mw() {
        m.putByte(addressBus, dataBus);
    }
    
    /** Memory Enable - set dataBus to contents of memory located at addressBus's pointer */
    private void me() {
        dataBus = m.getByte(addressBus);
    }
    
    /** Databus to ALU Reg 0 */
    private void au0() {
        alu.r0.setVal(dataBus);
    }
    
    /** Databus to ALU Reg 1 */
    private void au1() {
        alu.r1.setVal(dataBus);
    }
    
    /** ALU Out - Write ALU Output Reg to Databus */
    private void auo() {
        dataBus = alu.r2.getVal();
    }
    
    /** Address Bus copy to Program Counter */
    private void atp() {
        progCounter = addressBus;
    }
    
    /** Program Counter copy to Address Bus */
    private void pta() {
        addressBus = (char) progCounter;
    }
    
    /** Address Bus copy to Address Bus Register */
    private void atr() {
        abr.setVal((char) addressBus);
    }
    
    /** Address Bus Register copy to Address Bus */
    private void rta() {
        addressBus = abr.getVal();
    }
    
    /** Data Bus copy to Left Byte of Address Bus */
    private void ard0() {
        abr.setVal(bytesToShort(dataBus, getRightByte(abr.getVal())));
    }
    
    /** Data Bus copy to Right Byte of Address Bus */
    private void ard1() { 
        abr.setVal(bytesToShort(getLeftByte(abr.getVal()), dataBus));
    }
    
    /** Address Bus to H-mem at index {@code databus}*/
    private void ath() {
        h0.putByte(dataBus, getLeftByte((char) addressBus));
        h1.putByte(dataBus, getRightByte((char) addressBus));
    }
    
    /** Get index dataBus from header RAM and copy to Address Bus */
    private void hta() {
        abr.setVal(bytesToShort(h0.getByte(dataBus), h1.getByte(dataBus)));
    }
    
    // Following are databus -> register writes
    private void r0w() {
        r0.setVal(dataBus);
    }
    
    private void r1w() {
        r1.setVal(dataBus);
    }
    
    private void r2w() {
        r2.setVal(dataBus);
    }
    
    private void r3w() {
        r3.setVal(dataBus);
    }
    
    private void r4w() {
        r4.setVal(dataBus);
    }
    
    private void r5w() {
        r5.setVal(dataBus);
    }
    
    private void r6w() {
        r6.setVal(dataBus);
    }
    
    private void r7w() {
        r7.setVal(dataBus);
    }
    
    private void r8w() {
        input.setVal(dataBus);
    }
    
    private void s0w() {
        secret0.setVal(dataBus);
    }
    
    private void s1w() {
        secret1.setVal(dataBus);
    }
    
    /**
     * Push dataBus to stack.
     */
    private void stw() {
        stack.push(dataBus);
    }
    
    // Following are register -> databus writes
    private void r0e() {
        dataBus = r0.getVal();
    }
    
    private void r1e() {
        dataBus = r1.getVal();
    }
    
    private void r2e() {
        dataBus = r2.getVal();
    }
    
    private void r3e() {
        dataBus = r3.getVal();
    }
    
    private void r4e() {
        dataBus = r4.getVal();
    }
    
    private void r5e() {
        dataBus = r5.getVal();
    }
    
    private void r6e() {
        dataBus = r6.getVal();
    }
    
    private void r7e() {
        dataBus = r7.getVal();
    }
    
    private void r8e() {
        dataBus = input.getVal();
    }
    
    private void s0e() {
        dataBus = secret0.getVal();
    }
    
    private void s1e() {
        dataBus = secret1.getVal();
    }
    
    private void fe() {
        dataBus = flags.getVal();
    }
    
    /**
     * Pop stack to dataBus.
     */
    private void ste() {
        dataBus = stack.pop();
    }
    
    // HELPER METHODS
    protected void callRe(byte regId) {
        switch(regId) {
            case 0:
                r0e();
                break;
            case 1:
                r1e();
                break;
            case 2:
                r2e();
                break;
            case 3:
                r3e();
                break;
            case 4:
                r4e();
                break;
            case 5:
                r5e();
                break;
            case 6:
                r6e();
                break;
            case 7:
                r7e();
                break;
            case 8:
                r8e();
                break;
            case 10:
                ste();
                break;
            default:
                abort("Reading from nonexistent CPU register 0x" + Integer.toHexString(regId));
        }
    }
    
    private void callRw(byte regId) {
        switch(regId) {
            case 0:
                r0w();
                break;
            case 1:
                r1w();
                break;
            case 2:
                r2w();
                break;
            case 3:
                r3w();
                break;
            case 4:
                r4w();
                break;
            case 5:
                r5w();
                break;
            case 6:
                r6w();
                break;
            case 7:
                r7w();
                break;
            case 8:
                r8w();
                break;
            case 10:
                stw();
                break;
            default:
                throw new IllegalArgumentException("Writing to nonexistent CPU register! " + regId);
        }
    }
    
    public void pollInput(boolean left, boolean right, boolean up, boolean down,
                            boolean a, boolean d, boolean w, boolean s) {
        
        byte inputVal = composeByte(left, right, up, down, a, d, w, s);
        
        input.setVal(inputVal);
    }
    
    final char VRAM_START = 65391; // START OF VRAM
    final char VRAM_END =  65535; // END OF VRAM
    //unused, unused, not equal, less or equal, less, greater equal, greater, equal
    
    final int FLAGS_EQUAL = 0, FLAGS_GREATER = 1, FLAGS_GREATER_EQUAL = 2, FLAGS_LESS = 3, FLAGS_LESS_EQUAL = 4, FLAGS_NOT_EQUAL = 5;
    final int INPUT_LEFT = 0, INPUT_RIGHT = 1, INPUT_UP = 2, INPUT_DOWN = 3, INPUT_A = 4, INPUT_D = 5, INPUT_W = 6, INPUT_S = 7;

    public void updateDebug() {
        Celsior.debugFrame.update(r0.getVal(), r1.getVal(), r2.getVal(), r3.getVal(), r4.getVal(),
                    r5.getVal(), r6.getVal(), r7.getVal(),
                    input.getVal(), pc0.getVal(), pc1.getVal(), flags.getVal(), secret0.getVal(), secret1.getVal(),
                    abr.getVal(), addressBus, dataBus, progCounter, stack);
    }
}
