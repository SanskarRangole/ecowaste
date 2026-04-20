package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.CenterPickupAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AvailableRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private CenterPickupAdapter adapter;
    private List<PickupModel> pickupList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_requests);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CenterPickupAdapter(this, pickupList, pickup -> {
            Intent intent = new Intent(this, PickupDetailActivity.class);
            intent.putExtra("pickupId", pickup.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadAllAvailableRequests();
    }

    private void loadAllAvailableRequests() {
        // Broad query: show all requests that are in BULK_REQUESTED state for companies to bid on
        db.collection("pickups")
                .whereEqualTo("status", "BULK_REQUESTED")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    pickupList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Map<String, Object> data = doc.getData();
                                PickupModel p = new PickupModel();
                                p.setId(doc.getId());
                                p.setUserName("Bulk Batch #" + doc.getId().substring(0, 5));
                                p.setCategory((String) data.get("category"));
                                p.setType((String) data.get("type"));
                                p.setStatus((String) data.get("status"));
                                p.setAddress("Warehouse Location");
                                
                                Object weight = data.get("estimatedWeight");
                                p.setEstimatedWeightObj(weight);

                                Object createdAtObj = data.get("createdAt");
                                if (createdAtObj instanceof Timestamp) {
                                    p.setCreatedAt(((Timestamp) createdAtObj).toDate());
                                } else if (createdAtObj instanceof Long) {
                                    p.setCreatedAt(new Date((Long) createdAtObj));
                                }

                                pickupList.add(p);
                            } catch (Exception e) {
                                Log.e("AvailableRequests", "Error parsing", e);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}
