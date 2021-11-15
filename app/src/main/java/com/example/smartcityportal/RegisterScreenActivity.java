package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextInputEditText usernameTextInputEditText;
    TextInputEditText emailTextInputEditText;
    TextInputEditText phoneTextInputEditText;

    // This method avoids multiple SnackBars showing if one is currently being displayed
    public void showCustomSnackBar (View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Got it", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void getOtpButtonClicked (View view) {
        String phoneNumber = phoneTextInputEditText.getText().toString();
        String emailAddress = emailTextInputEditText.getText().toString();
        if (emailAddress.equals("") || phoneNumber.equals("")) {
            showCustomSnackBar(view, "All fields are mandatory.");
        }
        else if (phoneNumber.length() != 10) {
            showCustomSnackBar(view, "Enter a valid phone number.");
        }
        else if (!emailAddress.contains("@")) {
            showCustomSnackBar(view, "Enter a valid Email Id");
        }
        else {
            phoneNumber = "+91" + phoneNumber;
            Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
            intent.putExtra("phone", phoneNumber);
            intent.putExtra("email", emailAddress);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        getSupportActionBar().hide();

        usernameTextInputEditText = findViewById(R.id.usernameTextInputEditText);
        emailTextInputEditText = findViewById(R.id.emailTextInputEditText);
        phoneTextInputEditText = findViewById(R.id.phoneTextInputEditText);

    }
}