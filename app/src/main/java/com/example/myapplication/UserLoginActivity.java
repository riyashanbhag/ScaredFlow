package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserLoginActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (sharedPref.contains("user_phone")) {
            startActivity(new Intent(UserLoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_user_login);

        etName = findViewById(R.id.etUserName);
        etPhone = findViewById(R.id.etUserPhone);
        btnLogin = findViewById(R.id.btnUserLogin);

        btnLogin.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user details locally
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user_name", name);
            editor.putString("user_phone", phone);
            editor.apply();

            startActivity(new Intent(UserLoginActivity.this, MainActivity.class));
            finish();
        });
    }
}
