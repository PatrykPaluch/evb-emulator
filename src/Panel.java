import pk.lab06.sw.EvBEmulator;
import pk.lab06.sw.EvBProgram;
import java.math.BigInteger;

/**
 * Panel EvB
 * Autor: Konrad Paluch
 * Data: 2020 06 08
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

			String textScreenUpper = "                ";
			String textScreenLower = "                ";
			int potentiometer = -1;
			long itr = 1;
		
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
				
				byte[] packet = utils.emptyPacket( (byte)1 );
				write(packet, 0, 8);
				
				packet = utils.emptyPacket( (byte)3 );
				write(packet, 0, 8);
            }

            @Override
            public void loop() {
				
				// Co 10 sekund wyświetl temperature
				if (itr%100 == 0) {
					byte [] packet = utils.emptyPacket( (byte)2 );
					write(packet, 0, 8);
				}
				// Obsługa przycisków
				for (int i=0; i<8; i++) {
					if (justpressed[i] != getPinSwitch(i) ) {
						justpressed[i] = getPinSwitch(i);
						
						if( getPinSwitch(i) ) {
							byte[] packet = utils.emptyPacket( (byte)11 );
							packet[1] = (byte)(i);
							write(packet, 0, 8);
							
							// Po wcisnieciu wyswietla opis przycisku z informacjami otrzymanymi od hosta
							packet = utils.emptyPacket( (byte)4 );
							packet[1] = (byte)(i);
							write(packet, 0, 8);
						}
					}
				}
				
				// Update potencjometr
				if(getPotentiometer() != potentiometer) {
					potentiometer = getPotentiometer();
					byte[] packet = utils.emptyPacket( (byte)10 );
					byte[] value = utils.intToBytes(getPotentiometer());
					for (int i=2; i<4; i++) {
						packet[i-1] = value[i];
					}
					write(packet, 0, 8);
					
					// Zazadaj danych o glosnosci
					packet = utils.emptyPacket( (byte)1 );
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
							byte[] packet = utils.emptyPacket( (byte)75 );
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
							byte[] packet = utils.emptyPacket( (byte)10 );
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
							textScreenUpper = utils.centerText("Glosnosc", 16);
							textScreenLower = utils.centerText(String.valueOf(value), 16);
							
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
						case 76:
						{
							byte [] ram = new byte[4];
							for (int i = 1; i < 4; i++) {
								ram[i-1] = data[i];
							}
							int int_ram = utils.byteToInt(ram);
							
							byte tmp = data[6];
							textScreenUpper = utils.centerText("Memory " + (int_ram/1024) + "MB", 16);
							textScreenLower = utils.centerText("CPU " + utils.readableByte(tmp) + "\u00B0C", 16);
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
							String str1 = new String(data);
							str1 = str1.substring(2, str1.length()).replace("\0", "");
							textScreenUpper = utils.centerText("Button " + utils.readableByte(data[1]), 16);
							
							data = new byte[8];
							mlen = read(data, 0, 8);
							String str2 = new String(data).replace("\0", "");
							textScreenLower = utils.centerText(str1 + str2, 16);
							break;
						}
						case 128:
						{
							String str = new String(data);
							log("ping: '" + str.substring(1, str.length()).replace("\0", "") + "'");
							byte[] packet = utils.emptyPacket( (byte)129 );
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
				delay(100);
				itr++;
            }
        });
    }
	
	private static void showpacket(byte [] bytes) {
		for (int i = 0; i < 8; i++) {
			System.err.print( utils.readableByte(bytes[i]) + " ");
		}
		System.err.println("");
	}
}
