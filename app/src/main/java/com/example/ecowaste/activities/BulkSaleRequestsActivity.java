package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.BulkSaleRequestAdapter;
import com.example.ecowaste.models.BulkSaleModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BulkSaleRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBulkSales;
    private BulkSaleRequestAdapter adapter;
    private List<BulkSaleModel> bulkSaleList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentCenterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_sale_requests);

        db = FirebaseFirestore.getInstance();
        currentCenterId = FirebaseAuth.getInstance().getUid();

        initViews();
        loadBulkRequests();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewBulkSales = findViewById(R.id.recyclerViewBulkSales);
        recyclerViewBulkSales.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new BulkSaleRequestAdapter(bulkSaleList, bulkSale -> {
            Intent intent = new Intent(this, BulkSaleDetailActivity.class);
            intent.putExtra("bulkSaleId", bulkSale.getId());
            startActivity(intent);
        });
        recyclerViewBulkSales.setAdapter(adapter);
    }

    private void loadBulkRequests() {
        db.collection("bulk_sales")
                .whereArrayContains("targetCenterIds", currentCenterId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("BulkSaleRequests", "Error: " + error.getMessage());
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
                    }
                });
    }
}
