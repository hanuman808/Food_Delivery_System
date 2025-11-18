package GUI;

import models.DeliveryPerson;
import models.Order;
import services.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeliveryDashboard extends JFrame {

    private DeliveryPerson deliveryPerson;
    private OrderService orderService;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public DeliveryDashboard(DeliveryPerson deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
        this.orderService = new OrderService();

        setTitle("Delivery Dashboard - Welcome, " + deliveryPerson.getName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadOrders();
    }

    private void initComponents() {

        // --- Welcome Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel welcomeLabel =
                new JLabel("Welcome, " + deliveryPerson.getName() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.BLUE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.RED);
        logoutBtn.addActionListener(e -> logout());

        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(logoutBtn, BorderLayout.EAST);


        // --- Table ---
        tableModel = new DefaultTableModel(
                new String[]{"Order ID", "Customer ID", "Restaurant ID", "Status", "Total Amount", "Order Date"}, 0
        );
        ordersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);


        // --- Buttons ---
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadOrders());

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.addActionListener(e -> updateOrderStatus());

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(updateStatusBtn);


        // --- Add to Frame ---
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void logout() {
        int option = JOptionPane.showConfirmDialog(
                this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void loadOrders() {
        tableModel.setRowCount(0);

        List<Order> orders = orderService.getOrdersByDeliveryPerson(deliveryPerson.getUserId());

        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getRestaurantId(),
                    order.getStatus().name(),
                    order.getTotalAmount(),
                    order.getOrderDate()
            });
        }
    }

    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an order to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);

        Order.OrderStatus[] statuses = Order.OrderStatus.values();
        Order.OrderStatus newStatus = (Order.OrderStatus) JOptionPane.showInputDialog(
                this,
                "Select new status:",
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                statuses[0]
        );

        if (newStatus != null) {
            boolean updated = orderService.updateOrderStatus(orderId, newStatus);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Order status updated.");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
