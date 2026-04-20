package com.example.ecowaste.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecowaste.R;
import com.github.chrisbanes.photoview.PhotoView;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView btnBack, btnDownload, btnShare;
    private PhotoView fullScreenImageView;
    private TextView tvTitle, tvDescription, tvDate, tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String location = getIntent().getStringExtra("location");

        initViews();
        setupViews(title, description, date, location);
        loadImage(imageUrl);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnDownload = findViewById(R.id.btnDownload);
        btnShare = findViewById(R.id.btnShare);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        fullScreenImageView = findViewById(R.id.fullScreenImageView);

        btnBack.setOnClickListener(v -> finish());
        btnDownload.setOnClickListener(v -> downloadImage());
        btnShare.setOnClickListener(v -> shareImage());
    }

    private void setupViews(String title, String description, String date, String location) {
        if (title != null) tvTitle.setText(title);
        else tvTitle.setVisibility(View.GONE);

        if (description != null) tvDescription.setText(description);
        else tvDescription.setVisibility(View.GONE);

        if (date != null) tvDate.setText(date);
        else tvDate.setVisibility(View.GONE);

        if (location != null) tvLocation.setText(location);
        else tvLocation.setVisibility(View.GONE);
    }

    private void loadImage(String imageUrl) {
        Glide.with(this).load(imageUrl).into(fullScreenImageView);
    }

    private void downloadImage() {
        // Implement download functionality
    }

    private void shareImage() {
        // Implement share functionality
    }
}
