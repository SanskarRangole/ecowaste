package com.example.ecowaste.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.BulkOfferAdapter;
import com.example.ecowaste.models.BulkSaleModel;
import com.example.ecowaste.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class MemberBulkSaleManageActivity extends AppCompatActivity {

    private TextView tvWasteBreakdown, tvOfferCount, tvNoOffers;
    private RecyclerView recyclerViewOffers;
    private BulkOfferAdapter adapter;
    private FirebaseFirestore db;
    private String bulkSaleId;
    private BulkSaleModel bulkSale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_bulk_sale_manage);

        db = FirebaseFirestore.getInstance();
        bulkSaleId = getIntent().getStringExtra("bulkSaleId");

        initViews();
        loadBulkSaleDetails();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvWasteBreakdown = findViewById(R.id.tvWasteBreakdown);
        tvOfferCount = findViewById(R.id.tvOfferCount);
        tvNoOffers = findViewById(R.id.tvNoOffers);
        recyclerViewOffers = findViewById(R.id.recyclerViewOffers);
        recyclerViewOffers.setLayoutManager(new LinearLayoutManager(this));
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
        StringBuilder breakdown = new StringBuilder();
        if (bulkSale.getWasteWeights() != null) {
            for (Map.Entry<String, Double> entry : bulkSale.getWasteWeights().entrySet()) {
                breakdown.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" kg\n");
            }
        }
        tvWasteBreakdown.setText(breakdown.toString().trim());

        Map<String, Double> offers = bulkSale.getCenterOffers();
        int count = (offers != null) ? offers.size() : 0;
        tvOfferCount.setText(count + (count == 1 ? " offer" : " offers"));

        if (count == 0) {
            tvNoOffers.setVisibility(View.VISIBLE);
            recyclerViewOffers.setVisibility(View.GONE);
        } else {
            tvNoOffers.setVisibility(View.GONE);
            recyclerViewOffers.setVisibility(View.VISIBLE);
            
            adapter = new BulkOfferAdapter(offers, bulkSale.getCenterStatuses(), bulkSale.getFinalCenterId(), new BulkOfferAdapter.OnOfferDecisionListener() {
                @Override
                public void onAccept(String centerId, double amount) {
                    acceptOffer(centerId, amount);
                }

                @Override
                public void onReject(String centerId) {
                    rejectOffer(centerId);
                }
            });
            recyclerViewOffers.setAdapter(adapter);
        }
    }

    private void acceptOffer(String centerId, double amount) {
        db.collection("bulk_sales").document(bulkSaleId)
                .update("finalCenterId", centerId, "status", "COMPLETED")
                .addOnSuccessListener(aVoid -> {
                    notifyCenter(centerId, true);
                    for (String cid : bulkSale.getTargetCenterIds()) {
                        if (!cid.equals(centerId)) {
                            notifyCenter(cid, false);
                        }
                    }
                    Toast.makeText(this, "Sale Completed! Center Notified.", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectOffer(String centerId) {
        db.collection("bulk_sales").document(bulkSaleId)
                .update("centerStatuses." + centerId, "MEMBER_REJECTED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offer Rejected", Toast.LENGTH_SHORT).show();
                });
    }

    private void notifyCenter(String centerId, boolean accepted) {
        NotificationModel notification = new NotificationModel();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(centerId);
        notification.setTitle(accepted ? "Bulk Sale Won!" : "Bulk Offer Not Selected");
        notification.setMessage(accepted ? "Your offer was accepted for the bulk inventory." : "The member chose another offer for their bulk waste.");
        notification.setTimestamp(new Date());
        notification.setRead(false);
        db.collection("notifications").document(notification.getId()).set(notification);
    }
}
