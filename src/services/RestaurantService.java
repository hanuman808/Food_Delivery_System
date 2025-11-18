package services;

import models.Restaurant;
import models.User;
import utils.DatabaseConnection;

import java.sql.*;

public class RestaurantService {

    /**
     * Registers a new restaurant.
     * Inserts into users table AND restaurants table.
     * Rolls back transaction if any operation fails.
     */
    public boolean registerRestaurant(Restaurant restaurant) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert user record
            String userSql = "INSERT INTO users (username, email, password, phone, user_type) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {

                userStmt.setString(1, restaurant.getUsername());
                userStmt.setString(2, restaurant.getEmail());
                userStmt.setString(3, restaurant.getPassword());
                userStmt.setString(4, restaurant.getPhone());
                userStmt.setString(5, "RESTAURANT");

                int affected = userStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                ResultSet keys = userStmt.getGeneratedKeys();
                if (!keys.next()) {
                    conn.rollback();
                    return false;
                }

                int userId = keys.getInt(1);
                restaurant.setUserId(userId);
            }

            // Insert into restaurants table
            String restaurantSql =
                    "INSERT INTO restaurants (restaurant_id, name, address, cuisine_type) VALUES (?, ?, ?, ?)";

            try (PreparedStatement restStmt = conn.prepareStatement(restaurantSql)) {

                restStmt.setInt(1, restaurant.getUserId());
                restStmt.setString(2, restaurant.getName());
                restStmt.setString(3, restaurant.getAddress());
                restStmt.setString(4, restaurant.getCuisineType());

                int affected = restStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Success
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
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Restaurant login authentication.
     */
    public Restaurant authenticateRestaurant(String username, String password) {

        String sql =
                "SELECT u.*, r.name, r.address, r.cuisine_type " +
                "FROM users u " +
                "JOIN restaurants r ON u.user_id = r.restaurant_id " +
                "WHERE u.username = ? AND u.password = ? AND u.user_type = 'RESTAURANT'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Restaurant r = new Restaurant();
                r.setUserId(rs.getInt("user_id"));
                r.setUsername(rs.getString("username"));
                r.setEmail(rs.getString("email"));
                r.setPassword(rs.getString("password"));
                r.setPhone(rs.getString("phone"));
                r.setName(rs.getString("name"));
                r.setAddress(rs.getString("address"));
                r.setCuisineType(rs.getString("cuisine_type"));
                r.setUserType(User.UserType.RESTAURANT);
                return r;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Login failed
    }
}
