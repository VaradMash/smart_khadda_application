package com.example.smartcityportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class RegisterComplaintActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseStorage storage;
    TextView locationTextView;
    TextView imageCaptionTextView;
    TextInputEditText locationTextInputEditText, descriptionTextInputEditText;
    ProgressBar pbRegisterComplaint;
    ImageView photoImageView;
    private Menu menuList;
    String address = "";
    LinearLayout imageContainerLinearLayout;
    Uri image;
    String mCameraFileName;
    FirebaseAuth mAuth;
    boolean imageIsCaptured = false;
    TinyDB tinyDB;
    boolean userIsLoggedIn = true;
    private String locality;
    private Location current_location;

    public void onRefreshTapped(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void uploadPhotoPressed(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else if (imageIsCaptured) {
            showCustomSnackBar(imageCaptionTextView, "Photo already present. Press refresh to upload a new one.");
        } else {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            Date date = new Date();
            DateFormat df = new SimpleDateFormat("-mm-ss");

            String newPicFile = df.format(date) + ".jpg";
            String outPath = "/sdcard/" + newPicFile;
            File outFile = new File(outPath);

            mCameraFileName = outFile.toString();
            Uri outuri = Uri.fromFile(outFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);

            //Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // startActivityForResult(camera_intent, pic_id);
            startActivityForResult(intent, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                if (data != null) {
                    image = data.getData();
                    photoImageView.setImageURI(image);
                    photoImageView.setVisibility(View.VISIBLE);
                }
                if (image == null && mCameraFileName != null) {
                    image = Uri.fromFile(new File(mCameraFileName));
                    photoImageView.setImageURI(image);
                    photoImageView.setVisibility(View.VISIBLE);
                }
                File file = new File(mCameraFileName);
                if (!file.exists()) {
                    file.mkdir();
                }
                imageIsCaptured = true;
                imageCaptionTextView.setText("Above Photo will be Submitted");
            }
        }
        //Bitmap photo = (Bitmap) data.getExtras().get("data");
        //photoImageView.setImageBitmap(photo);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // This method avoids multiple SnackBars showing if one is currently being displayed
    public void showCustomSnackBar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Got it", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                {
                    renderLocation();
                }
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            showCustomSnackBar(locationTextView, "Please Grant all the Permissions.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menuList = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.myProfileMenuItem:
                intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.myComplaintsMenuItem:
                intent = new Intent(getApplicationContext(), MyComplaintsActivity.class);
                startActivity(intent);
                break;
            case R.id.logOutMenuItem:
                mAuth.signOut();
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                RegisterComplaintActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_complaint);

        tinyDB = new TinyDB(getApplicationContext());
        tinyDB.putBoolean("userIsLoggedIn", userIsLoggedIn);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationTextView = findViewById(R.id.locationTextView);
        imageCaptionTextView = findViewById(R.id.imageCaptionTextView);
        photoImageView = findViewById(R.id.photoImageView);
        locationTextInputEditText = findViewById(R.id.locationRCTextInputEditText);
        descriptionTextInputEditText = findViewById(R.id.descriptionTextInputEditText);
        imageContainerLinearLayout = findViewById(R.id.imageContainerLinearLayout);
        pbRegisterComplaint = findViewById(R.id.pbRegisterComplaint);

        renderLocation();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }

    private void renderLocation() {
        /*
         *  Input   :   None
         *  Utility :   Render location to edit text.
         *  Output  :   None
         */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        pbRegisterComplaint.setVisibility(View.VISIBLE);
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            current_location = task.getResult();
                            Geocoder geocoder = new Geocoder(RegisterComplaintActivity.this,
                                    Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(
                                        current_location.getLatitude(),
                                        current_location.getLongitude(),
                                        1);
                                Address current_address = addresses.get(0);
                                locality = current_address.getLocality();
                                address = current_address.getAddressLine(0);
                                locationTextInputEditText.setText(address);
                            } catch (IOException e) {
                                Toast.makeText(RegisterComplaintActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterComplaintActivity.this, "Could not acquire location !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        pbRegisterComplaint.setVisibility(View.GONE);
    }

    public void submitComplaint(View v)
    {
        /*
         *  Input   :   None
         *  Utility :   Submit complaint and update database with relevant details.
         *  Output  :   None
         */

        String description = descriptionTextInputEditText.getText().toString();

        if(description.isEmpty())
        {
            descriptionTextInputEditText.setError("Description cannot be empty !");
            descriptionTextInputEditText.requestFocus();
        }
        else if (address.isEmpty())
        {
            locationTextInputEditText.setError("Location cannot be empty !");
            locationTextInputEditText.requestFocus();
        }
        else if (!imageIsCaptured)
        {
            showCustomSnackBar(imageCaptionTextView, "Please capture image");
        }
        else
        {
            // Capture Date and time
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy_HH:mm:ss", Locale.getDefault());
            SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String complaint_date = date.format(new Date());
            String timestamp = sdf.format(new Date());

            // generate UID for complaint and image.
            String document_uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid() + "_" + timestamp;
            Log.d("Debug", "Document ID : " + document_uid);

            // upload document
            try
            {
                String contact_number = "+91" + tinyDB.getString("userPhoneNumber");
                String email_address = tinyDB.getString("userEmail");

                StorageReference reference = storage.getReference();

                String file_name = document_uid + ".jpg";
                pbRegisterComplaint.setVisibility(View.VISIBLE);
                reference.child("complaint_images").child(file_name).putFile(image)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    Map<String, Object> complaint_data = new HashMap<>();
                                    complaint_data.put("complainant_contact", contact_number);
                                    complaint_data.put("complainant_email", email_address);
                                    complaint_data.put("complaint_description", description);
                                    complaint_data.put("complaint_locality", locality);
                                    complaint_data.put("complaint_uid", document_uid);
                                    complaint_data.put("complaint_status", "active");
                                    complaint_data.put("complaint_address", address);
                                    complaint_data.put("lat", current_location.getLatitude());
                                    complaint_data.put("long", current_location.getLongitude());
                                    complaint_data.put("complaint_image", "complaint_images/" + file_name);
                                    complaint_data.put("complaint_date", complaint_date);
                                    DocumentReference complaint_document = FirebaseFirestore.getInstance()
                                            .collection("complaints_data")
                                            .document(document_uid);
                                    complaint_document.set(complaint_data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Toast.makeText(RegisterComplaintActivity.this, "Your complaint with id " + document_uid + " has been registered!", Toast.LENGTH_SHORT).show();
                                                        incrementUserTotalComplaints ();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(RegisterComplaintActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                                pbRegisterComplaint.setVisibility(View.GONE);
                            }
                        });
            }
            catch(Exception e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void incrementUserTotalComplaints()
    {
        /*
         *  Input   :   None
         *  Utility :   Increment the total complaints of the user by one.
         *  Output  :   None
         */
        try
        {
            String user_document_name = "+91" + tinyDB.getString("userPhoneNumber");
            DocumentReference user_document = FirebaseFirestore.getInstance()
                    .collection("user_data")
                    .document( user_document_name);

            user_document.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot user_data = task.getResult();
                            long total_complaints = user_data.getLong("total_complaints");
                            total_complaints += 1;
                            user_document.update("total_complaints", total_complaints);
                        }
                    });
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        /*
         *  Input   :   Back button press
         *  Utility :   Display dialog box for exit confirmation.
         *  Output  :   Destroy app conditionally
         */
        AlertDialog.Builder alert_dialog = new AlertDialog.Builder(RegisterComplaintActivity.this);
        alert_dialog.setTitle("Confirm Exit");
        View dialog_view = getLayoutInflater().inflate(R.layout.exit_dialog, null);
        Button btnExit = (Button)dialog_view.findViewById(R.id.btnExit);
        Button btnCancel = (Button)dialog_view.findViewById(R.id.btnCancel);

        alert_dialog.setView(dialog_view);
        AlertDialog alertDialog = alert_dialog.create();
        alert_dialog.setCancelable(false);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterComplaintActivity.this.finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
