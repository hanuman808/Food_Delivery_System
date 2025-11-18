package GUI;

import models.Customer;
import services.CustomerService;

import javax.swing.*;
import java.awt.*;

public class CustomerRegistrationDialog extends JDialog {

    private JTextField usernameField, emailField, phoneField, addressField;
    private JPasswordField passwordField;

    private JButton registerButton, cancelButton, loginButton;
    private boolean registered = false;

    public CustomerRegistrationDialog(Frame parent) {
        super(parent, "Customer Registration", true);
        setSize(430, 420);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
        setupLayout();
    }

    private void initComponents() {

        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        phoneField = new JTextField(20);
        addressField = new JTextField(20);

        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");

        // NEW LOGIN BUTTON
        loginButton = new JButton("Already Registered? Login");
        loginButton.setForeground(Color.BLUE);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        registerButton.addActionListener(e -> registerCustomer());
        cancelButton.addActionListener(e -> dispose());

        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void setupLayout() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Customer Registration", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.BLUE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        addRow(panel, gbc, "Username:", usernameField);
        addRow(panel, gbc, "Email:", emailField);
        addRow(panel, gbc, "Password:", passwordField);
        addRow(panel, gbc, "Phone:", phoneField);
        addRow(panel, gbc, "Address:", addressField);

        // Buttons
        JPanel btnPanel = new JPanel();
        btnPanel.add(registerButton);
        btnPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        // NEW LOGIN BUTTON (Under buttons)
        gbc.gridy++;
        panel.add(loginButton, gbc);

        add(panel);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);

        gbc.gridy++;
    }

    private void registerCustomer() {

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()
                || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer customer = new Customer(username, email, password, phone, address);
        CustomerService service = new CustomerService();
        boolean success = service.registerCustomer(customer);

        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            registered = true;
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }
}
