package com.example.smartcityportal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ComplaintElement extends ArrayAdapter<Map<String, Object>>{

    private List<Map<String, Object>> complaintList;
    private Activity context;
    private String user_type;

    public ComplaintElement(Activity context, List<Map<String, Object>> complaintList, String user_type)
    {
        super(context, R.layout.complaint_element, complaintList);
        this.complaintList = complaintList;
        this.context = context;
        this.user_type = user_type;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        Log.d("Debug", String.valueOf(position));
        @SuppressLint({"ViewHolder", "InflateParams"})
        View complaintElementView = inflater.inflate(R.layout.complaint_element, null, true);
        TextView tvComplainDate = complaintElementView.findViewById(R.id.tvComplaintDate);
        TextView tvComplaintLocation = complaintElementView.findViewById(R.id.tvComplaintLocation);
        Map<String, Object> complaint_details = complaintList.get(position);
        String date = "Date : " + complaint_details.get("complaint_date");
        String location = "Location : \n" + complaint_details.get("complaint_address");
        tvComplainDate.setText(date);
        tvComplaintLocation.setText(location);
        Button btnLocate = complaintElementView.findViewById(R.id.btnLocate);
        Button btnViewComplaint = complaintElementView.findViewById(R.id.btnViewComplaint);

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 *  Input   :   None
                 *  Utility :   Launch map location for complaint.
                 *  Output  :   Implicit intent launch.
                 */
                String lat =  String.valueOf(complaint_details.get("lat"));
                String longitude = String.valueOf(complaint_details.get("long"));
                String url = "geo:" + lat + "," + longitude + "?z=zoom";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            }
        });

        btnViewComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 *  Input   :   None
                 *  Utility :   Launch view complaint activity based on user type.
                 *  Output  :   View complaint activity launch.
                 */
                Intent intent = null;
                if(user_type.equals("user"))
                {
                    intent = new Intent(context.getApplicationContext(), UserViewComplaint.class);
                }
                else if (user_type.equals("admin"))
                {
                    intent = new Intent(context.getApplicationContext(), AdminComplaintsActivity.class);
                }
                intent.putExtra("complaint_uid", complaint_details.get("complaint_uid").toString());
                context.startActivity(intent);
                context.finish();
            }
        });
        return complaintElementView;
    }
}
