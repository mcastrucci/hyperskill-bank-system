package banking;

public class BankTerminalGui {

    final static String[] MENU_OPTIONS = {"1. Create an account", "2. Log into account", "0. Exit"};
    final static String[] USER_MENU_OPTIONS = {"1. Balance", "2. Add income", "3. Do a transfer", "4. Close account","5. Log out", "0. Exit"};
    private BankService service;
    private BankUtils utils;

    public BankTerminalGui(BankService service, BankUtils utils) {
        this.service = service;
        this.utils = utils;
    }

    /**
     * Runs the default menu and handle user options
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
                    handleCreateAccountOption();
                    break;
                case 2:
                    handleLogin();
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        } while(optionSelected != 0);
    }

    /**
     * handles createAccount option by creating an account in the service
     * and injecting it into the database
     */
    private void handleCreateAccountOption() {
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
    }

    private void handleLogin() {
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
    }

    /**
     * It runs the user Menu and handle each type of option selected
     * @param userCardNumber
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
                    showBalanceMenu(userCardNumber);
                    break;
                case 2:
                    showIncomeMenu(userCardNumber);
                    break;
                case 3:
                    showTransferMenu(userCardNumber);
                    break;
                case 4:
                    showCloseAccountMenu(userCardNumber);
                    break;
                case 5:
                    System.out.println("You have successfully logged out!");
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        } while (optionSelected != 0 && optionSelected != 5);
    }

    /**
     * Display default menu (not user)
     */
    private  void showMenu() {
        for (String option : MENU_OPTIONS) {
            System.out.println(option);
        }
    }

    /**
     * displays user Menu
     */
    private void showUserMenu() {
        for (String option : USER_MENU_OPTIONS) {
            System.out.println(option);
        }
    }

    /**
     * Displayes income menu and process
     * user income amount
     * @param cardNumber
     */
    private void showIncomeMenu(String cardNumber) {
        System.out.println("Enter income:");
        long amount = utils.getUserAmount();
        service.addIncome(cardNumber, amount);
        System.out.println("Income was added!");
    }

    /**
     * displays balance menu
     * and user balance
     * @param cardNumber
     */
    private void showBalanceMenu(String cardNumber) {
        System.out.println(String.format("Balance: %d", BankService.getInstance().getBalance(cardNumber)));

    }

    /**
     * displays transfer menu and handle user interactions
     * with that menu. It allow the user to transfer money to other account
     * @param cardNumber
     */
    private void showTransferMenu(String cardNumber) {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String userInputCard = utils.getUserString();
        boolean cardNumberCorrect = service.checkCardNumberValid(userInputCard);

        // for performance, we first check this
        if (!userInputCard.equalsIgnoreCase(cardNumber)) {
            if (cardNumberCorrect) {
                // now we should check if the account exist
                boolean cardExist = service.checkIfAccountExist(userInputCard);
                if (cardExist) {
                    System.out.println("Enter how much money you want to transfer:");
                    long amountToTransfer = utils.getUserAmount();
                    // we should check if current user has that amount
                    long userBalance = service.getBalance(cardNumber);
                    if (userBalance >= amountToTransfer) {
                        // ready to transfer
                        boolean success = service.transferMoney(cardNumber, userInputCard, amountToTransfer);
                        if (!success) {
                            System.out.println("failed while doing money transfer");
                            return;
                        }
                    } else {
                        System.out.println("Not enough money!");
                    }
                } else {
                    System.out.println("Such a card does not exist.");
                }
            } else {
                System.out.println("Probably you made a mistake in the card number. Please try again!");
                return;
            }
        } else {
            System.out.println("You can't transfer money to the same account!");
            return;
        }
    }

    /**
     * It displays the close account menu
     * and calls the service to remove current account
     * @param cardNumber
     */
    private void showCloseAccountMenu(String cardNumber) {
        boolean success = service.deleteAccount(cardNumber);
        if (success) {
            System.out.println("The account has been closed!");
        } else {
            System.out.println("failed while closing the account");
        }
    }

    /**
     * Exits the application
     */
    private  void exitProgram() {
        System.out.println("Bye!");
        System.exit(0);
    }
}