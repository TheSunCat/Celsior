package celsior.rendering;

import java.awt.event.KeyEvent;

public class Keyboard extends java.awt.event.KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                w = true;
                break;
            case KeyEvent.VK_A:
                a = true;
                break;
            case KeyEvent.VK_S:
                s = true;
                break;
            case KeyEvent.VK_D:
                d = true;
                break;
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_DOWN:
                down = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
        }
        
        celsior.Celsior.cpu.pollInput(left, right, up, down, a, d, w, s);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                w = false;
                break;
            case KeyEvent.VK_A:
                a = false;
                break;
            case KeyEvent.VK_S:
                s = false;
                break;
            case KeyEvent.VK_D:
                d = false;
                break;
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
        }
        
        celsior.Celsior.cpu.pollInput(left, right, up, down, a, d, w, s);
    }
    
    boolean w, a, s, d, up, left, down, right;
}
