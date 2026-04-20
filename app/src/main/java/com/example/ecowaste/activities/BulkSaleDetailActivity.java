package com.example.ecowaste.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.example.ecowaste.models.BulkSaleModel;
import com.example.ecowaste.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class BulkSaleDetailActivity extends AppCompatActivity {

    private TextView tvMemberName, tvWasteWeights;
    private LinearLayout layoutActionButtons, layoutOfferPrice;
    private EditText etOfferAmount;
    private MaterialButton btnAccept, btnReject, btnSubmitOffer;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private String currentCenterId;
    private String bulkSaleId;
    private BulkSaleModel bulkSale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_sale_detail);

        db = FirebaseFirestore.getInstance();
        currentCenterId = FirebaseAuth.getInstance().getUid();
        bulkSaleId = getIntent().getStringExtra("bulkSaleId");

        initViews();
        loadBulkSaleDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvMemberName = findViewById(R.id.tvMemberName);
        tvWasteWeights = findViewById(R.id.tvWasteWeights);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);
        layoutOfferPrice = findViewById(R.id.layoutOfferPrice);
        etOfferAmount = findViewById(R.id.etOfferAmount);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnSubmitOffer = findViewById(R.id.btnSubmitOffer);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnAccept.setOnClickListener(v -> {
            layoutActionButtons.setVisibility(View.GONE);
            layoutOfferPrice.setVisibility(View.VISIBLE);
        });

        btnReject.setOnClickListener(v -> rejectRequest());
        btnSubmitOffer.setOnClickListener(v -> submitOffer());
    }

    private void loadBulkSaleDetails() {
        db.collection("bulk_sales").document(bulkSaleId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        bulkSale = snapshot.toObject(BulkSaleModel.class);
                        bulkSale.setId(snapshot.getId());
                        displayDetails();
                    }
                });
    }

    private void displayDetails() {
        tvMemberName.setText(bulkSale.getMemberName());
        
        StringBuilder weights = new StringBuilder();
        for (Map.Entry<String, Double> entry : bulkSale.getWasteWeights().entrySet()) {
            weights.append(entry.getKey()).append(": ").append(entry.getValue()).append("kg\n");
        }
        tvWasteWeights.setText(weights.toString());

        String myStatus = bulkSale.getCenterStatuses().get(currentCenterId);
        if (!"PENDING".equals(myStatus)) {
            layoutActionButtons.setVisibility(View.GONE);
            layoutOfferPrice.setVisibility(View.GONE);
            Toast.makeText(this, "You have already responded: " + myStatus, Toast.LENGTH_SHORT).show();
        }
    }

    private void rejectRequest() {
        db.collection("bulk_sales").document(bulkSaleId)
                .update("centerStatuses." + currentCenterId, "REJECTED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void submitOffer() {
        String amountStr = etOfferAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etOfferAmount.setError("Enter total amount");
            return;
        }

        double offerAmount = Double.parseDouble(amountStr);

        db.collection("bulk_sales").document(bulkSaleId)
                .update("centerStatuses." + currentCenterId, "ACCEPTED",
                        "centerOffers." + currentCenterId, offerAmount)
                .addOnSuccessListener(aVoid -> {
                    notifyMember(offerAmount);
                    Toast.makeText(this, "Offer submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void notifyMember(double amount) {
        NotificationModel notification = new NotificationModel();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(bulkSale.getMemberId());
        notification.setTitle("Bulk Request Accepted");
        notification.setMessage("A recycling center has accepted your request and offered ₹" + amount);
        notification.setTimestamp(new Date());
        notification.setRead(false);
        db.collection("notifications").document(notification.getId()).set(notification);
    }
}
