package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityRegisterBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ActivityRegisterBinding binding;
    private String encodedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.IS_LOGGED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }

    private void setListeners() {
        // Handle back press for API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.loginTxt.setOnClickListener(v -> onBackPressed());
        } else {
            binding.loginTxt.setOnClickListener(v -> super.onBackPressed());
        }

        binding.registerBtn.setOnClickListener(v -> {
            if (isValidRegDetails()) {
                register();
            }
        });

        binding.profileImgLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            chooseImg.launch(intent);
        });
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void register() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();

        user.put(Constants.USERNAME, binding.regUsername.getText().toString());
        user.put(Constants.PASSWORD, binding.regPassword.getText().toString());
        user.put(Constants.IMAGE, encodedImg);
        database.collection(Constants.COLLECTION_USERS).add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.IS_LOGGED_IN, true);
                    preferenceManager.putString(Constants.USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.USERNAME, binding.regUsername.getText().toString());
                    preferenceManager.putString(Constants.IMAGE, encodedImg);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private String encImage(Bitmap bitmap) {
        int width = 150;
        int height = bitmap.getHeight() * width / bitmap.getWidth();

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> chooseImg = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imgUri = result.getData().getData();
                        try {
                            assert imgUri != null;
                            InputStream stream = getContentResolver().openInputStream(imgUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(stream);
                            binding.regProfilePic.setImageBitmap(bitmap);
                            binding.addImgTxt.setVisibility(View.GONE);
                            encodedImg = encImage(bitmap);
                        } catch (FileNotFoundException ex) {
                            showToast("File not found");
                            ex.printStackTrace();
                        }
                    } else {
                        showToast("No image selected");
                    }
                } else {
                    showToast("Image selection failed");
                }
            }
    );

    private boolean isValidRegDetails() {
        if (encodedImg == null) {
            showToast("Select profile picture");
            return false;
        } else if (binding.regUsername.getText().toString().trim().isEmpty() || binding.regPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter valid details in all fields");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.registerBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.registerBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
