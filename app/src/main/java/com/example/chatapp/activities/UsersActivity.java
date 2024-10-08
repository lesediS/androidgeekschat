package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.databinding.ActivityUsersBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        fetchUsers();
    }

    //TODO: Use GPT to figure out how to use the suggested methods for onBackPressed successfully (UPDATE UPDATE used getOnBackPressedDispatcher)
    private void setListeners(){
        binding.backBtnImg.setOnClickListener(v -> {
            //onBackPressed();
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void fetchUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.COLLECTION_USERS).get().addOnCompleteListener(task -> {
            loading(false);
            String curUserID = preferenceManager.getString(Constants.USER_ID);
            if(task.isSuccessful() && task.getResult() != null){
                List<User> users = new ArrayList<>();
                for(QueryDocumentSnapshot snapshot : task.getResult()){
                    if(curUserID.equals(snapshot.getId())){
                        continue;
                    }
                    User user = new User();

                    user.uName = snapshot.getString(Constants.USERNAME);
                    user.img = snapshot.getString(Constants.IMAGE);
                    user.token = snapshot.getString(Constants.FCM_TOKEN);
                    user.id = snapshot.getId();

                    users.add(user);
                }
                if(!users.isEmpty()){
                    UserAdapter adapter = new UserAdapter(users, this);
                    binding.usersRecycler.setAdapter(adapter);
                    binding.usersRecycler.setVisibility(View.VISIBLE);
                } else {
                    errorMsg();
                }
            } else {
                errorMsg();
            }
        });
    }

    private void errorMsg(){
        binding.errorTxt.setText(String.format("%s", "Ask your friends to join!"));
        binding.errorTxt.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
        finish();
    }
}