package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private EditText etMaxLimit;
    private Button btnUpdateLimit, btnScanEntry, btnScanExit;
    private TextView tvCrowdDensity, tvWaitTime, tvTempleTitle;
    private RecyclerView rvAlerts, rvAllBookings;
    
    private AlertAdapter alertAdapter;
    private List<Alert> alertList;
    
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    
    private DatabaseReference mDatabase;
    private int maxLimit = 5;
    private boolean isScanningEntry = true;
    private String adminTemple = "";

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    processScanResult(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminTemple = getIntent().getStringExtra("temple_name");
        if (adminTemple == null) adminTemple = "";

        tvTempleTitle = findViewById(R.id.tvTempleTitle);
        if (tvTempleTitle != null) tvTempleTitle.setText(adminTemple + " Admin Panel");

        etMaxLimit = findViewById(R.id.etMaxLimit);
        btnUpdateLimit = findViewById(R.id.btnUpdateLimit);
        btnScanEntry = findViewById(R.id.btnScanEntry);
        btnScanExit = findViewById(R.id.btnScanExit);
        tvCrowdDensity = findViewById(R.id.tvCrowdDensity);
        tvWaitTime = findViewById(R.id.tvWaitTime);
        rvAlerts = findViewById(R.id.rvAlerts);
        rvAllBookings = findViewById(R.id.rvAllBookings);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setupRecyclerViews();
        fetchData();

        btnUpdateLimit.setOnClickListener(v -> updateLimit());
        
        btnScanEntry.setOnClickListener(v -> {
            isScanningEntry = true;
            startScanner();
        });
        
        btnScanExit.setOnClickListener(v -> {
            isScanningEntry = false;
            startScanner();
        });
    }

    private void startScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan Booking QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivityPortrait.class);
        barcodeLauncher.launch(options);
    }

    private void processScanResult(String bookingId) {
        DatabaseReference bookingRef = mDatabase.child("bookings").child(bookingId);
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Booking b = snapshot.getValue(Booking.class);
                    if (b != null && !adminTemple.equals(b.templeName)) {
                        Toast.makeText(AdminActivity.this, "This ticket is for " + b.templeName, Toast.LENGTH_LONG).show();
                        return;
                    }

                    String status = isScanningEntry ? "Checked In" : "Checked Out";
                    long timestamp = System.currentTimeMillis();
                    
                    bookingRef.child("status").setValue(status);
                    if (isScanningEntry) {
                        bookingRef.child("checkInTime").setValue(timestamp);
                    } else {
                        bookingRef.child("checkOutTime").setValue(timestamp);
                    }
                    
                    Toast.makeText(AdminActivity.this, "Update Successful: " + status, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Invalid Booking QR Code", Toast.LENGTH_LONG).show();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupRecyclerViews() {
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        alertAdapter = new AlertAdapter(alertList);
        rvAlerts.setAdapter(alertAdapter);

        rvAllBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList);
        rvAllBookings.setAdapter(bookingAdapter);
    }

    private void fetchData() {
        if (!TextUtils.isEmpty(adminTemple)) {
            mDatabase.child("settings").child(adminTemple).child("max_limit").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            maxLimit = Integer.parseInt(snapshot.getValue().toString());
                            etMaxLimit.setText(String.valueOf(maxLimit));
                            calculateRealTimeStats();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        mDatabase.child("sos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alertList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Alert alert = ds.getValue(Alert.class);
                    if (alert != null) alertList.add(0, alert);
                }
                alertAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        mDatabase.child("bookings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    if (b != null && adminTemple.equals(b.templeName)) {
                        bookingList.add(0, b);
                    }
                }
                bookingAdapter.notifyDataSetChanged();
                calculateRealTimeStats();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateRealTimeStats() {
        if (bookingList == null || bookingList.isEmpty() || maxLimit <= 0) {
            tvCrowdDensity.setText("0%");
            tvWaitTime.setText("0 mins");
            return;
        }
        
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int peopleInside = 0;
        int peopleWaiting = 0;
        
        for (Booking b : bookingList) {
            // Only calculate stats for today's bookings
            if (today.equals(b.date)) {
                if ("Checked In".equals(b.status)) {
                    peopleInside += b.familySize;
                } else if ("Confirmed".equals(b.status)) {
                    peopleWaiting += b.familySize;
                }
            }
        }
        
        int density = (int) (((double) peopleInside / maxLimit) * 100); 
        if (density > 100) density = 100;
        tvCrowdDensity.setText(density + "%");

        int waitTime = (peopleWaiting * 5) / 2; 
        tvWaitTime.setText(waitTime + " mins");
        
        if (density > 80) tvCrowdDensity.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        else if (density > 50) tvCrowdDensity.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        else tvCrowdDensity.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

    private void updateLimit() {
        String limit = etMaxLimit.getText().toString();
        if (!TextUtils.isEmpty(limit) && !TextUtils.isEmpty(adminTemple)) {
            mDatabase.child("settings").child(adminTemple).child("max_limit").setValue(Integer.parseInt(limit))
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminActivity.this, "Limit Updated for " + adminTemple, Toast.LENGTH_SHORT).show());
        }
    }
}
