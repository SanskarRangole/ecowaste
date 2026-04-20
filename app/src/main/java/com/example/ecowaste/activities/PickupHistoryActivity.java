package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.PickupHistoryAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickupHistoryActivity extends AppCompatActivity {

    private static final String TAG = "PickupHistoryActivity";
    private ImageView btnBack, btnClearHistory;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private Chip chipAll, chipPending, chipAccepted, chipDelivered;

    private FirebaseFirestore db;
    private PickupHistoryAdapter adapter;
    private List<PickupModel> pickupList = new ArrayList<>();
    private String currentUserId;
    private String currentUserRole;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_history);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        getUserRole();
        setupRecyclerView();
        setupChips();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipAccepted = findViewById(R.id.chipAccepted);
        chipDelivered = findViewById(R.id.chipDelivered);
        
        // Hide unused chip
        Chip chipCompleted = findViewById(R.id.chipCompleted);
        if (chipCompleted != null) chipCompleted.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());
        
        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> {
                if (currentUserRole == null) {
                    Toast.makeText(this, "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
                    return;
                }
                showClearHistoryDialog();
            });
        }
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear your pickup history? This will delete all finished, rejected, and cancelled requests.")
                .setPositiveButton("Clear All", (dialog, which) -> clearHistoryFromFirestore())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearHistoryFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        
        Query query = db.collection("pickups");
        if ("user".equals(currentUserRole)) {
            query = query.whereEqualTo("userId", currentUserId);
        } else if ("center".equals(currentUserRole)) {
            query = query.whereEqualTo("centerId", currentUserId);
        } else if ("member".equals(currentUserRole)) {
            query = query.whereEqualTo("memberId", currentUserId);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            int count = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String status = doc.getString("status");
                if ("PICKED_UP".equals(status) || "COMPLETED".equals(status) || "REJECTED".equals(status) || "CANCELLED".equals(status)) {
                    batch.delete(doc.getReference());
                    count++;
                }
            }
            
            if (count > 0) {
                batch.commit().addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "History cleared successfully", Toast.LENGTH_SHORT).show();
                    loadPickupHistory();
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to clear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "No finished history to clear", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error fetching history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && "recycleit50@gmail.com".equalsIgnoreCase(user.getEmail())) {
            currentUserRole = "member";
            loadPickupHistory();
            return;
        }

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserRole = documentSnapshot.getString("role");
                    } else {
                        currentUserRole = "user";
                    }
                    loadPickupHistory();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user role", e);
                    currentUserRole = "user";
                    loadPickupHistory();
                });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PickupHistoryAdapter(this, pickupList, pickup -> {
            Intent intent = new Intent(this, PickupDetailActivity.class);
            intent.putExtra("pickupId", pickup.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupChips() {
        updateChipSelection(chipAll);
        
        chipAccepted.setText("Price Accepted");
        chipDelivered.setText("Finished");

        chipAll.setOnClickListener(v -> {
            currentFilter = "All";
            updateChipSelection(chipAll);
            loadPickupHistory();
        });

        chipPending.setOnClickListener(v -> {
            currentFilter = "PENDING";
            updateChipSelection(chipPending);
            loadPickupHistory();
        });

        chipAccepted.setOnClickListener(v -> {
            currentFilter = "USER_ACCEPTED";
            updateChipSelection(chipAccepted);
            loadPickupHistory();
        });

        chipDelivered.setOnClickListener(v -> {
            currentFilter = "PICKED_UP";
            updateChipSelection(chipDelivered);
            loadPickupHistory();
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        Chip[] chips = {chipAll, chipPending, chipAccepted, chipDelivered};
        for (Chip chip : chips) {
            if (chip == selectedChip) {
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                chip.setChecked(true);
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                chip.setChecked(false);
            }
        }
    }

    private void loadPickupHistory() {
        if (currentUserRole == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        Query query = db.collection("pickups");

        if ("user".equals(currentUserRole)) {
            query = query.whereEqualTo("userId", currentUserId);
        } else if ("member".equals(currentUserRole)) {
            query = query.whereEqualTo("memberId", currentUserId);
        } else if ("center".equals(currentUserRole)) {
            query = query.whereEqualTo("centerId", currentUserId);
        }

        query.orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Log.e(TAG, "Firestore Error: " + error.getMessage());
                        loadHistoryWithoutSorting();
                        return;
                    }
                    if (value != null) {
                        pickupList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                String status = doc.getString("status");
                                if (isFilterMatched(status)) {
                                    PickupModel pickup = doc.toObject(PickupModel.class);
                                    pickup.setId(doc.getId());
                                    pickupList.add(pickup);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Parsing error", e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (emptyState != null) {
                            emptyState.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                });
    }

    private void loadHistoryWithoutSorting() {
        Query query = db.collection("pickups");
        if ("user".equals(currentUserRole)) {
            query = query.whereEqualTo("userId", currentUserId);
        } else if ("member".equals(currentUserRole)) {
            query = query.whereEqualTo("memberId", currentUserId);
        } else if ("center".equals(currentUserRole)) {
            query = query.whereEqualTo("centerId", currentUserId);
        }

        query.get().addOnSuccessListener(value -> {
            if (value != null) {
                pickupList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    String status = doc.getString("status");
                    if (isFilterMatched(status)) {
                        PickupModel pickup = doc.toObject(PickupModel.class);
                        pickup.setId(doc.getId());
                        pickupList.add(pickup);
                    }
                }
                
                Collections.sort(pickupList, (a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                });

                adapter.notifyDataSetChanged();
                if (emptyState != null) {
                    emptyState.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private boolean isFilterMatched(String status) {
        if ("All".equals(currentFilter)) return true;
        if ("PENDING".equals(currentFilter)) {
            return "PENDING_REVIEW".equals(status) || "ASSIGNED_TO_CENTER".equals(status) || "OFFER_SENT".equals(status);
        }
        if ("PICKED_UP".equals(currentFilter)) {
            return "PICKED_UP".equals(status) || "COLLECTED".equals(status) || "COMPLETED".equals(status);
        }
        return currentFilter.equals(status);
    }
}