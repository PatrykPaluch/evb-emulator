package pk.lab06.sw.host;


import java.util.Scanner;

public class ButtonsConfigure {

    Button [] buttons;
    Scanner sc;
    ButtonsConfigure(Button [] buttons) {
        this.sc = new Scanner(System.in);
        this.buttons = buttons;
    }

    public void run() {
        try {
            System.out.println("Konfiguracja funkcji przyciskow:");
            System.out.println("0. Wyjscie");
            for (int i=1; i<9; i++) {
                System.out.println(i + ". " + buttons[i-1].getDescription());
            }
            System.out.println("Wybierz przycisk do konfiguracji");

            while (true) {
                System.out.print(">> ");
                String line = sc.nextLine();
                int value = Integer.parseInt(line);
                if (value >= 1 && value <= 8) {
                    selectButton(value-1);
                }
                else if (value == 0) {
                    break;
                }
                else {
                    System.out.println("Invalid command.");
                }

            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void selectButton(int i) {
        try {
            System.out.println("Wybrano przycisk " + i);
            System.out.println("0. Powrot");
            System.out.println("1. Opis: " + buttons[i].getDescription());
            System.out.println("2. Funkcja: " + buttons[i].getCommand());
            System.out.println("Co chcesz zmienic?");
            System.out.print(">>> ");
            String line = sc.nextLine();
            int value = Integer.parseInt(line);
            while (true) {
                if (value == 0) {
                    break;
                }
                else if (value == 1) {
                    System.out.println("Podaj opis (do 14 znakow).");
                    System.out.print(">>>> ");
                    line = sc.nextLine();
                    buttons[i].setDescription(line);
                    System.out.println("Zmieniono opis przycisku " + i + ".");
                    break;
                }
                else if (value == 2) {
                    System.out.println("Podaj komende.");
                    System.out.print(">>> ");
                    line = sc.nextLine();
                    buttons[i].setCommand(line);
                    System.out.println("Zmieniono komende przycisku " + i + ".");
                    break;
                }
            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Button[] getButtons() {
        return this.buttons;
    }
}






