package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private EditText etDate, etTimeSlot, etFamilySize;
    private Spinner spinnerReligion, spinnerTemple, spinnerCategory;
    private Button btnSubmitBooking, btnAddMemberDetails;
    private LinearLayout containerMembers;
    private TextView tvCrowdStatus, tvUserInfo;
    private DatabaseReference mDatabase;
    private int maxLimit = 30;
    private String userName, userPhone;

    private Map<String, String[]> religionToPlaces;
    private List<View> memberDetailViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userName = sharedPref.getString("user_name", "");
        userPhone = sharedPref.getString("user_phone", "");

        tvUserInfo = findViewById(R.id.tvUserInfo);
        etDate = findViewById(R.id.etDate);
        etTimeSlot = findViewById(R.id.etTimeSlot);
        etFamilySize = findViewById(R.id.etFamilySize);
        spinnerReligion = findViewById(R.id.spinnerReligion);
        spinnerTemple = findViewById(R.id.spinnerTemple);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmitBooking = findViewById(R.id.btnSubmitBooking);
        btnAddMemberDetails = findViewById(R.id.btnAddMemberDetails);
        containerMembers = findViewById(R.id.containerMembers);
        tvCrowdStatus = findViewById(R.id.tvCrowdStatus);

        if (tvUserInfo != null) {
            tvUserInfo.setText("Booking for: " + userName + " (" + userPhone + ")");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        initData();
        setupSpinners();

        etDate.setOnClickListener(v -> showDatePicker());
        etTimeSlot.setOnClickListener(v -> showTimePicker());
        btnAddMemberDetails.setOnClickListener(v -> generateMemberFields());
        btnSubmitBooking.setOnClickListener(v -> checkAndBook());
    }

    private void initData() {
        religionToPlaces = new HashMap<>();
        religionToPlaces.put("Hinduism", new String[]{"Jagannath Temple, Puri", "Kedarnath Temple", "Varanasi Ghats", "Tirupati Balaji", "Somnath Temple"});
        religionToPlaces.put("Islam", new String[]{"Jama Masjid, Delhi", "Ajmer Sharif Dargah", "Haji Ali Dargah", "Mecca Masjid, Hyderabad"});
        religionToPlaces.put("Christianity", new String[]{"Basilica of Bom Jesus", "St. Thomas Cathedral", "Santhome Church"});
        religionToPlaces.put("Sikhism", new String[]{"Golden Temple, Amritsar", "Hemkund Sahib", "Bangla Sahib Gurudwara"});
        religionToPlaces.put("Buddhism", new String[]{"Mahabodhi Temple", "Sanchi Stupa", "Tawang Monastery"});
    }

    private void setupSpinners() {
        String[] religions = religionToPlaces.keySet().toArray(new String[0]);
        ArrayAdapter<String> religionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, religions);
        spinnerReligion.setAdapter(religionAdapter);

        String[] categories = {"General", "Senior Citizen", "Disabled", "Pregnant Woman"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerReligion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedReligion = religions[position];
                updatePlacesSpinner(selectedReligion);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePlacesSpinner(String religion) {
        String[] places = religionToPlaces.get(religion);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, places);
        spinnerTemple.setAdapter(adapter);

        spinnerTemple.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMaxLimit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateMaxLimit() {
        String place = spinnerTemple.getSelectedItem() != null ? spinnerTemple.getSelectedItem().toString() : "";
        if (!TextUtils.isEmpty(place)) {
            mDatabase.child("settings").child(place).child("max_limit").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        maxLimit = Integer.parseInt(snapshot.getValue().toString());
                    } else {
                        maxLimit = 30; 
                    }
                    updateCrowdStatus();
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void generateMemberFields() {
        String sizeStr = etFamilySize.getText().toString();
        if (TextUtils.isEmpty(sizeStr)) return;
        
        int size = Integer.parseInt(sizeStr);
        containerMembers.removeAllViews();
        memberDetailViews.clear();

        for (int i = 0; i < size; i++) {
            View memberView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null); 
            // Better to create a custom layout, but for now I'll use code-generated views
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(0, 10, 0, 10);

            TextView tvHeader = new TextView(this);
            tvHeader.setText("Member " + (i + 1) + (i == 0 ? " (Primary User)" : ""));
            tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            
            EditText etMemberName = new EditText(this);
            etMemberName.setHint("Name");
            if (i == 0) etMemberName.setText(userName);

            EditText etMemberAge = new EditText(this);
            etMemberAge.setHint("Age");
            etMemberAge.setInputType(android.view.inputmethod.EditorInfo.TYPE_CLASS_NUMBER);

            Spinner spGender = new Spinner(this);
            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Male", "Female", "Other"});
            spGender.setAdapter(genderAdapter);

            layout.addView(tvHeader);
            layout.addView(etMemberName);
            layout.addView(etMemberAge);
            layout.addView(spGender);

            containerMembers.addView(layout);
            memberDetailViews.add(layout);
        }
    }

    private List<FamilyMember> collectMemberDetails() {
        List<FamilyMember> members = new ArrayList<>();
        for (View v : memberDetailViews) {
            LinearLayout layout = (LinearLayout) v;
            EditText etName = (EditText) layout.getChildAt(1);
            EditText etAge = (EditText) layout.getChildAt(2);
            Spinner spGender = (Spinner) layout.getChildAt(3);

            String name = etName.getText().toString();
            String ageStr = etAge.getText().toString();
            String gender = spGender.getSelectedItem().toString();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(ageStr)) {
                members.add(new FamilyMember(name, Integer.parseInt(ageStr), gender));
            }
        }
        return members;
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day));
            updateCrowdStatus();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hour, minute) -> {
            int roundedMinute = (minute < 15) ? 0 : (minute < 45) ? 30 : 0;
            int roundedHour = (minute >= 45) ? (hour + 1) % 24 : hour;
            
            String time = String.format(Locale.getDefault(), "%02d:%02d", roundedHour, roundedMinute);
            String timeRange = time + " - " + calculateEndTime(roundedHour, roundedMinute);
            
            etTimeSlot.setText(timeRange);
            updateCrowdStatus();
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private String calculateEndTime(int hour, int minute) {
        int endMinute = (minute + 30) % 60;
        int endHour = (minute + 30 >= 60) ? (hour + 1) % 24 : hour;
        return String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);
    }

    private void updateCrowdStatus() {
        String date = etDate.getText().toString();
        String timeRange = etTimeSlot.getText().toString();
        if (spinnerTemple.getSelectedItem() == null) return;
        String place = spinnerTemple.getSelectedItem().toString();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(timeRange)) return;

        mDatabase.child("bookings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalDisabledReserved = 0;
                int totalNormal = 0;
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    if (b != null && b.date.equals(date) && b.timeSlot.equals(timeRange) && b.templeName.equals(place)) {
                        if (isPriorityCategory(b.userCategory)) {
                            totalDisabledReserved += b.familySize;
                        } else {
                            totalNormal += b.familySize;
                        }
                    }
                }
                
                int reservedQuota = 10;
                int normalQuota = maxLimit - reservedQuota;
                
                tvCrowdStatus.setText("Priority: " + totalDisabledReserved + "/" + reservedQuota + 
                                     " | Normal: " + totalNormal + "/" + normalQuota);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean isPriorityCategory(String category) {
        return "Disabled".equals(category) || "Senior Citizen".equals(category) || "Pregnant Woman".equals(category);
    }

    private void checkAndBook() {
        String date = etDate.getText().toString();
        String timeRange = etTimeSlot.getText().toString();
        if (spinnerTemple.getSelectedItem() == null) return;
        String place = spinnerTemple.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String sizeStr = etFamilySize.getText().toString();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(timeRange) || TextUtils.isEmpty(sizeStr)) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        int familySize = Integer.parseInt(sizeStr);
        List<FamilyMember> members = collectMemberDetails();
        
        if (members.size() < familySize) {
            Toast.makeText(this, "Please provide details for all " + familySize + " members", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child("bookings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalDisabledReserved = 0;
                int totalNormal = 0;
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    if (b != null && b.date.equals(date) && b.timeSlot.equals(timeRange) && b.templeName.equals(place)) {
                        if (isPriorityCategory(b.userCategory)) {
                            totalDisabledReserved += b.familySize;
                        } else {
                            totalNormal += b.familySize;
                        }
                    }
                }

                int reservedQuota = 10;
                int normalQuota = maxLimit - reservedQuota;
                boolean canBook = false;

                if (isPriorityCategory(category)) {
                    if (totalDisabledReserved + familySize <= reservedQuota) {
                        canBook = true;
                    } else if (totalNormal + familySize <= normalQuota) {
                        canBook = true;
                    } else {
                        Toast.makeText(BookingActivity.this, "No priority or normal spots available!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (totalNormal + familySize <= normalQuota) {
                        canBook = true;
                    } else {
                        Toast.makeText(BookingActivity.this, "Normal quota full!", Toast.LENGTH_LONG).show();
                    }
                }

                if (canBook) {
                    String id = mDatabase.child("bookings").push().getKey();
                    Booking booking = new Booking(id, userName, userPhone, place, date, timeRange, category, "Confirmed", familySize, members);
                    mDatabase.child("bookings").child(id).setValue(booking);
                    Toast.makeText(BookingActivity.this, "Booking Successful!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
