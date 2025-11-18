package GUI;

import models.Order;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderDetailsDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;

    public OrderDetailsDialog(JFrame parent, int orderId, List<Order.OrderItem> items) {
        super(parent, "Order Details - Order #" + orderId, true);

        setSize(600, 400);
        setLocationRelativeTo(parent);
        
        initComponents();
        loadItems(items);
    }

    private void initComponents() {
        tableModel = new DefaultTableModel(
                new String[]{"Food Name", "Quantity", "Price", "Total"}, 0
        );

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeBtn);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadItems(List<Order.OrderItem> items) {

        tableModel.setRowCount(0);

        for (Order.OrderItem item : items) {
            double total = item.getPrice() * item.getQuantity();
            tableModel.addRow(new Object[]{
                    item.getFoodName(),
                    item.getQuantity(),
                    item.getPrice(),
                    total
            });
        }
    }
}
