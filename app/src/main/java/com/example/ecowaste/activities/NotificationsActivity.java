package com.example.ecowaste.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecowaste.R;
import com.example.ecowaste.adapters.NotificationAdapter;
import com.example.ecowaste.models.NotificationModel;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private TabLayout tabLayout;

    private FirebaseFirestore db;
    private NotificationAdapter adapter;
    private final List<NotificationModel> allNotifications = new ArrayList<>();
    private final List<NotificationModel> displayedNotifications = new ArrayList<>();
    private String currentUserId;
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupTabs();
        loadNotifications();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView btnMarkAll = findViewById(R.id.btnMarkAll);
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (btnMarkAll != null) {
            btnMarkAll.setOnClickListener(v -> markAllAsRead());
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, displayedNotifications, notification -> {
            if (!notification.isRead()) {
                markAsRead(notification.getId());
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterNotifications(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        if (notificationListener != null) {
            notificationListener.remove();
        }

        // Removed orderBy("timestamp") to avoid mandatory composite index requirement
        // We will sort manually in Java to ensure it works even if indexes aren't created yet
        notificationListener = db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Notifications", "Listen failed: " + error.getMessage(), error);
                        progressBar.setVisibility(View.GONE);
                        if (allNotifications.isEmpty()) emptyState.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (value != null) {
                        allNotifications.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            try {
                                NotificationModel notification = doc.toObject(NotificationModel.class);
                                notification.setId(doc.getId());
                                allNotifications.add(notification);
                            } catch (Exception e) {
                                Log.e("Notifications", "Error parsing", e);
                            }
                        }
                        
                        // Manual Sort: Newest first
                        Collections.sort(allNotifications, (n1, n2) -> {
                            if (n1.getTimestamp() == null || n2.getTimestamp() == null) return 0;
                            return n2.getTimestamp().compareTo(n1.getTimestamp());
                        });
                        
                        int currentTab = tabLayout.getSelectedTabPosition();
                        if (currentTab < 0) currentTab = 0; 
                        
                        filterNotifications(currentTab);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterNotifications(int position) {
        displayedNotifications.clear();
        
        if (position == 0) { // "ALL"
            displayedNotifications.addAll(allNotifications);
        } else { // "UNREAD"
            for (NotificationModel n : allNotifications) {
                if (!n.isRead()) {
                    displayedNotifications.add(n);
                }
            }
        }

        adapter.notifyDataSetChanged();
        
        if (displayedNotifications.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void markAsRead(String notificationId) {
        db.collection("notifications").document(notificationId)
                .update("read", true);
    }

    private void markAllAsRead() {
        WriteBatch batch = db.batch();
        boolean hasUnread = false;
        for (NotificationModel notification : allNotifications) {
            if (!notification.isRead()) {
                batch.update(db.collection("notifications").document(notification.getId()), "read", true);
                hasUnread = true;
            }
        }
        if (hasUnread) {
            batch.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}
