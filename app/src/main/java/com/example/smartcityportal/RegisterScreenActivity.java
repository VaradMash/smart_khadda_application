package com.example.smartcityportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextInputEditText usernameTextInputEditText;
    TextInputEditText emailTextInputEditText;
    TextInputEditText phoneTextInputEditText;
    TinyDB tinyDB;

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
        String name = usernameTextInputEditText.getText().toString();
        if (Objects.requireNonNull(emailTextInputEditText.getText()).toString().equals("") ||
                Objects.requireNonNull(phoneTextInputEditText.getText()).toString().equals("") ||
                Objects.requireNonNull(usernameTextInputEditText.getText()).toString().equals("")) {
            showCustomSnackBar(view, "All fields are mandatory.");
        }
        else if (phoneNumber.length() != 10) {
            showCustomSnackBar(view, "Enter a valid phone number.");
        }
        else if (!emailAddress.contains("@") && !emailAddress.contains(".com")) {
            showCustomSnackBar(view, "Enter a valid Email Id");
        }
        else {
            phoneNumber = "+91" + phoneNumber;
            tinyDB.putString("userName", usernameTextInputEditText.getText().toString());
            tinyDB.putString("userEmail", emailTextInputEditText.getText().toString());
            tinyDB.putString("userPhoneNumber", phoneTextInputEditText.getText().toString());
            Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
            intent.putExtra("phone", phoneNumber);
            intent.putExtra("email", emailAddress);
            intent.putExtra("name", name);
            startActivity(intent);
            RegisterScreenActivity.this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        getSupportActionBar().hide();
        tinyDB = new TinyDB(getApplicationContext());

        usernameTextInputEditText = findViewById(R.id.usernameTextInputEditText);
        emailTextInputEditText = findViewById(R.id.email2TextInputEditText);
        phoneTextInputEditText = findViewById(R.id.phoneTextInputEditText);

    }
}