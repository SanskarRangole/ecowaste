package com.example.ecowaste.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ecowaste.R;

public class RoleSelectActivity extends AppCompatActivity {

    private CardView cardUser, cardMember, cardCenter, cardCollector;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        cardUser = findViewById(R.id.cardUser);
        cardMember = findViewById(R.id.cardMember);
        cardCenter = findViewById(R.id.cardCenter);
        cardCollector = findViewById(R.id.cardCollector);
        tvLogin = findViewById(R.id.tvLogin);

        cardUser.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectActivity.this, UserRegisterActivity.class)));

        cardMember.setOnClickListener(v -> {
            Toast.makeText(this, "Member Registration is restricted. Please login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RoleSelectActivity.this, LoginActivity.class));
        });

        cardCenter.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectActivity.this, CenterRegisterActivity.class)));

        cardCollector.setOnClickListener(v -> {
            Toast.makeText(this, "Collector accounts are created by Members. Please login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RoleSelectActivity.this, LoginActivity.class));
        });

        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectActivity.this, LoginActivity.class)));
    }
}