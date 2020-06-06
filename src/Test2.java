import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Test2 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Wprowadź 3 liczby 0-255 oddzielone spacją i wciśnij ENTER");
        System.out.println("Przykład: ");
        System.out.println("> 127 255 0");
        System.out.println("By zakończyć wprowadź - (minus) i wciśnije ENTER");
        System.out.println("Przykład: ");
        System.out.println("> -");

        try {
            final String host = "127.0.0.1";
            final int port = 9999;
            System.out.println("== Łączenie z " + host + ":" + port + " ==");

            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();

            System.out.println("== Połączono! ==");

            String line;
            System.out.print("> ");
            while (!(line = sc.nextLine()).contains("-")) {
                String[] rgbStr = line.split(" ");
                if (rgbStr.length != 3) continue;

                int r = Integer.parseInt(rgbStr[0]);
                int g = Integer.parseInt(rgbStr[1]);
                int b = Integer.parseInt(rgbStr[2]);

                System.out.println("Wysyłanie: "+r+" "+g+" "+" "+b);
                os.write(r);
                os.write(g);
                os.write(b);
                System.out.print("> ");
            }
            s.close();
        }catch (IOException er){
            System.err.println( "Napotkano problem: " + er.getMessage() );

        }
    }
}
