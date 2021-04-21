/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package celsior.rendering;

import celsior.Celsior;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Screen {
    
    private BufferedImage buffer;
    private int scale;
    private Font font;

    /** Create a Screen object with a default scale of 8. */
    public Screen() {
        this(10);
    }

    /**
     * Create a Screen object with the specified scale.
     * @param screenScale The scale factor for the screen
     */
    public Screen(int screenScale) {
        scale = screenScale;
        
        createBackBuffer();
        
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS, 
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        Font tempFont;
        
        try {
            tempFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/res/font.ttf"));
        } catch (FontFormatException | IOException ex) {
            tempFont = new Font("Times", Font.PLAIN, 10 * scale);
        }
        
        Rectangle2D stringBounds = g.getFontMetrics(tempFont).getStringBounds("eeeeeeeeeeeeeeee", g);
        
        float fontSize2d = (float) (tempFont.getSize2D());
        float stringWidth = (float) (stringBounds.getWidth());
        
        tempFont = tempFont.deriveFont((float) ((fontSize2d * scale) * 128f / stringWidth));
        
        font = tempFont;
        
        g.dispose();
    }

    /**
     * Generates the BufferedImage that will act as the back buffer for the
     * screen. Flags the Screen state as having changed.
     */
    private void createBackBuffer() {
        buffer = new BufferedImage(
                128 * scale,
                72 * scale,
                BufferedImage.TYPE_4BYTE_ABGR);
    }
    
    public void render() {
        Graphics2D g = buffer.createGraphics();
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, 128 * scale, 72 * scale);
        
        if(Celsior.gpu.graphicsMode) {
            for(int y = 0; y < 72; y++) {
                for(int x = 0; x < 128; x++) {
                    try {
                        Color c = getColor(Celsior.gpu.m.getByte(y * 128 + x));
                        g.setColor(c);
                        g.fillRect(x * scale, y * scale, scale, scale);
                    } catch(ArrayIndexOutOfBoundsException ex) {
                        System.err.println("something [dab, but backwards (\"bad\")] happened while graphicing");
                    }
                }
            }
        } else {
            row0 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 0  ), (char) (Celsior.gpu.CHAR_START + 16 )));
            row1 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 16 ), (char) (Celsior.gpu.CHAR_START + 32 )));
            row2 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 32 ), (char) (Celsior.gpu.CHAR_START + 48 )));
            row3 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 48 ), (char) (Celsior.gpu.CHAR_START + 64 )));
            row4 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 64 ), (char) (Celsior.gpu.CHAR_START + 80 )));
            row5 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 80 ), (char) (Celsior.gpu.CHAR_START + 96)));
            row6 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 96 ), (char) (Celsior.gpu.CHAR_START + 112)));
            row7 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 112), (char) (Celsior.gpu.CHAR_START + 128)));
            row8 = toChar(Celsior.gpu.m.getBytes((char) (Celsior.gpu.CHAR_START + 128), (char) (Celsior.gpu.CHAR_START + 144)));

//            row0 = "eeeeeeeeeeeeeeee".toCharArray();
//            row1 = row0;
//            row2 = row0;
//            row3 = row0;
//            row4 = row0;
//            row5 = row0;
//            row6 = row0;
//            row7 = row0;
//            row8 = row0;
//            
//            FontMetrics fm = g.getFontMetrics(font);
//            System.out.println("Width: " + fm.stringWidth(new String(row0)) + ", Height: " + fm.getHeight());
            
            g.setFont(font);
            g.setColor(Color.white);
            
            g.drawString(new String(row0), 0, 72 * scale);
            g.drawString(new String(row1), 0, 64 * scale);
            g.drawString(new String(row2), 0, 56 * scale);
            g.drawString(new String(row3), 0, 48 * scale);
            g.drawString(new String(row4), 0, 40 * scale);
            g.drawString(new String(row5), 0, 32 * scale);
            g.drawString(new String(row6), 0, 24 * scale);
            g.drawString(new String(row7), 0, 16 * scale);
            g.drawString(new String(row8), 0, 8  * scale);
        }
        
        g.dispose();
    }
    
    private char[] toChar(byte[] in) {
        char[] ret = new char[in.length];
        for(int i = 0; i < in.length; i++) {
            char c = charMap[Byte.toUnsignedInt(in[i])];
            ret[i] = c;
        }
        return ret;
    }
    
    private final char[] charMap = {
        ' ', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '!', '?', '/', '\\', '.', ',', ':', '>', '<', '(', ')'
    };
    
    private char[] row0 = new char[16],
                   row1 = new char[16],
                   row2 = new char[16],
                   row3 = new char[16],
                   row4 = new char[16],
                   row5 = new char[16],
                   row6 = new char[16],
                   row7 = new char[16],
                   row8 = new char[16];
    
    public Color getColor(byte b) {
        int[] redBits = new int[3];
        for(int x = 0; x < 3; x++)
            redBits[x] = (b >>> x + 5) & 1;
        
        int[] greenBits = new int[3];
        for(int x = 0; x < 3; x++)
            greenBits[x] = (b >>> x + 2) & 1;
        
        int red = redBits[0] + redBits[1] * 2 + redBits[2] * 4;
        int green = greenBits[0] + greenBits[1] * 2 + greenBits[2] * 4;
        int blue = b & 3;
        
        
        float r = (red / 7f) * 255;
        float g = (green / 7f) * 255;
        float bl = (blue / 3f) * 255;
        
        Color c = new Color((int) r, (int) g, (int) bl);
        return c;
    }

    /**
     * Returns the scale of the screen.
     *
     * @return The scale factor of the screen
     */
    public int getScale() {
        return scale;
    }

    /**
     * Returns the BufferedImage that contains the contents of the screen.
     *
     * @return the backBuffer for the screen
     */
    public BufferedImage getBuffer() {
        return buffer;
    }
}
