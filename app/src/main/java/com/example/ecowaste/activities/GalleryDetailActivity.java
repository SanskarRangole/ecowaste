package com.example.ecowaste.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.ecowaste.R;

public class GalleryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        ImageView ivDetailImage = findViewById(R.id.ivDetailImage);
        TextView tvDetailTitle = findViewById(R.id.tvDetailTitle);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        int imageResId = getIntent().getIntExtra("imageResId", 0);
        String title = getIntent().getStringExtra("title");

        if (imageResId != 0) {
            Glide.with(this).load(imageResId).into(ivDetailImage);
        }
        
        if (title != null) {
            tvDetailTitle.setText(title);
        }
    }
}
