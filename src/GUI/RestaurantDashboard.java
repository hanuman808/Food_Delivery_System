package GUI;

import models.Order;
import models.Order.OrderItem;
import models.Order.OrderStatus;
import models.Restaurant;
import services.DeliveryService;
import services.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RestaurantDashboard extends JFrame {

    private Restaurant restaurant;
    private OrderService orderService;
    private DeliveryService deliveryService;

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterBox;
    private Timer autoRefreshTimer;
    private boolean playAlert = true; // avoid repeated beep spam

    public RestaurantDashboard(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.orderService = new OrderService();
        this.deliveryService = new DeliveryService();

        setTitle("Restaurant Dashboard - " + restaurant.getName());
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        loadOrders();
        startAutoRefresh();
    }

    private void initializeComponents() {
        // table columns: Order ID, Customer ID, Status, Total, Date
        tableModel = new DefaultTableModel(
                new String[]{"Order ID", "Customer ID", "Status", "Total (₹)", "Order Date"}, 0
        );

        ordersTable = new JTable(tableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // renderer for color-coding rows by status
        ordersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                // preserve selection colors
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                    return c;
                }

                Object statusObj = table.getValueAt(row, 2);
                String status = statusObj == null ? "" : statusObj.toString();

                switch (status) {
                    case "PENDING":
                        c.setBackground(new Color(255, 249, 196)); // soft yellow
                        break;
                    case "CONFIRMED":
                        c.setBackground(new Color(255, 236, 179)); // slightly warm
                        break;
                    case "PREPARING":
                        c.setBackground(new Color(187, 222, 251)); // light blue
                        break;
                    case "READY_FOR_PICKUP":
                        c.setBackground(new Color(200, 230, 201)); // light green
                        break;
                    case "OUT_FOR_DELIVERY":
                        c.setBackground(new Color(255, 224, 178)); // light orange
                        break;
                    case "DELIVERED":
                        c.setBackground(Color.LIGHT_GRAY);
                        break;
                    case "CANCELLED":
                        c.setBackground(new Color(244, 199, 195)); // light red
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                }

                c.setForeground(Color.BLACK);
                return c;
            }
        });

        // filter box (status)
        String[] filterOptions = {
                "ALL", "PENDING", "CONFIRMED", "PREPARING",
                "READY_FOR_PICKUP", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
        };
        filterBox = new JComboBox<>(filterOptions);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, " + restaurant.getName() + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 20));
        welcome.setForeground(new Color(8, 97, 153));
        headerPanel.add(welcome, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.RED);
        logoutBtn.addActionListener(e -> logout());
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // center table
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        // bottom controls
        JPanel btnPanel = new JPanel();

        JButton refreshBtn = new JButton("Refresh");
        JButton viewDetailsBtn = new JButton("View Details");
        JButton acceptBtn = new JButton("Accept Order");
        JButton rejectBtn = new JButton("Reject Order");
        JButton markPreparingBtn = new JButton("Mark PREPARING");
        JButton markReadyBtn = new JButton("Mark READY");
        JButton assignDeliveryBtn = new JButton("Assign Delivery");
        JButton smartAcceptBtn = new JButton("Smart Accept");

        btnPanel.add(new JLabel("Filter:"));
        btnPanel.add(filterBox);
        btnPanel.add(refreshBtn);
        btnPanel.add(viewDetailsBtn);
        btnPanel.add(acceptBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(markPreparingBtn);
        btnPanel.add(markReadyBtn);
        btnPanel.add(assignDeliveryBtn);
        btnPanel.add(smartAcceptBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // events
        refreshBtn.addActionListener(e -> loadOrders());
        viewDetailsBtn.addActionListener(e -> viewOrderDetails());
        acceptBtn.addActionListener(e -> updateStatus(OrderStatus.CONFIRMED));
        rejectBtn.addActionListener(e -> updateStatus(OrderStatus.CANCELLED));
        markPreparingBtn.addActionListener(e -> updateStatus(OrderStatus.PREPARING));
        markReadyBtn.addActionListener(e -> updateStatus(OrderStatus.READY_FOR_PICKUP));
        assignDeliveryBtn.addActionListener(e -> assignDeliveryPerson());
        smartAcceptBtn.addActionListener(e -> smartAccept());

        filterBox.addActionListener(e -> loadOrders());
    }

    private void logout() {
        int option = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            if (autoRefreshTimer != null) autoRefreshTimer.stop();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // Load orders and apply status filter
    private void loadOrders() {
        // preserve row count to detect new orders for beep
        int oldCount = tableModel.getRowCount();

        tableModel.setRowCount(0);

        String filter = (String) filterBox.getSelectedItem();
        if (filter == null) filter = "ALL";

        List<Order> orders = orderService.getOrdersByRestaurant(restaurant.getUserId());

        for (Order order : orders) {
            if (!"ALL".equals(filter) && order.getStatus() != null && !order.getStatus().name().equals(filter)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                    String.format("%.2f", order.getTotalAmount()),
                    order.getOrderDate()
            });
        }

        // beep if new rows and playAlert true
        int newCount = tableModel.getRowCount();
        if (newCount > oldCount && playAlert) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private int getSelectedOrderId() {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return -1;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    /** VIEW ITEMS using OrderService.getOrderItems(orderId) */
    private void viewOrderDetails() {
        int orderId = getSelectedOrderId();
        if (orderId == -1) return;

        List<OrderItem> list = orderService.getOrderItems(orderId);

        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items found for this order.");
            return;
        }

        StringBuilder sb = new StringBuilder("Order ID: " + orderId + "\n\n");
        for (OrderItem it : list) {
            // OrderItem must expose name via getFoodName(); if not, fallback to foodId
            String name;
            try {
                name = (String) it.getClass().getMethod("getFoodName").invoke(it);
            } catch (Exception ex) {
                name = "FoodID:" + it.getFoodId();
            }
            sb.append(name)
              .append(" — Qty: ").append(it.getQuantity())
              .append(" — ₹").append(String.format("%.2f", it.getPrice())).append("\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Update order status */
    private void updateStatus(OrderStatus status) {
        int orderId = getSelectedOrderId();
        if (orderId == -1) return;

        boolean ok = orderService.updateOrderStatus(orderId, status);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Order updated to: " + status);
            loadOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Assign delivery person (manual) */
    private void assignDeliveryPerson() {
        int orderId = getSelectedOrderId();
        if (orderId == -1) return;

        List<String> deliveryList = deliveryService.getAvailableDeliveryNames();
        if (deliveryList == null || deliveryList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No delivery persons available.");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select Delivery Person:",
                "Assign Delivery",
                JOptionPane.QUESTION_MESSAGE,
                null,
                deliveryList.toArray(),
                deliveryList.get(0)
        );

        if (selected == null) return;

        int deliveryId = deliveryService.getDeliveryIdByName(selected);
        if (deliveryId <= 0) {
            JOptionPane.showMessageDialog(this, "Invalid delivery person selected.");
            return;
        }

        boolean ok = deliveryService.assignDelivery(orderId, deliveryId);
        if (ok) {
            orderService.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY);
            JOptionPane.showMessageDialog(this, "Assigned to " + selected);
            loadOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Assignment failed.");
        }
    }

    /** Smart accept: accept + auto assign first available delivery */
    private void smartAccept() {
        int orderId = getSelectedOrderId();
        if (orderId == -1) return;

        List<String> list = deliveryService.getAvailableDeliveryNames();
        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No delivery persons available.");
            return;
        }

        String selected = list.get(0); // pick first available
        int deliveryId = deliveryService.getDeliveryIdByName(selected);
        if (deliveryId <= 0) {
            JOptionPane.showMessageDialog(this, "Failed to find delivery ID.");
            return;
        }

        boolean assigned = deliveryService.assignDelivery(orderId, deliveryId);
        if (assigned) {
            orderService.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY);
            JOptionPane.showMessageDialog(this, "Accepted and assigned to: " + selected);
            loadOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Smart accept failed.");
        }
    }

    /** Auto-refresh orders every 5 seconds */
    private void startAutoRefresh() {
        // stop existing
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) autoRefreshTimer.stop();

        autoRefreshTimer = new Timer(5000, e -> {
            // remember current selection and scroll position optionally
            int selectedRow = ordersTable.getSelectedRow();
            loadOrders();

            // restore selection if possible
            if (selectedRow >= 0 && selectedRow < ordersTable.getRowCount()) {
                ordersTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });
        autoRefreshTimer.setInitialDelay(5000);
        autoRefreshTimer.start();
    }

    // stop timer when closing window
    @Override
    public void dispose() {
        if (autoRefreshTimer != null) autoRefreshTimer.stop();
        super.dispose();
    }
}
