package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
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

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private final int TIMEOUT = 30000;
    private FirebaseAuth mAuth;
    private String phone, email, verificationId;
    boolean resendOtpAllowed = false;
    TextView resendOtpTextView;
    TextView timerTextView;
    CountDownTimer countDownTimer;
    TextInputEditText emailOtpTextInputEditText;
    TextInputEditText smsOtpTextInputEditText;

    public void otpConfirmedClicked (View view) {
        if (emailOtpTextInputEditText.getText().toString().equals("") || smsOtpTextInputEditText.getText().toString().equals("")) {
            showCustomSnackBar(view, "All fields are mandatory.");
            return;
        }

        String code = smsOtpTextInputEditText.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            Toast.makeText(OtpActivity.this, "Sign In Successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
                showCustomSnackBar(timerTextView, "Did not recieve OTP? Tap on Resend");
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

    private void getOTP()
    {
        // Invoke this method to get OTP on email address and phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)                               // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS)    // Timeout and unit
                        .setActivity(this)                                  // Activity (for callback binding)
                        .setCallbacks(mCallBack)                            // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.


                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                Toast.makeText(OtpActivity.this, code, Toast.LENGTH_SHORT).show();
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");

        // Request for OTP
        getOTP();

        resendOtpTextView = findViewById(R.id.resendOtpTextView);
        timerTextView = findViewById(R.id.timerTextView);
        emailOtpTextInputEditText = findViewById(R.id.emailOtpTextInputEditText);
        smsOtpTextInputEditText = findViewById(R.id.smsOtpTextInputEditText);

        startCounter();
    }
}