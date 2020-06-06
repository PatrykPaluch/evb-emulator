# EvBEmulator
Emulator jest dostarczony z source code'em przykładowej aplikacji (Plik [Test.java](src/Test.java)) 
i z prostym programem pomagającym testować wysyłanie danych do załączonej aplikacji
testowej (plik [Test2.java](src/Test2.java)).

## Spis metod
```java
//Program
public abstract String getAuthor()
public abstract void init()
public abstract void loop()

// UART
public int read()
public int read(char[] cBuf, int offset, int len)
public void write(int c)
public void write(char[] cBuf, int offset, int len)
public void write(String str)

// Przyciski
public int getPinSwitch()
public boolean getPinSwitch(int pin)

// LED
public void pinLED(int val)
public void pinLED(int pin, boolean val)

public int getPinLED()
public boolean getPinLED(int pin)

// RGB LED
public void pinRgbR(int val)
public void pinRgbG(int val)
public void pinRgbB(int val)
public int getPinRgbR()
public int getPinRgbG()
public int getPinRgbB()

// Potencjometr
public int getPotentiometer()

public void setScreenTextMultiline(CharSequence text)
public void setScreenText(int row, CharSequence text)
public void setScreenText(CharSequence text)
public void setScreenText(int col, int row, CharSequence text)
public void clearScreenLine(int row)
public void clearScreenText()

// EEPROM
public Eeprom EEPROM()

// Konsola logów
public void log(String text)

// Sleep 
public void delay(int ms)

// Obiekt Emulatora
public EvBEmulator getParent()
```


# Licencja
Licencja zawarta w pliku [LICENSE](LICENSE)
