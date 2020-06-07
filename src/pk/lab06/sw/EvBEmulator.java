package pk.lab06.sw;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.BitSet;

/**
 * <p>
 *  Simple EvB "Emulator".
 * </p>
 *
 * <p>
 *  "Emulator" EvB <br>
 *  Data: 2020 06 06 <br>
 *  Na potrzeby projektu z przedmiotu Systemy Wbudowane <br>
 *  Grupa lab06 PK 2020 <br>
 * </p>
 * @author Patryk Paluch
 */
public class EvBEmulator {
    private final Object lock = new Object();

    //==== UI
    protected final JFrame o;
    protected JTextArea logArea;
    protected LedPinPanel panelLedUI;
    protected ButtonPinPanel panelSwitchUI;
    protected JLabel rgbLedUI;
    protected JSlider potentiometerUI;
    protected JTextArea screenUI;
    protected JButton startBtUI;
    protected JLabel iterationCounter;


    //==== Emulator
    protected final BitSet panelLed;
    protected final BitSet panelSwitch;
    protected final int[] ledRgb;
    protected int potentiometer;
    protected final char[] screenText;

    protected final Eeprom eeprom;

    private EvBProgram program;
    private Thread programThread;

    private boolean running = false;

    protected final ServerSocket uartServerSocket;
    private boolean uartReady;
    protected Socket uartSocket;
    protected OutputStream tx;
    protected InputStream rx;
    protected Thread uartConnectionThread;

    public EvBEmulator(){
        eeprom = new Eeprom(266_240 ); //260kB
        ledRgb = new int[3];
        panelSwitch = new BitSet(8);
        panelLed = new BitSet(8);
        potentiometer = 0;
        screenText = new char[2*16];
        Arrays.fill(screenText, ' ');

        o = new JFrame("EvB Emulator - lab06");
        o.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUi();
        o.setSize(900, 400);
        o.setLocationRelativeTo(null);
        o.setVisible(true);

        updateLedRgb();

        try {
            uartServerSocket = new ServerSocket(9999);
            uartReady = false;
            waitForUartConnection();
        }catch (IOException err){
            throw new EvBEmulatorException(err);
        }
    }

    protected void waitForUartConnection(){
        if(uartConnectionThread != null && uartConnectionThread.isAlive()) return;
        if(uartServerSocket.isClosed()) return;

        uartConnectionThread = new Thread(()->{
            log("[SYSTEM] UART is waiting for connection");
            while (!uartServerSocket.isClosed()) {
                try {
                    Socket connection = uartServerSocket.accept();

                    synchronized (lock) {
                        if (uartReady) { // already connected to UART - close connection
                            connection.close();
                            log("[SYSTEM] New UART connection closed. UART is busy.");
                            continue;
                        }

                        // Set connection
                        uartSocket = connection;
                        tx = uartSocket.getOutputStream();
                        rx = uartSocket.getInputStream();
                        uartReady = true;
                        log("[SYSTEM] UART connected!");
                    }
                } catch (IOException err) {
                    System.err.println(err.getMessage());
                    log("[SYSTEM] Connection error");
                    log(err.getMessage());
                }
            }
        });
        uartConnectionThread.setDaemon(true);
        uartConnectionThread.start();
    }

    protected void closeUartConnection(){
        synchronized (lock) {
            try {
                uartReady = false;
                if(uartSocket==null) return;
                if (!uartSocket.isClosed()) {
                    uartSocket.close();
                }
                log("[SYSTEM] UART connection closed!");
            } catch (IOException er) {
                log("[SYSTEM] Error when closing UART connection!");
                log(er.getMessage());
            }
        }
    }

