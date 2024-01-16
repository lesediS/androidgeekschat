package com.example.chatapp.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.databinding.ActivityUsersBinding;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

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

    //TODO: Use GPT to figure out how to use the suggested methods for onBackPressed successfully
    private void setListeners(){
        binding.backBtnImg.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void fetchUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.COLLECTION_NAME).get().addOnCompleteListener(task -> {
            loading(false);
            String curUserID = preferenceManager.getString(Constants.USER_ID);
            if(task.isSuccessful() && task.getResult() != null){
                List<User> users = new ArrayList<>();
                for(QueryDocumentSnapshot snapshot : task.getResult()){
                    if(curUserID.equals(snapshot.getId())){
                        continue;
                    }
                    User user = new User();

                    user.fName = snapshot.getString(Constants.NAME);
                    user.sName = snapshot.getString(Constants.SURNAME);
                    user.uName = snapshot.getString(Constants.USERNAME);
                    user.email = snapshot.getString(Constants.EMAIL);
                    user.img = snapshot.getString(Constants.IMAGE);
                    user.token = snapshot.getString(Constants.FCM_TOKEN);

                    users.add(user);
                }
                if(users.size() > 0){
                    UserAdapter adapter = new UserAdapter(users);
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
        binding.errorTxt.setText(String.format("%s", "User/s not available"));
        binding.errorTxt.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}