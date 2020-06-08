package pk.lab06.sw.program;

import java.io.*;
import java.util.Scanner;

class Listener implements Runnable {
    InputStream is;
    OutputStream os;
    boolean isRunning;
    double glosnosc; // TODO
    Runtime runtime;
    Button [] buttons;

    Listener(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
        this.isRunning = true;
        this.glosnosc = 75;
        this.runtime = Runtime.getRuntime();
        this.buttons = new Button[8];
        for (int i=0; i<8; i++) {
            buttons[i] = new Button();
        }

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

                System.out.println( "Packed received: " + Utils.readableByte(packet[0]) );

                switch( Utils.readableByte(packet[0]) ) {
                    case 0:
                    {
                        isRunning = false;
                        System.out.println("UART is busy.");
                    }
                    case 1:
                    {
                        System.out.println("\t[Prosba o wyslanie glosnosci]" );

                        // send response
                        byte [] outgoing_packet = Utils.emptyPacket((byte)(75));
                        byte [] value = Utils.intToBytes((int)(this.glosnosc*100));
                        for (int i=0; i<value.length; i++) {
                            outgoing_packet[i+1] = value[i];
                        }
                        Utils.send(outgoing_packet, os);
                        break;
                    }
                    case 2:
                    {
                        System.out.println("\t[Prosba o wyslanie danych obciazenia systemu]" );

                        // send response
                        long allocatedMemory = runtime.totalMemory()/1024;
                        System.out.println("\t[allocatedMemory] " + allocatedMemory);
                        byte [] outgoing_packet = Utils.emptyPacket((byte)(76));
                        byte [] RAM = Utils.intToBytes( (int)(allocatedMemory) );

                        for (int i = 0; i < RAM.length; i++) {
                            outgoing_packet[i+1] = RAM[i];
                        }
                        outgoing_packet[6] = (byte)(60); // Brak łatwo dostepnego czujnika w javie, więc wartość testowo ustawiona na "sztywno".
                        Utils.send(outgoing_packet, os);
                        break;
                    }
                    case 3:
                    {
                        System.out.println("\t[Prosba o informacje o systemie]" );

                        // send response
                        long maxMemory = runtime.maxMemory()/1024;
                        System.out.println("\t[maxMemory] " + maxMemory);
                        byte [] outgoing_packet = Utils.emptyPacket((byte)(74));
                        byte [] RAM = Utils.intToBytes( (int)(maxMemory) );
                        for (int i = 0; i < RAM.length; i++) {
                            outgoing_packet[i+1] = RAM[i];
                        }
                        outgoing_packet[6] = (byte)(85);
                        Utils.send(outgoing_packet, os);
                        break;
                    }
                    case 4:
                    {
                        int button_number = Utils.byteToInt( (byte)(0), packet[1] );
                        System.out.println("\t[Prosba o informacje o przycisku "+button_number+"]" );
                        // send response

                        byte[] b = this.buttons[button_number].getDescription().getBytes();

                        byte [] outgoing_packet = Utils.emptyPacket((byte)78);
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
                    case 10:
                    {
                        int value = Utils.byteToInt( packet[1], packet[2]);
                        this.glosnosc = value / 1023.0;
                        System.out.println("\tcontent: '" + value + "'" );
                        System.out.println("Ustawiono glosnosc na: " + (int)(this.glosnosc*100) + "%" );
                        Utils.setVolume((int)(this.glosnosc*100));
                        break;
                    }
                    case 11:
                    {
                        try {
                            int button_number = Utils.byteToInt( (byte)(0), packet[1] );
                            System.out.println("Wcisnieto przycisk: " + button_number );
                            if (buttons[button_number].getCommand().equals("")) break;
                            // pk.lab06.sw.program.Host po otrzymaniu powinien wykonać przypisaną do danego przycisku funkcję. pk.lab06.sw.program.Host sam ustala przypisane funkcje.
                            String [] cmd = {"/bin/bash", "-c", buttons[button_number].getCommand()};
                            Process process = runtime.exec(cmd, null);

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
                        int intvalue = Utils.byteToInt( packet[1], packet[2]);
                        System.out.println("\tcontent: '" + intvalue + "'" );
                        break;
                    }
                    case 128:
                    {

                        // send response
                        String str = new String(packet);
                        System.out.println("\tcontent: '" + str.substring(1, str.length()).replace("\0", "") + "'");
                        byte[] outgoing_packet = Utils.emptyPacket( (byte)129 );
                        for (int i = 1; i < 8; i++) {
                            outgoing_packet[i] = packet[i];
                        }
                        Utils.send(outgoing_packet, os);
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
        catch (IOException er) {
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