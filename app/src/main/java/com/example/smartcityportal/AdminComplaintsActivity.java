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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminComplaintsActivity extends AppCompatActivity {

    private Menu menuList;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Map<String, Object>> complaints_list;
    private String locality;
    private ProgressBar pbAdminComplaints;
    private TinyDB tinyDB;
    private CollectionReference complaint_data;
    private ListView complaintsListViewAdmin;

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
        complaintsListViewAdmin = (ListView)findViewById(R.id.complaintsListViewAdmin);

        // Capture locality
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        pbAdminComplaints = (ProgressBar)findViewById(R.id.pbAdminComplaints);
        tinyDB = new TinyDB(getApplicationContext());
        complaint_data = FirebaseFirestore.getInstance().collection("complaints_data");
        complaints_list = new ArrayList<>();


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
        try
        {
            pbAdminComplaints.setVisibility(View.VISIBLE);
            String phone = "+91" + tinyDB.getString("userPhoneNumber");
            complaint_data
                    .whereEqualTo("complaint_status", "active")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                QuerySnapshot result = task.getResult();
                                if(result.isEmpty())
                                {
                                    Toast.makeText(getApplicationContext(), "No active complaints found !", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    for(DocumentSnapshot document : result)
                                    {
                                        if (document.get("complaint_locality").equals(locality))
                                        {
                                            complaints_list.add(document.getData());
                                        }
                                    }
                                    ComplaintElement adapter = new ComplaintElement(AdminComplaintsActivity.this, complaints_list, "admin");
                                    complaintsListViewAdmin.setAdapter(adapter);
                                }
                                pbAdminComplaints.setVisibility(View.GONE);
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                pbAdminComplaints.setVisibility(View.GONE);
                            }
                        }
                    });
        }
        catch(Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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