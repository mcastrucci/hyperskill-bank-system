package banking;

import banking.dto.Card;

import java.util.HashMap;
import java.util.Random;

/*
    Singleton Bank service to simulate a data base and a static service
 */
public class BankService {

    final  int BIN_NUMBER = 400000;
    Random randomSeedGenerator = new Random(); // only used to generate a random seed for each program run
    Random random = new Random(randomSeedGenerator.nextLong());

    // simulated data base
    private  long[] listOfCustomers;
    private  String[] accountNumbers;
    private  HashMap<String, Integer> pinAccountHm = new HashMap <String, Integer>();
    private  int balance = 0;
    private static BankService instance = null;
    private BankDb dataBase;

    private BankService() {}

    public static BankService getInstance() {
        if (instance == null) {
            instance = new BankService();
        }
        return instance;
    }

    /**
     * Creates an account and insert it into the database
     * @return the account number, null if it fails
     */
    public String createAccount() {
        String cardNumber = "";
        boolean numberCreated = false;
        boolean successCreation = true;
        int pinNumber = 0;

        int accountNumber = 0;

        while (!numberCreated) {
            accountNumber = createRandomAccountNumber();
            numberCreated = true;

            String tempAccount = dataBase.getAccount(Integer.toString(accountNumber)); // check if account already exist

            if (tempAccount.length() > 0) {
                // account exists, we continue
                numberCreated = false;
                continue;
            }
        }
        // checking if accountNumber was created and adding checksum
        if (accountNumber > 0) {
            // card number will be BIN + accountNumber + random checksum
            String cardNumberWithoutChecksum = String.format("%d%d", BIN_NUMBER, accountNumber);

            int checksum = generateChecksum(cardNumberWithoutChecksum);
            cardNumber = String.format("%s%d", cardNumberWithoutChecksum, checksum);
        } else { // this should not happen, but here we go
            successCreation = false;
        }

        // we have now a new cardNumber. lets check if already exists in our database (it should not happen after the
        // first check
        if(cardNumber != null && cardNumber.length() > 0) {
            Card card = dataBase.getCard(cardNumber);
            if (card != null) { // something went wrong and card already exist
                successCreation = false;
            }
        } else { // this should not happen
            successCreation = false;
        }

        // finally we create pin number
        int pinInterval = 9999 - 1000 + 1;
        pinNumber = random.nextInt(pinInterval) + 1000;

        if(successCreation) { // last check
            // we add everything to the database
            dataBase.createCard(cardNumber, Integer.toString(pinNumber), Integer.toString(accountNumber));
            return cardNumber;
        } else { // we will just re-execute
            return createAccount();
        }
    }

    /**
     * Checks the pin number for current card
     * @param cardNumber
     * @param pinNumber
     * @return true if Card exists and pin number matches, false if not
     */
    public boolean checkPinNumber(String cardNumber, int pinNumber) {
        boolean correct = false;

        Card card = dataBase.getCard(cardNumber);

        if(card != null) {
            if (card.getPin().equalsIgnoreCase(Integer.toString(pinNumber))){
                correct = true;
            }
        }
        return correct;
    }

    /*
        It generates a checksum for the currentAcount number using Luhn Algorithm
        It returns -1 if something fails
    */
    public int generateChecksum(String cardNumberWithoutChecksum) {
        // first we need to calculate the control number, for that, we will summ all digits of the cardNumber
        int sum = 0;
        int checksum = -1;
        try {
            for (int i = 0; i < cardNumberWithoutChecksum.length(); i++) {
                // if odd index (i+1) we will multiply it by 2
                int currentNumber = Character.getNumericValue(cardNumberWithoutChecksum.charAt(i));
                int digitToSum = (i + 1) % 2 == 0 ? currentNumber : currentNumber * 2;
                // if result number is higher than 10, we substract 9
                digitToSum = digitToSum > 9 ? (digitToSum - 9) : digitToSum;
                sum += digitToSum;
            }
            // now that we have the sum, we need to find the next number that suming it to the total, will be multiple of 10
            for (int i = 0; i < 10; i++) {
                if ((sum + i) % 10 == 0) {
                    checksum = i;
                    break;
                }
            }
        } catch (NumberFormatException nfe) {
            System.out.println(String.format("Invalid card number while generating checksum -> %s"
                    , cardNumberWithoutChecksum));
        }

        return checksum;
    }

    /**
     * Gets the pin number for current Card
     * @param cardNumber
     * @return pinNumber
     */
    public Integer getPinNumber(String cardNumber) {
        Integer pinNumber = -1;
        Card card = dataBase.getCard(cardNumber);

        if (card != null) {
            pinNumber = Integer.parseInt(card.getPin());
        }
        return pinNumber;
    }

    /*
        It creates a random account Number
     */
    private int createRandomAccountNumber() {
        int intervalAccountNumber = 999999999 - 100000000 + 1;
        return random.nextInt(intervalAccountNumber) + 100000000;
    }

    /**
     * gets currentUserBalance
     * @param cardNumber
     * @return
     */
    public long getBalance(String cardNumber) {
        long balance = 0;
        Card card = dataBase.getCard(cardNumber);

        if (card != null) {
            balance = card.getBalance();
        }
        return balance;    }

    public void setDataBase(BankDb dataBase) {
        this.dataBase = dataBase;
    }
}