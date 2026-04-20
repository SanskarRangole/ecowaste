package com.example.ecowaste.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.ecowaste.R;
import com.example.ecowaste.adapters.CollectorPickupAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CollectorDashboardActivity extends AppCompatActivity {

    private RecyclerView rvAssignedPickups;
    private CollectorPickupAdapter adapter;
    private List<PickupModel> pickupList = new ArrayList<>();
    private FirebaseFirestore db;
    private String collectorId;
    
    private TextView tvCollectorName, tvPendingCount, tvMismatchCount, tvCompletedCount;
    private ImageView btnLogout;
    private LinearLayout emptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector_dashboard);

        db = FirebaseFirestore.getInstance();
        collectorId = getIntent().getStringExtra("collectorId");

        if (collectorId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadCollectorStats();
        loadAssignedPickups();
        setupClickListeners();
    }

    private void initViews() {
        tvCollectorName = findViewById(R.id.tvCollectorName);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvMismatchCount = findViewById(R.id.tvMismatchCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        btnLogout = findViewById(R.id.btnLogout);
        rvAssignedPickups = findViewById(R.id.rvAssignedPickups);
        emptyState = findViewById(R.id.emptyState);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupRecyclerView() {
        rvAssignedPickups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CollectorPickupAdapter(pickupList, pickupId -> {
            Intent intent = new Intent(this, PickupDetailActivity.class);
            intent.putExtra("pickupId", pickupId);
            intent.putExtra("collectorId", collectorId);
            startActivity(intent);
        });
        rvAssignedPickups.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            // Clear saved collector session
            SharedPreferences.Editor editor = getSharedPreferences("collector_prefs", MODE_PRIVATE).edit();
            editor.remove("collectorId");
            editor.apply();

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadCollectorStats();
            loadAssignedPickups();
        });
    }

    private void loadCollectorStats() {
        db.collection("collectors").document(collectorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvCollectorName.setText(doc.getString("name"));
                    }
                });

        db.collection("pickups").whereEqualTo("collectorId", collectorId)
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    int pending = 0;
                    int mismatch = 0;
                    int completed = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        String status = doc.getString("status");
                        if ("PICKUP_SCHEDULED".equals(status)) pending++;
                        else if ("MISMATCH_REPORTED".equals(status)) mismatch++;
                        else if ("COLLECTED".equals(status) || "COMPLETED".equals(status) || "PICKED_UP".equals(status)) completed++;
                    }
                    tvPendingCount.setText(String.valueOf(pending));
                    tvMismatchCount.setText(String.valueOf(mismatch));
                    tvCompletedCount.setText(String.valueOf(completed));
                });
    }

    private void loadAssignedPickups() {
        db.collection("pickups")
                .whereEqualTo("collectorId", collectorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    if (error != null) {
                        Log.e("CollectorDashboard", "Error: " + error.getMessage());
                        return;
                    }

                    pickupList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            PickupModel pickup = doc.toObject(PickupModel.class);
                            pickup.setId(doc.getId());
                            // Show active pickups (scheduled or issues reported)
                            if (!"COMPLETED".equals(pickup.getStatus()) && !"CANCELLED".equals(pickup.getStatus()) && !"PICKED_UP".equals(pickup.getStatus()) && !"COLLECTED".equals(pickup.getStatus())) {
                                pickupList.add(pickup);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

}
