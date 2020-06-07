import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.math.BigInteger;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.util.Scanner;
import A.Utils;
/**
 * Host EvB
 * Autor: Konrad Paluch
 * Data: 2020 06 06
 * Na potrzeby projektu z przedmiotu Systemy Wbudowane
 * Grupa lab06 PK 2020
 */
public class Host {
	
    public static void main(String[] args) {
		
        Scanner sc = new Scanner(System.in);
		InputStream is;
		OutputStream os;
		Listener listener;

		Thread thr_listen;
			
        try {
            final String host = "127.0.0.1";
            final int port = 9999;
			Utils utils = new Utils();
			
			//
			/*
			byte [] RAM = utils.intToBytes( (int)(131072) );
						
			for (int i = 0; i < RAM.length; i++) {
				System.out.println("\t[RAM]"+i +"=" + RAM[i]);
			}
			System.out.println("\t[Powrot]"+"=" + utils.byteToInt(RAM) );
			*/
			//
            System.out.println("[System] Laczenie z " + host + ":" + port);

            Socket s = new Socket(host, port);
            is = s.getInputStream();
            os = s.getOutputStream();
			listener = new Listener(is, os);
			thr_listen = new Thread(listener, "Listener-Thread");
			thr_listen.start();
		
            System.out.println("[System] Polaczono!");
			System.out.println("[0] Wyjscie.");
			//System.out.println("[1] Prosba o Dane glosnosci.");
			//System.out.println("[2] Prosba o Dane obciazenia systemu. ");
			//System.out.println("[3] Prosba o informacje o systemie. ");
			//System.out.println("[4] Prosba o informacje o przyciskach. [NOT IMPLEMENTED]");
			//System.out.println("[10] [value] Ustawienie glosnosci 0-1023.");
			//System.out.println("[11] Przycisk Funkcji. [NOT IMPLEMENTED]");
			System.out.println("[64] Prosba o odczytanie glosnosci.");
			//System.out.println("[74] Wyslanie informacji o systemie.");
			//System.out.println("[75] Wyslanie glosnosci.");
			//System.out.println("[76] Wyslanie obciazenia systemu.");
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
					listener.terminate();
				
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
				// W przypadku zakonczenie procesu nasluchujacego, zakoncz proces w tle
				if (!listener.isRunning()) {
					thr_listen.stop();
					s.close();
					break;
				}
            }
			listener.terminate();
			thr_listen.join();
            s.close();
        }
		catch (Exception er) {
			System.err.println( "[1] Napotkano problem: " + er.getMessage() );
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
	Runtime runtime;
	Button [] buttons;
	
	Listener(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
		this.isRunning = true;
		this.utils = new Utils();
		this.glosnosc = 75;
		this.runtime = Runtime.getRuntime();
		this.buttons = new Button[8];
		for (int i=0; i<8; i++) {
			buttons[i] = new Button();
		}
		
		//DEBUG
		buttons[1].setCommand("notepad.exe");
	}
	
	void terminate() {
		this.isRunning = false;
		saveButtons();
	}
	public boolean isRunning() {
		return this.isRunning;
	}
	
	private void loadButtons(){
		try {
			File myObj = new File("buttons.txt");
			Scanner myReader = new Scanner(myObj);
			int i = 0;
			while (myReader.hasNextLine()) {
				String description = myReader.nextLine();
				buttons[i].setDescription(description);
				String command = myReader.nextLine();
				buttons[i].setCommand(description);
				String emptyLine = myReader.nextLine();
			}
			myReader.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
	}
	private void saveButtons(){
		try {
			FileWriter myWriter = new FileWriter("buttons.txt");
			for (int i=0; i<8; i++) {
				myWriter.write(buttons[i].getDescription() + '\n');
				myWriter.write(buttons[i].getCommand() + '\n');
				myWriter.write('\n');
			}
			myWriter.close();
		}
		catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	@Override
	public void run() {	
		try {
			while(this.isRunning)
			{
				byte [] packet = utils.emptyPacket();
				
				utils.receive(packet, is);
				
				System.out.println( "Packed received: " + utils.readableByte(packet[0]) );
				
				switch( utils.readableByte(packet[0]) ) {
					case 1:
					{
						System.out.println("\t[Prosba o wyslanie glosnosci]" );
						
						// send response
						byte [] outgoing_packet = utils.emptyPacket((byte)(75));
						byte [] value = utils.intToBytes((int)(this.glosnosc*100));
						for (int i=0; i<value.length; i++) {
							outgoing_packet[i+1] = value[i];
						}
						utils.send(outgoing_packet, os);
						break;
					}
					case 2:
					{
						System.out.println("\t[Prosba o wyslanie danych obciazenia systemu]" );

						// send response
						long allocatedMemory = runtime.totalMemory()/1024;
						System.out.println("\t[allocatedMemory] " + allocatedMemory);
						byte [] outgoing_packet = utils.emptyPacket((byte)(76));
						byte [] RAM = utils.intToBytes( (int)(allocatedMemory) );
						
						for (int i = 0; i < RAM.length; i++) {
							outgoing_packet[i+1] = RAM[i];
						}
						outgoing_packet[6] = (byte)(60); // Brak łatwo dostepnego czujnika w javie, więc wartość testowo ustawiona na "sztywno".
						utils.send(outgoing_packet, os);
						break;
					}
					case 3:
					{
						System.out.println("\t[Prosba o informacje o systemie]" );
						
						// send response
						long maxMemory = runtime.maxMemory()/1024;
						System.out.println("\t[maxMemory] " + maxMemory);
						byte [] outgoing_packet = utils.emptyPacket((byte)(74));
						byte [] RAM = utils.intToBytes( (int)(maxMemory) );
						for (int i = 0; i < RAM.length; i++) {
							outgoing_packet[i+1] = RAM[i];
						}
						outgoing_packet[6] = (byte)(85);
						utils.send(outgoing_packet, os);
						break;
					}
					case 4:
					{
						int button_number = utils.byteToInt( (byte)(0), packet[1] );
						System.out.println("\t[Prosba o informacje o przycisku "+button_number+"]" );
						// send response
						
						byte[] b = this.buttons[button_number].getDescription().getBytes();

						byte [] outgoing_packet = utils.emptyPacket((byte)78);
						outgoing_packet[1] = (byte)(button_number);
						
						for (int i = 2; i < 8 && i < b.length+2; i++) {
							outgoing_packet[i] = b[i-2];
						}
						utils.send(outgoing_packet, os); // First Packet
						
						outgoing_packet = utils.emptyPacket();
						for (int i = 0; i < 8 && (i+6) < b.length; i++) {
							outgoing_packet[i] = b[i+6];
						}
						utils.send(outgoing_packet, os); // Second Packet
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
					case 11:
					{
						try {
							int button_number = utils.byteToInt( (byte)(0), packet[1] );
							System.out.println("Wcisnieto przycisk: " + button_number );
							if (buttons[button_number].getCommand().equals("")) break;
							// Host po otrzymaniu powinien wykonać przypisaną do danego przycisku funkcję. Host sam ustala przypisane funkcje.
							Process process = runtime.exec(buttons[button_number].getCommand(), null);
							
							// deal with OutputStream to send inputs
							process.getOutputStream();
							 
							// deal with InputStream to get ordinary outputs
							process.getInputStream();
							 
							// deal with ErrorStream to get error outputs
							process.getErrorStream();
						}
						catch (Exception e) {
							System.out.println("Invalid command in button or command arguments.");
							break;
						}

						break;
					}
					case 75:
					{
						int intvalue = utils.byteToInt( packet[1], packet[2]);
						System.out.println("\tcontent: '" + intvalue + "'" );
						break;
					}
					case 128:
					{
						
						// send response
						String str = new String(packet);
						System.out.println("\tcontent: '" + str.substring(1, str.length()).replace("\0", "") + "'");
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
			System.err.println( "[2] Napotkano problem: " + er.getMessage() );
			this.isRunning = false;
			saveButtons();
			return;
        }
		saveButtons();
	}
}


class Button {
	private String description;
	private String command;
	
	Button() {
		description = "empty";
		command = "";
	}
	
	public void setDescription(String desc) {
		if (description.length() > 14){
			description = description.substring(0, 13);
		}
		this.description = description;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void set(String desc, String command) {
		if (description.length() > 14){
			description = description.substring(0, 13);
		}
		this.description = description;
		this.command = command;
	}
	
	public String getDescription() {
		return this.description;
	}
	public String getCommand() {
		return this.command;
	}
	
}