package services;

import models.DeliveryPerson;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryService {

    /** LOGIN DELIVERY PERSON */
    public DeliveryPerson authenticateDelivery(String username, String password) {

        String sql = "SELECT * FROM delivery_persons WHERE name = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                DeliveryPerson d = new DeliveryPerson();

                // User parent class (userId = delivery_id)
                d.setUserId(rs.getInt("delivery_id"));
                d.setUsername(rs.getString("name"));
                d.setEmail(rs.getString("email"));
                d.setPhone(rs.getString("phone"));

                // availability
                d.setAvailable(rs.getString("status").equalsIgnoreCase("AVAILABLE"));

                return d;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /** GET AVAILABLE DELIVERY PERSONS */
    public List<String> getAvailableDeliveryNames() {

        List<String> list = new ArrayList<>();

        String sql = "SELECT name FROM delivery_persons WHERE status = 'AVAILABLE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("name"));
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }


    /** GET DELIVERY ID BY NAME */
    public int getDeliveryIdByName(String name) {

        String sql = "SELECT delivery_id FROM delivery_persons WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("delivery_id");
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return -1;
    }


    /** ASSIGN DELIVERY PERSON TO ORDER */
    public boolean assignDelivery(int orderId, int deliveryId) {

        String sql = "UPDATE orders SET delivery_id = ?, status='OUT_FOR_DELIVERY' WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deliveryId);
            stmt.setInt(2, orderId);

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                setDeliveryStatus(deliveryId, "BUSY");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    /** UPDATE DELIVERY PERSON STATUS */
    public boolean setDeliveryStatus(int deliveryId, String status) {

        String sql = "UPDATE delivery_persons SET status = ? WHERE delivery_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, deliveryId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
