package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class AdminViewComplaint extends AppCompatActivity {

    private String complaint_uid;
    private DocumentReference complaint_document;
    private ProgressBar pbAdminViewComplaint;
    private ImageView complaintImageViewAdmin;
    private TextView tvAdminComplaintDate, tvAdminComplaintLocality, tvAdminComplaintDescription;
    private Map<String, Object> complaint_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_complaint);

        Intent intent = getIntent();
        complaint_uid = intent.getStringExtra("complaint_uid");
        pbAdminViewComplaint = (ProgressBar)findViewById(R.id.pbAdminViewComplaint);
        complaintImageViewAdmin = (ImageView)findViewById(R.id.complaintImageViewAdmin);
        tvAdminComplaintDate = (TextView)findViewById(R.id.tvAdminComplaintDate);
        tvAdminComplaintDescription = (TextView)findViewById(R.id.tvAdminComplaintDescription);
        tvAdminComplaintLocality = (TextView)findViewById(R.id.tvAdminComplaintLocality);

    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
         *  Input   :   None
         *  Utility :   Fetch document for complaint
         *  Output  :   Render data on screen.
         */
        pbAdminViewComplaint.setVisibility(View.VISIBLE);
        complaint_document = FirebaseFirestore.getInstance().collection("complaints_data").document(complaint_uid);
        complaint_document.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            complaint_data = task.getResult().getData();
                            String date = "Date : " + complaint_data.get("complaint_date").toString();
                            String locality = "Locality : " + complaint_data.get("complaint_locality").toString();
                            String description = "Description :\n\n" + complaint_data.get("complaint_description").toString();
                            tvAdminComplaintDate.setText(date);
                            tvAdminComplaintDescription.setText(description);
                            tvAdminComplaintLocality.setText(locality);
                            renderImage(complaint_data.get("complaint_image").toString());
                            pbAdminViewComplaint.setVisibility(View.GONE);
                        }
                        else
                        {
                            Toast.makeText(AdminViewComplaint.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void renderImage(String image_path)
    {
        /*
         *  Input   :   Image path in storage
         *  Utility :   Render image to screen.
         *  Output  :   None.
         */

        StorageReference image_reference = FirebaseStorage.getInstance().getReference().child(image_path);
        image_reference.getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            String url = task.getResult().toString();
                            Glide.with(getApplicationContext())
                                    .load(Uri.parse(url))
                                    .into(complaintImageViewAdmin);
                            complaintImageViewAdmin.setBackground(null);
                        }
                        else
                        {
                            Toast.makeText(AdminViewComplaint.this, "Could not load image !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void locateComplaintAdmin(View v)
    {
        /*
         *  Launch implicit intent for locating complaint on map.
         */
        String lat =  String.valueOf(complaint_data.get("lat"));
        String longitude = String.valueOf(complaint_data.get("long"));
        String url = "geo:" + lat + "," + longitude + "?z=zoom";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void confirmDeleteAdmin(View v)
    {
        AlertDialog.Builder alert_dialog = new AlertDialog.Builder(AdminViewComplaint.this);
        alert_dialog.setTitle("Confirm Resolution");
        View dialog_view = getLayoutInflater().inflate(R.layout.confirm_dialog, null);
        Button btnExit = (Button)dialog_view.findViewById(R.id.btnExit);
        Button btnCancel = (Button)dialog_view.findViewById(R.id.btnCancel);
        TextView tvConfirmationMessage = (TextView)dialog_view.findViewById(R.id.tvConfirmationMessage);
        tvConfirmationMessage.setText("Are you sure that the complaint is resolved?");
        alert_dialog.setView(dialog_view);
        AlertDialog alertDialog = alert_dialog.create();
        alert_dialog.setCancelable(false);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeComplaint();
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

    public void contactComplainant(View v)
    {
        /*
         *  Input   :   None
         *  Utility :   Launch email intent for contacting complainant
         *  Output  :   Email intent launch.
         */
        String url = "mailto:" + complaint_data.get("complainant_email").toString();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void removeComplaint()
    {
        /*
         *  Input   :   None
         *  Utility :   Mark status of complaint as inactive in database.
         *  Output  :   None
         */
        complaint_document.update("complaint_status", "inactive")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            String message = "Complaint with ID " + complaint_uid + " marked as completed !";
                            Toast.makeText(AdminViewComplaint.this, message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MyComplaintsActivity.class);
                            startActivity(intent);
                            AdminViewComplaint.this.finish();
                        }
                        else
                        {
                            String message = "Could not remove complaint!\nPlease try again later!";
                            showNotification(message);
                        }
                    }
                });
    }

    private void showNotification(String message)
    {
        /*
         *  Input   :   None
         *  Utility :   Show confirmation message
         *  Output  :   None
         */
        AlertDialog.Builder alert_dialog = new AlertDialog.Builder(AdminViewComplaint.this);
        View dialog_view = getLayoutInflater().inflate(R.layout.notification_dialog, null);
        Button btnOk = dialog_view.findViewById(R.id.btnOk);
        TextView tvNotificationMessage = dialog_view.findViewById(R.id.tvNotificationMessage);
        alert_dialog.setView(dialog_view);
        AlertDialog alertDialog = alert_dialog.create();
        alert_dialog.setCancelable(false);

        tvNotificationMessage.setText(message);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*
         *  Input   :   Back button press
         *  Utility :   Navigate to My complaints activity for user.
         *  Output  :   Activity launch.
         */
        Intent intent = new Intent(getApplicationContext(), AdminComplaintsActivity.class);
        startActivity(intent);
        AdminViewComplaint.this.finish();
    }
}