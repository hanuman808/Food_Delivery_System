package services;

import models.Customer;
import models.User;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    /**
     * Registers a new customer:
     * 1. Insert into users table
     * 2. Insert into customers table
     * Both must succeed together (transaction)
     */
    public boolean registerCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement custStmt = null;
        ResultSet keys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert into users table
            String userQuery =
                    "INSERT INTO users (username, email, password, phone, user_type) VALUES (?, ?, ?, ?, ?)";

            userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, customer.getUsername());
            userStmt.setString(2, customer.getEmail());
            userStmt.setString(3, customer.getPassword());
            userStmt.setString(4, customer.getPhone());
            userStmt.setString(5, "CUSTOMER");

            if (userStmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            // Get new user_id
            keys = userStmt.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                return false;
            }

            int userId = keys.getInt(1);
            customer.setUserId(userId);

            // Insert into customers table
            String custQuery = "INSERT INTO customers (customer_id, address) VALUES (?, ?)";

            custStmt = conn.prepareStatement(custQuery);
            custStmt.setInt(1, userId);
            custStmt.setString(2, customer.getAddress());

            if (custStmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;

        } finally {
            try { if (keys != null) keys.close(); } catch (Exception ignored) {}
            try { if (userStmt != null) userStmt.close(); } catch (Exception ignored) {}
            try { if (custStmt != null) custStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    /**
     * Authenticate a customer at login
     */
    public Customer authenticateCustomer(String username, String password) {
        String query =
                "SELECT u.*, c.address " +
                "FROM users u " +
                "JOIN customers c ON u.user_id = c.customer_id " +
                "WHERE u.username = ? AND u.password = ? AND u.user_type = 'CUSTOMER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer();
                customer.setUserId(rs.getInt("user_id"));
                customer.setUsername(rs.getString("username"));
                customer.setEmail(rs.getString("email"));
                customer.setPassword(rs.getString("password"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));

                // Correct method
                customer.setUserType(User.UserType.CUSTOMER);

                return customer;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns all customers (Admin)
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();

        String query =
                "SELECT u.*, c.address " +
                "FROM users u " +
                "JOIN customers c ON u.user_id = c.customer_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setUserId(rs.getInt("user_id"));
                customer.setUsername(rs.getString("username"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));

                // Correct method
                customer.setUserType(User.UserType.CUSTOMER);

                customers.add(customer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }
}
