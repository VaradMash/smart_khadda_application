package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private final int TIMEOUT = 30000;
    private FirebaseAuth mAuth;
    private String phone, email, verificationId, name;
    boolean resendOtpAllowed = false;
    TextView resendOtpTextView;
    TextView timerTextView;
    ProgressBar pbOTPActivity;
    CountDownTimer countDownTimer;
    TextInputEditText smsOtpTextInputEditText;

    public void otpConfirmedClicked (View view) {

        String code = smsOtpTextInputEditText.getText().toString();

        if (code.isEmpty()) {
            showCustomSnackBar(view, "All fields are mandatory.");
        }
        else {
            verifyCode(code);
        }
    }

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

    public void resendOtpClicked (View view) {
        if (!resendOtpAllowed) {
            showCustomSnackBar(resendOtpTextView, "Please wait till timer expires.");
        }
        else {
            sendVerificationCode(phone);
            startCounter();
            showCustomSnackBar(resendOtpTextView, "OTP Resent.");
            resendOtpAllowed = false;
        }
    }

    public void startCounter () {
        countDownTimer = new CountDownTimer(TIMEOUT, 1000) {
            @Override
            public void onTick(long l) {
                updateTimer((int) l / 1000);
            }

            @Override
            public void onFinish() {
                showCustomSnackBar(timerTextView, "Did not receive OTP? Tap on Resend");
                countDownTimer.cancel();
                resendOtpAllowed = true;
            }
        }.start();
    }

    public void updateTimer(int secondsLeft) {
        int minutes = secondsLeft/60;
        int seconds = secondsLeft - (minutes*60);
        String secondStrings =Integer.toString(seconds);
        if (0 <= Integer.parseInt(secondStrings) && Integer.parseInt(secondStrings) < 10)
            secondStrings = "0" + secondStrings;
        timerTextView.setText("Wait: " + minutes + ":" + secondStrings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");
        name = intent.getStringExtra("name");

        resendOtpTextView = findViewById(R.id.resendOtpTextView);
        timerTextView = findViewById(R.id.timerTextView);
        smsOtpTextInputEditText = findViewById(R.id.smsOtpTextInputEditText);
        pbOTPActivity = findViewById(R.id.pbOTPActivity);

        // send verification code
        sendVerificationCode(phone);
        startCounter();
    }

    // Backend routines
    private void sendVerificationCode(String phone)
    {
        /*
         *  Input   :   Phone number
         *  Utility :   Send verification code to phone.
         *  Output  :   None
         */
        pbOTPActivity.setVisibility(View.VISIBLE);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)                          // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)      // Timeout and unit
                        .setActivity(this)                              // Activity (for callback binding)
                        .setCallbacks(mCallbacks)                       // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        pbOTPActivity.setVisibility(View.GONE);
    }

    // Setting callback for phone authentication provider
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks
            = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null)
            {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // Log error
            Log.d("Debug", e.getMessage());
            // Display error message to screen.
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String code)
    {
        /*
         *  Input   :   OTP
         *  Utility :   Verify OTP sent by system.
         *  Output  :   None
         */
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(this.verificationId, code);
        signInUserWithCredentials(credential);
    }

    private void update_database()
    {
        /*
         *  Input   :   None
         *  Utility :   Update database if user document does not exist.
         *  Output  :   None
         */
        CollectionReference user_collection = FirebaseFirestore.getInstance().collection("user_data");
        Map<String, Object> user_data = new HashMap<>();
        user_data.put("username", name);
        user_data.put("email", email);
        user_data.put("phone_number", phone);
        user_data.put("user_type", "user");
        user_data.put("total_complaints", 0);
        user_collection.document(phone)
                .set(user_data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(OtpActivity.this, "Welcome !", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(OtpActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInUserWithCredentials(PhoneAuthCredential credential) {
        /*
         *  Input   :   Credentials
         *  Utility :   Sign in user with credentials
         *  Output  :   None
         */
        mAuth.signInWithCredential(credential).
                addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            pbOTPActivity.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "OTP verified successfully !", Toast.LENGTH_SHORT).show();
                            OtpActivity.this.finish();
                            Task<QuerySnapshot> user_reference = FirebaseFirestore.getInstance().collection("user_data")
                                    .whereEqualTo("phone_number", phone).get();
                            user_reference.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.getResult().isEmpty())
                                    {
                                        update_database();
                                        Intent intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
                                        startActivity(intent);
                                        pbOTPActivity.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        DocumentSnapshot user_document = task.getResult().getDocuments().get(0);
                                        String user_type = user_document.get("user_type").toString();
                                        Intent intent;
                                        if (user_type.equals("user"))
                                        {
                                            intent = new Intent(getApplicationContext(), RegisterComplaintActivity.class);
                                        }
                                        else
                                        {
                                            intent = new Intent(getApplicationContext(), AdminComplaintsActivity.class);
                                        }
                                        startActivity(intent);
                                        pbOTPActivity.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*
         *  Input   :   Back Button press
         *  Utility :   Navigate to registration activity
         *  Output  :   Registration activity launch
         */
        Intent intent = new Intent(getApplicationContext(), RegisterScreenActivity.class);
        startActivity(intent);
        this.finish();
    }
}