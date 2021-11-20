package com.example.smartcityportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
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
                                }
                                else
                                {
                                    for(DocumentSnapshot document : result)
                                    {
                                        if(document.get("complaint_status").equals("active"))
                                        {
                                            user_active_complaints.add(document.getData());
                                        }
                                    }
                                    ComplaintElement adapter = new ComplaintElement(MyComplaintsActivity.this, user_active_complaints);
                                    myComplaintsListView.setAdapter(adapter);
                                    Log.d("Debug", String.valueOf(user_active_complaints.size()));
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
}