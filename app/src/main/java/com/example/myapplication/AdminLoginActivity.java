package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private Map<String, String> adminTemples = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        etUsername = findViewById(R.id.etAdminUsername);
        etPassword = findViewById(R.id.etAdminPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Hindu Places
        adminTemples.put("admin_puri", "Jagannath Temple, Puri");
        adminTemples.put("admin_kedar", "Kedarnath Temple");
        adminTemples.put("admin_vns", "Varanasi Ghats");
        adminTemples.put("admin_tirupati", "Tirupati Balaji");
        
        // Muslim Places
        adminTemples.put("admin_jama", "Jama Masjid, Delhi");
        adminTemples.put("admin_ajmer", "Ajmer Sharif Dargah");
        
        // Christian Places
        adminTemples.put("admin_basilica", "Basilica of Bom Jesus");
        
        // Sikh Places
        adminTemples.put("admin_amritsar", "Golden Temple, Amritsar");
        
        // Buddhist Places
        adminTemples.put("admin_bodhi", "Mahabodhi Temple");

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (adminTemples.containsKey(username) && password.equals("admin123")) {
                String assignedPlace = adminTemples.get(username);
                Toast.makeText(AdminLoginActivity.this, "Welcome " + assignedPlace + " Admin", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
                intent.putExtra("temple_name", assignedPlace); // Using 'temple_name' key for backward compatibility
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(AdminLoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
