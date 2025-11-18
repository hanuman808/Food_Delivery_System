package models;

public class DeliveryPerson extends User {

    private boolean available;
    private String vehicleType;
    private String licenseNumber;

    public DeliveryPerson() {
        super();
        this.setUserType(UserType.DELIVERY);
        this.available = true;
    }

    public DeliveryPerson(String username, String email, String password, String phone,
                          String vehicleType, String licenseNumber) {
        super(username, email, password, phone, UserType.DELIVERY);
        this.available = true;
        this.vehicleType = vehicleType;
        this.licenseNumber = licenseNumber;
    }

    // availability
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // optional fields
    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}
