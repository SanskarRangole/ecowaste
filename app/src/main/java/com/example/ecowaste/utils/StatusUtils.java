package com.example.ecowaste.utils;

public class StatusUtils {
    public static String getDisplayStatus(String rawStatus) {
        if (rawStatus == null) return "Pending";
        
        switch (rawStatus) {
            case "PENDING_REVIEW":
                return "Waiting for Review";
            case "OFFER_SENT":
                return "Price Offered";
            case "USER_ACCEPTED":
                return "Price Accepted";
            case "USER_REJECTED":
                return "Price Rejected";
            case "PICKUP_SCHEDULED":
                return "Agent Assigned";
            case "MISMATCH_REPORTED":
                return "Item Discrepancy";
            case "PICKED_UP":
                return "Picked Up";
            case "COLLECTED":
                return "Collected & Paid";
            case "BULK_REQUESTED":
                return "Warehouse Stock";
            case "ACCEPTED_BY_COMPANY":
                return "Bulk Offer Received";
            case "COMPLETED":
                return "Cycle Completed";
            case "REJECTED":
                return "Rejected";
            case "CANCELLED":
                return "Cancelled";
            default:
                return rawStatus;
        }
    }
}
