package com.example.ecowaste.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.PickupAdapter;
import com.example.ecowaste.models.Pickup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CompletedPickupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PickupAdapter adapter;
    private List<Pickup> pickupList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_pickups, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        pickupList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewCompleted);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PickupAdapter(getContext(), pickupList);
        recyclerView.setAdapter(adapter);

        loadCompletedPickups();

        return view;
    }

    private void loadCompletedPickups() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        db.collection("pickups")
                .whereEqualTo("memberId", currentUserId)
                .whereEqualTo("status", "COMPLETED")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CompletedPickups", "Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        pickupList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Map<String, Object> data = doc.getData();
                                Pickup pickup = new Pickup();
                                pickup.setId(doc.getId());
                                pickup.setUserName((String) data.get("userName"));
                                pickup.setType((String) data.get("type"));
                                pickup.setStatus((String) data.get("status"));
                                pickup.setAddress((String) data.get("address"));
                                pickup.setPreferredDate((String) data.get("preferredDate"));

                                Object createdAtObj = data.get("createdAt");
                                if (createdAtObj instanceof Timestamp) {
                                    pickup.setCreatedAt(((Timestamp) createdAtObj).toDate());
                                } else if (createdAtObj instanceof Long) {
                                    pickup.setCreatedAt(new Date((Long) createdAtObj));
                                }
                                
                                pickupList.add(pickup);
                            } catch (Exception e) {
                                Log.e("CompletedPickups", "Parsing error", e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
