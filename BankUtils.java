package banking;

import java.util.Scanner;

public class BankUtils {
    /*
        It gets the user input and checks if it is a correct format
     */
    public int getUserNumericInput() {
        Scanner scanner = new Scanner(System.in);
        int userSelection = 0;
        try {
            userSelection = scanner.nextInt();
        } catch(Exception ex) {
            System.out.println(String.format("incorrect input, expected a number from 0 to %d"
                    , BankTerminalGui.MENU_OPTIONS.length));
            userSelection = getUserNumericInput(); // executing again
        }
        return userSelection;
    }

    /*
        It gets a user String from input
     */
    public String getUserString() {
        Scanner scanner = new Scanner(System.in);
        String userString = "";
        try {
            userString = scanner.nextLine();
        } catch(Exception ex) {
            System.out.println(String.format("unexpectedInput"
                    , BankTerminalGui.MENU_OPTIONS.length));
            userString = getUserString(); // executing again
        }
        return userString;
    }
}