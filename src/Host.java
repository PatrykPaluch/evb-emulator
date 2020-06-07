import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.math.BigInteger;
import A.*;

public class Panel {
	
    public static void main(String[] args) {
		
        Scanner sc = new Scanner(System.in);
		
        try {
            final String host = "127.0.0.1";
            final int port = 9999;
			Utils utils = new Utils();
			
            System.out.println("[System] Laczenie z " + host + ":" + port);

            Socket s = new Socket(host, port);
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
			Listener listener = new Listener(is, os);

			Thread thr_listen = new Thread(listener, "Listener-Thread");
			thr_listen.start();
		
            System.out.println("[System] Polaczono!");
			System.out.println("[0] Wyjscie.");
			//System.out.println("[1] Prosba o Dane glosnosci.");
			//System.out.println("[2] Prosba o Dane obciazenia systemu. [NOT IMPLEMENTED]");
			//System.out.println("[3] Prosba o informacje o systemie. [NOT IMPLEMENTED]");
			//System.out.println("[4] Prosba o informacje o przyciskach. [NOT IMPLEMENTED]");
			//System.out.println("[10] [value] Ustawienie glosnosci 0-1023.");
			//System.out.println("[11] Przycisk Funkcji. [NOT IMPLEMENTED]");
			System.out.println("[64] Prosba o odczytanie glosnosci.");
			//System.out.println("[74] Wyslanie informacji o systemie. [NOT IMPLEMENTED]");
			//System.out.println("[75] Wyslanie glosnosci.");
			//System.out.println("[76] Wyslanie obciazenia systemu. [NOT IMPLEMENTED]");
			System.out.println("[77] [r g b] Wyslanie koloru.");
			//System.out.println("[78] Wyslanie informacji o przyciskach. [NOT IMPLEMENTED]");
			System.out.println("[128] [message] Ping");
			//System.out.println("[128] Pong");
			
            String line;
			while (true) {
                System.out.print("> ");
				line = sc.nextLine();
				
                String[] command = line.split(" ");
				
				
				
				if (command[0].equals("0"))
					break;
				
				if (command[0].equals("send")) {
					
					if(command.length == 1) {
						System.out.println("Invalid command.");
						
					}
					else {
						switch (Integer.parseInt(command[1])) {
							
							case 64:
							{
								byte [] packet = utils.emptyPacket((byte)64);
								utils.send(packet, os);
								break;
							}
							case 77:
							{
								if (command.length == 5) {
									
									byte [] packet = utils.emptyPacket((byte)77);
									packet[1] = (byte)Integer.parseInt(command[2]);
									packet[2] = (byte)Integer.parseInt(command[3]);
									packet[3] = (byte)Integer.parseInt(command[4]);
									utils.send(packet, os);
								}
								else {
									System.out.println("Invalid syntax");
								}
								break;
							}
							/*
							case 74:
							{
								byte [] packet = utils.emptyPacket((byte)74);
								byte RAM = utils.intToBytes(2048);
								for (int i=0; i<RAM.length; i++) {
									packet[i+1] = RAM[i];
								}
								packet[6] = (byte)(85);
								utils.send(packet, os);
								break;
							}
							*/
							case 128:
							{
								byte [] packet = utils.emptyPacket((byte)128);
								if (command.length >= 3){
									byte[] b = command[2].getBytes();
									for (int i = 1; i < 8 && i < command[2].length()+1; i++) {
										packet[i] = b[i-1];
									}
								}
								utils.send(packet, os);
								break;
							}
							default:
								System.out.println("Invalid command.");
								break;
						}
					}
				}
				// W przypadku zakonczenie procesu nasluchujacego, zakoncz program
				if (!listener.isRunning()) {
					break;
				}
            }
			listener.terminate();
			thr_listen.join();
            s.close();
        }
		catch (Exception er) {
			System.err.println( "Napotkano problem: " + er.getMessage() );
			return;
        }
    }
	
	
}

class Listener implements Runnable {
	InputStream is;
	OutputStream os;
	boolean isRunning;
	Utils utils;
	double glosnosc; // TODO
	
	Listener(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
		this.isRunning = true;
		this.utils = new Utils();
		this.glosnosc = 75;
	}
	
	void terminate() {
		this.isRunning = false;
	}
	public boolean isRunning() {
		return this.isRunning;
	}
	@Override
	public void run() {	
		try {
			while(isRunning)
			{
				byte [] packet = utils.emptyPacket();
				
				utils.receive(packet, is);
				
				System.out.println( "Packed received: " + utils.readableByte(packet[0]) );
				
				switch( utils.readableByte(packet[0]) ) {
					case 1:
					{
						int intvalue = utils.byteToInt( packet[1], packet[2]);
						System.out.println("\t[Prosba o wyslanie glosnosci]" );
						
						// send response
						byte [] outgoing_packet = utils.emptyPacket((byte)(75));
						byte [] value = utils.intToBytes((int)(this.glosnosc));
						for (int i=0; i<value.length; i++) {
							outgoing_packet[i+1] = value[i];
						}
						utils.send(outgoing_packet, os);
						break;
					}
					case 2:
					{
						int intvalue = utils.byteToInt( packet[1], packet[2]);
						System.out.println("\tcontent: 'null' [Prosba o wyslanie danych obciazenia systemu]" );
						break;
					}
					case 3:
					{
						int intvalue = utils.byteToInt( packet[1], packet[2]);
						System.out.println("\tcontent: 'null' [Prosba o informacje o systemie]" );
						
						// send response
						byte [] outgoing_packet = utils.emptyPacket((byte)(74));
						byte [] RAM = utils.intToBytes( (int)(2048) );
						for (int i=0; i<RAM.length; i++) {
							outgoing_packet[i+1] = RAM[i];
						}
						outgoing_packet[6] = (byte)(85);
						utils.send(outgoing_packet, os);
						break;
					}
					case 4:
					{
						int intvalue = utils.byteToInt( packet[1], packet[2]);
						System.out.println("\tcontent: 'null' [Prosba o informacje o przyciskach]" );
						break;
					}
					case 10:
					{
						int value = utils.byteToInt( packet[1], packet[2]);
						this.glosnosc = value / 1023.0;
						System.out.println("\tcontent: '" + value + "'" );
						System.out.println("Ustawiono glosnosc na: " + (int)(this.glosnosc*100) + "%" );
						break;
					}
					case 11: // TODO
					{
						int button_number = utils.byteToInt( (byte)(0), packet[1] );
						// TODO
						// Host po otrzymaniu powinien wykonać przypisaną do danego przycisku funkcję. Host sam ustala przypisane funkcje.
						break;
					}/*
					case 75:
					{
						int intvalue = utils.byteToInt( packet[1], packet[2]);
						System.out.println("\tcontent: '" + intvalue + "'" );
						break;
					}*/
					case 128:
					{
						
						// send response
						byte[] outgoing_packet = utils.emptyPacket( (byte)129 );
						for (int i = 1; i < 8; i++) {
							outgoing_packet[i] = packet[i];
						}
						utils.send(outgoing_packet, os);
						break;
					}
					case 129:
					{
						String str = new String(packet);
						System.out.println("\tcontent: '" + str.substring(1, str.length()).replace("\0", "") + "'");
						break;
					}
				} 
			}	
		}
		catch (Exception er) {
			System.err.println( "Napotkano problem: " + er.getMessage() );

        }
	}
}