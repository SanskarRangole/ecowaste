package com.example.ecowaste.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.CenterSelectAdapter;
import com.example.ecowaste.models.BulkSaleModel;
import com.example.ecowaste.models.CenterModel;
import com.example.ecowaste.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectCentersActivity extends AppCompatActivity {

    private static final String TAG = "SelectCenters";
    private RecyclerView recyclerViewCenters;
    private MaterialButton btnSendRequest;
    private MaterialToolbar toolbar;
    private CenterSelectAdapter adapter;
    private List<CenterModel> centerList = new ArrayList<>();
    private Map<String, Double> wasteWeights;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_centers);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        wasteWeights = (Map<String, Double>) getIntent().getSerializableExtra("wasteWeights");

        initViews();
        loadCenters();

        btnSendRequest.setOnClickListener(v -> sendBulkRequest());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewCenters = findViewById(R.id.recyclerViewCenters);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewCenters.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CenterSelectAdapter(centerList);
        recyclerViewCenters.setAdapter(adapter);
    }

    private void loadCenters() {
        Log.d(TAG, "Loading centers from Firestore...");
        db.collection("centers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    centerList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No documents found in 'centers' collection.");
                        Toast.makeText(this, "No recycling centers found in database.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " center documents.");
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            CenterModel center = doc.toObject(CenterModel.class);
                            
                            // 1. Ensure ID is set
                            if (center.getCenterId() == null) {
                                center.setCenterId(doc.getId());
                            }

                            // 2. Comprehensive Name Search
                            String name = center.getCenterName();
                            if (isInvalid(name)) name = doc.getString("centerName");
                            if (isInvalid(name)) name = doc.getString("name");
                            if (isInvalid(name)) name = doc.getString("center_name");
                            if (isInvalid(name)) name = doc.getString("email");
                            
                            if (isInvalid(name)) {
                                name = "Unknown Center (" + doc.getId().substring(0, Math.min(doc.getId().length(), 5)) + ")";
                                Log.w(TAG, "Could not find a name for center ID: " + doc.getId());
                            }
                            
                            center.setCenterName(name);
                            centerList.add(center);
                            Log.d(TAG, "Successfully added center: " + name);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing center document: " + doc.getId(), e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore query failed", e);
                    Toast.makeText(this, "Connection error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isInvalid(String s) {
        return s == null || s.trim().isEmpty() || s.equals("null");
    }

    private void sendBulkRequest() {
        List<String> selectedIds = adapter.getSelectedCenterIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one center", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendRequest.setEnabled(false);
        btnSendRequest.setText("Sending...");

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String memberName = userDoc.getString("name");
            if (memberName == null) memberName = "Member";
            
            BulkSaleModel bulkSale = new BulkSaleModel();
            bulkSale.setId(UUID.randomUUID().toString());
            bulkSale.setMemberId(currentUserId);
            bulkSale.setMemberName(memberName);
            bulkSale.setWasteWeights(wasteWeights);
            bulkSale.setTargetCenterIds(selectedIds);
            bulkSale.setCenterOffers(new HashMap<>());
            bulkSale.setCenterStatuses(new HashMap<>());
            for (String cid : selectedIds) {
                bulkSale.getCenterStatuses().put(cid, "PENDING");
            }
            bulkSale.setStatus("SENT");
            bulkSale.setCreatedAt(new Date());

            db.collection("bulk_sales").document(bulkSale.getId()).set(bulkSale)
                    .addOnSuccessListener(aVoid -> {
                        for (String centerId : selectedIds) {
                            sendNotificationToCenter(centerId, bulkSale.getId());
                        }
                        Toast.makeText(this, "Request sent to selected centers!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSendRequest.setEnabled(true);
                        btnSendRequest.setText("Send Request");
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void sendNotificationToCenter(String centerId, String saleId) {
        NotificationModel notification = new NotificationModel();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(centerId);
        notification.setTitle("New Bulk Waste Request");
        notification.setMessage("A member has sent a new bulk sale request. Tap to view.");
        notification.setTimestamp(new Date());
        notification.setRead(false);
        db.collection("notifications").document(notification.getId()).set(notification);
    }
}
