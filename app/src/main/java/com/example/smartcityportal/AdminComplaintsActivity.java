package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AdminComplaintsActivity extends AppCompatActivity {

    private Menu menuList;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String locality;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menuList = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.adminMyProfileMenuItem:
                intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.adminLogOutMenuItem:
                mAuth.signOut();
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                AdminComplaintsActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaints);

        mAuth = FirebaseAuth.getInstance();

        // Capture locality
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location last_location = task.getResult();
                            Geocoder geocoder = new Geocoder(AdminComplaintsActivity.this,
                                    Locale.getDefault());
                            try
                            {
                                List<Address> addresses = geocoder.getFromLocation(
                                        last_location.getLatitude(),
                                        last_location.getLongitude(),
                                        1);
                                Address current_address = addresses.get(0);
                                locality = current_address.getLocality();
                                Toast.makeText(AdminComplaintsActivity.this, "Acquiring complaints for " + locality, Toast.LENGTH_SHORT).show();
                                renderComplaints();
                            }
                            catch (IOException e)
                            {
                                Toast.makeText(AdminComplaintsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AdminComplaintsActivity.this, "Could not acquire location !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void renderComplaints()
    {
        /*
         *  Input   :   None
         *  Utility :   Render complaints based on locality on the screen.
         *  Output  :   None
         */
    }

    @Override
    public void onBackPressed() {
        /*
         *  Input   :   Back button press
         *  Utility :   Display dialog box for exit confirmation.
         *  Output  :   Destroy app conditionally
         */
        AlertDialog.Builder alert_dialog = new AlertDialog.Builder(AdminComplaintsActivity.this);
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
                AdminComplaintsActivity.this.finish();
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