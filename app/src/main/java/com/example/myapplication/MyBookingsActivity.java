package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private DatabaseReference mDatabase;
    private String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userPhone = sharedPref.getString("user_phone", "");

        rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(bookingList);
        rvBookings.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("bookings");
        fetchMyBookings();
    }

    private void fetchMyBookings() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    if (b != null && b.userPhone != null && b.userPhone.equals(userPhone)) {
                        bookingList.add(b);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyBookingsActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
