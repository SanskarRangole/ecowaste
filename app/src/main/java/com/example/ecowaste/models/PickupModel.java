package com.example.ecowaste.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.Map;

public class PickupModel {
    private String id;
    private String userId;
    private String userName;
    private String userContact;
    private String memberId;
    private String centerId;
    private String collectorId;
    private String collectorName;
    private String address;
    private String area;
    private String preferredDate;
    private String preferredTime;
    private String scheduledDate;
    private String scheduledTime;
    private String pickedUpDate;
    private String pickedUpTime;
    private String status;
    private String category;
    private String type;
    private String description;
    private String imageUrl;
    private Integer imageResId;

    private double estimatedWeight;
    private double baseAmount;
    private double userAmount;
    private double memberMargin;

    private String weightUnit = "kg";
    private boolean imageVerified = false;
    private String pickupPin;

    private Map<String, String> extraDetails;

    @ServerTimestamp
    private Date createdAt;

    public PickupModel() {
        this.imageResId = 0;
        this.estimatedWeight = 0.0;
        this.baseAmount = 0.0;
        this.userAmount = 0.0;
        this.memberMargin = 0.0;
        this.status = "PENDING_REVIEW";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getCenterId() { return centerId; }
    public void setCenterId(String centerId) { this.centerId = centerId; }
    public String getCollectorId() { return collectorId; }
    public void setCollectorId(String collectorId) { this.collectorId = collectorId; }
    public String getCollectorName() { return collectorName; }
    public void setCollectorName(String collectorName) { this.collectorName = collectorName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getPreferredDate() { return preferredDate; }
    public void setPreferredDate(String preferredDate) { this.preferredDate = preferredDate; }
    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }
    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getPickedUpDate() { return pickedUpDate; }
    public void setPickedUpDate(String pickedUpDate) { this.pickedUpDate = pickedUpDate; }
    public String getPickedUpTime() { return pickedUpTime; }
    public void setPickedUpTime(String pickedUpTime) { this.pickedUpTime = pickedUpTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getImageResId() { return imageResId; }
    public void setImageResId(Integer imageResId) { this.imageResId = imageResId; }
    
    public double getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(double estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    
    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }
    
    public double getUserAmount() { return userAmount; }
    public void setUserAmount(double userAmount) { this.userAmount = userAmount; }
    
    public double getMemberMargin() { return memberMargin; }
    public void setMemberMargin(double memberMargin) { this.memberMargin = memberMargin; }
    
    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }
    public boolean isImageVerified() { return imageVerified; }
    public void setImageVerified(boolean imageVerified) { this.imageVerified = imageVerified; }
    
    public String getPickupPin() { return pickupPin; }
    public void setPickupPin(String pickupPin) { this.pickupPin = pickupPin; }
    
    public Map<String, String> getExtraDetails() { return extraDetails; }
    public void setExtraDetails(Map<String, String> extraDetails) { this.extraDetails = extraDetails; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Helper for manual data parsing from Map
    @Exclude
    public void setEstimatedWeightObj(Object value) {
        this.estimatedWeight = convertToDouble(value);
    }
    @Exclude
    public void setBaseAmountObj(Object value) {
        this.baseAmount = convertToDouble(value);
    }
    @Exclude
    public void setUserAmountObj(Object value) {
        this.userAmount = convertToDouble(value);
    }
    @Exclude
    public void setMemberMarginObj(Object value) {
        this.memberMargin = convertToDouble(value);
    }

    private double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); }
            catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }
}