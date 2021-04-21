package celsior;

import java.util.Random;

public class GPU {
    
    // False = display text, True = display pixels
    public boolean graphicsMode;
    public Memory m;
    
    // used for debugging
    private final Random rand = new Random();
    
    public GPU() {
        reset(true);
    }
    
    public void reset(boolean clearMem) {
        m = new Memory(9360); // 9kb graphics + 144b text
        
        graphicsMode = false;
    }
    
    /**
     * Called every time the screen will be displayed.
     */
    public void clock() {
//        for(int i = 0; i < m.size(); i++) {
//            m.putByte(i, rand.nextInt(255));
//        }
    }
    
    /**
     * Puts a pixel at VRAM with specified color.
     * @param rX the register containing the x position
     * @param rY the register containing the y position
     * @param rColor the register containing the color of the pixel
     */
    public void pxl(byte rX, byte rY, byte rColor) {
        Celsior.cpu.callRe(rX);
        byte x = Celsior.cpu.dataBus;
        
        Celsior.cpu.callRe(rY);
        byte y = Celsior.cpu.dataBus;
        
        Celsior.cpu.callRe(rColor);
        byte color = Celsior.cpu.dataBus;

        if((x >= 0 && x <= 127) && (y >= 0 && y <= 71))
            m.putByte(Byte.toUnsignedInt(y) * 128 + Byte.toUnsignedInt(x), color);
        else
            System.err.println("Error when pixeling: " + x + ", " + y);
    }
    
    /**
     * Draws a line in VRAM with specified color.
     * @param rX the register containing the x position
     * @param rY the register containing y position
     * @param rX1 the register containing the second x position
     * @param rY1 the register containing the second y position
     * @param rColor the register containing the color of the line
     */
    public void line(byte rX, byte rY, byte rX1, byte rY1, byte rColor) {
        Celsior.cpu.callRe(rX);
        byte x = Celsior.cpu.dataBus;
        
        Celsior.cpu.callRe(rY);
        byte y = Celsior.cpu.dataBus;
        
        Celsior.cpu.callRe(rX1);
        byte x1 = Celsior.cpu.dataBus;
        
        Celsior.cpu.callRe(rY1);
        byte y1 = Celsior.cpu.dataBus;
        
        Celsior.cpu.callRe(rColor);
        byte color = Celsior.cpu.dataBus;

        
        int width  = Byte.toUnsignedInt(x1) - Byte.toUnsignedInt(x);
        int height = Byte.toUnsignedInt(y1) - Byte.toUnsignedInt(y);
        
        // ********************************************
        // determine the direction the line is going in
        // ********************************************
        
        // assume line is drawn going down and to the right
        byte dx1 = 1, dx2 = 1; // horizontal
        byte dy1 = 1, dy2 = 1; // vertical
        
        if (width < 0) // line is going left
            dx1 = dx2 = -1;
        
        if (height < 0) // line is going up
            dy1 = -1;
        
        
        // **************************************
        // determine if width or height is bigger
        // **************************************
        
        int longest = Math.abs(width); // assume width > height
        int shortest = Math.abs(height);
        
        if (!(longest > shortest)) { // if false, then height > width
            longest = Math.abs(height);
            shortest = Math.abs(width);
            
            if (height < 0) // moving up
                dy2 = -1;
            else if (height > 0)
                dy2 = 1; // moving down
            
            dx2 = 0;            
        }
        
        int numerator = longest >> 1;
        
        int i = 0;
        while(i <= longest) { // loop [i] until i <= the longest distance
            
            pxl(x, y, color); // draw pixel
            
            numerator += shortest; // add shortest distance to "numerator"
            
            if (!(numerator < longest)) { // if numerator is less than longest distance
                numerator -= longest; // subtract longest distance from numerator
                x += dx1; // add 1 or -1 to x pos (depending on direction of line)
                y += dy1; // add 1 or -1 to y pos (depending on direction of line)
            } else {
                x += dx2; // same as above??
                y += dy2;
            }
            
            i++; // increment i
        }
    }
    
    /**
     * Print {@code letter} to the screen at {@code x} and {@code y}.
     * @param x the x pos (higher X means to the right)
     * @param y the y pos (higher X means lower on screen)
     * @param letter the letter to print
     */
    public void prt(byte x, byte y, byte letter) {
        m.putByte(CHAR_START + y * 128 + x, letter);
    }
    
    /**
     * Toggle graphics mode from pixels to text or vice versa.
     */
    public void gmt() {
        //graphicsMode = !graphicsMode;
    }
    
    public final int CHAR_START = 9216;
}
