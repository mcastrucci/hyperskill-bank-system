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
                        + "	id integer NOT NULL PRIMARY KEY,\n"
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
     * INSERT a new card number into the Database
     * @param cardNumber - the card number
     * @param pin - The pin number of the card
     * @param accountNumber - the account number
     */
    public void createCard (String cardNumber, String pin, String accountNumber) {
        try (Connection con = this.getConnection()) {
            // first we find next id
            try (Statement statement = con.createStatement()) {
                int nextIndex = 0;
                String countStatement = "SELECT COUNT(*) AS count FROM card";
                ResultSet rs = statement.executeQuery(countStatement);
                if (rs.next()) {
                    nextIndex = rs.getInt("count");
                }
                // now that we have the next ID we can insert into the table
                String queryCard = "INSERT INTO card (id, number, pin, balance) VALUES (?, ?, ?, ?)";
                String queryAccount = "INSERT INTO account (id, account) VALUES (?, ?)";

                try (PreparedStatement pstmt = con.prepareStatement(queryCard)) {
                    pstmt.setInt(1, nextIndex);
                    pstmt.setString(2, cardNumber);
                    pstmt.setString(3, pin);
                    pstmt.setLong(4, 0); // default balance
                    pstmt.executeUpdate();
                }
                try (PreparedStatement accountPstmt = con.prepareStatement(queryAccount)) {
                    accountPstmt.setInt(1, nextIndex);
                    accountPstmt.setString(2, accountNumber);
                    accountPstmt.executeUpdate();
                }
            }
        } catch(SQLException se) {
          se.printStackTrace();
          System.out.println("failed while creating new card number");
        }

    }

    /**
     * Gets a card Object from the Database
     * @param cardNumber
     * @return Card dto if exits, null if it doesnt
     */
    public Card getCard (String cardNumber) {
        try (Connection con = this.getConnection()) {
            String countQuery = "SELECT COUNT(*) AS count FROM card "
                    + "WHERE number = ?";
            // first we count the results
            try (PreparedStatement countStatement = con.prepareStatement(countQuery)) {
                countStatement.setString(1, cardNumber);
                ResultSet countResult = countStatement.executeQuery();

                if (countResult.next()) {
                    int cardResults = countResult.getInt("count");

                    if (cardResults == 1) { // we should not have more than one result
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
                        throw new Exception("More than one result, Database error");
                    }
                } else {
                    return null; // card does not exist
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("failed while getting card from database");
        }
        return null;
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
