package com.example.ecowaste.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister, tvWelcome, tvSub;
    private android.view.View cardLogo;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSub = findViewById(R.id.tvSub);
        cardLogo = findViewById(R.id.cardLogo);

        startEntryAnimations();

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RoleSelectActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Password reset link sent", Toast.LENGTH_SHORT).show());
    }

    private void startEntryAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        cardLogo.startAnimation(bounce);
        tvWelcome.startAnimation(fadeIn);
        tvSub.startAnimation(fadeIn);
        btnLogin.startAnimation(slideUp);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        db.collection("collectors").document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && password.equals(documentSnapshot.getString("password"))) {
                        String collId = documentSnapshot.getString("collectorId");
                        
                        // Save session to SharedPreferences to prevent repeated login
                        SharedPreferences.Editor editor = getSharedPreferences("collector_prefs", MODE_PRIVATE).edit();
                        editor.putString("collectorId", collId);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, CollectorDashboardActivity.class);
                        intent.putExtra("collectorId", collId);
                        startActivity(intent);
                        finish();
                    } else {
                        performFirebaseAuthLogin(email, password);
                    }
                })
                .addOnFailureListener(e -> performFirebaseAuthLogin(email, password));
    }

    private void performFirebaseAuthLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String uid = currentUser.getUid();
        String email = currentUser.getEmail();

        if (email != null && email.equalsIgnoreCase("recycleit50@gmail.com")) {
            startActivity(new Intent(LoginActivity.this, MemberDashboardActivity.class));
            finish();
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Intent intent;
                        if ("user".equals(role)) {
                            intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                        } else if ("member".equals(role)) {
                            intent = new Intent(LoginActivity.this, MemberDashboardActivity.class);
                        } else if ("center".equals(role)) {
                            intent = new Intent(LoginActivity.this, CenterDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, RoleSelectActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
