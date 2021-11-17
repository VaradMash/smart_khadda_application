package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    TinyDB tinyDB;

    public void onGetStartedPressed(View view) {
        try {
            tinyDB = new TinyDB(getApplicationContext());
            if (tinyDB.getBoolean("userIsLoggedIn")) {
                Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(getApplicationContext(), RegisterScreenActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        try {
            tinyDB = new TinyDB(getApplicationContext());
            if (tinyDB.getBoolean("userIsLoggedIn")) {
                Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {}

    }
}