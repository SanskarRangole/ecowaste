package com.example.ecowaste.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class BulkSaleModel {
    private String id;
    private String memberId;
    private String memberName;
    private Map<String, Double> wasteWeights; // e.g., {"Mobile": 10.5, "Laptop": 50.0}
    private List<String> pickupIds; // IDs of individual pickups included in this bulk sale
    private List<String> targetCenterIds;
    private Map<String, Double> centerOffers; // centerId -> offeredAmount
    private Map<String, String> centerStatuses; // centerId -> "PENDING", "ACCEPTED", "REJECTED"
    private String finalCenterId;
    private String status; // "SENT", "COMPLETED", "CANCELLED"
    private Date createdAt;

    public BulkSaleModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Map<String, Double> getWasteWeights() { return wasteWeights; }
    public void setWasteWeights(Map<String, Double> wasteWeights) { this.wasteWeights = wasteWeights; }
    public List<String> getPickupIds() { return pickupIds; }
    public void setPickupIds(List<String> pickupIds) { this.pickupIds = pickupIds; }
    public List<String> getTargetCenterIds() { return targetCenterIds; }
    public void setTargetCenterIds(List<String> targetCenterIds) { this.targetCenterIds = targetCenterIds; }
    public Map<String, Double> getCenterOffers() { return centerOffers; }
    public void setCenterOffers(Map<String, Double> centerOffers) { this.centerOffers = centerOffers; }
    public Map<String, String> getCenterStatuses() { return centerStatuses; }
    public void setCenterStatuses(Map<String, String> centerStatuses) { this.centerStatuses = centerStatuses; }
    public String getFinalCenterId() { return finalCenterId; }
    public void setFinalCenterId(String finalCenterId) { this.finalCenterId = finalCenterId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
