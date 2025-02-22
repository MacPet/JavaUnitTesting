package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackList {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/unittestDB";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "test";

    public static void clear() {
         String query = "DELETE FROM black_list";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void addAccountToBlackList(String pesel, String reason) {

        String query = "INSERT INTO black_list (pesel, reason) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pesel);
            statement.setString(2, reason);
            statement.executeUpdate();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

    }


    public static void update(String pesel, String reason) {


        String query = "UPDATE black_list  SET reason = ? WHERE pesel = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);


             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, reason);
            statement.setString(2, pesel);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    public static String getReason(String pesel) {
        String result = "";
        String query = "SELECT reason FROM black_list WHERE pesel = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, pesel);
            try (ResultSet set = statement.executeQuery()) {

                if (set.next()) {
                result = set.getString("reason");

                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public static int getLength() {
        String query = "SELECT COUNT(*) AS count FROM black_list";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)) {
            if (results.next()) {
                return results.getInt("count");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static boolean isAccountOnBlackList(String pesel) {
        String query = "SELECT COUNT(*) AS count FROM black_list where pesel = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

             PreparedStatement statement = connection.prepareStatement(query)) {
             statement.setString(1, pesel);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("count") > 0;

                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }


    public static void remove(String pesel) {
        String query = "DELETE FROM black_list WHERE pesel = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, pesel);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }





}
