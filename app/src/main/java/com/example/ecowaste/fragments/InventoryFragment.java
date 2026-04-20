package com.example.ecowaste.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.ecowaste.activities.PickupDetailActivity;
import com.example.ecowaste.adapters.CenterPickupAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private CenterPickupAdapter adapter;
    private List<PickupModel> pickupList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentMemberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accepted_pickups, container, false); // Reusing layout

        db = FirebaseFirestore.getInstance();
        currentMemberId = FirebaseAuth.getInstance().getUid();

        recyclerView = view.findViewById(R.id.recyclerViewAccepted);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CenterPickupAdapter(getContext(), pickupList, pickup -> {
            Intent intent = new Intent(getActivity(), PickupDetailActivity.class);
            intent.putExtra("pickupId", pickup.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadInventory();

        return view;
    }

    private void loadInventory() {
        if (currentMemberId == null) return;

        db.collection("pickups")
                .whereEqualTo("memberId", currentMemberId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    pickupList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        PickupModel p = doc.toObject(PickupModel.class);
                        p.setId(doc.getId());
                        // Include both PICKED_UP and COLLECTED in inventory
                        if ("PICKED_UP".equals(p.getStatus()) || "COLLECTED".equals(p.getStatus()) || "COMPLETED".equals(p.getStatus())) {
                            pickupList.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (tvEmpty != null) {
                        tvEmpty.setText("Inventory is empty.\nItems will appear here after being picked up.");
                        tvEmpty.setVisibility(pickupList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
