package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.CenterPickupAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CenterDashboardActivity extends AppCompatActivity {

    private static final String TAG = "CenterDashboard";
    private TextView tvCenterName, tvTotalWaste, tvCO2Saved, tvPickups, tvBulkCountLabel;
    private View layoutEmptyState;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigation;
    private FirebaseFirestore db;
    private String currentUserId;
    private CenterPickupAdapter adapter;
    private final List<PickupModel> pickupList = new ArrayList<>();
    private View cardViewRequests;
    private ImageView cardRefresh;
    private LinearLayout layoutStatsActive, layoutStatsStock, layoutStatsImpact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_dashboard);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        loadDashboardData();
        loadMyInventory();
        loadBulkRequestCount();
        setupBottomNavigation();
    }

    private void initViews() {
        tvCenterName = findViewById(R.id.tvCenterName);
        tvTotalWaste = findViewById(R.id.tvTotalWaste);
        tvCO2Saved = findViewById(R.id.tvCO2Saved);
        tvPickups = findViewById(R.id.tvPickups);
        tvBulkCountLabel = findViewById(R.id.tvBulkCountLabel);
        layoutEmptyState = findViewById(R.id.tvEmptyState);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        cardViewRequests = findViewById(R.id.cardViewRequests);
        cardRefresh = findViewById(R.id.cardRefresh);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new CenterPickupAdapter(this, pickupList, pickup -> {
                Intent intent = new Intent(this, PickupDetailActivity.class);
                intent.putExtra("pickupId", pickup.getId());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }

        if (cardViewRequests != null) {
            cardViewRequests.setOnClickListener(v -> 
                    startActivity(new Intent(this, BulkSaleRequestsActivity.class)));
        }

        if (cardRefresh != null) {
            cardRefresh.setOnClickListener(v -> {
                Toast.makeText(this, "Refreshing Hub...", Toast.LENGTH_SHORT).show();
                loadDashboardStats();
                loadBulkRequestCount();
                loadMyInventory();
            });
        }
    }

    private void loadDashboardData() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                if (tvCenterName != null) {
                    tvCenterName.setText(name != null ? name : "Eco Center Hub");
                }
            }
        });
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        db.collection("pickups")
                .whereEqualTo("centerId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    
                    double stockWeight = 0;
                    int activeTasks = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        PickupModel p = doc.toObject(PickupModel.class);
                        String status = p.getStatus();
                        
                        // Statuses that contribute to current stock
                        if ("PICKED_UP".equals(status) || "COLLECTED".equals(status)) {
                            stockWeight += p.getEstimatedWeight();
                        }
                        
                        // Statuses that need center attention
                        if ("ASSIGNED_TO_CENTER".equals(status) || "OFFER_SENT".equals(status) || "USER_ACCEPTED".equals(status) || "PICKUP_SCHEDULED".equals(status)) {
                            activeTasks++;
                        }
                    }
                    if (tvTotalWaste != null) tvTotalWaste.setText(String.format(Locale.getDefault(), "%.1f", stockWeight));
                    if (tvCO2Saved != null) tvCO2Saved.setText(String.format(Locale.getDefault(), "%.1f", stockWeight * 1.44));
                    if (tvPickups != null) tvPickups.setText(String.valueOf(activeTasks));
                });
    }

    private void loadBulkRequestCount() {
        db.collection("bulk_sales")
                .whereArrayContains("targetCenterIds", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        int pendingCount = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, String> centerStatuses = (Map<String, String>) doc.get("centerStatuses");
                            if (centerStatuses != null && "PENDING".equals(centerStatuses.get(currentUserId))) {
                                pendingCount++;
                            }
                        }
                        if (tvBulkCountLabel != null) {
                            tvBulkCountLabel.setText(pendingCount > 0 ? pendingCount + " New Supply Opportunities" : "Browse available stock");
                        }
                    }
                });
    }

    private void loadMyInventory() {
        db.collection("pickups")
                .whereEqualTo("centerId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Inventory query failed: ", error);
                        return;
                    }

                    pickupList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            PickupModel p = doc.toObject(PickupModel.class);
                            p.setId(doc.getId());
                            // Centers want to see items they are handling or have stocked
                            String status = p.getStatus();
                            if (!"CANCELLED".equals(status) && !"REJECTED".equals(status) && !"COMPLETED".equals(status)) {
                                pickupList.add(p);
                            }
                        }
                    }
                    
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) return true;
                if (id == R.id.nav_history) {
                    startActivity(new Intent(this, PickupHistoryActivity.class));
                    return true;
                } else if (id == R.id.nav_notifications) {
                    startActivity(new Intent(this, NotificationsActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }
}
