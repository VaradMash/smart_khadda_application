package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class AdminMyProfileActivity extends AppCompatActivity {

    TextInputEditText adminUsernameTextInputEditText;
    TextInputEditText adminEmailTextInputEditText;
    TextInputEditText adminPhoneTextInputEditText;
    String username;
    String email;
    String phone;
    TinyDB tinyDB;

    public void onBackPressed (View view) {
        Intent intent = new Intent(getApplicationContext(), AdminComplaintsActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void setValues() {
        try {
            username = tinyDB.getString("userName");
            email = tinyDB.getString("userEmail");
            phone = "+91" + tinyDB.getString("userPhoneNumber");
            adminUsernameTextInputEditText.setText(username);
            adminEmailTextInputEditText.setText(email);
            adminPhoneTextInputEditText.setText(phone);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_my_profile);

        getSupportActionBar().hide();

        adminUsernameTextInputEditText = findViewById(R.id.adminUsernameTextInputEditText);
        adminEmailTextInputEditText = findViewById(R.id.adminEmail2TextInputEditText);
        adminPhoneTextInputEditText = findViewById(R.id.adminPhone2TextInputEditText);
        tinyDB = new TinyDB(getApplicationContext());

        setValues();
    }
}