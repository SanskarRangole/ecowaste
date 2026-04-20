package com.example.ecowaste.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.ecowaste.R;
import com.example.ecowaste.models.PickupModel;
import com.example.ecowaste.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RequestPickupActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteType, autoCompleteTime;
    private EditText etWeight, etAddress, etPreferredDate;
    private Button btnSubmit, btnCamera, btnGallery;
    private ImageView ivCapturedPhoto;
    private LinearLayout chipGroupQuickSelect, layoutCaptureBtns;
    private MaterialCardView cardCapturedPhoto;
    private TextView tvRetake;

    private String selectedCategory = "Other Electronics";
    private String selectedDate = "";
    private String selectedTimeSlot = "";
    private Uri imageUri;
    private boolean isImageCaptured = false;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final String[] categories = {"Mobile", "Laptop", "Television", "Battery", "Charger", "Other Electronics"};
    private Map<String, List<String>> typeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_pickup);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initCloudinary();
        initializeData();
        initViews();
        setupSpinners();
        setupListeners();
        setupQuickSelect();
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "duiwjsfby");
            MediaManager.init(this, config);
        } catch (Exception e) {
            // Already initialized
        }
    }

    private void initializeData() {
        typeMap.put("Mobile", Arrays.asList("Smartphone", "Feature Phone", "Broken Phone"));
        typeMap.put("Laptop", Arrays.asList("Gaming Laptop", "Business Laptop", "Old Scrap"));
        typeMap.put("Television", Arrays.asList("LED TV", "LCD TV", "CRT Monitor"));
        typeMap.put("Battery", Arrays.asList("Lead Acid", "Lithium Ion", "Dry Cell"));
        typeMap.put("Charger", Arrays.asList("Mobile Adapter", "Laptop Power Brick"));
        typeMap.put("Other Electronics", Arrays.asList("Keyboard", "Mouse", "Printer", "Cables"));
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        autoCompleteType = findViewById(R.id.autoCompleteType);
        autoCompleteTime = findViewById(R.id.autoCompleteTime);
        etWeight = findViewById(R.id.etWeight);
        etAddress = findViewById(R.id.etAddress);
        etPreferredDate = findViewById(R.id.etPreferredDate);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        tvRetake = findViewById(R.id.tvRetake);

        ivCapturedPhoto = findViewById(R.id.ivCapturedPhoto);
        cardCapturedPhoto = findViewById(R.id.cardCapturedPhoto);
        layoutCaptureBtns = findViewById(R.id.layoutCaptureBtns);
        chipGroupQuickSelect = findViewById(R.id.chipGroupQuickSelect);

        etWeight.setFilters(new InputFilter[]{new WeightInputFilter(), new InputFilter.LengthFilter(6)});
    }

    private void setupSpinners() {
        String[] timeSlots = {"Morning (9am–12pm)", "Afternoon (12pm–4pm)", "Evening (4pm–7pm)"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timeSlots);
        autoCompleteTime.setAdapter(timeAdapter);
        autoCompleteTime.setOnItemClickListener((p, v, pos, id) -> selectedTimeSlot = timeSlots[pos]);
        
        updateTypeDropdown(selectedCategory);
    }

    private void updateTypeDropdown(String category) {
        List<String> types = typeMap.get(category);
        if (types != null) {
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
            autoCompleteType.setAdapter(typeAdapter);
            autoCompleteType.setText("", false);
        }
    }

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    showCapturedImage(bitmap);
                    try {
                        File tempFile = File.createTempFile("pickup_", ".jpg", getCacheDir());
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                        fos.close();
                        imageUri = Uri.fromFile(tempFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    showCapturedImage(uri);
                }
            }
    );

    private void showCapturedImage(Object source) {
        isImageCaptured = true;
        cardCapturedPhoto.setVisibility(View.VISIBLE);
        layoutCaptureBtns.setVisibility(View.GONE);
        tvRetake.setVisibility(View.VISIBLE);
        
        if (source instanceof Bitmap) {
            ivCapturedPhoto.setImageBitmap((Bitmap) source);
        } else {
            ivCapturedPhoto.setImageURI((Uri) source);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            });

    private void setupListeners() {
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(null);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
        
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        tvRetake.setOnClickListener(v -> {
            cardCapturedPhoto.setVisibility(View.GONE);
            layoutCaptureBtns.setVisibility(View.VISIBLE);
            tvRetake.setVisibility(View.GONE);
            isImageCaptured = false;
        });

        cardCapturedPhoto.setOnClickListener(v -> {
            if (imageUri != null) {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", imageUri.toString());
                intent.putExtra("title", "Waste Proof");
                startActivity(intent);
            }
        });

        etPreferredDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Pickup Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
                etPreferredDate.setText(selectedDate);
            });
            picker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        btnSubmit.setOnClickListener(v -> validateAndUpload());
    }

    private void setupQuickSelect() {
        for (String cat : categories) {
            Button chip = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);
            chip.setText(cat);
            chip.setAllCaps(false);
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                updateTypeDropdown(cat);
                Toast.makeText(this, "Selected: " + cat, Toast.LENGTH_SHORT).show();
            });
            chipGroupQuickSelect.addView(chip);
        }
    }

    private void validateAndUpload() {
        String weight = etWeight.getText().toString();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(weight) || Double.parseDouble(weight) < 0.1 || Double.parseDouble(weight) > 500) {
            Toast.makeText(this, "Please enter a valid weight between 0.1 and 500 kg", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please enter a valid pickup address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Please select a preferred pickup date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedTimeSlot)) {
            Toast.makeText(this, "Please select a preferred time slot", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isImageCaptured || imageUri == null) {
            Toast.makeText(this, "Please capture or select a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Toast.makeText(this, "No internet connection. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        uploadToCloudinary();
    }

    private void uploadToCloudinary() {
        btnSubmit.setEnabled(false);
        MediaManager.get().upload(imageUri)
                .unsigned("new_request")
                .option("folder", "ecowaste_pickups")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) (((double) bytes / totalBytes) * 100);
                        runOnUiThread(() -> btnSubmit.setText("Uploading... " + progress + "%"));
                    }
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        submitData(imageUrl);
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Confirm Request");
                            Toast.makeText(RequestPickupActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        });
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void submitData(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
            String name = doc.exists() && doc.getString("name") != null ? doc.getString("name") : user.getEmail();
            String phone = doc.exists() && doc.getString("phone") != null ? doc.getString("phone") : "";

            PickupModel pickup = new PickupModel();
            pickup.setId(UUID.randomUUID().toString());
            pickup.setUserId(user.getUid());
            pickup.setUserName(name);
            pickup.setUserContact(phone);
            pickup.setCategory(selectedCategory);
            pickup.setType(autoCompleteType.getText().toString());
            pickup.setEstimatedWeight(Double.parseDouble(etWeight.getText().toString()));
            pickup.setAddress(etAddress.getText().toString());
            pickup.setPreferredDate(selectedDate);
            pickup.setPreferredTime(selectedTimeSlot);
            pickup.setImageUrl(imageUrl);
            pickup.setImageVerified(true);
            pickup.setCreatedAt(new Date());
            
            // Client-side PIN generation for the secure verification system
            String pin = String.format(Locale.getDefault(), "%04d", new Random().nextInt(10000));
            pickup.setPickupPin(pin);

            db.collection("pickups").document(pickup.getId()).set(pickup)
                    .addOnSuccessListener(aVoid -> {
                        sendPushAndInAppNotification(user.getUid(), "Request Submitted", "We've received your pickup request.", pickup.getId());
                        showSuccessSheet();
                    })
                    .addOnFailureListener(e -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Confirm Request");
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void showSuccessSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_success, null);
        sheet.setContentView(sheetView);
        sheet.setCancelable(false);
        sheetView.findViewById(R.id.btnDone).setOnClickListener(v -> {
            sheet.dismiss();
            finish();
        });
        sheet.show();
    }

    private void sendPushAndInAppNotification(String targetUserId, String title, String message, String pickupId) {
        String id = UUID.randomUUID().toString();
        NotificationModel n = new NotificationModel();
        n.setId(id); n.setUserId(targetUserId); n.setTitle(title); n.setMessage(message);
        n.setTimestamp(new Date()); n.setRead(false);
        db.collection("notifications").document(id).set(n);
    }

    private static class WeightInputFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                String input = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend);
                if (input.isEmpty() || input.equals(".")) return null;
                double val = Double.parseDouble(input);
                if (val <= 500) return null;
                return "";
            } catch (NumberFormatException e) { return ""; }
        }
    }
}