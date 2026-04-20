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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AcceptedPickupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private MemberPickupAdapter adapter;
    private List<PickupModel> pickupList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accepted_pickups, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewAccepted);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MemberPickupAdapter(getContext(), pickupList);
        recyclerView.setAdapter(adapter);

        loadAcceptedPickups();

        return view;
    }

    private void loadAcceptedPickups() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        db.collection("pickups")
                .whereEqualTo("memberId", currentUserId)
                .whereIn("status", Arrays.asList("OFFER_SENT", "USER_ACCEPTED", "PICKUP_SCHEDULED"))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AcceptedPickups", "Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        pickupList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                PickupModel p = doc.toObject(PickupModel.class);
                                p.setId(doc.getId());
                                pickupList.add(p);
                            } catch (Exception e) {
                                Log.e("AcceptedPickups", "Error parsing: " + e.getMessage());
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
