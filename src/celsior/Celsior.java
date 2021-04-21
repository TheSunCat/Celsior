package celsior;

import celsior.rendering.Keyboard;
import celsior.rendering.Screen;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.prefs.Preferences;
import javax.swing.*;

public class Celsior {
    public static Celsior instance;
    
    public static CPU cpu;
    public static GPU gpu;
    public static Screen screen;
    public static boolean debugging;
    public static boolean stopped = true;
    public static boolean paused = false;
    
    public static DebugFrame debugFrame;
    
    private String progName = "";
    
    private int cpuFreqHz;
    private int periodNanos;
    private int cyclesPerRefresh;
    
    private static JFrame container;
    private static Canvas canvas;
    private static JMenuBar menuBar;
    private static JButton pauseButton;
    private static JButton stepButton;
    
    private final Image icon;
    
    public static final String NAME = "Celsior";
    
    private static final Preferences PREFS = Preferences.userNodeForPackage(Celsior.class);
    
    // not celsius
    public static void main(String[] args) throws IOException {
        if(args.length == 0)
            instance = new Celsior(1000);
        else {
            if(args[0].matches("-?\\d+(\\.\\d+)?")) // if arg is an int
                instance = new Celsior(Integer.parseInt(args[0]));
            else
                return;
        }
        
        instance.startScreen();
    }
    
    public Celsior(int freqHz) throws IOException {
        cpu = new CPU();
        gpu = new GPU();
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        
        screen = new Screen();
        
        debugFrame = new DebugFrame();
        
        icon = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/icon32.png"));
        prepareGUI();
        
        setFreq(freqHz);
        
        boolean debug = PREFS.getBoolean("celsior_debugmode", false);
        
        updateDebugMode(debug);
        
        stopEmulation();
    }
    
    public void pause() {
        paused = true;
        
        System.out.println("[INFO] Emulation paused.");
        
        instance.refreshScreen();
        
        pauseButton.setText("Unpause");
        
        stepButton.setEnabled(true);
    }
    
    public void unpause() {
        paused = false;
        
        System.out.println("[INFO] Emulation unpaused.");
        
        pauseButton.setText("Pause");
        
        stepButton.setEnabled(false);
        
        if(stopped) {
            stopEmulation();
        }
    }
    
    private void setFreq(int freqHz) {
        cpuFreqHz = freqHz;
        periodNanos = 1000000000 / freqHz;
        cyclesPerRefresh = freqHz / 25;
        
        setName();
    }
    
    private void setName() {
        String name = NAME + " - ";
        
        name += (stopped ? "Stopped " : (paused ? "Paused " : "Running "));
        
        if(!progName.isEmpty())
            name += progName;
        
        container.setTitle(name);
        debugFrame.setTitle(name + " - DEBUGGER");
    }
    
