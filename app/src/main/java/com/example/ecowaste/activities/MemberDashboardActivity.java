package com.example.ecowaste.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.MemberPagerAdapter;
import com.example.ecowaste.models.PickupModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;

public class MemberDashboardActivity extends AppCompatActivity {

    private TextView tvMemberName, tvTotalPickups, tvTodayPickups, tvEarnings, tvRating, tvRatingCount;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private ExtendedFloatingActionButton fabSaleWaste, fabAddCollector;

    private FirebaseFirestore db;
    private String currentUserId;
    private boolean isSpecialMember = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_dashboard);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            if (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase("recycleit50@gmail.com")) {
                isSpecialMember = true;
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupViewPager();
        loadMemberData();
        setupBottomNavigation();
        setupRealtimeNotifications();
    }

    private void initViews() {
        ImageView profileImage = findViewById(R.id.profileImage);
        tvMemberName = findViewById(R.id.tvMemberName);
        tvTotalPickups = findViewById(R.id.tvTotalPickups);
        tvTodayPickups = findViewById(R.id.tvTodayPickups);
        tvEarnings = findViewById(R.id.tvEarnings);
        tvRating = findViewById(R.id.tvRating);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabSaleWaste = findViewById(R.id.fabSaleWaste);
        fabAddCollector = findViewById(R.id.fabAddCollector);

        if (isSpecialMember) {
            profileImage.setImageResource(R.drawable.logo);
        }

        profileImage.setOnClickListener(v ->
                startActivity(new Intent(MemberDashboardActivity.this, ProfileActivity.class)));
                
        fabSaleWaste.setOnClickListener(v -> 
                startActivity(new Intent(MemberDashboardActivity.this, BulkSaleActivity.class)));

        fabAddCollector.setOnClickListener(v ->
                startActivity(new Intent(MemberDashboardActivity.this, AddCollectorActivity.class)));
    }

    private void setupViewPager() {
        MemberPagerAdapter adapter = new MemberPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("New Requests"); break;
                        case 1: tab.setText("Ongoing"); break;
                        case 2: tab.setText("Stock/Bulk"); break;
                        case 3: tab.setText("Finished"); break;
                    }
                }).attach();
    }

    private void setupRealtimeNotifications() {
        db.collection("pickups")
                .whereEqualTo("status", "PENDING_REVIEW")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            showLocalNotification("New Request Available", "A user has submitted a new e-waste pickup request.");
                        }
                    }
                });
        
        // Notification for Mismatch Reported
        db.collection("pickups")
                .whereEqualTo("status", "MISMATCH_REPORTED")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                            showLocalNotification("Discrepancy Alert", "A field agent reported an item mismatch.");
                        }
                    }
                });
    }

    private void loadMemberData() {
        if (isSpecialMember) {
            tvMemberName.setText("ReCycleIT HQ");
        } else {
            db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
                tvMemberName.setText(doc.getString("name") != null ? doc.getString("name") : "Member");
            });
        }
        loadPickupStats();
        loadRating();
    }

    private void loadPickupStats() {
        // Broaden stats to include PICKED_UP status as success
        db.collection("pickups").whereEqualTo("memberId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        double totalWeight = 0;
                        double totalProfit = 0;
                        int successfulCount = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            PickupModel pickup = doc.toObject(PickupModel.class);
                            String status = pickup.getStatus();
                            
                            if ("PICKED_UP".equals(status) || "COLLECTED".equals(status) || "COMPLETED".equals(status) || "SOLD".equals(status)) {
                                totalWeight += pickup.getEstimatedWeight();
                                totalProfit += (pickup.getBaseAmount() - pickup.getUserAmount());
                                successfulCount++;
                            }
                        }
                        tvTotalPickups.setText(String.format(Locale.getDefault(), "%.1f kg", totalWeight));
                        tvEarnings.setText(String.format(Locale.getDefault(), "₹%.0f", totalProfit));
                        tvTodayPickups.setText(String.valueOf(successfulCount));
                    }
                });
    }

    private void loadRating() {
        tvRating.setText("4.9");
        tvRatingCount.setText("(32 reviews)");
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, PickupHistoryActivity.class));
                return true;
            }
            if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showLocalNotification(String title, String message) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "member_notifications_channel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Member Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
