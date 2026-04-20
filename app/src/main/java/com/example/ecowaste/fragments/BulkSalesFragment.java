package com.example.ecowaste.fragments;

import android.content.Intent;
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
import com.example.ecowaste.activities.MemberBulkSaleManageActivity;
import com.example.ecowaste.adapters.BulkSaleRequestAdapter;
import com.example.ecowaste.models.BulkSaleModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BulkSalesFragment extends Fragment {

    private static final String TAG = "BulkSalesFragment";
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private BulkSaleRequestAdapter adapter;
    private final List<BulkSaleModel> bulkSaleList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentMemberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulk_sales, container, false);

        db = FirebaseFirestore.getInstance();
        currentMemberId = FirebaseAuth.getInstance().getUid();

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BulkSaleRequestAdapter(bulkSaleList, bulkSale -> {
            Intent intent = new Intent(getActivity(), MemberBulkSaleManageActivity.class);
            intent.putExtra("bulkSaleId", bulkSale.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadBulkSales();

        return view;
    }

    private void loadBulkSales() {
        if (currentMemberId == null) return;
        
        db.collection("bulk_sales")
                .whereEqualTo("memberId", currentMemberId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading bulk sales: ", error);
                        return;
                    }
                    if (value != null) {
                        bulkSaleList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            BulkSaleModel bulkSale = doc.toObject(BulkSaleModel.class);
                            bulkSale.setId(doc.getId());
                            bulkSaleList.add(bulkSale);
                        }

                        // Sort the list manually by createdAt descending to avoid index requirement
                        bulkSaleList.sort((a, b) -> {
                            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        });

                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(bulkSaleList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