    //==================== UI
    protected void initUi(){
        //Popup Menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem aboutMenuItem = new JMenuItem("O aplikacji");
        aboutMenuItem.addActionListener(
                (e)-> JOptionPane.showMessageDialog(o,
                                "\"Emulator\" EvB\n" +
                                "Autor emulatora: Patryk Paluch\n" +
                                ((program==null)?"": "Autor emulowanego programu: "+ program.getAuthor()+"\n") +
                                "Na potrzeby projektu z przedmiotu Systemy Wbudowane\n" +
                                "Grupa lab06 PK czerwiec 2020",
                                "O Aplikacji", JOptionPane.INFORMATION_MESSAGE)
        );
        popupMenu.add(aboutMenuItem);

        // Root pane
        Container root = o.getContentPane();
        root.setLayout(new GridBagLayout());

        // Log
        logArea = new JTextArea(10, 75);
        logArea.setEditable(false);
        JScrollPane logAreaScrollPane = new JScrollPane(logArea);
        ((DefaultCaret)logArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        logArea.setComponentPopupMenu(popupMenu);

        // Emulator stuff container
        JPanel container = new JPanel();
        container.setLayout(new GridLayout(4, 2, 5, 5));
        container.setComponentPopupMenu(popupMenu);

        // Emulator stuff
        panelLedUI = new LedPinPanel(BoxLayout.X_AXIS);

        panelSwitchUI = new ButtonPinPanel(BoxLayout.X_AXIS);
        panelSwitchUI.addButtonPanelActionListener(panelSwitch::set);

        rgbLedUI = new JLabel(" RGB ");
        rgbLedUI.setOpaque(true);
        rgbLedUI.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        rgbLedUI.setBackground(Color.BLACK);

        potentiometerUI = new JSlider(0, 1023, 0);
        potentiometerUI.addChangeListener((e)-> potentiometer = potentiometerUI.getValue());

        screenUI = new JTextArea(16,2);
        screenUI.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        screenUI.setEditable(false);
        screenUI.setInheritsPopupMenu(true);

        startBtUI = new JButton("Start");
        startBtUI.addActionListener((e)->{
            if(isRunning()) stopProgram();
            else run();
        });

        iterationCounter = new JLabel("Iteration: -");
        iterationCounter.setInheritsPopupMenu(true);

        // Adding Emulator stuff to container
        container.add(startBtUI);
        container.add(panelSwitchUI);
        container.add(panelLedUI);
        container.add(potentiometerUI);
        container.add(screenUI);
        container.add(rgbLedUI);
        container.add(iterationCounter);

        // Adding emulator stuff container and logArea
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5,5,5,5);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        root.add(container, constraints);

        constraints.weighty = 0.6;
        constraints.gridx = 0;
        constraints.gridy = 1;
        root.add(logAreaScrollPane, constraints);

        o.pack();
    }


    //==================== Emulator
    public void log(String str){
        log(str, true);
    }
    public void log(String str, boolean logToStd){
        if(logToStd) System.out.println(str);
        synchronized (lock) {
            logArea.append(str);
            logArea.append("\n");
        }
    }

    public void stopProgram(){
        try {
            //noinspection deprecation
            programThread.stop();
            programThread.join();
            setRunning(false);
        }catch (InterruptedException er){
            er.printStackTrace();
            log("[SYSTEM] Stoping program ERROR! " + er.getMessage());
        }
    }

    protected final boolean isUartReady(){ return uartReady; }
    protected final boolean isRunning(){
        return running;
    }
    protected final void setRunning(boolean r){
        synchronized (lock) {
            this.running = r;
            log("[SYSTEM] Running=" + this.running);
            startBtUI.setBackground(this.running? Color.GREEN : Color.RED);
            startBtUI.setText(this.running? "Stop" : "Start");
        }
    }

    public void setProgram(EvBProgram program) {
        synchronized (lock) {
            if (isRunning()) throw new EvBEmulatorException("Emulator is running. Can't change program.");
            this.program = program;
            this.program.setParent(this);
        }
    }

    public void run(){
        if(isRunning()) throw new EvBEmulatorException("Program already running");

        programThread = new Thread(()->{
            setRunning(true);
            log("[SYSTEM] Emulator Program started");
            try {
                int iteration = 0;
                program.running = true;
                program.init();
                while(program.running){
                    ++iteration;
                    iterationCounter.setText("Iteration: "+ iteration);

                    if(uartSocket!=null && !uartSocket.isConnected()){
                        closeUartConnection();
                    }

                    program.loop();
                }
            } catch (Throwable er){

                StackTraceElement[] ste = er.getStackTrace();
                if(er instanceof ThreadDeath) {
                    log("[SYSTEM] Program was stopped!");
                    final int n = 5;
                    StringWriter sw = new StringWriter(n * ste[0].toString().length());
                    sw.append(er.toString());
                    sw.append('\n');
                    for (int i = 0; i < n && i < ste.length; i++) {
                        sw.append('\t');
                        sw.append(ste[i].toString());
                        sw.append('\n');
                    }
                    log(sw.toString(), false);
                }else {
                    er.printStackTrace();
                    log("[SYSTEM] Program error");
                    if (ste.length > 0) {
                        StringWriter sw = new StringWriter(ste.length * ste[0].toString().length());
                        er.printStackTrace(new PrintWriter(sw));

                        log(sw.toString(), false);
                    }
                }


            }
            setRunning(false);
        });
        programThread.setName("EvB-Program");
        programThread.setDaemon(true);
        programThread.start();
    }

    public int uartRead(byte[] cBuf, int offset, int len){
        if (uartReady) {
            try {
                int r = rx.read(cBuf, offset, len);
                if(r == -1) closeUartConnection();
                return r;
            } catch (IOException er) {
                closeUartConnection();
            }
        }
        return -1;
    }

