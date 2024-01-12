package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        //TODO: onBackPressed() is deprecated, onBackPressedDispatcher() does not work, getOnBackInvokedDispatcher(), Call requires API level 33 (current min is 19): android.app.Activity#getOnBackInvokedDispatcher More... (Ctrl+F1)
        binding.loginTxt.setOnClickListener(v -> onBackPressed());
    }
}