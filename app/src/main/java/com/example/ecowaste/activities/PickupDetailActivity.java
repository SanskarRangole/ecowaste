package com.example.ecowaste.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.example.ecowaste.R;
import com.example.ecowaste.models.CollectorModel;
import com.example.ecowaste.models.NotificationModel;
import com.example.ecowaste.models.PickupModel;
import com.example.ecowaste.utils.StatusUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PickupDetailActivity extends AppCompatActivity {

    private TextView tvPickupId, tvUserName, tvUserContact, tvWasteType, tvAddress, tvDescription, tvStatus, tvAmount, tvExtraDetails, tvUserPin, tvScheduleInfo, tvMemberMarginDisplay, tvSelectedCollector, tvMismatchAlert;
    private ImageView ivWasteImage, btnBack;
    private View layoutContact; 
    private LinearLayout layoutMemberAction, layoutMemberButtons, layoutCenterAction, layoutVerifyPickup, layoutUserDecisionButtons, layoutMismatchReport;
    private EditText etBaseRate, etVerifyPin, etMismatchReason;
    private Button btnAssignToCenter, btnMemberAccept, btnMemberReject, btnCenterAccept, btnCenterReject, btnVerifyPinBtn, btnCancelRequest, btnUserAcceptOffer, btnUserRejectOffer, btnReportMismatch;
    private CardView cardAmount, cardPin, cardMismatch;

    private FirebaseFirestore db;
    private String currentUserId;
    private String userRole;
    private String collectorIdFromIntent;
    
    private String selectedCollectorId;
    private String selectedCollectorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_detail);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        collectorIdFromIntent = getIntent().getStringExtra("collectorId");

        String pickupId = getIntent().getStringExtra("pickupId");

        initViews();
        getUserRoleAndLoadDetails(pickupId);
    }

    private void initViews() {
        tvPickupId = findViewById(R.id.tvPickupId);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserContact = findViewById(R.id.tvUserContact);
        tvWasteType = findViewById(R.id.tvWasteType);
        tvAddress = findViewById(R.id.tvAddress);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);
        tvAmount = findViewById(R.id.tvAmount);
        tvExtraDetails = findViewById(R.id.tvExtraDetails);
        tvUserPin = findViewById(R.id.tvUserPin);
        tvScheduleInfo = findViewById(R.id.tvScheduleInfo);
        tvMemberMarginDisplay = findViewById(R.id.tvMemberMarginDisplay);
        tvSelectedCollector = findViewById(R.id.tvSelectedCollector);
        ivWasteImage = findViewById(R.id.ivWasteImage);
        btnBack = findViewById(R.id.btnBack);
        layoutContact = findViewById(R.id.layoutContact);
        
        cardAmount = findViewById(R.id.cardAmount);
        cardPin = findViewById(R.id.cardPin);
        cardMismatch = findViewById(R.id.cardMismatch);
        tvMismatchAlert = findViewById(R.id.tvMismatchAlert);

        layoutMemberAction = findViewById(R.id.layoutMemberAction);
        layoutMemberButtons = findViewById(R.id.layoutMemberButtons);
        btnMemberAccept = findViewById(R.id.btnMemberAccept);
        btnMemberReject = findViewById(R.id.btnMemberReject);
        btnAssignToCenter = findViewById(R.id.btnAssignCenter);
        
        layoutCenterAction = findViewById(R.id.layoutCenterAction);
        etBaseRate = findViewById(R.id.etBaseRate);
        btnCenterAccept = findViewById(R.id.btnCenterAccept);
        btnCenterReject = findViewById(R.id.btnCenterReject);

        layoutVerifyPickup = findViewById(R.id.layoutVerifyPickup);
        etVerifyPin = findViewById(R.id.etVerifyPin);
        btnVerifyPinBtn = findViewById(R.id.btnVerifyPin);
        
        btnCancelRequest = findViewById(R.id.btnCancelRequest);

        layoutUserDecisionButtons = findViewById(R.id.layoutUserDecisionButtons);
        btnUserAcceptOffer = findViewById(R.id.btnUserAcceptOffer);
        btnUserRejectOffer = findViewById(R.id.btnUserRejectOffer);

        layoutMismatchReport = findViewById(R.id.layoutMismatchReport);
        etMismatchReason = findViewById(R.id.etMismatchReason);
        btnReportMismatch = findViewById(R.id.btnReportMismatch);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void getUserRoleAndLoadDetails(String pickupId) {
        if (pickupId == null) return;

        if (collectorIdFromIntent != null) {
            userRole = "collector";
            loadPickupDetails(pickupId);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && "recycleit50@gmail.com".equalsIgnoreCase(currentUser.getEmail())) {
            userRole = "member";
            loadPickupDetails(pickupId);
            return;
        }

        if (currentUserId == null) return;

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    userRole = documentSnapshot.getString("role");
                    loadPickupDetails(pickupId);
                });
    }

    private void loadPickupDetails(String pickupId) {
        db.collection("pickups").document(pickupId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        PickupModel pickup = documentSnapshot.toObject(PickupModel.class);
                        if (pickup != null) {
                            pickup.setId(documentSnapshot.getId());
                            displayDetails(pickup);
                        }
                    }
                });
    }

    private void displayDetails(PickupModel pickup) {
        tvPickupId.setText("#ID-" + (pickup.getId().length() > 8 ? pickup.getId().substring(0, 8) : pickup.getId()));
        tvWasteType.setText(pickup.getCategory() + " - " + pickup.getType());
        tvDescription.setText(pickup.getDescription());
        
        tvStatus.setText(StatusUtils.getDisplayStatus(pickup.getStatus()).toUpperCase());

        if (pickup.getImageUrl() != null && !pickup.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(pickup.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(ivWasteImage);
            
            ivWasteImage.setOnClickListener(v -> {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", pickup.getImageUrl());
                intent.putExtra("title", pickup.getType());
                startActivity(intent);
            });
        } else {
            int imageRes = getRealisticIcon(pickup.getCategory());
            ivWasteImage.setImageResource(imageRes);
        }

        if (pickup.getExtraDetails() != null && !pickup.getExtraDetails().isEmpty()) {
            StringBuilder sb = new StringBuilder("Specifications:\n");
            for (Map.Entry<String, String> entry : pickup.getExtraDetails().entrySet()) {
                sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            tvExtraDetails.setText(sb.toString());
            tvExtraDetails.setVisibility(View.VISIBLE);
        } else {
            tvExtraDetails.setVisibility(View.GONE);
        }

        if (pickup.getScheduledDate() != null) {
            tvScheduleInfo.setText("Scheduled: " + pickup.getScheduledDate() + " " + pickup.getScheduledTime());
            tvScheduleInfo.setVisibility(View.VISIBLE);
        } else {
            tvScheduleInfo.setVisibility(View.GONE);
        }

        if ("center".equalsIgnoreCase(userRole)) {
            layoutContact.setVisibility(View.GONE);
            tvUserName.setText("User: [Protected]");
            tvUserContact.setText("Contact: [Protected]");
            tvAddress.setText("Area: " + (pickup.getArea() != null ? pickup.getArea() : "Privacy Protected"));
            cardPin.setVisibility(View.GONE);
        } else {
            layoutContact.setVisibility(View.VISIBLE);
            tvUserName.setText(pickup.getUserName() != null ? pickup.getUserName() : "N/A");
            tvUserContact.setText("Phone: " + (pickup.getUserContact() != null ? pickup.getUserContact() : "N/A"));
            tvAddress.setText(pickup.getAddress() != null ? pickup.getAddress() : "N/A");
            
            // Fix placeholder logic: show masking until revealing secure PIN
            if ("user".equalsIgnoreCase(userRole) && pickup.getPickupPin() != null && !"PICKED_UP".equals(pickup.getStatus()) && !"COMPLETED".equals(pickup.getStatus())) {
                if ("PICKUP_SCHEDULED".equals(pickup.getStatus())) {
                    tvUserPin.setText(pickup.getPickupPin());
                } else {
                    tvUserPin.setText("••••");
                }
                cardPin.setVisibility(View.VISIBLE);
            } else {
                cardPin.setVisibility(View.GONE);
            }
        }

        if (pickup.getUserAmount() > 0) {
            tvAmount.setText("₹" + String.format(Locale.getDefault(), "%.2f", pickup.getUserAmount()));
            cardAmount.setVisibility(View.VISIBLE);
        } else {
            tvAmount.setText("Awaiting Offer");
            cardAmount.setVisibility(View.VISIBLE);
        }

        setupRoleBasedActions(pickup);
    }

    private int getRealisticIcon(String category) {
        if (category == null) return R.drawable.placeholder;
        switch (category) {
            case "Mobile": return R.drawable.ic_mobile_realistic;
            case "Laptop": return R.drawable.ic_laptop_realistic;
            case "Television": return R.drawable.ic_tv_realistic;
            case "Battery": return R.drawable.ic_battery_realistic;
            case "Charger": return R.drawable.ic_charger_realistic;
            default: return R.drawable.ic_other_realistic;
        }
    }

    private void setupRoleBasedActions(PickupModel pickup) {
        layoutMemberAction.setVisibility(View.GONE);
        layoutCenterAction.setVisibility(View.GONE);
        layoutVerifyPickup.setVisibility(View.GONE);
        btnCancelRequest.setVisibility(View.GONE);
        layoutUserDecisionButtons.setVisibility(View.GONE);
        layoutMismatchReport.setVisibility(View.GONE);
        cardMismatch.setVisibility(View.GONE);

        if ("PICKED_UP".equals(pickup.getStatus()) || "COMPLETED".equals(pickup.getStatus()) || "CANCELLED".equals(pickup.getStatus()) || "REJECTED".equals(pickup.getStatus())) return;

        if ("user".equalsIgnoreCase(userRole)) {
            if ("PENDING_REVIEW".equals(pickup.getStatus())) {
                btnCancelRequest.setVisibility(View.VISIBLE);
                btnCancelRequest.setOnClickListener(v -> updateStatus(pickup, "CANCELLED", "Cancelled", "Request cancelled by user."));
            } else if ("OFFER_SENT".equals(pickup.getStatus())) {
                layoutUserDecisionButtons.setVisibility(View.VISIBLE);
                btnUserAcceptOffer.setOnClickListener(v -> updateStatus(pickup, "USER_ACCEPTED", "Offer Accepted", "You accepted the rate. Waiting for schedule."));
                btnUserRejectOffer.setOnClickListener(v -> updateStatus(pickup, "USER_REJECTED", "Offer Rejected", "You rejected the offer."));
            }
            if ("MISMATCH_REPORTED".equals(pickup.getStatus())) {
                cardMismatch.setVisibility(View.VISIBLE);
                tvMismatchAlert.setText("Admin is reviewing a discrepancy reported by the field agent.");
            }
        }

        if ("member".equalsIgnoreCase(userRole)) {
            if ("PENDING_REVIEW".equals(pickup.getStatus())) {
                layoutCenterAction.setVisibility(View.VISIBLE);
                etBaseRate.setHint("Base Rate (Offer to User)");
                btnCenterAccept.setText("Send Offer");
                btnCenterAccept.setOnClickListener(v -> sendMemberOffer(pickup));
                btnCenterReject.setText("Reject Request");
                btnCenterReject.setOnClickListener(v -> updateStatus(pickup, "REJECTED", "Rejected", "Member rejected your request."));
            } else if ("USER_ACCEPTED".equals(pickup.getStatus())) {
                layoutCenterAction.setVisibility(View.VISIBLE);
                btnCenterAccept.setText("Schedule Pickup");
                btnCenterAccept.setOnClickListener(v -> showScheduleDialog(pickup));
                etBaseRate.setVisibility(View.GONE);
            } else if ("MISMATCH_REPORTED".equals(pickup.getStatus())) {
                cardMismatch.setVisibility(View.VISIBLE);
                tvMismatchAlert.setText("Reason: " + (pickup.getExtraDetails() != null ? pickup.getExtraDetails().get("mismatchReason") : "N/A"));
                
                layoutCenterAction.setVisibility(View.VISIBLE);
                etBaseRate.setVisibility(View.VISIBLE);
                etBaseRate.setHint("Corrected Rate (₹)");
                btnCenterAccept.setText("Resolve & Resume");
                btnCenterAccept.setOnClickListener(v -> resolveMismatch(pickup));
                btnCenterReject.setText("Cancel Request");
                btnCenterReject.setOnClickListener(v -> updateStatus(pickup, "CANCELLED", "Mismatch - Cancelled", "Request cancelled due to discrepancy."));
            }
        }

        if ("collector".equalsIgnoreCase(userRole)) {
            if ("PICKUP_SCHEDULED".equals(pickup.getStatus())) {
                layoutVerifyPickup.setVisibility(View.VISIBLE);
                tvSelectedCollector.setVisibility(View.GONE);
                etVerifyPin.setHint("Enter Customer PIN");
                btnVerifyPinBtn.setText("Verify & Collect");
                btnVerifyPinBtn.setOnClickListener(v -> verifyPinAndCollect(pickup));
                
                layoutMismatchReport.setVisibility(View.VISIBLE);
                btnReportMismatch.setOnClickListener(v -> reportMismatch(pickup));
            }
        }
    }

    private void resolveMismatch(PickupModel pickup) {
        String rate = etBaseRate.getText().toString().trim();
        if (TextUtils.isEmpty(rate)) {
            etBaseRate.setError("Enter corrected rate");
            return;
        }
        double amt = Double.parseDouble(rate);
        db.collection("pickups").document(pickup.getId())
                .update("status", "OFFER_SENT", "userAmount", amt)
                .addOnSuccessListener(aVoid -> {
                    sendPushAndInAppNotification(pickup.getUserId(), "Updated Offer", "Member updated the offer due to item discrepancy: ₹" + rate, pickup.getId());
                    Toast.makeText(this, "Mismatch resolved! New offer sent.", Toast.LENGTH_SHORT).show();
                });
    }

    private void reportMismatch(PickupModel pickup) {
        String reason = etMismatchReason.getText().toString().trim();
        if (TextUtils.isEmpty(reason)) {
            etMismatchReason.setError("Describe the mismatch");
            return;
        }

        Map<String, String> extra = pickup.getExtraDetails();
        if (extra == null) extra = new java.util.HashMap<>();
        extra.put("mismatchReason", reason);

        db.collection("pickups").document(pickup.getId())
                .update("status", "MISMATCH_REPORTED", "extraDetails", extra)
                .addOnSuccessListener(aVoid -> {
                    if (pickup.getMemberId() != null) {
                        sendPushAndInAppNotification(pickup.getMemberId(), "Alert: Discrepancy", 
                            "Collector reported mismatch for ID " + pickup.getId().substring(0,8), pickup.getId());
                    }
                    Toast.makeText(this, "Reported to Admin.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void verifyPinAndCollect(PickupModel pickup) {
        String enteredPin = etVerifyPin.getText().toString().trim();
        if (TextUtils.isEmpty(enteredPin)) {
            etVerifyPin.setError("Enter PIN");
            return;
        }

        if (enteredPin.equals(pickup.getPickupPin())) {
            // Set status to PICKED_UP so it shows in the correct history filter
            updateStatus(pickup, "PICKED_UP", "E-Waste Collected", "Verified and collected by Field Agent.");
            if (pickup.getMemberId() != null) {
                sendPushAndInAppNotification(pickup.getMemberId(), "E-Waste Collected", "Field Agent collected waste for ID " + pickup.getId().substring(0,8), pickup.getId());
            }
            Toast.makeText(this, "PIN Verified! Marked as Collected.", Toast.LENGTH_SHORT).show();
        } else {
            etVerifyPin.setError("Invalid PIN.");
        }
    }

    private void sendMemberOffer(PickupModel pickup) {
        String rate = etBaseRate.getText().toString().trim();
        if (TextUtils.isEmpty(rate)) {
            etBaseRate.setError("Enter offer rate");
            return;
        }
        double amt = Double.parseDouble(rate);
        db.collection("pickups").document(pickup.getId())
                .update("status", "OFFER_SENT", "userAmount", amt, "memberId", currentUserId)
                .addOnSuccessListener(aVoid -> {
                    sendPushAndInAppNotification(pickup.getUserId(), "New Offer", "Member offered ₹" + rate + " for your waste.", pickup.getId());
                    Toast.makeText(this, "Offer sent!", Toast.LENGTH_SHORT).show();
                });
    }

    private void showScheduleDialog(PickupModel pickup) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v1, y, m, d) -> {
            String date = d + "/" + (m + 1) + "/" + y;
            new TimePickerDialog(this, (v2, h, min) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", h, min);
                showCollectorSelectionDialog(pickup, date, time);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showCollectorSelectionDialog(PickupModel pickup, String date, String time) {
        db.collection("collectors")
                .whereEqualTo("memberId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CollectorModel> collectors = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CollectorModel cm = doc.toObject(CollectorModel.class);
                        collectors.add(cm);
                        names.add(cm.getName());
                    }

                    if (names.isEmpty()) {
                        Toast.makeText(this, "No field agents found.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Assign Field Agent")
                            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                                CollectorModel selected = collectors.get(which);
                                db.collection("pickups").document(pickup.getId())
                                        .update("status", "PICKUP_SCHEDULED", 
                                                "scheduledDate", date, 
                                                "scheduledTime", time,
                                                "collectorId", selected.getCollectorId(),
                                                "collectorName", selected.getName())
                                        .addOnSuccessListener(aVoid -> {
                                            sendPushAndInAppNotification(pickup.getUserId(), "Agent Assigned", 
                                                "Field Agent " + selected.getName() + " scheduled for " + date, pickup.getId());
                                            
                                            sendPushAndInAppNotification(selected.getCollectorId(), "New Task", 
                                                "Pickup assigned at " + pickup.getAddress(), pickup.getId());
                                                
                                            Toast.makeText(this, "Scheduled & Agent Assigned!", Toast.LENGTH_SHORT).show();
                                        });
                            }).show();
                });
    }

    private void updateStatus(PickupModel pickup, String status, String title, String msg) {
        db.collection("pickups").document(pickup.getId()).update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status: " + status, Toast.LENGTH_SHORT).show();
                    sendPushAndInAppNotification(pickup.getUserId(), title, msg, pickup.getId());
                    
                    if ("USER_ACCEPTED".equals(status) && pickup.getMemberId() != null) {
                        sendPushAndInAppNotification(pickup.getMemberId(), "Offer Accepted", 
                            "Value accepted by user", pickup.getId());
                    }
                });
    }

    private void sendPushAndInAppNotification(String targetUserId, String title, String message, String pickupId) {
        String id = UUID.randomUUID().toString();
        NotificationModel n = new NotificationModel();
        n.setId(id); n.setUserId(targetUserId); n.setTitle(title); n.setMessage(message);
        n.setTimestamp(new Date()); n.setRead(false);
        db.collection("notifications").document(id).set(n);
        showRealPopupNotification(title, message, pickupId);
    }

    private void showRealPopupNotification(String title, String message, String pickupId) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String cid = "ecowaste_pro_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(cid, "EcoWaste Live Alerts", NotificationManager.IMPORTANCE_HIGH));
        }
        Intent i = new Intent(this, PickupDetailActivity.class);
        i.putExtra("pickupId", pickupId);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, cid)
                .setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pi).setAutoCancel(true);
        nm.notify((int) System.currentTimeMillis(), b.build());
    }
}
