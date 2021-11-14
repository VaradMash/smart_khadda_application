package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class RegisterScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        getSupportActionBar().hide();

    }
}