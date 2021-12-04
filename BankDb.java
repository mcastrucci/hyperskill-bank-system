package banking;

import banking.dto.Card;
import org.sqlite.SQLiteDataSource;

import java.sql.*;

/**
 * Database interface service class.
 * It inits the database connection and provides APIs to interact with Data Base data
 */
public class BankDb {

    private String url;
    private SQLiteDataSource dataSource;


    /**
     * Create an instance of BankDb API
     * to interact with the database
     * It also calls init method to start dataSource
     *
     * @param url
     */
    public BankDb(String url) {
        this.url = url;
        init();
    }

    /**
     * Inits Data source to be used in the class to
     * make connections and statements
     */
    private void init() {
        this.dataSource = new SQLiteDataSource();
        this.dataSource.setUrl("jdbc:sqlite:" + this.url);
        this.createDefaultTables();
    }

    /**
     * creates a Connection and
     * doesnt close it giving another method the independence to close it
     * in different contexts
     *
     * @return the Connection if it was established,
     *
     * null if there was an error
     */
    private Connection getConnection() {
        Connection con = null;
        try {
            con = this.dataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            return con;
        }
    }

    /**
     * closes the connection with the database
     * @param con - The connection with the database
     * @return true if it was succesfully closed, null if it did fail
     */
    private boolean closeConnection(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
            if (con.isClosed()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Creates default tables needed in the database
     * If this fails, it exits the application
     */
    private void createDefaultTables () {
        try (Connection con = this.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String cardQuery = "CREATE TABLE IF NOT EXISTS card (\n"
                        + "	id integer NOT NULL PRIMARY KEY,\n"
                        + "	number text NOT NULL,\n"
                        + "	pin text NOT NULL,\n"
                        + " balance INTEGER DEFAULT 0\n"
                        + ");";
                statement.executeUpdate(cardQuery);
            }
            try (Statement st = con.createStatement()) {
                String accountQuery = "CREATE TABLE IF NOT EXISTS account (\n"
                        + "	id integer NOT NULL,\n"
                        + "	account text NOT NULL\n"
                        + ");";
                st.executeUpdate(accountQuery);
            }
        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("failed to create default tables");
            System.exit(0);
        }
    }

    /**
     * checks in the database if the card exist by executing
     * a count query.
     * @return true if everything is fine, false if it does not exist or there is a database error (more than 1 result)
     * @param card
     */
    public boolean checkCardNumber(String card) {
        try (Connection con = this.getConnection()) {
            String countQuery = "SELECT COUNT(*) AS count FROM card "
                    + "WHERE number = ?";
            // first we count the results
            try (PreparedStatement countStatement = con.prepareStatement(countQuery)) {
                countStatement.setString(1, card);
                ResultSet countResult = countStatement.executeQuery();

                if (countResult.next()) {
                    int cardResults = countResult.getInt("count");

                    if (cardResults == 1) { // we should not have more than one result
                        return true; // card exist
                    } else if (cardResults == 0) {
                        return false; // card does not exist case
                    } else {
                        throw new Exception("More than one result, Database error!!");
                    }
                } else {
                    throw new Exception("No result set, error!!");
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("failed while getting card from database");
        }
        return false;
    }

    /**
     * gets next card index by executing a count query
     * @return the next DB index or -1 if something went wrong
     */
    private int getNextCardIndex() {
        int nextIndex = -1;
        try (Connection con = this.getConnection()) {
            // first we find next id
            try (Statement statement = con.createStatement()) {
                String countStatement = "SELECT COUNT(*) AS count FROM card";
                ResultSet rs = statement.executeQuery(countStatement);
                if (rs.next()) {
                    nextIndex = rs.getInt("count");
                }
            }
        } catch(SQLException se) {
            se.printStackTrace();
            System.out.println("failed while getting next Index");
        }
        return nextIndex;
    }

    /**
     * INSERT a new card number into the Database
     * @param cardNumber - the card number
     * @param pin - The pin number of the card
     * @param accountNumber - the account number
     * @return true if it was successful, false if there was an issue
     */
    public boolean createCard (String cardNumber, String pin, String accountNumber) {
        boolean wasSuccessful = false;
        try (Connection con = this.getConnection()) {
            // init transaction
            con.setAutoCommit(false);

            // first we find next id
            int nextIndex = getNextCardIndex();

            // now that we have the next ID we can insert into the table
            String queryCard = "INSERT INTO card (id, number, pin, balance) VALUES (?, ?, ?, ?)";
            String queryAccount = "INSERT INTO account (id, account) VALUES (?, ?)";

            try (PreparedStatement pstmt = con.prepareStatement(queryCard);

                PreparedStatement accountPstmt = con.prepareStatement(queryAccount)) {
                pstmt.setInt(1, nextIndex);
                pstmt.setString(2, cardNumber);
                pstmt.setString(3, pin);
                pstmt.setLong(4, 0); // default balance
                pstmt.executeUpdate();

                accountPstmt.setInt(1, nextIndex);
                accountPstmt.setString(2, accountNumber);
                accountPstmt.executeUpdate();

                con.commit(); // execute transaction
                wasSuccessful = true;
            }
        } catch(SQLException se) {
          se.printStackTrace();
          System.out.println("failed while creating new card number");
        }
        return wasSuccessful;
    }

    /**
     * Gets a card Object from the Database
     * @param cardNumber
     * @return Card dto if exits, null if it doesnt
     */
    public Card getCard (String cardNumber) {
        try (Connection con = this.getConnection()) {
            // first we check if the card exist and there is only one result
            boolean cardExist = checkCardNumber(cardNumber);

            if(cardExist) { // happy case
                String query = "SELECT * FROM card "
                + "WHERE number = ?";
                try (PreparedStatement statement = con.prepareStatement(query)) {
                    statement.setString(1, cardNumber);
                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        Card card = new Card();

                        card.setBalance(rs.getLong("balance"));
                        card.setNumber(rs.getString("number"));
                        card.setPin(rs.getString("pin"));
                        return card;
                    } else {
                        throw new Exception("error while getting card Number");
                    }
                }
            } else {
                return null; // card does not exist, that is not bad
            }

        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("failed while getting card from database");
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return null; // wasnt able to create card
        }
    }

    /**
     * Adds an input to selected user cardNumber
     * @param cardNumber
     * @return true if it was successful, false if not
     */
    public boolean updateBalance(String cardNumber, long amount) {
        try (Connection con = this.getConnection()) {
            // check if card number exist
            boolean cardCorrect = checkCardNumber(cardNumber);

            if (cardCorrect) {
                String updateQuery = "UPDATE card SET balance = balance + ? WHERE number = ?";
                try (PreparedStatement pstmt = con.prepareStatement(updateQuery)) {
                    pstmt.setLong(1, amount);
                    pstmt.setString(2, cardNumber);
                    pstmt.executeUpdate();
                    return true;
                }
            } else {
                throw new Exception("card does not exist or not correct");
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            System.out.println("failed to add income into data base to current card number");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Server error - failed to add income into current card number");
            return false;
        }
    }

    /**
     * transfer money from an account to other
     * @param receptor - the account number that will get the money
     * @param originCard - The account that will transfer his balance
     * @param amount - The amount to transfer
     * @return true if it was successful, false if not
     */
    public boolean transfer(String originCard, String receptor, long amount) {
        try (Connection con = this.getConnection()) {
            con.setAutoCommit(false);
            // check if card number exist
            boolean cardCorrect = checkCardNumber(originCard) && checkCardNumber(receptor);

            if (cardCorrect) {
                String originAccountQuery = "UPDATE card SET balance = balance - ? WHERE number = ?";
                String receptorAccountQuery = "UPDATE card SET balance = balance + ? WHERE number = ?";
                try (PreparedStatement originAccountStatement = con.prepareStatement(originAccountQuery) ;
                     PreparedStatement receptorAccountStatement = con.prepareStatement(receptorAccountQuery)) {
                    originAccountStatement.setLong(1, amount);
                    originAccountStatement.setString(2, originCard);
                    originAccountStatement.executeUpdate();

                    receptorAccountStatement.setLong(1, amount);
                    receptorAccountStatement.setString(2, receptor);
                    receptorAccountStatement.executeUpdate();
                    con.commit();
                    return true;
                }
            } else {
                throw new Exception("card does not exist or not correct");
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            try {
                getConnection().rollback();
            } catch (SQLException sqlException) {
                System.out.println("Failed to make rollback to transfer transaction!!");
            }
            System.out.println("failed to add income into data base to current card number");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Server error - failed to add income into current card number");
            return false;
        }
    }

    /**
     * Delete the selected account / card number
     * @param cardNumber
     * @return true if it was successful, false if not
     */
    public boolean deleteAccount (String cardNumber) {
        boolean wasSuccessful = false;
        try (Connection con = this.getConnection()) {
            // init transaction
            con.setAutoCommit(false);

            // now that we have the next ID we can insert into the table
            String queryCard = "DELETE FROM card WHERE number = ?";
            String queryAccount = "DELETE FROM account WHERE account = ?";

            try (PreparedStatement pstmt = con.prepareStatement(queryCard);
                 PreparedStatement accountPstmt = con.prepareStatement(queryAccount)) {

                pstmt.setString(1, cardNumber);
                pstmt.executeUpdate();

                accountPstmt.setString(1, cardNumber);
                accountPstmt.executeUpdate();

                con.commit(); // execute transaction
                wasSuccessful = true;
            }
        } catch(SQLException se) {
            se.printStackTrace();
            System.out.println("failed while deleting account");
        }
        return wasSuccessful;
    }

    /**
     * Gets an account from database
     * @param accountNumber
     * @return Card dto if exits, null if it doesnt
     */
    public String getAccount (String accountNumber) {
        String account = "";
        try (Connection con = this.getConnection()) {
            String query = "SELECT * FROM account "
                    + "WHERE account = ?";
            try (PreparedStatement statement = con.prepareStatement(query)) {
                statement.setString(1, accountNumber);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    account = rs.getString("account");
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return account;
    }

    /**
     * Gets an account from database
     * @param accountNumber
     * @return Card dto if exits, null if it doesnt
     */
    public Card getCardFromAccount (String accountNumber) {
        Card card = null;
        try (Connection con = this.getConnection()) {
            String query = "SELECT * FROM account "
                    + "WHERE account = ?";
            try (PreparedStatement statement = con.prepareStatement(query)) {
                statement.setString(1, accountNumber);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    String account = rs.getString("account");
                    int id = rs.getInt("id");

                    if (account != null && account.length() > 0) {

                        String cardQuery = "SELECT * FROM card "
                                + "WHERE id = ?";
                        try (PreparedStatement cardStatement = con.prepareStatement(cardQuery)) {
                            cardStatement.setInt(1, id);
                            ResultSet cardResultSet = cardStatement.executeQuery();

                            if (cardResultSet.next()) {
                                card = new Card();

                                card.setBalance(cardResultSet.getLong("balance"));
                                card.setNumber(cardResultSet.getString("number"));
                                card.setPin(cardResultSet.getString("pin"));
                                return card;
                            } else {
                                throw new Exception("error while getting card Number");
                            }

                        }
                    }
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return card;
    }

}
