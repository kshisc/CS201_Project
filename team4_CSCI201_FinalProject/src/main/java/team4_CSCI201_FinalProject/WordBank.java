

package team4_CSCI201_FinalProject;

import java.sql.*;
import java.util.*;

public class WordBank {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Set<String> usedWords;
    
    private static String defaultUrl = "jdbc:mysql://localhost:3306/drawinggame";
    private static String defaultUser = "root";
    private static String defaultPassword = "root";
    
    public WordBank() {
        this.dbUrl = defaultUrl;
        this.dbUser = defaultUser;
        this.dbPassword = defaultPassword;
        this.usedWords = new HashSet<>();
    }

    public WordBank(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.usedWords = new HashSet<>();
    }

    public String getNewWord() throws SQLException {
        String newWord = null;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Random random = new Random();
            boolean found = false;

            while (!found) {
                String query = "SELECT word FROM Words ORDER BY RAND() LIMIT 1";
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(query)) {

                    if (resultSet.next()) {
                        newWord = resultSet.getString("word");
                        if (!usedWords.contains(newWord)) {
                            found = true;
                            usedWords.add(newWord);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error fetching a new word: " + e.getMessage(), e);
        }

        return newWord;
    }

    public void resetUsedWords() {
        usedWords.clear();
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/drawinggame";
        String user = "root";
        String password = "root";

        WordBank wordBank = new WordBank(url, user, password);

        try {
            System.out.println("New Word: " + wordBank.getNewWord());
            System.out.println("New Word: " + wordBank.getNewWord());
            System.out.println("New Word: " + wordBank.getNewWord());
            wordBank.resetUsedWords();
            System.out.println("Reset used words.");
            System.out.println("New Word: " + wordBank.getNewWord());
            System.out.println("New Word: " + wordBank.getNewWord());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


