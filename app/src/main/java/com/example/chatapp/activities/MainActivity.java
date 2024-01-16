package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_login);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        loadDetails();
        getToken();
    }

    private void setListeners(){
        binding.logoutImg.setOnClickListener(v -> logout());
        binding.newChatBtn.setOnClickListener(v -> {
            startActivity((new Intent(getApplicationContext(), UsersActivity.class)));
        });
    }

    //TODO: Move logout button and stuff to under/in profile
    private void logout(){
        showToast("Logging out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference reference = database.collection(Constants.COLLECTION_NAME).document(
                preferenceManager.getString(Constants.USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.FCM_TOKEN, FieldValue.delete());
        reference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }).addOnFailureListener(e -> showToast("Could not log out. Try again."));
    }

    private void loadDetails() {
        binding.nameTxt.setText(preferenceManager.getString(Constants.NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.profilePic.setImageBitmap(bitmap);
    }

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void tokenUpdate(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection(Constants.COLLECTION_NAME)
                .document(preferenceManager.getString(Constants.USER_ID));

        reference.update(Constants.FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Failed to update token"));
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::tokenUpdate);
    }

}