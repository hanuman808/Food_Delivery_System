package models;

public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String phone;
    private UserType userType;

    public enum UserType {
        CUSTOMER, RESTAURANT, DELIVERY
    }

    public User() {}

    public User(String username, String email, String password, String phone, UserType userType) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.userType = userType;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    // 🔥 Add these so DeliveryPerson works
    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }
}
