package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyProfileActivity extends AppCompatActivity {

    public void onBackPressed (View view) {
        this.finish();
    }

    private DocumentReference user_document;
    TextInputEditText usernameTextInputEditText;
    TextInputEditText emailTextInputEditText;
    TextInputEditText phoneTextInputEditText;
    TextView complaintCountTextView;
    String username;
    String email;
    String phone;
    String complaint_count;
    TinyDB tinyDB;

    public void setValues() {
        try {
            username = tinyDB.getString("userName");
            email = tinyDB.getString("userEmail");
            phone = tinyDB.getString("userPhoneNumber");
            // capture value for number of complaints
            phone = "+91" + phone;
            user_document = FirebaseFirestore.getInstance().collection("user_data").document(phone);
            user_document.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot user_document = task.getResult();
                            if (task.isSuccessful())
                            {
                                complaint_count = user_document.get("total_complaints").toString();
                                complaintCountTextView.setText(complaint_count);
                            }
                            else
                            {
                                Toast.makeText(MyProfileActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                complaintCountTextView.setText("No data");
                            }
                        }
                    });
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

        getSupportActionBar().hide();

        usernameTextInputEditText = findViewById(R.id.usernameTextInputEditText);
        emailTextInputEditText = findViewById(R.id.email2TextInputEditText);
        phoneTextInputEditText = findViewById(R.id.phone2TextInputEditText);
        complaintCountTextView = findViewById(R.id.complaintCountTextView);
        tinyDB = new TinyDB(getApplicationContext());

        setValues();

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
        MyProfileActivity.this.finish();
    }
}