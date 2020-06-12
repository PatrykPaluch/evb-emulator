package pk.lab06.sw.program;

import pk.lab06.sw.emulator.EvBEmulator;
import pk.lab06.sw.emulator.EvBProgram;
import java.math.BigInteger;

import static pk.lab06.sw.program.Utils.PACKAGE_PANEL_ASK_FOR_VOLUME;
import static pk.lab06.sw.program.Utils.PACKAGE_PANEL_ASK_FOR_SYSTEM_USAGE;
import static pk.lab06.sw.program.Utils.PACKAGE_PANEL_ASK_FOR_SYSTEM_INFO;
import static pk.lab06.sw.program.Utils.PACKAGE_PANEL_ASK_FOR_BUTTONS;
import static pk.lab06.sw.program.Utils.PACKAGE_PANEL_SET_VOLUME;
import static pk.lab06.sw.program.Utils.PACKAGE_PANEL_USE_BUTTON;

import static pk.lab06.sw.program.Utils.PACKAGE_HOST_ASK_FOR_VOLUME;
import static pk.lab06.sw.program.Utils.PACKAGE_HOST_SYSTEM_INFO;
import static pk.lab06.sw.program.Utils.PACKAGE_HOST_SEND_VOLUME;
import static pk.lab06.sw.program.Utils.PACKAGE_HOST_SEND_SYSTEM_USAGE;
import static pk.lab06.sw.program.Utils.PACKAGE_HOST_SEND_COLOR;
import static pk.lab06.sw.program.Utils.PACKAGE_HOST_SEND_BUTTON_INFO;

import static pk.lab06.sw.program.Utils.PACKAGE_PING;
import static pk.lab06.sw.program.Utils.PACKAGE_PONG;

/**
 * Panel EvB
 * Autor: Konrad Paluch
 * Data: 2020 06 08
 * Na potrzeby projektu z przedmiotu Systemy Wbudowane
 * Grupa lab06 PK 2020
 */
