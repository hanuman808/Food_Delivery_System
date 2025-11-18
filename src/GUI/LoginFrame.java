package GUI;

import models.Customer;
import models.Restaurant;
import models.DeliveryPerson;
import services.CustomerService;
import services.RestaurantService;
import services.DeliveryService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private JButton loginButton;
    private JButton registerLink;

    private CustomerService customerService;
    private RestaurantService restaurantService;
    private DeliveryService deliveryService;

    public LoginFrame() {

        customerService = new CustomerService();
        restaurantService = new RestaurantService();
        deliveryService = new DeliveryService();

        setTitle("Food Delivery - Login");
        setSize(420, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {

        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);

        userTypeCombo = new JComboBox<>(new String[]{"Customer", "Restaurant", "Delivery"});

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(40, 130, 255));
        loginButton.setForeground(Color.BLUE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));

        // 🔵 Register Link
        registerLink = new JButton("New user? Create an account");
        registerLink.setForeground(Color.BLUE);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void setupLayout() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Welcome Back!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(30, 90, 200));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Password
        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // User Type
        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Login As:"), gbc);

        gbc.gridx = 1;
        panel.add(userTypeCombo, gbc);

        // Login button
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        // Register link
        gbc.gridy++;
        panel.add(registerLink, gbc);

        add(panel);
    }

    private void setupEventHandlers() {
        getRootPane().setDefaultButton(loginButton);

        registerLink.addActionListener(e -> handleRegister());
        loginButton.addActionListener(e -> handleLogin());
    }


    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String type = (String) userTypeCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        /** CUSTOMER LOGIN **/
        if (type.equals("Customer")) {
            Customer c = customerService.authenticateCustomer(username, password);

            if (c != null) {
                JOptionPane.showMessageDialog(this, "Welcome, " + c.getUsername() + "!");
                dispose();
                new CustomerDashboard(c).setVisible(true);
                return;
            }
        }

        /** RESTAURANT LOGIN **/
        if (type.equals("Restaurant")) {
            Restaurant r = restaurantService.authenticateRestaurant(username, password);

            if (r != null) {
                JOptionPane.showMessageDialog(this, "Welcome, " + r.getName() + "!");
                dispose();
                new RestaurantDashboard(r).setVisible(true);
                return;
            }
        }

        /** DELIVERY LOGIN **/
        if (type.equals("Delivery")) {
            DeliveryPerson d = deliveryService.authenticateDelivery(username, password);

            if (d != null) {
                JOptionPane.showMessageDialog(this, "Welcome, " + d.getName() + "!");
                dispose();
                new DeliveryDashboard(d).setVisible(true);
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Invalid username or password!",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
    }


    private void handleRegister() {
        String type = (String) userTypeCombo.getSelectedItem();

        if (type.equals("Customer")) new CustomerRegistrationDialog(this).setVisible(true);
        else if (type.equals("Restaurant")) new RestaurantRegistrationDialog(this).setVisible(true);
        else JOptionPane.showMessageDialog(this, "Delivery staff cannot register here.");
    }
}
