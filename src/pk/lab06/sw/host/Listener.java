package pk.lab06.sw.host;

import com.sun.management.OperatingSystemMXBean;
import pk.lab06.sw.program.Utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.awt.Toolkit;
import java.awt.Robot;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

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


public class Listener implements Runnable {

	InputStream is;
	OutputStream os;
	boolean isRunning;
	Runtime runtime;
	Button [] buttons;
	long r1, g1, b1;
	private double lasfFrame = 0;
	long lastVolumeFrame = System.currentTimeMillis();
	int lastVolume = Utils.getVolume();

	public Listener(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
		this.isRunning = true;
		Utils.showLogs(false);
		this.runtime = Runtime.getRuntime();
		this.buttons = new Button[8];
		for (int i=0; i<8; i++) {
			buttons[i] = new Button();
		}
		this.r1 = 0;
		this.g1 = 0;
		this.b1 = 0;
		//DEBUG
		loadButtons();
	}

	void terminate() {
		this.isRunning = false;
		saveButtons();
	}
	public boolean isRunning() {
		return this.isRunning;
	}
	public Button[] getButtons() {
		return this.buttons;
	}
	public void setButtons(Button [] buttons) {
		this.buttons = buttons;
	}
	
	private void loadButtons(){
		try {
			File myObj = new File("buttons.txt");
			Scanner myReader = new Scanner(myObj);
			for (int i=0; i<8; i++) {
				String description = myReader.nextLine();
				buttons[i].setDescription(description);
				String command = myReader.nextLine();
				buttons[i].setCommand(command);
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
				byte [] packet = Utils.emptyPacket();
				
				Utils.receive(packet, is);
				
				if (Utils.readableByte(packet[0]) != 0)
					Utils.log( "Packed received: " + Utils.readableByte(packet[0]) );
				
				switch( Utils.readableByte(packet[0]) ) {
					case PACKAGE_PANEL_ASK_FOR_VOLUME:
					{
						Utils.log("\t[Prosba o wyslanie glosnosci]" );
						
						// send response
						byte [] outgoing_packet = Utils.emptyPacket((byte)(PACKAGE_HOST_SEND_VOLUME));
						byte [] value = Utils.intToBytes( Utils.getVolume() );
						System.arraycopy(value, 0, outgoing_packet, 1, value.length);
						Utils.send(outgoing_packet, os);
						break;
					}
					case PACKAGE_PANEL_ASK_FOR_SYSTEM_USAGE:
					{
						Utils.log("\t[Prosba o wyslanie danych obciazenia systemu]" );

						long allocatedMemory = Utils.getMemoryUsageMb()[1];

						double[] temp = Utils.getCurrTemp();
						int curr = (int) temp[0];

						// send response
						Utils.log("\t[allocatedMemory] " + allocatedMemory);
						byte [] outgoing_packet = Utils.emptyPacket((byte)(PACKAGE_HOST_SEND_SYSTEM_USAGE));
						byte [] RAM = Utils.intToBytes( (int)(allocatedMemory) );

						System.arraycopy(RAM, 0, outgoing_packet, 1, Math.min(RAM.length, 4));
						outgoing_packet[5] = (byte)(curr&0xFF);
						outgoing_packet[6] = (byte)((curr>>8)&0xFF);

						Utils.send(outgoing_packet, os);
						break;
					}
					case PACKAGE_PANEL_ASK_FOR_SYSTEM_INFO:
					{
						Utils.log("\t[Prosba o informacje o systemie]" );
						
						// send response
						long maxMemory = Utils.getMemoryUsageMb()[2];

						double[] temp = Utils.getCurrTemp();
						int crit = (int) temp[1];

						Utils.log("\t[maxMemory] " + maxMemory);
						byte [] outgoing_packet = Utils.emptyPacket((byte)(PACKAGE_HOST_SYSTEM_INFO));
						byte [] RAM = Utils.intToBytes( (int)(maxMemory) );
						System.arraycopy(RAM, 0, outgoing_packet, 1, Math.min(RAM.length, 4));
						outgoing_packet[5] = (byte)(crit&0xFF);
						outgoing_packet[6] = (byte)((crit>>8)&0xFF);
						Utils.send(outgoing_packet, os);
						break;
					}
					case PACKAGE_PANEL_ASK_FOR_BUTTONS:
					{
						int button_number = Utils.byteToInt( (byte)(0), packet[1] );
						Utils.log("\t[Prosba o informacje o przycisku "+button_number+"]" );
						// send response
						
						byte[] b = this.buttons[button_number].getDescription().getBytes();

						byte [] outgoing_packet = Utils.emptyPacket((byte)PACKAGE_HOST_SEND_BUTTON_INFO);
						outgoing_packet[1] = (byte)(button_number);
						
						for (int i = 2; i < 8 && i < b.length+2; i++) {
							outgoing_packet[i] = b[i-2];
						}
						Utils.send(outgoing_packet, os); // First Packet
						
						outgoing_packet = Utils.emptyPacket();
						for (int i = 0; i < 8 && (i+6) < b.length; i++) {
							outgoing_packet[i] = b[i+6];
						}
						Utils.send(outgoing_packet, os); // Second Packet
						break;
					}
					case PACKAGE_PANEL_SET_VOLUME:
					{
						int value = Utils.byteToInt( packet[1], packet[2]);
						value = (int) (value / 1023.0 * 100);
						Utils.log("\tcontent: '" + value + "'" );
						Utils.log("Ustawiono glosnosc na: " + value + "%" );
						Utils.setVolume( value );
						break;
					}
					case PACKAGE_PANEL_USE_BUTTON:
					{
						try {
							int button_number = Utils.byteToInt( (byte)(0), packet[1] );
							Utils.log("Wcisnieto przycisk: " + button_number );
							if (buttons[button_number].getCommand().equals("")) break;
							// Host po otrzymaniu powinien wykonać przypisaną do danego przycisku funkcję. Host sam ustala przypisane funkcje.
							String [] cmd = {"/bin/bash", "-c", buttons[button_number].getCommand()};
							Process process = runtime.exec(cmd, null);

							// deal with InputStream to get ordinary outputs
							InputStream is = process.getInputStream();
							if(is != null) Utils.clearStream(is);

							InputStream eis = process.getErrorStream();
							if(eis != null) Utils.clearStream(eis);

						}
						catch (Exception e) {
							Utils.log("Invalid command in button or command arguments.");
							break;
						}

						break;
					}
					case PACKAGE_HOST_SEND_VOLUME:
					{
						int intvalue = Utils.byteToInt( packet[1], packet[2]);
						Utils.log("\tcontent: '" + intvalue + "'" );
						break;
					}
					case PACKAGE_PING:
					{

						// send response
						String str = new String(packet);
						Utils.log("\tcontent: '" + str.substring(1, str.length()).replace("\0", "") + "'");
						byte[] outgoing_packet = Utils.emptyPacket( (byte)129 );
						System.arraycopy(packet, 1, outgoing_packet, 1, 7);
						Utils.send(outgoing_packet, os);
						break;
					}
					case PACKAGE_PONG:
					{
						String str = new String(packet);
						Utils.log("\tcontent: '" + str.substring(1, str.length()).replace("\0", "") + "'");
						break;
					}
				}


				if((System.currentTimeMillis() - lastVolumeFrame)>500){
					lastVolumeFrame = System.currentTimeMillis();
					int currVolume = Utils.getVolume();
					if(currVolume != lastVolume) {
						lastVolume = currVolume;

						Utils.log("\t[Wyslanie glosnosci]");

						// send response
						byte[] outgoing_packet = Utils.emptyPacket((byte) (PACKAGE_HOST_SEND_VOLUME));
						byte[] value = Utils.intToBytes(lastVolume);
						System.arraycopy(value, 0, outgoing_packet, 1, value.length);
						Utils.send(outgoing_packet, os);
					}
				}


				if((System.currentTimeMillis()-lasfFrame)>1000){
					BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
					long r = 0, g = 0, b = 0;

					for (int x = 0; x < image.getWidth(); x++) {
						for (int y = 0; y < image.getHeight(); y++) {
							Color pixel = new Color(image.getRGB(x, y));
							r += pixel.getRed();
							g += pixel.getGreen();
							b += pixel.getBlue();
						}
					}
					r = r / (image.getWidth()*image.getHeight());
					g = g / (image.getWidth()*image.getHeight());
					b = b / (image.getWidth()*image.getHeight());


					if (Utils.colorDifference(r, g, b, r1, g1, b1) > 5) {
						byte[] outgoing_packet = Utils.emptyPacket((byte) 77);
						outgoing_packet[1] = (byte) r;
						outgoing_packet[2] = (byte) g;
						outgoing_packet[3] = (byte) b;
						r1 = r;
						g1 = g;
						b1 = b;
						Utils.send(outgoing_packet, os);
					}
					lasfFrame = System.currentTimeMillis();
				}
			}
		}
		catch (Exception er) {
			if (!er.getMessage().equals("Connection reset")
			&&  !er.getMessage().equals("Socket closed")){
				System.err.println( "[2] Error occurred: " + er.getMessage() );
			}
			this.isRunning = false;
			saveButtons();
			return;
        }
		saveButtons();
	}
}
