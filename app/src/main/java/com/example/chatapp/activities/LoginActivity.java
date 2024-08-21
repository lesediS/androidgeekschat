package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityLoginBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setListeners();
        setContentView(binding.getRoot());
    }

    private void setListeners() {
        binding.createAccountReg.setOnClickListener(v -> startActivity(
                new Intent(getApplicationContext(), RegisterActivity.class)));

        binding.loginBtn.setOnClickListener(v -> {
            if (isValidLoginDetails()) {
                login();
            }
        });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.loginBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.loginBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void login() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.COLLECTION_USERS)
                .whereEqualTo(Constants.USERNAME, binding.loginUsername.getText().toString())
                .whereEqualTo(Constants.PASSWORD, binding.loginPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);

                        preferenceManager.putBoolean(Constants.IS_LOGGED_IN, true);
                        preferenceManager.putString(Constants.USER_ID, snapshot.getId());
                        preferenceManager.putString(Constants.USERNAME, snapshot.getString(Constants.USERNAME));
                        preferenceManager.putString(Constants.IMAGE, snapshot.getString(Constants.IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Oops, try again");
                    }
                });
    }


    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidLoginDetails() {
        if (binding.loginUsername.getText().toString().trim().isEmpty()) {
            showToast("Enter valid details");
            return false;
        } else if (binding.loginPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter valid details");
            return false;
        } else {
            return true;
        }
    }
}