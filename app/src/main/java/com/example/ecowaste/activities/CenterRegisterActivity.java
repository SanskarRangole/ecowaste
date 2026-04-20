package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.example.ecowaste.models.CenterModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class CenterRegisterActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etCenterName, etEmail, etPhone, etLicense, etGST, etAddress, etCity, etPincode;
    private EditText etOperatingHours, etContactPerson, etContactPhone, etProcessingCapacity, etCertifications, etTerms;
    private Spinner spinnerWasteType;
    private EditText etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private String[] wasteTypes = {"Electronics", "Plastic", "Metal", "Glass", "Paper", "Battery", "All Types"};
    private String selectedWasteType;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etCenterName = findViewById(R.id.etCenterName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etLicense = findViewById(R.id.etLicense);
        etGST = findViewById(R.id.etGST);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPincode = findViewById(R.id.etPincode);
        etOperatingHours = findViewById(R.id.etOperatingHours);
        etTerms = findViewById(R.id.etTerms);
        etContactPerson = findViewById(R.id.etContactPerson);
        etContactPhone = findViewById(R.id.etContactPhone);
        etProcessingCapacity = findViewById(R.id.etProcessingCapacity);
        etCertifications = findViewById(R.id.etCertifications);
        spinnerWasteType = findViewById(R.id.spinnerWasteType);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wasteTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWasteType.setAdapter(adapter);

        spinnerWasteType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWasteType = wasteTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWasteType = "Electronics";
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(CenterRegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerCenter());
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() == 10 && TextUtils.isDigitsOnly(phone);
    }

    private void registerCenter() {
        final String centerName = etCenterName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();
        final String license = etLicense.getText().toString().trim();
        final String gst = etGST.getText().toString().trim();
        final String address = etAddress.getText().toString().trim();
        final String city = etCity.getText().toString().trim();
        final String pincode = etPincode.getText().toString().trim();
        final String operatingHours = etOperatingHours.getText().toString().trim();
        final String terms = etTerms.getText().toString().trim();
        final String contactPerson = etContactPerson.getText().toString().trim();
        final String contactPhone = etContactPhone.getText().toString().trim();
        final String capacityStr = etProcessingCapacity.getText().toString().trim();
        final String certifications = etCertifications.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String confirmPassword = etConfirmPassword.getText().toString().trim();
        final String wasteType = selectedWasteType;

        if (TextUtils.isEmpty(centerName)) { etCenterName.setError("Center name required"); return; }
        
        if (TextUtils.isEmpty(email)) { 
            etEmail.setError("Email required"); 
            return; 
        } else if (!isValidEmail(email)) {
            etEmail.setError("Enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(phone)) { 
            etPhone.setError("Phone required"); 
            return; 
        } else if (!isValidPhone(phone)) {
            etPhone.setError("Enter a valid 10-digit phone number");
            return;
        }

        if (TextUtils.isEmpty(license)) { etLicense.setError("License number required"); return; }
        if (TextUtils.isEmpty(address)) { etAddress.setError("Address required"); return; }
        if (TextUtils.isEmpty(city)) { etCity.setError("City required"); return; }
        if (TextUtils.isEmpty(pincode)) { etPincode.setError("Pincode required"); return; }
        if (TextUtils.isEmpty(operatingHours)) { etOperatingHours.setError("Operating hours required"); return; }
        if (TextUtils.isEmpty(terms)) { etTerms.setError("Terms required"); return; }
        if (TextUtils.isEmpty(contactPerson)) { etContactPerson.setError("Contact person required"); return; }
        
        if (TextUtils.isEmpty(contactPhone)) { 
            etContactPhone.setError("Contact phone required"); 
            return; 
        } else if (!isValidPhone(contactPhone)) {
            etContactPhone.setError("Enter a valid 10-digit phone number");
            return;
        }

        final int capacity = TextUtils.isEmpty(capacityStr) ? 0 : Integer.parseInt(capacityStr);

        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }
        if (password.length() < 6) { etPassword.setError("Minimum 6 characters"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords don't match"); return; }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveCenterToFirestore(
                                centerName, email, phone, license, gst, address, city, pincode,
                                operatingHours, terms, contactPerson, contactPhone, capacity, certifications, wasteType
                        );
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register Center");
                        Toast.makeText(CenterRegisterActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCenterToFirestore(String centerName, String email, String phone,
                                       String license, String gst, String address, String city,
                                       String pincode, String operatingHours, String terms, String contactPerson,
                                       String contactPhone, int capacity, String certifications,
                                       String wasteType) {

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("name", centerName);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("role", "center");
        userMap.put("createdAt", System.currentTimeMillis());

        CenterModel center = new CenterModel(centerName, email, phone, license, address, city, pincode, wasteType);
        center.setCenterId(userId);
        center.setGstNumber(gst);
        center.setOperatingHours(operatingHours);
        center.setTermsAndConditions(terms);
        center.setContactPerson(contactPerson);
        center.setContactPhone(contactPhone);
        center.setProcessingCapacity(capacity);
        center.setCertifications(certifications);

        WriteBatch batch = db.batch();
        batch.set(db.collection("users").document(userId), userMap);
        batch.set(db.collection("centers").document(userId), center);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(CenterRegisterActivity.this,
                    "Center Registered Successfully!",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(CenterRegisterActivity.this, LoginActivity.class));
            finish();
        }).addOnFailureListener(e -> {
            btnRegister.setEnabled(true);
            btnRegister.setText("Register Center");
            Toast.makeText(CenterRegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
