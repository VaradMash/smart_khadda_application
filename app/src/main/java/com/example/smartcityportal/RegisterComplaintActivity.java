package com.example.smartcityportal;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterComplaintActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    TextView locationTextView;
    TextView imageCaptionTextView;
    TextInputEditText locationTextInputEditText;
    ImageView photoImageView;
    private Menu menuList;
    String address = "";
    LinearLayout imageContainerLinearLayout;
    Uri image;
    String mCameraFileName;
    boolean imageIsCaptured = false;
    TinyDB tinyDB;
    boolean userIsLoggedIn = true;

    public void onRefreshTapped (View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void uploadPhotoPressed (View view) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else if (imageIsCaptured) {
            showCustomSnackBar(imageCaptionTextView, "Photo already present. Press refresh to upload a new one.");
        }
        else {
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
            }
        }
        //Bitmap photo = (Bitmap) data.getExtras().get("data");
        //photoImageView.setImageBitmap(photo);
        imageCaptionTextView.setText("Above Photo will be Submitted");
        imageIsCaptured = true;
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        else {
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
                Toast.makeText(this, "Log Out Code goes here.", Toast.LENGTH_SHORT).show();
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

        locationTextView = findViewById(R.id.locationTextView);
        imageCaptionTextView = findViewById(R.id.imageCaptionTextView);
        photoImageView = findViewById(R.id.photoImageView);
        locationTextInputEditText = findViewById(R.id.locationRCTextInputEditText);
        imageContainerLinearLayout = findViewById(R.id.imageContainerLinearLayout);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }
}
