package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnBack.setOnClickListener(v -> finish());
        tvLogin.setOnClickListener(v -> startActivity(new Intent(UserRegisterActivity.this, LoginActivity.class)));
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() == 10 && TextUtils.isDigitsOnly(phone);
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Name required"); return; }
        
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

        if (TextUtils.isEmpty(address)) { etAddress.setError("Address required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }
        if (password.length() < 6) { etPassword.setError("Min 6 characters"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords don't match"); return; }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(name, email, phone, address);
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Create Account");
                        Toast.makeText(UserRegisterActivity.this,
                                "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String name, String email, String phone, String address) {
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("address", address);
        user.put("role", "user");
        user.put("ecoPoints", 0);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserRegisterActivity.this,
                            "Registration Successful! Please login.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserRegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create Account");
                    Toast.makeText(UserRegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}