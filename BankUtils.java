package banking;

import java.util.Scanner;

public class BankUtils {

    /**
     * It gets the user input and checks if it is a correct format
     * @return the option selected by the user
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

    /**
     * it uses Scanner to get user amount
     * @return the amount that the user did input
     */
    public long getUserAmount() {
        Scanner scanner = new Scanner(System.in);
        long userAmount = 0;
        try {
            userAmount = scanner.nextLong();
        } catch(Exception ex) {
            System.out.println("Incorrect input amount");
            userAmount = getUserAmount(); // executing again
        }
        return userAmount;
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