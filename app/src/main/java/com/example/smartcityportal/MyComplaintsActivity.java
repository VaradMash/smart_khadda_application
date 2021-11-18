package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MyComplaintsActivity extends AppCompatActivity {

    public void onBackPressed (View view) {
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);
        getSupportActionBar().hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*
         *  Input   :   Back button press
         *  Utility :   Route to RegisterComplaintActivity.
         *  Output  :   Activity launch
         */
        Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
        startActivity(intent);
        MyComplaintsActivity.this.finish();
    }
}