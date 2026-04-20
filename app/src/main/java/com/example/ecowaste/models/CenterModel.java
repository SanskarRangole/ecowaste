package com.example.ecowaste.models;

public class CenterModel {
    private String centerId;
    private String centerName;
    private String email;
    private String phone;
    private String licenseNumber;
    private String gstNumber;
    private String address;
    private String city;
    private String pincode;
    private String wasteTypes;
    private String operatingHours;
    private String contactPerson;
    private String contactPhone;
    private int processingCapacity;
    private String certifications;
    private String termsAndConditions;
    private double currentRate; // Daily dynamic rate
    private String status;
    private boolean verified;
    private long createdAt;

    public CenterModel() {}

    public CenterModel(String centerName, String email, String phone, String licenseNumber,
                       String address, String city, String pincode, String wasteTypes) {
        this.centerName = centerName;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
        this.wasteTypes = wasteTypes;
        this.status = "pending";
        this.verified = false;
        this.createdAt = System.currentTimeMillis();
        this.currentRate = 0.0;
    }

    // Getters and Setters
    public String getCenterId() { return centerId; }
    public void setCenterId(String centerId) { this.centerId = centerId; }
    public String getCenterName() { return centerName; }
    public void setCenterName(String centerName) { this.centerName = centerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getWasteTypes() { return wasteTypes; }
    public void setWasteTypes(String wasteTypes) { this.wasteTypes = wasteTypes; }
    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public int getProcessingCapacity() { return processingCapacity; }
    public void setProcessingCapacity(int processingCapacity) { this.processingCapacity = processingCapacity; }
    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }
    public double getCurrentRate() { return currentRate; }
    public void setCurrentRate(double currentRate) { this.currentRate = currentRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}