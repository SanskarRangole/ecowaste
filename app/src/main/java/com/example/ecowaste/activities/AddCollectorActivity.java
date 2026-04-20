package com.example.ecowaste.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCollectorActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword;
    private MaterialButton btnAddCollector;
    private MaterialToolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentMemberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_collector);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentMemberId = mAuth.getUid();

        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnAddCollector = findViewById(R.id.btnAddCollector);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnAddCollector.setOnClickListener(v -> createCollectorAccount());
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() == 10 && TextUtils.isDigitsOnly(phone);
    }

    private void createCollectorAccount() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Required"); return; }
        
        if (TextUtils.isEmpty(email)) { 
            etEmail.setError("Required"); 
            return; 
        } else if (!isValidEmail(email)) {
            etEmail.setError("Enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(phone)) { 
            etPhone.setError("Required"); 
            return; 
        } else if (!isValidPhone(phone)) {
            etPhone.setError("Enter a valid 10-digit phone number");
            return;
        }

        if (TextUtils.isEmpty(password)) { etPassword.setError("Required"); return; }
        if (password.length() < 6) { etPassword.setError("Min 6 chars"); return; }

        btnAddCollector.setEnabled(false);
        btnAddCollector.setText("Creating...");

        // Optimized: Using email as document ID for direct lookup (faster than query)
        db.collection("collectors").document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        btnAddCollector.setEnabled(true);
                        btnAddCollector.setText("Create Collector Account");
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        saveCollectorToFirestore(name, email, phone, password);
                    }
                })
                .addOnFailureListener(e -> {
                    btnAddCollector.setEnabled(true);
                    btnAddCollector.setText("Create Collector Account");
                    Toast.makeText(this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCollectorToFirestore(String name, String email, String phone, String password) {
        Map<String, Object> collector = new HashMap<>();
        collector.put("collectorId", email); // Using email as ID
        collector.put("memberId", currentMemberId);
        collector.put("name", name);
        collector.put("email", email);
        collector.put("phone", phone);
        collector.put("password", password);
        collector.put("role", "collector");
        collector.put("createdAt", System.currentTimeMillis());

        // Optimized: Direct set using email as document ID
        db.collection("collectors").document(email).set(collector)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Collector added successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnAddCollector.setEnabled(true);
                    btnAddCollector.setText("Create Collector Account");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}