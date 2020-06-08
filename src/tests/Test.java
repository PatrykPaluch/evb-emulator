package tests;

import pk.lab06.sw.emulator.EvBEmulator;
import pk.lab06.sw.emulator.EvBProgram;

/**
 * tests.Test "Emulatora" EvB
 * Autor: Patryk Paluch
 * Data: 2020 06 06
 * Na potrzeby projektu z przedmiotu Systemy Wbudowane
 * Grupa lab06 PK 2020
 */
public class Test {

    public static void main(String[] args) {
        EvBEmulator em = new EvBEmulator();
        em.setProgram(new EvBProgram() {
            @Override
            public String getAuthor() {
                return "Patryk w Paluch";
            }

            boolean blik;
            @Override
            public void init() {
                log("Started!");
                blik = false;
            }

            @Override
            public void loop() {
                // Kopiowanie stanu przycisków na ledy
                int st = getPinSwitch();
                pinLED(st);

                // Miganie LED numer 7 (nie zależnie od przycisku 7)
                pinLED(7, blik);
                blik = !blik;

                // Wyświetlenie wartości potencjometru
                int pot = getPotentiometer();
                setScreenText(0,"Potencjometr: ");
                setScreenText(5, 1, ""+pot+"       ");

                if(available() >= 3) {
                    // Odczyt danych z "UART" - 3 liczby
                    byte[] rgbData = new byte[3];
                    // Jeżeli UART jest podłączony to spowoduje zatrzymanie do czasu otrzymania danych
                    int d = read(rgbData, 0, 3);

                    if (d == 3) {
                        //Ustawianie wartości R G B na diodę RGB
                        pinRgbR(rgbData[0] & 0xFF);
                        pinRgbG(rgbData[1] & 0xFF);
                        pinRgbB(rgbData[2] & 0xFF);
                        log("Kolor: " + ((int) rgbData[0] & 0xFF) + " " + ((int) rgbData[1] & 0xFF) + " " + ((int) rgbData[2] & 0xFF));
                    }
                }
                // Zatrzymanie programu na 250ms
                delay(250);
            }
        });

    }
}
