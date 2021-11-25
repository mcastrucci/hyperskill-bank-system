package banking.dto;

public class Card {
    private int id;
    private String number;
    private String pin;
    private long balance;

    public int getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }

    public String getNumber() {
        return number;
    }

    public String getPin() {
        return pin;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
