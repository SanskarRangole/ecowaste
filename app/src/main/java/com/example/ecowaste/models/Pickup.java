package com.example.ecowaste.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Pickup {
    private String id;
    private String userId;
    private String userName;
    private String userContact;
    private String memberId;
    private String centerId;
    private String address;
    private String area;
    private String preferredDate;
    private String preferredTime;
    private String scheduledDate;
    private String scheduledTime;
    private String pickedUpDate;
    private String pickedUpTime;
    private String status; 
    private String type;
    private String category;
    private String description;
    private String imageUrl;
    private int imageResId;
    private Object estimatedWeight;
    private Object baseAmount;
    private Object userAmount;
    private float userRating;
    private String pickupPin;
    @ServerTimestamp
    private Date createdAt;

    public Pickup() {}

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
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    @Exclude
    public double getEstimatedWeight() { return convertToDouble(estimatedWeight); }
    public void setEstimatedWeight(Object estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    
    public Object getEstimatedWeightRaw() { return estimatedWeight; }

    @Exclude
    public double getBaseAmount() { return convertToDouble(baseAmount); }
    public void setBaseAmount(Object baseAmount) { this.baseAmount = baseAmount; }

    @Exclude
    public double getUserAmount() { return convertToDouble(userAmount); }
    public void setUserAmount(Object userAmount) { this.userAmount = userAmount; }

    private double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); }
            catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }

    public float getUserRating() { return userRating; }
    public void setUserRating(float userRating) { this.userRating = userRating; }
    public String getPickupPin() { return pickupPin; }
    public void setPickupPin(String pickupPin) { this.pickupPin = pickupPin; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
