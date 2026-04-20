package com.example.ecowaste.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.MemberPickupAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AvailablePickupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private MemberPickupAdapter adapter;
    private List<PickupModel> pickupList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available_pickups, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewAvailable);
        tvEmpty = view.findViewById(R.id.tvEmpty); // Assuming tvEmpty exists in layout

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MemberPickupAdapter(getContext(), pickupList);
        recyclerView.setAdapter(adapter);

        loadAvailablePickups();

        return view;
    }

    private void loadAvailablePickups() {
        db.collection("pickups")
                .whereEqualTo("status", "PENDING_REVIEW")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AvailablePickups", "Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        pickupList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                PickupModel pickup = doc.toObject(PickupModel.class);
                                pickup.setId(doc.getId());
                                pickupList.add(pickup);
                            } catch (Exception e) {
                                Log.e("AvailablePickups", "Parsing error", e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (tvEmpty != null) {
                            tvEmpty.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                });
    }
}
