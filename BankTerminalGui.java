package banking;

import banking.dto.Card;

public class BankTerminalGui {

    final static String[] MENU_OPTIONS = {"1. Create an account", "2. Log into account", "0. Exit"};
    final static String[] USER_MENU_OPTIONS = {"1. Balance", "2. Log out", "0. Exit"};
    private BankService service;
    private BankUtils utils;

    public BankTerminalGui(BankService service, BankUtils utils) {
        this.service = service;
        this.utils = utils;
    }
    /*
        It runs the default menu and listen for options
     */

    public void runDefaultMenu() {
        int optionSelected = 0;
        do {
            showMenu();
            optionSelected = utils.getUserNumericInput();

            switch (optionSelected) {
                case 0:
                    exitProgram();
                    break;
                case 1:
                    String cardNumber = service.createAccount();

                    if (cardNumber != null && service.getPinNumber(cardNumber).intValue() != -1) {
                        String output = String.format("Your card has been created%n" +
                                "Your card number:%n" +
                                "%s" +
                                "%nYour card PIN:%n" +
                                "%d",cardNumber, service.getPinNumber(cardNumber).intValue());
                        System.out.println(output);
                    } else {
                        System.out.println("failed to create new account");
                    }
                    break;
                case 2:
                    System.out.println("Enter your card number:");
                    String userCardNumberString = utils.getUserString();
                    // user cardNumber should be cateable to long
                    try {
                        // we ask for a pin
                        System.out.println("Enter your PIN:");
                        int pinNumber = utils.getUserNumericInput();
                        // check if pin is correct
                        boolean pinCorrect = service.checkPinNumber(userCardNumberString, pinNumber);
                        if (pinCorrect) {
                            System.out.println("You have successfully logged in!");
                            runUserMenu(userCardNumberString);
                        } else {
                            throw new Exception("invalid number");
                        }
                    } catch (Exception ex) {
                        System.out.println("Wrong card number or PIN!");
                    }
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        } while(optionSelected != 0);
    }

    /*
        It displays the User menu and asks User for an option input
    */
    private  void runUserMenu(String userCardNumber) {
        int optionSelected = 0;
        do {
            showUserMenu();
            optionSelected = utils.getUserNumericInput();
            switch (optionSelected) {
                case 0:
                    exitProgram();
                    break;
                case 1:
                    System.out.println(String.format("Balance: %d", BankService.getInstance().getBalance(userCardNumber)));
                    break;
                case 2:
                    System.out.println("You have successfully logged out!");
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        } while (optionSelected != 0 && optionSelected != 2);
    }

    /*
        It displays the main Menu by using an array of options
     */
    private  void showMenu() {
        for (String option : MENU_OPTIONS) {
            System.out.println(option);
        }
    }

    /*
        It displays the main Menu by using an array of options
    */
    private  void showUserMenu() {
        for (String option : USER_MENU_OPTIONS) {
            System.out.println(option);
        }
    }

    /*
        it just exits the program
     */
    private  void exitProgram() {
        System.out.println("Bye!");
        System.exit(0);
    }
}