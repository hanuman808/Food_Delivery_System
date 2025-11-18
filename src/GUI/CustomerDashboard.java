package GUI;

import models.Cart;
import models.Customer;
import models.FoodItem;
import models.Order;
import services.FoodService;
import services.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JFrame {

    private Customer customer;
    private FoodService foodService;
    private OrderService orderService;

    private JTable foodTable;
    private JTable cartTable;
    private JTable orderTable;

    private DefaultTableModel foodTableModel;
    private DefaultTableModel cartTableModel;
    private DefaultTableModel orderTableModel;

    private JLabel totalLabel;
    private JLabel welcomeLabel;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        this.foodService = new FoodService();
        this.orderService = new OrderService();

        setTitle("Customer Dashboard - Welcome, " + customer.getUsername());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {

        // --- WELCOME LABEL ---
        welcomeLabel = new JLabel("Welcome, " + customer.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLUE);

        foodTableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Description", "Price", "Category"}, 0);
        foodTable = new JTable(foodTableModel);

        cartTableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Price", "Quantity", "Total"}, 0);
        cartTable = new JTable(cartTableModel);

        orderTableModel = new DefaultTableModel(
                new String[]{"Order ID", "Date", "Status", "Total"}, 0);
        orderTable = new JTable(orderTableModel);

        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
    }

    private void setupLayout() {

        setLayout(new BorderLayout());

        // --- TOP HEADER PANEL WITH LOGOUT ---
        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.RED);
        logoutBtn.addActionListener(e -> logout());

        headerPanel.add(logoutBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // TOP FOOD PANEL
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Available Food Items"));
        topPanel.add(new JScrollPane(foodTable), BorderLayout.CENTER);

        JPanel foodBtnPanel = new JPanel();
        JButton addToCartBtn = new JButton("Add to Cart");
        JButton refreshFoodBtn = new JButton("Refresh");

        foodBtnPanel.add(addToCartBtn);
        foodBtnPanel.add(refreshFoodBtn);

        topPanel.add(foodBtnPanel, BorderLayout.SOUTH);

        // CART PANEL
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setBorder(BorderFactory.createTitledBorder("Your Cart"));
        midPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartBtnPanel = new JPanel();
        JButton placeOrderBtn = new JButton("Place Order");
        JButton clearCartBtn = new JButton("Clear Cart");

        cartBtnPanel.add(totalLabel);
        cartBtnPanel.add(placeOrderBtn);
        cartBtnPanel.add(clearCartBtn);

        midPanel.add(cartBtnPanel, BorderLayout.SOUTH);

        // ORDER HISTORY PANEL
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Order History"));
        bottomPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel orderBtnPanel = new JPanel();
        JButton refreshOrdersBtn = new JButton("Refresh Orders");
        JButton orderDetailsBtn = new JButton("Order Details");
        JButton trackOrderBtn = new JButton("Track Delivery");

        orderBtnPanel.add(refreshOrdersBtn);
        orderBtnPanel.add(orderDetailsBtn);
        orderBtnPanel.add(trackOrderBtn);

        bottomPanel.add(orderBtnPanel, BorderLayout.SOUTH);

        // SPLIT PANELS
        JSplitPane splitTopMiddle = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, midPanel);
        splitTopMiddle.setResizeWeight(0.45);

        JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTopMiddle, bottomPanel);
        splitAll.setResizeWeight(0.75);

        add(splitAll, BorderLayout.CENTER);

        // EVENT HANDLERS
        addToCartBtn.addActionListener(e -> addToCart());
        refreshFoodBtn.addActionListener(e -> loadFoodItems());
        placeOrderBtn.addActionListener(e -> placeOrder());
        clearCartBtn.addActionListener(e -> clearCart());
        refreshOrdersBtn.addActionListener(e -> loadOrders());
        orderDetailsBtn.addActionListener(e -> viewOrderDetails());
        trackOrderBtn.addActionListener(e -> trackDelivery());
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void loadData() {
        loadFoodItems();
        loadCartItems();
        loadOrders();
    }

    private void loadFoodItems() {
        foodTableModel.setRowCount(0);
        List<FoodItem> items = foodService.getAllFoodItems();

        for (FoodItem item : items) {
            foodTableModel.addRow(new Object[]{
                    item.getFoodId(),
                    item.getName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.getCategory()
            });
        }
    }

    private void loadCartItems() {
        cartTableModel.setRowCount(0);
        List<Cart> items = foodService.getCartItems(customer.getUserId());

        double total = 0;

        for (Cart item : items) {
            double itemTotal = item.getFoodItem().getPrice() * item.getQuantity();
            total += itemTotal;

            cartTableModel.addRow(new Object[]{
                    item.getFoodId(),
                    item.getFoodItem().getName(),
                    item.getFoodItem().getPrice(),
                    item.getQuantity(),
                    itemTotal
            });
        }

        totalLabel.setText("Total: ₹" + total);
    }

    private void loadOrders() {
        orderTableModel.setRowCount(0);
        List<Order> orders = orderService.getOrdersByCustomer(customer.getUserId());

        for (Order order : orders) {
            orderTableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getOrderDate(),
                    order.getStatus().name(),
                    order.getTotalAmount()
            });
        }
    }

    private void addToCart() {
        int row = foodTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a food item.");
            return;
        }

        int foodId = (int) foodTableModel.getValueAt(row, 0);
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity:");

        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();

            if (foodService.addToCart(customer.getUserId(), foodId, qty)) {
                JOptionPane.showMessageDialog(this, "Added to cart!");
                loadCartItems();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!");
        }
    }

    private void placeOrder() {
        List<Cart> cartItems = foodService.getCartItems(customer.getUserId());

        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        String address = JOptionPane.showInputDialog(this, "Enter delivery address:", customer.getAddress());

        if (address == null || address.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address required!");
            return;
        }

        double total = 0;
        int restaurantId = cartItems.get(0).getFoodItem().getRestaurantId();

        Order order = new Order(customer.getUserId(), restaurantId, 0, address);

        for (Cart c : cartItems) {
            double price = c.getFoodItem().getPrice();
            total += price * c.getQuantity();
            order.getItems().add(new Order.OrderItem(c.getFoodId(), c.getQuantity(), price));
        }

        order.setTotalAmount(total);

        boolean success = orderService.placeOrder(order, cartItems);

        if (success) {
            JOptionPane.showMessageDialog(this, "Order placed!");
            foodService.clearCart(customer.getUserId());
            loadCartItems();
            loadOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Order failed!");
        }
    }

    private void clearCart() {
        if (foodService.clearCart(customer.getUserId())) {
            JOptionPane.showMessageDialog(this, "Cart cleared.");
            loadCartItems();
        }
    }

    private void viewOrderDetails() {
        int row = orderTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order.");
            return;
        }

        int orderId = (int) orderTableModel.getValueAt(row, 0);

        List<Order.OrderItem> items = orderService.getOrderItems(orderId);

        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items found!");
            return;
        }

        OrderDetailsDialog dialog = new OrderDetailsDialog(this, orderId, items);
        dialog.setVisible(true);
    }

    private void trackDelivery() {
        int row = orderTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order to track.");
            return;
        }

        int orderId = (int) orderTableModel.getValueAt(row, 0);

        DeliveryTrackingDialog dialog = new DeliveryTrackingDialog(this, orderId);
        dialog.setVisible(true);
    }
}
