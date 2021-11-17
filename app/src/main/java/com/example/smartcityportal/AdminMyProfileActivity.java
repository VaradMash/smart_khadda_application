package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class AdminMyProfileActivity extends AppCompatActivity {

    public void onBackPressed (View view) {
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_my_profile);

        getSupportActionBar().hide();
    }
}