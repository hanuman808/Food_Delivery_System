package GUI;




import models.Restaurant;
import services.RestaurantService;

import javax.swing.*;
import java.awt.*;

public class RestaurantRegistrationDialog extends JDialog {
    private JTextField usernameField, emailField, passwordField, phoneField, nameField, addressField, cuisineTypeField;
    private JButton registerButton, cancelButton;
    private boolean registered = false;

    public RestaurantRegistrationDialog(Frame parent) {
        super(parent, "Restaurant Registration", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(8, 2, 10, 10));
        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Phone:"));
        phoneField = new JTextField();
        add(phoneField);

        add(new JLabel("Restaurant Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Address:"));
        addressField = new JTextField();
        add(addressField);

        add(new JLabel("Cuisine Type:"));
        cuisineTypeField = new JTextField();
        add(cuisineTypeField);

        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        add(registerButton);
        add(cancelButton);

        registerButton.addActionListener(e -> registerRestaurant());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(getParent());
    }

    private void registerRestaurant() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String cuisineType = cuisineTypeField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                phone.isEmpty() || name.isEmpty() || address.isEmpty() || cuisineType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Restaurant restaurant = new Restaurant(username, email, password, phone, name, address, cuisineType);
        RestaurantService service = new RestaurantService();
        boolean success = service.registerRestaurant(restaurant);

        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            registered = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }
}