public class Panel {
    public static void main(String[] args) {
        EvBEmulator em = new EvBEmulator();
		boolean [] justpressed = new boolean[8];
		for (int i=0; i<8; i++) {
			justpressed[i] = false;
		}
		
        em.setProgram(new EvBProgram() {

			String textScreenUpper = "                ";
			String textScreenLower = "                ";
			int potentiometer = -1;
			long itr = 1;
		
            @Override
            public String getAuthor() {
                return "Łączył: Konrad Paluch; Poprawki: Patryk Paluch";
            }

            boolean blik;
            @Override
            public void init() {
				potentiometer = -1;
				textScreenLower = "" + getPotentiometer();
                blik = false;
				
				byte[] packet = Utils.emptyPacket( (byte)1 );
				write(packet, 0, 8);
				
				packet = Utils.emptyPacket( (byte)3 );
				write(packet, 0, 8);
            }

            @Override
            public void loop() {
				
				// Co 2 sekund wyświetl temperature
				if (itr%20 == 0) {
					byte [] packet = Utils.emptyPacket( (byte)PACKAGE_PANEL_ASK_FOR_SYSTEM_USAGE );
					write(packet, 0, 8);
				}
				// Obsługa przycisków
				for (int i=0; i<8; i++) {
					if (justpressed[i] != getPinSwitch(i) ) {
						justpressed[i] = getPinSwitch(i);
						
						if( getPinSwitch(i) ) {
							byte[] packet = Utils.emptyPacket( (byte)11 );
							packet[1] = (byte)(i);
							write(packet, 0, 8);
							
							// Po wcisnieciu wyswietla opis przycisku z informacjami otrzymanymi od hosta
							packet = Utils.emptyPacket( (byte)4 );
							packet[1] = (byte)(i);
							write(packet, 0, 8);
						}
					}
				}
				
				// Update potencjometr
				if(getPotentiometer() != potentiometer) {
					potentiometer = getPotentiometer();
					byte[] packet = Utils.emptyPacket( (byte)10 );
					byte[] value = Utils.intToBytes(getPotentiometer());
					for (int i=2; i<4; i++) {
						packet[i-1] = value[i];
					}
					write(packet, 0, 8);
					
					// Zazadaj danych o glosnosci
					packet = Utils.emptyPacket( (byte)1 );
					write(packet, 0, 8);
				}

                // Wyświetlenie wartości potencjometru
                setScreenText(0, textScreenUpper);
                setScreenText(1, textScreenLower);

				
                byte[] data = new byte[8];
                int mlen = -1;
				
				// Sprawdza czy dostępna jest wiadomość
				// Zapobiega zablokowaniu socketu
				if( available() >= 8) {
					mlen = read(data, 0, 8);
				}

				if (mlen != -1)
				{
					int type = data[0];
					showpacket( data );
					
					//System.out.println( "Packed received: " + Utils.readableByte(packet[0]) );
					
					switch(type&0xFF) {
						case PACKAGE_PANEL_ASK_FOR_VOLUME:
						{
							byte [] packet = Utils.emptyPacket( (byte)PACKAGE_HOST_SEND_VOLUME );
							byte [] value = BigInteger.valueOf( getPotentiometer() ).toByteArray();
							for (int i = 0; i < value.length; i++) {
								packet[i+1] = value[i];
							}
							showpacket(packet);
							write(packet, 0, 8);
							break;
						}
						case PACKAGE_HOST_ASK_FOR_VOLUME:
						{
							byte [] packet = Utils.emptyPacket( (byte)PACKAGE_PANEL_SET_VOLUME );
							byte [] value = BigInteger.valueOf( getPotentiometer() ).toByteArray();
							for (int i = 0; i < value.length; i++) {
								packet[i+1] = value[i];
							}
							showpacket(packet);
							write(packet, 0, 8);
							break;
						}
						case PACKAGE_HOST_SYSTEM_INFO:
						{
							byte [] ram = new byte[4];
							for (int i = 1; i <= 4; i++) {
								ram[i-1] = data[i];
							}
							int int_ram = Utils.byteToInt(ram);
							
							int tmp = (data[5]&0xFF) + ((data[6]&0xFF)<<8);
							
							log("Max Memory: " + int_ram + " MB" );
							log("Max CPU Temperature: " + tmp + " \u00B0C" );
							break;
						}
						case PACKAGE_HOST_SEND_VOLUME:
						{
							byte [] arr = { data[1], data[2], data[3], data[4] };
							int value = Utils.byteToInt( arr );
							textScreenUpper = Utils.centerText("Glosnosc", 16);
							textScreenLower = Utils.centerText(String.valueOf(value), 16);
							
							for (int i=0; i<8; i++) {
								if (i < value*0.08) {
									pinLED(i, true);
								}
								else {
									pinLED(i, false);
								}
							}
							
							break;
						}
						case PACKAGE_HOST_SEND_SYSTEM_USAGE:
						{
							byte [] ram = new byte[4];
							for (int i = 1; i <= 4; i++) {
								ram[i-1] = data[i];
							}
							int int_ram = Utils.byteToInt(ram);
							
							int tmp = (data[5]&0xFF) + ((data[6]&0xFF)<<8);
							textScreenUpper = Utils.centerText("Memory " + (int_ram) + "MB", 16);
							textScreenLower = Utils.centerText("CPU " + tmp + "\u00B0C", 16);
							break;
						}
						case PACKAGE_HOST_SEND_COLOR:
						{
							pinRgbR(Utils.readableByte(data[1]));
							pinRgbG(Utils.readableByte(data[2]));
							pinRgbB(Utils.readableByte(data[3]));
							break;
						}
						case PACKAGE_HOST_SEND_BUTTON_INFO:
						{
							String str1 = new String(data);
							str1 = str1.substring(2, str1.length()).replace("\0", "");
							textScreenUpper = Utils.centerText("Button " + Utils.readableByte(data[1]), 16);
							
							data = new byte[8];
							mlen = read(data, 0, 8);
							String str2 = new String(data).replace("\0", "");
							textScreenLower = Utils.centerText(str1 + str2, 16);
							break;
						}
						case PACKAGE_PING:
						{
							String str = new String(data);
							log("ping: '" + str.substring(1, str.length()).replace("\0", "") + "'");
							byte[] packet = Utils.emptyPacket( (byte)PACKAGE_PONG );
							for (int i = 1; i < 8; i++) {
								packet[i] = data[i];
							}
							write(packet, 0, 8);
							break;
						}
						case PACKAGE_PONG:
						{
							String str = new String(data);
							log("pong: '" + str.substring(1, str.length()) + "'");
							break;
						}
						default:
						{
							log("[ERROR] Received unknown packet type (" + (type&0xFF) + ").");
							break;
						}
					}	
				}
				delay(100);
				itr++;
            }
        });
    }
	
	private static void showpacket(byte [] bytes) {
		for (int i = 0; i < 8; i++) {
			System.err.print( Utils.readableByte(bytes[i]) + " ");
		}
		System.err.println("");
	}
}
