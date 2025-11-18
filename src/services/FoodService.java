package services;

import models.Cart;
import models.FoodItem;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodService {

    public boolean addFoodItem(FoodItem foodItem) {
        String query = "INSERT INTO food_items (restaurant_id, name, description, price, category, is_available) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, foodItem.getRestaurantId());
            stmt.setString(2, foodItem.getName());
            stmt.setString(3, foodItem.getDescription());
            stmt.setDouble(4, foodItem.getPrice());
            stmt.setString(5, foodItem.getCategory());
            stmt.setBoolean(6, foodItem.isAvailable());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> foodItems = new ArrayList<>();
        String query = "SELECT * FROM food_items WHERE is_available = true";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                FoodItem foodItem = new FoodItem();
                foodItem.setFoodId(rs.getInt("food_id"));
                foodItem.setRestaurantId(rs.getInt("restaurant_id"));
                foodItem.setName(rs.getString("name"));
                foodItem.setDescription(rs.getString("description"));
                foodItem.setPrice(rs.getDouble("price"));
                foodItem.setCategory(rs.getString("category"));
                foodItem.setAvailable(rs.getBoolean("is_available"));
                foodItems.add(foodItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foodItems;
    }

    // by restaurant
    public List<FoodItem> getFoodItemsByRestaurant(int restaurantId) {
        List<FoodItem> foodItems = new ArrayList<>();
        String query = "SELECT * FROM food_items WHERE restaurant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FoodItem foodItem = new FoodItem();
                    foodItem.setFoodId(rs.getInt("food_id"));
                    foodItem.setRestaurantId(rs.getInt("restaurant_id"));
                    foodItem.setName(rs.getString("name"));
                    foodItem.setDescription(rs.getString("description"));
                    foodItem.setPrice(rs.getDouble("price"));
                    foodItem.setCategory(rs.getString("category"));
                    foodItem.setAvailable(rs.getBoolean("is_available"));
                    foodItems.add(foodItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foodItems;
    }

    public boolean addToCart(int customerId, int foodId, int quantity) {
        String checkQuery = "SELECT quantity FROM cart WHERE customer_id = ? AND food_id = ?";
        String updateQuery = "UPDATE cart SET quantity = quantity + ? WHERE customer_id = ? AND food_id = ?";
        String insertQuery = "INSERT INTO cart (customer_id, food_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, customerId);
                checkStmt.setInt(2, foodId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, quantity);
                            updateStmt.setInt(2, customerId);
                            updateStmt.setInt(3, foodId);
                            return updateStmt.executeUpdate() > 0;
                        }
                    } else {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                            insertStmt.setInt(1, customerId);
                            insertStmt.setInt(2, foodId);
                            insertStmt.setInt(3, quantity);
                            return insertStmt.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<Cart> getCartItems(int customerId) {
        List<Cart> cartItems = new ArrayList<>();
        String query = "SELECT c.cart_id, c.customer_id, c.food_id, c.quantity, f.name, f.price, f.restaurant_id " +
                "FROM cart c JOIN food_items f ON c.food_id = f.food_id WHERE c.customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cart cart = new Cart();
                    cart.setCartId(rs.getInt("cart_id"));
                    cart.setCustomerId(rs.getInt("customer_id"));
                    cart.setFoodId(rs.getInt("food_id"));
                    cart.setQuantity(rs.getInt("quantity"));

                    FoodItem foodItem = new FoodItem();
                    foodItem.setFoodId(rs.getInt("food_id"));
                    foodItem.setName(rs.getString("name"));
                    foodItem.setPrice(rs.getDouble("price"));
                    foodItem.setRestaurantId(rs.getInt("restaurant_id"));
                    cart.setFoodItem(foodItem);

                    cartItems.add(cart);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    public boolean clearCart(int customerId) {
        String query = "DELETE FROM cart WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
