package GUI;

import models.Order;
import services.OrderService;

import javax.swing.*;
import java.awt.*;

public class DeliveryTrackingDialog extends JDialog {

    private int orderId;
    private JLabel statusLabel;
    private OrderService orderService;

    private Timer timer;

    public DeliveryTrackingDialog(Frame parent, int orderId) {
        super(parent, "Delivery Tracking - Order " + orderId, true);
        this.orderId = orderId;
        this.orderService = new OrderService();

        setSize(400, 250);
        setLocationRelativeTo(parent);

        statusLabel = new JLabel("Loading delivery status...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.CENTER);
        add(closeBtn, BorderLayout.SOUTH);

        startAutoRefresh();
    }

    /** Auto-refresh every 4 seconds */
    private void startAutoRefresh() {
        timer = new Timer(4000, e -> refreshStatus());
        timer.start();
        refreshStatus(); // first immediate update
    }

    private void refreshStatus() {
        Order order = orderService.getOrderById(orderId);

        if (order == null) {
            statusLabel.setText("Order not found!");
            return;
        }

        statusLabel.setText("Current Status: " + order.getStatus().name());
    }

    @Override
    public void dispose() {
        if (timer != null) timer.stop();
        super.dispose();
    }
}
