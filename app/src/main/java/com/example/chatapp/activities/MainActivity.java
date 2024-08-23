package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.adapters.RecentChatAdapter;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.listeners.ChatListener;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements ChatListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> chats;

    private RecentChatAdapter chatsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_login);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        init();
        setListeners();
        listenChats();
        loadDetails();
        getToken();
    }

    private void init() {
        chats = new ArrayList<>();
        chatsAdapter = new RecentChatAdapter(chats, this);
        binding.recentChatsRecycler.setAdapter(chatsAdapter);
        database = FirebaseFirestore.getInstance();
        binding.logoutImg.setOnClickListener(v -> logout());
    }

    private void setListeners() {
        binding.newChatBtn.setOnClickListener(v -> {
            // Fetch the list of users from Firestore
            database.collection(Constants.COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<User> userList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                User user = documentSnapshot.toObject(User.class);
                                if (user != null) {
                                    userList.add(user);
                                }
                            }

                            if (!userList.isEmpty()) {
                                // Pass the list of users to the new activity
                                Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
                                intent.putExtra("userList", (Serializable) userList);
                                startActivity(intent);
                            } else {
                                showToast("No users found");
                            }
                        } else {
                            showToast("No users found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore Error", e.getMessage());
                        showToast("Failed to load users");
                    });
        });
    }


    //TODO: Move logout button and stuff to under/in profile
    private void logout() {
        showToast("Logging out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference reference = database.collection(Constants.COLLECTION_USERS).document(
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
        binding.nameTxt.setText(preferenceManager.getString(Constants.USERNAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.profilePic.setImageBitmap(bitmap);
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void listenChats() {
        database.collection(Constants.COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Constants.COLLECTION_CONVERSATIONS)
                .whereEqualTo("receiverId.id", preferenceManager.getString(Constants.USER_ID)) // Access nested map field
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            Log.e("Firestore Error", error.getMessage());
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                DocumentSnapshot document = documentChange.getDocument();
                String senderID = document.getString(Constants.SENDER_ID);
                Map<String, Object> receiverMap = (Map<String, Object>) document.get(Constants.RECEIVER_ID);

                if (senderID != null && receiverMap != null) {
                    String receiverID = (String) receiverMap.get("id");
                    String receiverName = (String) receiverMap.get("name");
                    String receiverImage = (String) receiverMap.get("image");

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderID;
                    chatMessage.receiverId = receiverID;

                    if (preferenceManager.getString(Constants.USER_ID).equals(senderID)) {
                        chatMessage.chatName = receiverName != null ? receiverName : "Unknown";
                        chatMessage.chatImg = receiverImage != null ? receiverImage : null;
                        chatMessage.chatId = receiverID;
                    } else {
                        chatMessage.chatName = document.getString(Constants.SENDER_NAME);
                        chatMessage.chatImg = document.getString(Constants.SENDER_IMG);
                        chatMessage.chatId = senderID;
                    }

                    chatMessage.message = document.getString(Constants.LAST_MSG);
                    chatMessage.dateObj = document.getDate(Constants.TIME_STAMP);

                    // Check if this chat already exists
                    boolean exists = false;
                    for (int i = 0; i < chats.size(); i++) {
                        ChatMessage existingChat = chats.get(i);
                        if (existingChat.chatId.equals(chatMessage.chatId)) {
                            // Update the existing chat message and move it to the top
                            chats.remove(i);
                            exists = true;
                            break;
                        }
                    }

                    // Add the chat message at the top
                    chats.add(0, chatMessage);

                    // Sort chats by date in descending order
                    Collections.sort(chats, (obj1, obj2) -> obj2.dateObj.compareTo(obj1.dateObj));

                    // Notify the adapter about the change
                    chatsAdapter.notifyDataSetChanged();
                }
            }

            // Ensure the list is scrolled to the top
            binding.recentChatsRecycler.smoothScrollToPosition(0);
            binding.recentChatsRecycler.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void tokenUpdate(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection(Constants.COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.USER_ID));

        reference.update(Constants.FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Failed to update token"));
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::tokenUpdate);
    }

    @Override
    public void onChatClicked(User user) {
        try{
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.USER, user);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Opening chat error", e.getMessage());
        }
    }
}