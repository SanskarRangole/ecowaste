package com.example.ecowaste.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ecowaste.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private View logoView;
    private TextView tvAppName, tvTagline;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoView = findViewById(R.id.logoCard);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        startAnimations();
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                proceedAfterSplash();
            }
        } else {
            proceedAfterSplash();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            proceedAfterSplash();
        }
    }

    private void proceedAfterSplash() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Priority 1: Check for saved Collector Session (since they might not use Firebase Auth)
            SharedPreferences prefs = getSharedPreferences("collector_prefs", MODE_PRIVATE);
            String savedCollectorId = prefs.getString("collectorId", null);
            if (savedCollectorId != null) {
                Intent intent = new Intent(SplashActivity.this, CollectorDashboardActivity.class);
                intent.putExtra("collectorId", savedCollectorId);
                startActivity(intent);
                finish();
                return;
            }

            // Priority 2: Check Firebase Auth Session
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                checkUserRoleAndRedirect(currentUser);
            } else {
                startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                finish();
            }
        }, 2000); 
    }

    private void startAnimations() {
        if (logoView != null) {
            logoView.setScaleX(0f);
            logoView.setScaleY(0f);
            logoView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1200)
                    .setInterpolator(new AnticipateOvershootInterpolator())
                    .start();
        }

        tvAppName.animate().alpha(1f).translationY(0f).setDuration(1000).setStartDelay(600).start();
        tvTagline.animate().alpha(1f).translationY(0f).setDuration(1000).setStartDelay(900).start();
        progressBar.animate().alpha(1f).setDuration(800).setStartDelay(1200).start();
    }

    private void checkUserRoleAndRedirect(FirebaseUser user) {
        String email = user.getEmail();
        
        // Immediate check for hardcoded member
        if (email != null && email.equalsIgnoreCase("recycleit50@gmail.com")) {
            startActivity(new Intent(SplashActivity.this, MemberDashboardActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent;
                        if ("user".equals(role)) {
                            intent = new Intent(SplashActivity.this, UserDashboardActivity.class);
                        } else if ("member".equals(role)) {
                            intent = new Intent(SplashActivity.this, MemberDashboardActivity.class);
                        } else if ("center".equals(role)) {
                            intent = new Intent(SplashActivity.this, CenterDashboardActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, RoleSelectActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        // If no user doc, might be a collector with Auth but no 'users' doc
                        // Or session is stale.
                        mAuth.signOut();
                        startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                    finish();
                });
    }
}