    public int uartRead(){
        if (uartReady) {
            try {
                int r = rx.read();
                if(r == -1) closeUartConnection();
                return r;
            } catch (IOException er) {
                closeUartConnection();
            }
        }
        return -1;
    }

    public void uartWrite(int c){
        if (uartReady) {
            try {
                tx.write(c);
            } catch (IOException er) {
                closeUartConnection();
            }
        }
    }

    public void uartWrite(byte[] cBuf, int offset, int len){
        if (uartReady) {
            try {
                tx.write(cBuf, offset, len);
            } catch (IOException er) {
                closeUartConnection();
            }
        }
    }

    public void uartWrite(byte[] str){
        if (uartReady) {
            try {
                tx.write(str);
            } catch (IOException er) {
                closeUartConnection();
            }
        }
    }

    public int uartAvailable(){
        if(uartReady){
            try {
                return rx.available();
            } catch (IOException er){
                closeUartConnection();
            }
        }
        return 0;
    }

    public long uartSkip(long n){
        if(uartReady){
            try {
                return rx.skip(n);
            } catch (IOException er){
                closeUartConnection();;
            }
        }
        return 0;
    }


    protected void updateLedRgb(){
        Color c = new Color(ledRgb[0], ledRgb[1], ledRgb[2]);
        double luminance = (0.299*ledRgb[0] + 0.587 * ledRgb[1] + 0.114 * ledRgb[2])/255;

        rgbLedUI.setBackground(c);
        rgbLedUI.setForeground( luminance > 0.5 ? Color.BLACK : Color.WHITE);
        rgbLedUI.setText(ledRgb[0]+" "+ledRgb[1]+" "+ledRgb[2]);
    }

    public Eeprom getEeprom(){
        return eeprom;
    }

    public int getPanelLed() {
        int val = 0;
        for(int i = 0 ; i < 8 ; i++)
            val += (panelLed.get(i)?1:0)<<i;
        return val;
    }
    public boolean getPanelLed(int pin){
        return panelLed.get(pin);
    }

    public int getPanelSwitch() {
        int val = 0;
        for(int i = 0 ; i < 8 ; i++)
            val += (panelSwitch.get(i)?1:0)<<i;
        return val;
    }
    public boolean getPanelSwitch(int pin){
        return panelSwitch.get(pin);
    }

    public void setPanelLed(int val){
        for(int i = 0 ; i<8 ; i++){
            panelLed.set(i, (val&1)==1);
            val >>= 1;
        }
        panelLedUI.set(panelLed);
    }
    public void setPanelLed(int pin, boolean state){
        panelLed.set(pin, state);
        panelLedUI.set(panelLed);
    }
    public void setPanelSwitch(int val){
        for(int i = 0 ; i<8 ; i++){
            panelSwitch.set(i, (val&1)==1);
            val >>= 1;
        }
        panelSwitchUI.set(panelSwitch);
    }
    public void setPanelSwitch(int pin, boolean state){
        panelSwitch.set(pin, state);
        panelSwitchUI.set(panelSwitch);
    }

    private int correctColorValue(int v){
        if(v<0) return 0;
        if(v>255) return 255;
        return v;
    }

    public void setLedRgbR(int v){
        ledRgb[0] = correctColorValue(v);
        updateLedRgb();
    }
    public void setLedRgbG(int v){
        ledRgb[1] = correctColorValue(v);
        updateLedRgb();
    }
    public void setLedRgbB(int v){
        ledRgb[2] = correctColorValue(v);
        updateLedRgb();
    }
    public int getLegRgbR() {
        return ledRgb[0];
    }
    public int getLegRgbG() {
        return ledRgb[1];
    }
    public int getLegRgbB() {
        return ledRgb[2];
    }

    public int getPotentiometer() {
        return potentiometer;
    }

    public void setPotentiometer(int potentiometer) {
        this.potentiometer = potentiometer;
        this.potentiometerUI.setValue(this.potentiometer);
    }

    public String getScreenText() {
        return new String(screenText);
    }

    public void setScreenText(int col, int row, CharSequence text) {
        if(row != 0 && row != 1) return;

        int tCol = col;
        int tRow = row;
        for(int i = 0 ; i < text.length() ; i++){
            char c = text.charAt(i);
            if(c=='\n'){
                tRow += 1;
                tCol = 0;
                if(tRow > 1) break;
                continue;
            }
            if(c < ' ' || c > '~') continue;
            if(tCol<16) screenText[row*16 + tCol] = c;

            ++tCol;
        }
        //noinspection StringBufferReplaceableByString
        screenUI.setText(
                new StringBuilder(2*16+1)
                        .append(screenText, 0, 16)
                        .append('\n')
                        .append(screenText, 16, 16)
                        .toString()
        );
    }

    public void clearScreenText(){
        Arrays.fill(screenText, ' ');
    }
}
