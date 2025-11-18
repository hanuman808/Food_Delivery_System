package services;

import models.Order;
import models.Order.OrderItem;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    /** PLACE ORDER **/
    public boolean placeOrder(Order order, List<models.Cart> cartItems) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String insertOrder =
                    "INSERT INTO orders (customer_id, restaurant_id, status, total_amount, delivery_address) " +
                            "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getCustomerId());
            orderStmt.setInt(2, order.getRestaurantId());
            orderStmt.setString(3, order.getStatus().name());
            orderStmt.setDouble(4, order.getTotalAmount());
            orderStmt.setString(5, order.getDeliveryAddress());
            orderStmt.executeUpdate();

            ResultSet keys = orderStmt.getGeneratedKeys();
            keys.next();
            int orderId = keys.getInt(1);

            String insertItem = "INSERT INTO order_items (order_id, food_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(insertItem);

            for (models.Cart c : cartItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, c.getFoodId());
                itemStmt.setInt(3, c.getQuantity());
                itemStmt.setDouble(4, c.getFoodItem().getPrice());
                itemStmt.addBatch();
            }

            itemStmt.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    /** GET ORDERS BY CUSTOMER **/
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();

        String query = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("customer_id"),
                        rs.getInt("restaurant_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("delivery_address")
                );

                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setOrderDate(rs.getTimestamp("order_date"));

                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /** GET ORDERS BY RESTAURANT **/
    public List<Order> getOrdersByRestaurant(int restaurantId) {
        List<Order> orders = new ArrayList<>();

        String query = "SELECT * FROM orders WHERE restaurant_id = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("customer_id"),
                        rs.getInt("restaurant_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("delivery_address")
                );

                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setOrderDate(rs.getTimestamp("order_date"));

                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /** GET ORDERS BY DELIVERY PERSON **/
    public List<Order> getOrdersByDeliveryPerson(int deliveryId) {
        List<Order> orders = new ArrayList<>();

        String query = "SELECT * FROM orders WHERE delivery_id = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, deliveryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("customer_id"),
                        rs.getInt("restaurant_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("delivery_address")
                );

                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setOrderDate(rs.getTimestamp("order_date"));

                orders.add(order);
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return orders;
    }

    /** UPDATE ORDER STATUS **/
    public boolean updateOrderStatus(int orderId, Order.OrderStatus status) {

        String query = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** GET ORDER ITEMS (FOR POPUP DETAILS) **/
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();

        String query =
                "SELECT oi.food_id, oi.quantity, oi.price, f.name " +
                "FROM order_items oi " +
                "JOIN food_items f ON oi.food_id = f.food_id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem(
                        rs.getInt("food_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );

                item.setFoodName(rs.getString("name")); // Now this works

                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /** GET ORDER BY ID (FOR LIVE TRACKING) **/
    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = new Order(
                        rs.getInt("customer_id"),
                        rs.getInt("restaurant_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("delivery_address")
                );

                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setDeliveryId(rs.getInt("delivery_id"));

                return order;
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return null;
    }
}
