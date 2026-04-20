package com.example.ecowaste.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class NotificationModel {
    private String id;
    private String userId;
    private String title;
    private String message;
    private boolean isRead;
    private String saleId; // Reference to bulk sale if applicable
    private String type; // "NORMAL", "BULK_OFFER"
    @ServerTimestamp
    private Date timestamp;

    public NotificationModel() {
        this.type = "NORMAL";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
