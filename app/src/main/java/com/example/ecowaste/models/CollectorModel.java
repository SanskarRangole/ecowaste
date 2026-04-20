package com.example.ecowaste.models;

import java.io.Serializable;

public class CollectorModel implements Serializable {
    private String collectorId;
    private String memberId;
    private String name;
    private String email;
    private String phone;
    private String role;

    public CollectorModel() {
    }

    public CollectorModel(String collectorId, String memberId, String name, String email, String phone) {
        this.collectorId = collectorId;
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = "collector";
    }

    public String getCollectorId() { return collectorId; }
    public void setCollectorId(String collectorId) { this.collectorId = collectorId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}