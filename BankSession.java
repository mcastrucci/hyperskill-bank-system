package banking;

public class BankSession {

    /**
     * Starts the bank application and initialize the Database
     * @param databaseUrl - location of the database files
     */
    public void runBankProgram(String databaseUrl) {
        BankDb dataBase = new BankDb(databaseUrl);

        BankService service = BankService.getInstance();
        service.setDataBase(dataBase);

        BankUtils utils = new BankUtils();
        BankTerminalGui terminalGui = new BankTerminalGui(service, utils);
        terminalGui.runDefaultMenu();
    }
}