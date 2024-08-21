package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    private String chatId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMsgs();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList, getBitmapEncodedString(receiverUser.img),
                preferenceManager.getString(Constants.USER_ID));
        binding.chatRecycler.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMsg() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
        message.put(Constants.RECEIVER_ID, receiverUser.id);
        message.put(Constants.MESSAGE, binding.msgInputTxt.getText().toString());
        message.put(Constants.TIME_STAMP, new Date());
        database.collection(Constants.COLLECTION_CHAT).add(message);

        if (chatId != null) {
            updateConvert(binding.msgInputTxt.getText().toString());
        } else {
            HashMap<String, Object> convert = new HashMap<>();

            convert.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
            convert.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.USERNAME)); //Constants.USERNAME
            convert.put(Constants.SENDER_IMG, preferenceManager.getString(Constants.IMAGE));

            convert.put(Constants.RECEIVER_ID, receiverUser);
            convert.put(Constants.RECEIVER_NAME, receiverUser.uName); //receiverUser.uName
            convert.put(Constants.RECEIVER_IMG, receiverUser.img);

            convert.put(Constants.LAST_MSG, binding.msgInputTxt.getText().toString());
            convert.put(Constants.TIME_STAMP, new Date());
            addConvert(convert);
        }
        binding.msgInputTxt.setText(null);
    }


    private void listenMsgs() {
        database.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
    }

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error != null){
                return;
            }
            if(value != null){
                if(value.getLong(Constants.AVAILABILITY) != null){
                    int available = Objects.requireNonNull(
                            value.getLong(Constants.AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = available == 1;
                }
            }

            if(isReceiverAvailable){
                binding.textAvail.setVisibility(View.VISIBLE);
            } else {
                binding.textAvail.setVisibility(View.GONE);
            }
        });
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();

                    chatMessage.senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.MESSAGE);
                    chatMessage.timeStamp = getReadableTimeStamp(documentChange.getDocument().getDate(Constants.TIME_STAMP));
                    chatMessage.dateObj = documentChange.getDocument().getDate(Constants.TIME_STAMP);
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dateObj.compareTo(obj2.dateObj));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.chatRecycler.smoothScrollToPosition(chatMessageList.size() - 1);
            }

            binding.chatRecycler.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

        if (chatId == null) {
            checkForConvert();
        }
    };

    private Bitmap getBitmapEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.USER);
        assert receiverUser != null;
        binding.chatNameTxt.setText(receiverUser.uName);
    }

    private void setListeners() {
        //binding.backBtnImg.setOnClickListener(v -> onBackPressed());
        binding.backBtnImg.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(intent);
            finish();
        });
        binding.sendFrameLayout.setOnClickListener(v -> sendMsg());
    }

    private String getReadableTimeStamp(Date date) {
        return new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConvert(HashMap<String, Object> convert) {
        database.collection(Constants.COLLECTION_CONVERSATIONS)
                .add(convert)
                .addOnSuccessListener(documentReference -> chatId = documentReference.getId());
    }

    private void updateConvert(String message) {
        DocumentReference docRef = database.collection(Constants.COLLECTION_CONVERSATIONS).document(chatId);
        docRef.update(
                Constants.LAST_MSG, message,
                Constants.TIME_STAMP, new Date()
        );
    }

    private void checkForConvert() {
        if (!chatMessageList.isEmpty()) {
            checkConversionRemote(
                    preferenceManager.getString(Constants.USER_ID),
                    receiverUser.id
            );
            checkConversionRemote(
                    receiverUser.id,
                    preferenceManager.getString(Constants.USER_ID)
            );
        }
    }

    private void checkConversionRemote(String senderId, String receiverId) {
        database.collection(Constants.COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, senderId)
                .whereEqualTo(Constants.RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(convertOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> convertOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot docSnapshot = task.getResult().getDocuments().get(0);
            chatId = docSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}