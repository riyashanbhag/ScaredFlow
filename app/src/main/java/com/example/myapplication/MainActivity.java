package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnSOS, btnBook, btnMyBookings, btnAdmin, btnLogout;
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login status
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (!sharedPref.contains("user_phone")) {
            startActivity(new Intent(MainActivity.this, UserLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        btnSOS = findViewById(R.id.btnSOS);
        btnBook = findViewById(R.id.btnBook);
        btnMyBookings = findViewById(R.id.btnMyBookings);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        String userName = sharedPref.getString("user_name", "User");
        tvWelcome.setText("Welcome, " + userName);

        // Open SOS Activity
        btnSOS.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SOSActivity.class)));

        // Open Booking Activity
        btnBook.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BookingActivity.class)));

        // Open My Bookings Activity
        btnMyBookings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyBookingsActivity.class)));

        // Open Admin Login
        btnAdmin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminLoginActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(MainActivity.this, UserLoginActivity.class));
            finish();
        });
    }
}
