package models;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private String address;
    private List<Order> orders;

    public Customer() {
        super();
        this.orders = new ArrayList<>();
    }

    public Customer(String username, String email, String password, String phone, String address) {
        super(username, email, password, phone, UserType.CUSTOMER);
        this.address = address;
        this.orders = new ArrayList<>();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}