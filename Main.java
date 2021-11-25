package banking;

public class Main {

    public static void main(String[] args) {
        BankSession session = new BankSession();
        try {
            // check filename Argument
            if (args.length > 0) {
                boolean isFilename = false;
                String databaseUrl = "";
                for (String arg : args) {
                    if (isFilename) {
                        databaseUrl = arg;
                        break;
                    } else if (arg.equalsIgnoreCase("-fileName")) {
                        isFilename = true;
                    }
                }
                if (isFilename && databaseUrl.length() > 0) {
                    session.runBankProgram(databaseUrl);
                } else {
                    throw new Exception("missing fileName argument");
                }
            } else {
                throw new Exception("arguments missing -fileName");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }
}