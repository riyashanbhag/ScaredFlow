package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SOSActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;
    private TextView tvLocation;
    private Button btnSendSOS;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String userName, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userName = sharedPref.getString("user_name", "Unknown User");
        userPhone = sharedPref.getString("user_phone", "Unknown Phone");

        tvLocation = findViewById(R.id.tvLocation);
        btnSendSOS = findViewById(R.id.btnSendSOS);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("sos");
        
        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnSendSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndSendSOS();
            }
        });
    }

    private void checkPermissionsAndSendSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS
            }, PERMISSION_REQUEST_CODE);
        } else {
            getLocationAndSend();
        }
    }

    private void getLocationAndSend() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;

                    tvLocation.setText("Latitude: " + lat + "\nLongitude: " + lon);

                    // 1. Send to Firebase
                    String sosId = mDatabase.push().getKey();
                    Map<String, Object> sosData = new HashMap<>();
                    sosData.put("status", "Emergency: " + userName + " (" + userPhone + ")");
                    sosData.put("timestamp", System.currentTimeMillis());
                    sosData.put("latitude", lat);
                    sosData.put("longitude", lon);
                    sosData.put("userName", userName);
                    sosData.put("userPhone", userPhone);

                    if (sosId != null) {
                        mDatabase.child(sosId).setValue(sosData);
                    }

                    // 2. Send SMS (Change to a valid number for testing)
                    sendSMS("7972316489", "EMERGENCY! " + userName + " is at location: " + mapsUrl);

                    // 3. Open Google Maps
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                    startActivity(intent);

                    Toast.makeText(SOSActivity.this, "SOS Sent Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SOSActivity.this, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSend();
            } else {
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
