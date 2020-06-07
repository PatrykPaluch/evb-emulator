package pk.lab06.sw;

import java.io.IOException;

/**
 *   <p>
 *      Provides all needed functionality to create programs that uses
 *      8 LED, RGB LED, 8 buttons, screen (2x16), potentiometer and UART.
 *   </p>
 *
 *   <p>
 *      "Emulator" EvB | Program do emulacji <br>
 *      Data: 2020 06 06<br>
 *      Na potrzeby projektu z przedmiotu Systemy Wbudowane<br>
 *      Grupa lab06 PK 2020<br>
 *   </p>
 *
 * @author Patryk Paluch
 */
public abstract class EvBProgram {
    /**
     * Determinate if program is running. Set it to False to stop program between {@link #loop()} calls.
     */
    protected boolean running;
    private EvBEmulator parent;

    /**
     * Called ones before first loop.
     */
    public abstract void init();

    /**
     * Called in loop
     */
    public abstract void loop();

    /**
     * Emulator uses it to show about info in UI.
     * @return Author of this EvBProgram
     */
    public abstract String getAuthor();

    /**
     * Reads single byte from UART.
     * @return The byte read, or -1 if the end of the stream has been reached
     */
    public int read(){
        return parent.uartRead();
    }

    /**
     * Reads byte array into a buffer from UART.
     * @param cBuf Destination buffer
     * @param offset Offset at which to start storing bytes
     * @param len Maximum number of bytes to read
     * @return The number of bytes read, or -1 if the end of the stream has been reached
     */
    public int read(byte[] cBuf, int offset, int len) {
        return parent.uartRead(cBuf, offset, len);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream.
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking or 0 when it reaches the end of the input stream.
     */
    public int available(){
        return parent.uartAvailable();
    }

    /**
     * Skips over and discards n bytes of data from this input stream. The skip method may, for a variety of reasons, end up skipping over some smaller number of bytes, possibly 0.
     * @param n the number of bytes to be skipped
     * @return the actual number of bytes skipped
     */
    public long skip(long n){
        return parent.uartSkip(n);
    }

    /**
     * Writes a single byte to UART.
     * @param c byte to be written
     */
    public void write(int c){
        parent.uartWrite(c);
    }

    /**
     * Writes specific byte array to UART.
     * @param cBuf Buffer of bytes
     * @param offset Offset from which to start writing bytes
     * @param len Number of bytes to write
     */
    public void write(byte[] cBuf, int offset, int len){
        parent.uartWrite(cBuf, offset, len);
    }

    /**
     * Writes specific byte array to UART.
     * @param str Bytes to be written
     */
    public void write(byte[] str){
        parent.uartWrite(str);
    }


    /**
     * Sets state of each LED.<br>
     * Each bit has corresponding LED from 0 to 7, where bit<sub>i</sub>==1 is True and bit<sub>i</sub>==0 is False (enabled or not).
     * @param val 8 element list of True/False as int
     */
    public void pinLED(int val){
        parent.setPanelLed(val);
    }

    /**
     * Turns specific LED on or off
     * @param pin led to change (0-7)
     * @param val True if enabled, false otherwise
     */
    public void pinLED(int pin, boolean val){
        parent.setPanelLed(pin, val);
    }

    /**
     * Returns state of all LEDs.<br>
     * Each bit has corresponding LED from 0 to 7, where bit<sub>i</sub>==1 is True and bit<sub>i</sub>==0 is False (enabled or not).
     * @see #getPinLED(int)
     * @return 8 element list of True/False as int
     */
    public int getPinLED(){
        return parent.getPanelLed();
    }

    /**
     * Returns state of specific LED.
     * @param pin LED to check (0-7)
     * @return true if LED is enabled, false otherwise
     */
    public boolean getPinLED(int pin){
        return parent.getPanelLed(pin);
    }

    /**
     * Returns state of all buttons.<br>
     * Each bit has corresponding button from 0 to 7, where bit<sub>i</sub>==1 is True and bit<sub>i</sub>==0 is False (pressed or not)
     * @see #getPinSwitch(int) 
     * @return 8 element list of True/False as int
     */
    public int getPinSwitch(){
        return parent.getPanelSwitch();
    }

    /**
     * Returns state of specific button.
     * @param pin Button to check (0-7)
     * @return true if button is pressed, false otherwise
     */
    public boolean getPinSwitch(int pin){
        return parent.getPanelLed(pin);
    }

    /**
     * Sets Red value of RGB LED.
     * @param val Color value form 0 to 255
     */
    public void pinRgbR(int val){
        parent.setLedRgbR(val);
    }

    /**
     * Sets Green value of RGB LED.
     * @param val Color value form 0 to 255
     */
    public void pinRgbG(int val){
        parent.setLedRgbG(val);
    }

    /**
     * Sets Blue value of RGB LED.
     * @param val Color value form 0 to 255
     */
    public void pinRgbB(int val){
        parent.setLedRgbB(val);
    }

    /**
     * @return Red value of RGB LED (0-255)
     */
    public int getPinRgbR(){
        return parent.getLegRgbR();
    }

    /**
     * @return Green value of RGB LED (0-255)
     */
    public int getPinRgbG(){
        return parent.getLegRgbG();
    }

    /**
     * @return Blue value of RGB LED (0-255)
     */
    public int getPinRgbB(){
        return parent.getLegRgbB();
    }

    /**
     * Returns value of potentiometer.
     * @return Integer from 0 to 1024 (0-255)
     */
    public int getPotentiometer(){
        return parent.getPotentiometer();
    }


    /**
     * Writes text to screen at first column and row.<br>
     * <ul>
     *  <li>Screen is 16 characters width and 2 lines height</li>
     *  <li>Columns and rows starts at 0</li>
     *  <li>New line character '\n' move text to next row and first column</li>
     *  <li>Auto word warp</li>
     *  <li>Any character outside screen bounds will be ignored</li>
     * </ul>
     * @param text text to write
     */
    public void setScreenTextMultiline(CharSequence text){
        char[][] lines = new char[2][16];
        int col = 0;
        int row = 0;
        for(int i = 0 ; i < text.length() && i < 2*16; i++){
            char c = text.charAt(i);
            if(c=='\n' || col >= 16){
                col = 0;
                row++;
            }
            else {
                lines[row][col++] = c;
            }
        }
        setScreenText(0, new String(lines[0]));
        setScreenText(1, new String(lines[1]));
    }

    /**
     * Writes text to screen at first column and specific row.<br>
     * <ul>
     *  <li>Screen is 16 characters width and 2 lines height</li>
     *  <li>Columns and rows starts at 0</li>
     *  <li>New line character '\n' move text to next row and first column</li>
     *  <li>No word warp</li>
     *  <li>Any character outside screen bounds will be ignored</li>
     * </ul>
     * @param row row to start writing
     * @param text text to write
     */
    public void setScreenText(int row, CharSequence text){
        this.setScreenText(0, row, text);
    }

    /**
     * Writes text to screen at first column and row.<br>
     * <ul>
     *  <li>Screen is 16 characters width and 2 lines height</li>
     *  <li>Columns and rows starts at 0</li>
     *  <li>New line character '\n' move text to next row and first column</li>
     *  <li>No word warp</li>
     *  <li>Any character outside screen bounds will be ignored</li>
     * </ul>
     * @param text text to write
     */
    public void setScreenText(CharSequence text){
        this.setScreenText(0, 0, text);
    }

    /**
     * Writes text to screen at specific column and row.<br>
     * <ul>
     *  <li>Screen is 16 characters width and 2 lines height</li>
     *  <li>Columns and rows starts at 0</li>
     *  <li>New line character '\n' move text to next row and first column</li>
     *  <li>No word warp</li>
     *  <li>Any character outside screen bounds will be ignored</li>
     * </ul>
     * @param col column to start writing
     * @param row row to start writing
     * @param text text to write
     */
    public void setScreenText(int col, int row, CharSequence text){
        parent.setScreenText(col, row, text);
    }

    /**
     * Clears screen line by setting all characters in specific line to SPACE.
     * @param row row to clear (0, 1)
     */
    public void clearScreenLine(int row){
        //World's best screen clear!
        parent.setScreenText(0, row, "                ");
    }

    /**
     * Clears screen text by setting all his characters to SPACE.
     */
    public void clearScreenText(){
        parent.clearScreenText();
    }

    /**
     * @return Emulator EEPROM
     */
    public Eeprom EEPROM(){
        return parent.getEeprom();
    }

    /**
     * Writes text to logger
     * @param text text to write
     */
    public void log(String text){
        parent.log(text);
    }


    public void delay(int ms){
        try{
            Thread.sleep(ms);
        }catch (InterruptedException ignored){}
    }

    /**
     * @return Emulator object that running this program
     */
    public final EvBEmulator getParent(){
        return parent;
    }
    final void setParent(EvBEmulator parent){
        this.parent = parent;
    }
}
