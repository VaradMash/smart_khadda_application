package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    TinyDB tinyDB;
    FirebaseAuth mAuth;
    FirebaseUser user;
    private ProgressBar pbMainActivity;
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

        try {
            pbMainActivity.setVisibility(View.VISIBLE);
            tinyDB = new TinyDB(getApplicationContext());
            String phone = "+91" + tinyDB.getString("userPhoneNumber");
            DocumentReference user_document = FirebaseFirestore.getInstance()
                    .collection("user_data")
                    .document();
            user_document.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot user_details = task.getResult();
                                String role = user_details.get("user_type").toString();
                                Intent intent;
                                if (role.equals("user")) {
                                    intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
                                } else {
                                    intent = new Intent(getApplicationContext(), AdminComplaintsActivity.class);
                                }
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), RegisterScreenActivity.class);
                                startActivity(intent);
                            }
                            pbMainActivity.setVisibility(View.GONE);
                            MainActivity.this.finish();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

        pbMainActivity = findViewById(R.id.pbMainActivity);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null)
        {
            try
            {
                pbMainActivity.setVisibility(View.VISIBLE);
                tinyDB = new TinyDB(getApplicationContext());
                String phone = "+91" + tinyDB.getString("userPhoneNumber");
                DocumentReference user_document = FirebaseFirestore.getInstance()
                        .collection("user_data")
                        .document(phone);
                user_document.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    DocumentSnapshot user_details = task.getResult();
                                    String role = user_details.get("user_type").toString();
                                    Intent intent;
                                    if (role.equals("user"))
                                    {
                                        intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
                                    }
                                    else
                                    {
                                        intent = new Intent(getApplicationContext(), AdminComplaintsActivity.class);
                                    }
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                    pbMainActivity.setVisibility(View.GONE);
                                }
                                else
                                {
                                    Log.d("Debug", task.getException().toString());
                                    Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    pbMainActivity.setVisibility(View.GONE);
                                }
                            }
                        });
            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            pbMainActivity.setVisibility(View.GONE);
        }
    }
}