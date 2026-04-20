package com.example.ecowaste.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ecowaste.R;

public class AwarenessActivity extends AppCompatActivity {

    private Button btnWHO, btnUNEP, btnEwasteGuide, btnCpcb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awareness);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupExternalLinks();
    }

    private void initViews() {
        btnWHO = findViewById(R.id.btnWHO);
        btnUNEP = findViewById(R.id.btnUNEP);
        btnEwasteGuide = findViewById(R.id.btnEwasteGuide);
        btnCpcb = findViewById(R.id.btnCpcb);
    }

    private void setupExternalLinks() {
        if (btnWHO != null) {
            btnWHO.setOnClickListener(v -> openUrl("https://www.who.int/news-room/fact-sheets/detail/electronic-waste"));
        }

        if (btnUNEP != null) {
            btnUNEP.setOnClickListener(v -> openUrl("https://www.unep.org/explore-topics/resource-efficiency/what-we-do/cities/electronic-waste"));
        }
        
        if (btnEwasteGuide != null) {
            btnEwasteGuide.setOnClickListener(v -> openUrl("https://www.ewasteguide.info/"));
        }
        
        if (btnCpcb != null) {
            btnCpcb.setOnClickListener(v -> openUrl("https://cpcb.nic.in/e-waste-rules/"));
        }
    }
    
    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show();
        }
    }
}
