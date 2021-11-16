package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class MyProfileActivity extends AppCompatActivity {

    public void onBackPressed (View view) {
        this.finish();
    }

    TextInputEditText usernameTextInputEditText;
    TextInputEditText emailTextInputEditText;
    TextInputEditText phoneTextInputEditText;
    TextView complaintCountTextView;
    String username;
    String email;
    String phone;
    TinyDB tinyDB;

    public void setValues() {
        try {
            username = tinyDB.getString("userName");
            email = tinyDB.getString("userEmail");
            phone = tinyDB.getString("userPhoneNumber");
            usernameTextInputEditText.setText(username);
            emailTextInputEditText.setText(email);
            phoneTextInputEditText.setText(phone);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        usernameTextInputEditText = findViewById(R.id.usernameTextInputEditText);
        emailTextInputEditText = findViewById(R.id.emailOtpTextInputEditText);
        phoneTextInputEditText = findViewById(R.id.phoneTextInputEditText);
        complaintCountTextView = findViewById(R.id.complaintCountTextView);

        tinyDB = new TinyDB(getApplicationContext());

        setValues();

    }
}