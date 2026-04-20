package com.example.ecowaste.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ecowaste.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, btnEdit;
    private ImageView profileImage;
    private TextView tvUserName, tvUserEmail, tvStat1, tvStat2, tvStat1Label, tvStat2Label, tvToolbarTitle;
    private Chip chipRole;
    private CardView cardStat1, cardStat2;
    private LinearLayout menuEditProfile, menuNotifications;
    private MaterialButton btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String userRole = "user";
    private boolean isSpecialMember = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        
        if (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase("recycleit50@gmail.com")) {
            isSpecialMember = true;
        }

        initViews();
        loadUserData();
        setupClickListeners();
        startEntryAnimations();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        profileImage = findViewById(R.id.profileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        chipRole = findViewById(R.id.chipRole);
        cardStat1 = findViewById(R.id.cardStat1);
        cardStat2 = findViewById(R.id.cardStat2);
        tvStat1 = findViewById(R.id.tvStat1);
        tvStat2 = findViewById(R.id.tvStat2);
        tvStat1Label = findViewById(R.id.tvStat1Label);
        tvStat2Label = findViewById(R.id.tvStat2Label);
        menuEditProfile = findViewById(R.id.menuEditProfile);
        menuNotifications = findViewById(R.id.menuNotifications);
        btnLogout = findViewById(R.id.btnLogout);
        
        if (isSpecialMember) {
            tvToolbarTitle.setText("Member Profile");
            btnEdit.setVisibility(View.GONE);
            menuEditProfile.setVisibility(View.GONE);
            profileImage.setImageResource(R.drawable.logo); 
        }
    }

    private void loadUserData() {
        if (isSpecialMember) {
            tvUserName.setText("ReCycleIT");
            tvUserEmail.setText("recycleit50@gmail.com");
            chipRole.setVisibility(View.VISIBLE);
            chipRole.setText("RECYCLEIT MEMBER");
            userRole = "member";
            updateStatsLabels(userRole);
            loadUserStats(userRole);
            return;
        }

        db.collection("users").document(currentUserId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) return;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String role = documentSnapshot.getString("role");
                        userRole = role != null ? role : "user";

                        tvUserName.setText(name != null ? name : "User");
                        tvUserEmail.setText(email != null ? email : "email@example.com");

                        if (role != null) {
                            chipRole.setVisibility(View.VISIBLE);
                            chipRole.setText(role.toUpperCase());
                        }

                        if ("center".equals(userRole)) {
                            tvToolbarTitle.setText("Center Profile");
                            profileImage.setImageResource(R.drawable.pro_logo);
                        } else {
                            tvToolbarTitle.setText("User Profile");
                        }

                        updateStatsLabels(userRole);
                        loadUserStats(userRole);
                    }
                });
    }

    private void updateStatsLabels(String role) {
        if ("user".equals(role)) {
            tvStat1Label.setText("Pickups");
            tvStat2Label.setText("Eco Points");
        } else if ("member".equals(role)) {
            tvStat1Label.setText("Processed");
            tvStat2Label.setText("Rating");
        } else if ("center".equals(role)) {
            tvStat1Label.setText("Waste (kg)");
            tvStat2Label.setText("Scheduled");
        }
    }

    private void loadUserStats(String role) {
        if ("user".equals(role)) {
            db.collection("pickups").whereEqualTo("userId", currentUserId).get()
                    .addOnSuccessListener(qds -> animateNumberChange(tvStat1, 0, qds.size()));
            db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener(ds -> {
                        Long p = ds.getLong("ecoPoints");
                        animateNumberChange(tvStat2, 0, p != null ? p.intValue() : 0);
                    });
        } else if ("member".equals(role)) {
            db.collection("pickups").whereEqualTo("memberId", currentUserId).get()
                    .addOnSuccessListener(qds -> animateNumberChange(tvStat1, 0, qds.size()));
            tvStat2.setText("4.8");
        } else if ("center".equals(role)) {
            db.collection("pickups").whereEqualTo("centerId", currentUserId).whereEqualTo("status", "COMPLETED").get()
                    .addOnSuccessListener(qds -> {
                        double total = 0;
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : qds) {
                            Double w = doc.getDouble("estimatedWeight");
                            if (w != null) total += w;
                        }
                        animateNumberChange(tvStat1, 0, (int) total);
                    });
            db.collection("pickups").whereEqualTo("centerId", currentUserId).whereEqualTo("status", "ACCEPTED_BY_CENTER").get()
                    .addOnSuccessListener(qds -> animateNumberChange(tvStat2, 0, qds.size()));
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> showEditProfileDialog());
        menuEditProfile.setOnClickListener(v -> showEditProfileDialog());
        menuNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Logout").setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }).setNegativeButton("No", null).show();
        });
    }

    private void showEditProfileDialog() {
        if (isSpecialMember) {
            Toast.makeText(this, "Admin profile cannot be edited.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);

        db.collection("users").document(currentUserId).get().addOnSuccessListener(ds -> {
            if (ds.exists()) {
                etName.setText(ds.getString("name"));
                etPhone.setText(ds.getString("phone"));
                etAddress.setText(ds.getString("address"));
            }
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("phone", newPhone);
            updates.put("address", newAddress);

            db.collection("users").document(currentUserId).update(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void startEntryAnimations() {
        profileImage.setScaleX(0f);
        profileImage.setScaleY(0f);
        profileImage.animate().scaleX(1f).scaleY(1f).setDuration(500).setInterpolator(new BounceInterpolator()).start();
    }

    private void animateNumberChange(final TextView textView, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> textView.setText(animation.getAnimatedValue().toString()));
        animator.start();
    }
}
