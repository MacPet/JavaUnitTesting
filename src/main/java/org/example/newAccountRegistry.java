package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class newAccountRegistry {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/unittestDB";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "test";



    private static Double[] convertToDoubleArray(double[] history) {
        return Arrays.stream(history).boxed().toArray(Double[]::new);
    }

    public static void clear() {
        String query = "DELETE FROM private_accounts";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void add(KontoOsobiste account) {

        double[] history = account.getHistoria();


        String query = "INSERT INTO private_accounts (name, surname, pesel, coupon, balance, history) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, account.getImie());
            statement.setString(2, account.getNazwisko());
            statement.setString(3, account.getPesel());
            statement.setString(4, account.getKupon());
            statement.setDouble(5, account.getSaldo());
            statement.setArray(6, connection.createArrayOf("DOUBLE", convertToDoubleArray(history)));

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        account.setDatabaseID(generatedKeys.getInt(1));
                    }
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }


    public static void update(KontoOsobiste account) {
        if(account.getDatabaseID() == null){
            System.out.println("Update of account failed - unknown account id.");
             return;
        }

        double[] history = account.getHistoria();

        String query = "UPDATE private_accounts SET name = ?, surname = ?, pesel = ?, coupon = ?, balance = ?, history = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);


             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, account.getImie());
            statement.setString(2, account.getNazwisko());
            statement.setString(3, account.getPesel());
            statement.setString(4, account.getKupon());
            statement.setDouble(5, account.getSaldo());
            statement.setArray(6,  connection.createArrayOf("DOUBLE", convertToDoubleArray(history)));
            statement.setInt(7, account.getDatabaseID());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();}
    }

//    public static KontoOsobiste getByID(int id) {
//        KontoOsobiste account = null;
//        String query = "SELECT id, name, surname, pesel, coupon, balance, history FROM private_accounts WHERE id = ?";
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//             PreparedStatement statement = connection.prepareStatement(query)) {
//
//            statement.setInt(1, id);
//            try (ResultSet results = statement.executeQuery()) {
//                if (results.next()) {
//
//                   Array historyArray = results.getArray("history");
//                   Double[] doubleArray = (Double[]) historyArray.getArray();
//                   double[] history = Arrays.stream(doubleArray).mapToDouble(Double::doubleValue).toArray();
//
//
//                    account = new KontoOsobiste(
//                            results.getString("name"),
//                            results.getString("surname"),
//                            results.getString("pesel"),
//                            results.getString("coupon"),
//                            results.getDouble("balance"),
//                            history,
//                            results.getInt("id"));
//
//                }
//            }
//        } catch (SQLException exception) {
//            exception.printStackTrace();
//        }
//        return account;
//    }


    public static List<KontoOsobiste> getByPesel(String pesel) {
        List<KontoOsobiste> result = new ArrayList<>();
        String query = "SELECT id, name, surname, pesel, coupon, balance, history FROM private_accounts WHERE pesel = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, pesel);
            try (ResultSet set = statement.executeQuery()) {

               while (set.next()) {
                    Array historyArray = set.getArray("history");
                    Double[] doubleArray = (Double[]) historyArray.getArray();
                    double[] history = Arrays.stream(doubleArray).mapToDouble(Double::doubleValue).toArray();

                    result.add(new KontoOsobiste(
                            set.getString("name"),
                            set.getString("surname"),
                            set.getString("pesel"),
                            set.getString("coupon"),
                            set.getDouble("balance"),
                            history,
                            set.getInt("id")));

                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public static int getLength() {
        String query = "SELECT COUNT(*) AS count FROM private_accounts";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)) {
            if (results.next()) {
                return results.getInt("count");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();}
        return -1;
    }


    public static void removeByPesel(String pesel) {
        String query = "DELETE FROM private_accounts WHERE pesel = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, pesel);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static KontoOsobiste getLast() {
        String query =
                "SELECT id, name, surname, pesel, coupon, balance, history \n" +
                "FROM private_accounts \n" +
                "ORDER BY id DESC \n" +
                "LIMIT 1";
        KontoOsobiste account = null;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet results = statement.executeQuery()) {

                if(results.next()){


                Array historyArray = results.getArray("history");
                Double[] doubleArray = (Double[]) historyArray.getArray();
                double[] history = Arrays.stream(doubleArray).mapToDouble(Double::doubleValue).toArray();

                account = new KontoOsobiste(
                        results.getString("name"),
                        results.getString("surname"),
                        results.getString("pesel"),
                        results.getString("coupon"),
                        results.getDouble("balance"),
                        history,
                        results.getInt("id"));
                }

            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return account;
}

}
