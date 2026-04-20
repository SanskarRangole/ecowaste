package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.BulkSaleRequestAdapter;
import com.example.ecowaste.models.BulkSaleModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MemberBulkSalesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBulkSales;
    private TextView tvEmptyState;
    private BulkSaleRequestAdapter adapter;
    private List<BulkSaleModel> bulkSaleList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentMemberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_bulk_sales);

        db = FirebaseFirestore.getInstance();
        currentMemberId = FirebaseAuth.getInstance().getUid();

        initViews();
        loadMyBulkSales();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewBulkSales = findViewById(R.id.recyclerViewBulkSales);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        recyclerViewBulkSales.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BulkSaleRequestAdapter(bulkSaleList, bulkSale -> {
            Intent intent = new Intent(this, MemberBulkSaleManageActivity.class);
            intent.putExtra("bulkSaleId", bulkSale.getId());
            startActivity(intent);
        });
        recyclerViewBulkSales.setAdapter(adapter);
    }

    private void loadMyBulkSales() {
        db.collection("bulk_sales")
                .whereEqualTo("memberId", currentMemberId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        bulkSaleList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            BulkSaleModel bulkSale = doc.toObject(BulkSaleModel.class);
                            bulkSale.setId(doc.getId());
                            bulkSaleList.add(bulkSale);
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (bulkSaleList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
