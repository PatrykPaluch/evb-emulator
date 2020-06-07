import pk.lab06.sw.EvBEmulator;
import pk.lab06.sw.EvBProgram;
import java.math.BigInteger;

import A.Utils;
/**
 * Panel EvB
 * Autor: Konrad Paluch
 * Data: 2020 06 06
 * Na potrzeby projektu z przedmiotu Systemy Wbudowane
 * Grupa lab06 PK 2020
 */
public class Panel {
	static Utils utils;
    public static void main(String[] args) {
		utils = new Utils();
        EvBEmulator em = new EvBEmulator();
		boolean [] justpressed = new boolean[8];
		for (int i=0; i<8; i++) {
			justpressed[i] = false;
		}
		
        em.setProgram(new EvBProgram() {

			String textScreenUpper = " Potencjometr:  ";
			String textScreenLower = "                ";
			int potentiometer = -1;
		
            @Override
            public String getAuthor() {
                return "Konrad Paluch";
            }

            boolean blik;
            @Override
            public void init() {
				potentiometer = -1;
				textScreenLower = "" + getPotentiometer();
                blik = false;
				
				byte[] packet = emptyPacket( (byte)1 );
				write(packet, 0, 8);
				
				packet = emptyPacket( (byte)2 );
				write(packet, 0, 8);
				
				packet = emptyPacket( (byte)3 );
				write(packet, 0, 8);
				
				// Get information of all 8 buttons.
				/*
				for (int i=0; i<8; i++) {
					packet = emptyPacket( (byte)4 );
					packet[1] = (byte)(i);
					write(packet, 0, 8);
				}
				*/
            }

            @Override
            public void loop() {
                // Kopiowanie stanu przycisków na ledy
                int st = getPinSwitch();
                pinLED(st);
				
				// Obsługa przycisków
				for (int i=0; i<8; i++) {
					
					if (justpressed[i] != getPinSwitch(i) ) {
						justpressed[i] = getPinSwitch(i);
						
						if( getPinSwitch(i) ) {
							
							byte[] packet = emptyPacket( (byte)11 );
							packet[1] = (byte)(i);
							write(packet, 0, 8);
						}
					}
				}
				
				// Update potencjometr
				if(getPotentiometer() != potentiometer) {
					potentiometer = getPotentiometer();
					byte[] packet = emptyPacket( (byte)10 );
					byte[] value = utils.intToBytes(getPotentiometer());
					for (int i=2; i<4; i++) {
						packet[i-1] = value[i];
					}
					write(packet, 0, 8);
					
					// Zazadaj danych o glosnosci
					packet = emptyPacket( (byte)1 );
					write(packet, 0, 8);
				}

                // Wyświetlenie wartości potencjometru
                setScreenText(0, textScreenUpper);
                setScreenText(1, textScreenLower);

                // Jeżeli UART jest podłączony to spowoduje zatrzymanie do czasu otrzymania danych
				
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
						case 74:
						{
							byte [] ram = new byte[4];
							for (int i = 1; i < 4; i++) {
								ram[i-1] = data[i];
							}
							int int_ram = utils.byteToInt(ram);
							
							byte tmp = data[6];
							
							log("Max Memory: " + int_ram + " KB" );
							log("Max CPU Temperature: " + utils.readableByte(tmp)+ " \u00B0C" );
							break;
						}
						case 75:
						{
							byte [] arr = { data[1], data[2], data[3], data[4] };
							int value = utils.byteToInt( arr );
							log("Glosnosc: " + value);
							textScreenUpper = "   Glosnosc:    ";
							String tmp = "";
							for(int i=0; i<8-String.valueOf(value).length()/2; i++){
								tmp = tmp + " ";
							}
							textScreenLower = tmp + value + tmp;
							break;
						}
						case 76:
						{
							byte [] ram = new byte[4];
							for (int i = 1; i < 4; i++) {
								ram[i-1] = data[i];
							}
							int int_ram = utils.byteToInt(ram);
							
							byte tmp = data[6];
							log("Memory Usage: " + int_ram + " KB" );
							log("Current CPU Temperature: " + utils.readableByte(tmp) + " \u00B0C");
							break;
						}
						case 77:
						{
							pinRgbR(utils.readableByte(data[1]));
							pinRgbG(utils.readableByte(data[2]));
							pinRgbB(utils.readableByte(data[3]));
							break;
						}
						case 78:
						{
							String str = new String(data);
							str = str.substring(2, str.length());
							log("Button " + data[1] + ": " + str);
							
							data = new byte[8];
							mlen = read(data, 0, 8);
							showpacket(data);
							str = new String(data);
							log(str);
							
							break;
						}
						case 128:
						{
							String str = new String(data);
							log("ping: '" + str.substring(1, str.length()).replace("\0", "") + "'");
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
							log("pong: '" + str.substring(1, str.length()) + "'");
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
