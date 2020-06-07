package src;

import pk.lab06.sw.EvBEmulator;
import pk.lab06.sw.EvBProgram;
import java.math.BigInteger;

import A.*;
/**
 * Host EvB
 * Autor: Konrad Paluch
 * Data: 2020 06 06
 * Na potrzeby projektu z przedmiotu Systemy Wbudowane
 * Grupa lab06 PK 2020
 */
public class Host {
	static Utils utils;
    public static void main(String[] args) {
		utils = new Utils();
        EvBEmulator em = new EvBEmulator();
		
        em.setProgram(new EvBProgram() {

			String textScreenUpper = "Potencjometr: ";
			String textScreenLower = "";
		
            @Override
            public String getAuthor() {
                return "Konrad Paluch";
            }

            boolean blik;
            @Override
            public void init() {
				textScreenLower = "" + getPotentiometer();
                blik = false;
				
				byte[] packet = emptyPacket( (byte)1 );
				write(packet, 0, 8);
				
				packet = emptyPacket( (byte)2 );
				write(packet, 0, 8);
				
				packet = emptyPacket( (byte)3 );
				write(packet, 0, 8);
				
				packet = emptyPacket( (byte)4 );
				write(packet, 0, 8);
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
                setScreenText(0, textScreenUpper);
                setScreenText(1, textScreenLower);

                // Jeżeli UART jest podłączony to spowoduje zatrzymanie do czasu otrzymania danych
				
                byte[] data = new byte[8];
                int mlen = read(data, 0, 8);
		
				//System.out.println("mlen: " + mlen);
				
				if (mlen != -1)
				{
					int type = data[0];
					showpacket( data );
					
					//System.out.println( "Packed received: " + utils.readableByte(packet[0]) );
					
					switch((int)type&0xFF) {
						case 1:
						{
							byte[] packet = emptyPacket( (byte)75 );
							byte [] value = BigInteger.valueOf( getPotentiometer() ).toByteArray();
							for (int i = 0; i < value.length; i++) {
								packet[i+1] = value[i];
							}
							showpacket(packet);
							write(packet, 0, 8);
							break;
						}
						case 64:
						{
							byte[] packet = emptyPacket( (byte)10 );
							byte [] value = BigInteger.valueOf( getPotentiometer() ).toByteArray();
							for (int i = 0; i < value.length; i++) {
								packet[i+1] = value[i];
							}
							showpacket(packet);
							write(packet, 0, 8);
							break;
						}
						case 75:
						{
							int value = utils.byteToInt( data[2], data[1]);
							textScreenUpper = "Glosnosc:";
							textScreenLower = "" + value;
							break;
						}
						case 77:
						{
							pinRgbR(utils.readableByte(data[1]));
							pinRgbG(utils.readableByte(data[2]));
							pinRgbB(utils.readableByte(data[3]));
							break;
						}
						case 128:
						{
							byte[] packet = emptyPacket( (byte)129 );
							for (int i = 1; i < 8; i++) {
								packet[i] = data[i];
							}
							write(packet, 0, 8);
							break;
						}
						case 129:
						{
							String str = new String(data);
							System.out.println("\tcontent: '" + str.substring(1, str.length()) + "'");
							break;
						}
						default:
						{
							log("[ERROR] Received unknown packet type (" + ((int)type&0xFF) + ").");
							break;
						}
					}	
				}
                
				delay(250);
            }
        });

    }
	
	private static byte[] emptyPacket(){
		byte[] packet = {0,0,0,0,0,0,0,0};
		return packet;
	}
	
	private static byte[] emptyPacket(byte type){
		byte[] packet = { type,0,0,0,0,0,0,0};
		return packet;
	}
	private static void showpacket(byte [] bytes) {
		for (int i = 0; i < 8; i++) {
			System.err.print( utils.readableByte(bytes[i]) + " ");
		}
		System.err.println("");
	}
}
