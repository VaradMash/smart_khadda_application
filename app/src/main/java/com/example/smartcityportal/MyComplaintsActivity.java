package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyComplaintsActivity extends AppCompatActivity {

    private ListView myComplaintsListView;
    private ProgressBar pbMyComplaints;
    private CollectionReference complaint_data;
    private TinyDB tinyDB;
    private List<Map<String, Object>> user_active_complaints;
    public void onBackPressed (View view) {
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);
        getSupportActionBar().hide();

        myComplaintsListView = (ListView)findViewById(R.id.myComplaintsListView);
        pbMyComplaints = (ProgressBar)findViewById(R.id.pbMyComplaints);

        complaint_data = FirebaseFirestore.getInstance().collection("complaints_data");
        tinyDB = new TinyDB(getApplicationContext());
        user_active_complaints = new ArrayList<>();

    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
         *  Input   :   None
         *  Utility :   Get relevant documents for the active complaints of current user.
         *  Output  :   Render records on screen.
         */
        try
        {
            user_active_complaints.removeAll(user_active_complaints);
            pbMyComplaints.setVisibility(View.VISIBLE);
            String phone = "+91" + tinyDB.getString("userPhoneNumber");
            complaint_data
                    .whereEqualTo("complainant_contact", phone)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                QuerySnapshot result = task.getResult();
                                if(result.isEmpty())
                                {
                                    Toast.makeText(MyComplaintsActivity.this, "No active complaints found !", Toast.LENGTH_SHORT).show();
                                    String message = "No active complaints found !";
                                    showNotification(message);
                                }
                                else
                                {
                                    int count = 0;
                                    for(DocumentSnapshot document : result)
                                    {
                                        if(document.get("complaint_status").equals("active"))
                                        {
                                            count += 1;
                                            user_active_complaints.add(document.getData());
                                        }
                                    }
                                    if (count == 0)
                                    {
                                        String message = "No active complaints found !";
                                        showNotification(message);
                                    }
                                    else
                                    {
                                        ComplaintElement adapter = new ComplaintElement(MyComplaintsActivity.this, user_active_complaints, "user");
                                        myComplaintsListView.setAdapter(adapter);
                                    }
                                }
                                pbMyComplaints.setVisibility(View.GONE);
                            }
                            else
                            {
                                Toast.makeText(MyComplaintsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                pbMyComplaints.setVisibility(View.GONE);
                            }
                        }
                    });
        }
        catch(Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void showNotification(String message)
    {
        /*
         *  Input   :   None
         *  Utility :   Show confirmation message
         *  Output  :   None
         */
        AlertDialog.Builder alert_dialog = new AlertDialog.Builder(MyComplaintsActivity.this);
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
}