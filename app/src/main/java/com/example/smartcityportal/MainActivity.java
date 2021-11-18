package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TinyDB tinyDB;
    FirebaseAuth mAuth;
    FirebaseUser user;

    public void onGetStartedPressed(View view) {
//        try {
//            tinyDB = new TinyDB(getApplicationContext());
//            if (tinyDB.getBoolean("userIsLoggedIn")) {
//                Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
//                startActivity(intent);
//            }
//            else {
//                Intent intent = new Intent(getApplicationContext(), RegisterScreenActivity.class);
//                startActivity(intent);
//            }
//        } catch (Exception e) {
        Intent intent;
        if (user != null)
        {
            intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
        }
        else
        {
            intent = new Intent(getApplicationContext(), RegisterScreenActivity.class);
        }
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

//        try {
//            tinyDB = new TinyDB(getApplicationContext());
//            if (tinyDB.getBoolean("userIsLoggedIn")) {
//                Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
//                startActivity(intent);
//            }
//        } catch (Exception e) {}
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null)
        {
            Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
            startActivity(intent);
        }
    }
}