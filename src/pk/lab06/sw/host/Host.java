package pk.lab06.sw.host;

import pk.lab06.sw.program.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * pk.lab06.sw.host.Host
 * Autor: Konrad Paluch
 * Data: 2020 06 08
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

            System.out.println("[System] Laczenie z " + host + ":" + port);

            Socket s = new Socket(host, port);
            is = s.getInputStream();
            os = s.getOutputStream();
			listener = new Listener(is, os);
			thr_listen = new Thread(listener, "pk.lab06.sw.host.Listener-Thread");
			thr_listen.start();
		
            System.out.println("[System] Polaczono!");
			System.out.println("[0] Wyjscie.");
			//System.out.println("[64] Prosba o odczytanie glosnosci.");
			System.out.println("buttons");
			System.out.println("send [message]");
			
            String line;
			while (true) {
                System.out.print("> ");
				line = sc.nextLine();
				
                String[] command = line.split(" ");
				

				if (command[0].equals("0"))
					listener.terminate();
				
				if (command[0].equals("send")) {
						
					byte [] packet = Utils.emptyPacket((byte)128);
					if (command.length >= 2){
						byte[] b = command[1].getBytes();
						for (int i = 1; i < 8 && i < command[1].length()+1; i++) {
							packet[i] = b[i-1];
						}
					}
					Utils.send(packet, os);
				}
				
				if (command[0].equals("buttons")) {
					ButtonsConfigure bf = new ButtonsConfigure(listener.getButtons());
					bf.run();
					listener.setButtons(bf.getButtons());
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
        }
    }
}