    public void startScreen() {
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                refreshScreen();
            }
        }, 0L, 40L);
    }
    
    public void startEmulation() {
        pauseButton.setEnabled(true);
        
        stopped = false;
        
        if(debugging)
            pause();
        else
            unpause();
        
        setName();
        
        startEmulationLoop();
        
        System.out.println("[INFO] Emulation ended.");
    }
    
    private void startEmulationLoop() {
        // Used to ontrol refresh rate. Reset every cyclesPerRefresh cycles
        int refreshCycles = 0;

        // Variables used to measure time
        long initTime;
        long endTime;

        while(!stopped) {
            while(paused) {}
            
            initTime = System.nanoTime();

            // Execute a CPU cycle
            try {
                cpu.clock();
                gpu.clock();
            } catch (Exception ex) {
                error(ex.getLocalizedMessage());
            }
            
            if(debugging)
                cpu.updateDebug();
            
            // Refresh the screen
            if(refreshCycles % (cyclesPerRefresh) == 0) {
                refreshCycles = 0;
                refreshScreen();
            }

            endTime = System.nanoTime();
            refreshCycles++;

            
            // Wait some time to simulate real cycle
            long nanosToWait = periodNanos - (endTime - initTime);
            long initNanos = System.nanoTime();
            long targetNanos = initNanos + nanosToWait;
            while(System.nanoTime() < targetNanos){
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Loads a file into memory and sets the CPU running the new ROM. Will open alert dialogues if there are errors
     * opening the file.
     * @throws java.io.IOException if there was an error reading the file
     */
    public void loadFile() throws IOException {
        stopEmulation();
        
        JFileChooser chooser = new JFileChooser(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        javax.swing.filechooser.FileNameExtensionFilter filter =
                new javax.swing.filechooser.FileNameExtensionFilter("Celsior Assembly Files (.casm)", "casm");
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("Load CASM program...");
        if(chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        File f = chooser.getSelectedFile();
        
        if(f != null) {
            progName = f.getName();
            
            byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
            
            cpu.m.putBytes(0, bytes);
            
            stopped = false;
            
            new Thread(() -> {
                startEmulation();
            }).start();
        } else {
            progName = "";
        }
        
        setName();
    }
    
    public void error(String errorMessage) {
        JOptionPane.showMessageDialog(container, errorMessage, NAME, JOptionPane.ERROR_MESSAGE);
        
        stopped = true;
    }
    
    public void stopEmulation() {
        if(paused)
            pauseButton.doClick();
        
        pauseButton.setEnabled(false);
        stepButton.setEnabled(false);
        
        stopped = true;
        
        cpu.reset(true);
        gpu.reset(true);
        
        progName = "";
        
        setName();
        refreshScreen();
    }
    
    public static void updateDebugMode(boolean debug) {
        debugging = debug;
        
        PREFS.putBoolean("celsior_debugmode", debugging);
        
        stepButton.setVisible(debugging);
        debugFrame.setVisible(debugging);
    }

    /**
     * Will redraw the contents of the screen to the emulator window.
     */
    private void refreshScreen() {
        gpu.clock();
        
        Graphics2D graphics = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
        screen.render();
        
        graphics.drawImage(screen.getBuffer(), null, 0, 0);
        
        canvas.getBufferStrategy().show();
        graphics.dispose();
    }
    
    /**
     * Initializes the JFrame that the emulator will use to draw onto. Will set up the menu system and
     * link the action listeners to the menu items. Returns the JFrame that contains all of the emulator
     * screen elements.
     */
    private void prepareGUI() {
        container = new JFrame();
        menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openFile = new JMenuItem("Open", KeyEvent.VK_O);
        openFile.addActionListener((ActionEvent e) -> {
            try {
                loadFile();
            } catch (IOException ex) {
                error("Failed to load file!");
            }
        });
        
        fileMenu.add(openFile);
        
        // CPU Menu
        JMenu cpuMenu = new JMenu("Options");
        fileMenu.setMnemonic(KeyEvent.VK_C);

        JMenuItem clockSpeed = new JMenuItem("Overclocking...", KeyEvent.VK_S);
        clockSpeed.addActionListener((ActionEvent e) -> {
            Object userInput = JOptionPane.showInputDialog(container, "Enter new CPU frequency in Hz...",
            NAME,
            JOptionPane.INFORMATION_MESSAGE,
            new ImageIcon(),
            null,
            cpuFreqHz);
            
            if(userInput != null) {
                try {
                    setFreq(Integer.parseInt(userInput.toString()));
                } catch(NumberFormatException ex) {
                    error("Please enter an integer value.");
                }
            }
        });
        
        JMenuItem debugMode = new JMenuItem("Debug mode - false", KeyEvent.VK_S);
        debugMode.addActionListener((ActionEvent e) -> {
            updateDebugMode(!debugging);
            
            debugMode.setText("Debug mode - " + ("" + debugging).toLowerCase());
        });
        
        cpuMenu.add(clockSpeed);
        cpuMenu.add(debugMode);
        
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener((ActionEvent e) -> {
            updateDebugMode(true);
            
            if(!paused)
                pause();
            else
                unpause();
            
            setName();
        });
        
        stepButton = new JButton("Step");
        stepButton.addActionListener((ActionEvent e) -> {
            try {
                if(stopped) {
                    stopEmulation();
                }
                
                cpu.clock();
                gpu.clock();
            } catch(Exception ex) {
                error(ex.getLocalizedMessage());
            }
            
            refreshScreen();
        });
        
        menuBar.add(fileMenu);
        menuBar.add(cpuMenu);
        menuBar.add(pauseButton);
        menuBar.add(stepButton);
        
        container.add(menuBar);
        container.setIconImage(icon);
        
        pauseButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "action_pause");
        pauseButton.getActionMap().put("action_pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseButton.doClick();
            }
        });
        
        stepButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F8"), "action_step");
        stepButton.getActionMap().put("action_step", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(stopped)
                    stopEmulation();
                else
                    stepButton.doClick();
            }
        });
        
        
        JMenuItem[] itmList = {openFile, clockSpeed, debugMode};
        
        for(JMenuItem itm : itmList)
            setUI(itm);

        attachCanvas();
    }
    
    private void setUI(JMenuItem itm) {
        itm.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
                @Override
                public void paintMenuItem(Graphics g, JComponent c,
                        Icon checkIcon, Icon arrowIcon,
                        Color background, Color foreground,
                        int defaultTextIconGap) {
                    // Save original graphics font and color
                    Font holdf = g.getFont();
                    Color holdc = g.getColor();

                    JMenuItem mi = (JMenuItem) c;
                    g.setFont(mi.getFont());

                    Rectangle viewRect = new Rectangle(5, 0, mi.getWidth(), mi.getHeight());

                    paintBackground(g, mi, background);
                    paintText(g, mi, viewRect, mi.getText());

                    // Restore original graphics font and color
                    g.setColor(holdc);
                    g.setFont(holdf);
                }
                
                @Override
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    FontMetrics fm = g.getFontMetrics();
                    
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(
                            RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                    
                    
                    Color oldColor = g2d.getColor();
                    
                    g2d.setColor(Color.black);
                    
                    g2d.drawString(text, textRect.x, textRect.y + fm.getAscent());
                    
                    // get keystroke text
                    KeyStroke accelerator = menuItem.getAccelerator();
                    
                    if(accelerator != null) {
                        String accText = "";
                        int modifiers = accelerator.getModifiers();
                        if (modifiers > 0) {
                            accText = java.awt.event.KeyEvent.getKeyModifiersText(modifiers);
                            accText += acceleratorDelimiter;
                        }

                        int keyCode = accelerator.getKeyCode();
                        if (keyCode != 0)
                            accText += java.awt.event.KeyEvent.getKeyText(keyCode);
                        else
                            accText += accelerator.getKeyChar();

                        g2d.drawString(accText,
                                menuItem.getWidth() - fm.stringWidth(accText) - 5,
                                textRect.y + fm.getAscent());
                    }
                    g2d.setColor(oldColor);
                }
            });
    }

    /**
     * Generates the canvas of the appropriate size and attaches it to the
     * main jFrame for the emulator.
     */
    private void attachCanvas() {
        int scaleFactor = screen.getScale();
        int scaledWidth = 128 * scaleFactor;
        int scaledHeight = 72 * scaleFactor;

        JPanel panel = (JPanel) container.getContentPane();
        panel.removeAll();
        panel.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        panel.setLayout(null);

        canvas = new Canvas();
        canvas.setBounds(0, 0, scaledWidth, scaledHeight);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        container.setJMenuBar(menuBar);
        container.pack();
        container.setResizable(false);
        container.setVisible(true);
        container.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        container.setLocationRelativeTo(null);
        canvas.createBufferStrategy(2);
        canvas.setFocusable(true);
        canvas.requestFocus();

        canvas.addKeyListener(new Keyboard());
    }
}
