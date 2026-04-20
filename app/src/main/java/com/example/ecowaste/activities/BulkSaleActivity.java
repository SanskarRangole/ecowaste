package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BulkSaleActivity extends AppCompatActivity {

    private LinearLayout layoutWasteItems;
    private MaterialButton btnAddItem, btnNext;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_sale);

        initViews();
        // Add one initial row
        addWasteRow();

        btnAddItem.setOnClickListener(v -> addWasteRow());
        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutWasteItems = findViewById(R.id.layoutWasteItems);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnNext = findViewById(R.id.btnNext);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void addWasteRow() {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_bulk_waste_row, layoutWasteItems, false);
        ImageView btnRemove = rowView.findViewById(R.id.btnRemove);
        
        btnRemove.setOnClickListener(v -> {
            if (layoutWasteItems.getChildCount() > 1) {
                layoutWasteItems.removeView(rowView);
            } else {
                Toast.makeText(this, "At least one item is required", Toast.LENGTH_SHORT).show();
            }
        });

        layoutWasteItems.addView(rowView);
    }

    private void validateAndProceed() {
        Map<String, Double> wasteWeights = new HashMap<>();
        
        for (int i = 0; i < layoutWasteItems.getChildCount(); i++) {
            View row = layoutWasteItems.getChildAt(i);
            EditText etType = row.findViewById(R.id.etWasteType);
            EditText etWeight = row.findViewById(R.id.etWasteWeight);

            String type = etType.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (TextUtils.isEmpty(type) || TextUtils.isEmpty(weightStr)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);
                wasteWeights.put(type, weight);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid weight format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (wasteWeights.isEmpty()) {
            Toast.makeText(this, "No items added", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SelectCentersActivity.class);
        intent.putExtra("wasteWeights", (Serializable) wasteWeights);
        startActivity(intent);
    }
}
