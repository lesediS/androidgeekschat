package com.example.chatapp.adapters;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.RecentChatItemContainerBinding;
import com.example.chatapp.listeners.ChatListener;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;

import java.util.List;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ChatViewHolder> {

    private final ChatListener chatListener;
    private final List<ChatMessage> chatMessageList;

    public RecentChatAdapter(List<ChatMessage> chatMessageList, ChatListener chatListener) {
        this.chatMessageList = chatMessageList;
        this.chatListener = chatListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(
                RecentChatItemContainerBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        RecentChatItemContainerBinding binding;

        ChatViewHolder(RecentChatItemContainerBinding recentChatItemContainerBinding) {
            super(recentChatItemContainerBinding.getRoot());
            binding = recentChatItemContainerBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imgProfile.setImageBitmap(getConvertImg(getConvertImg(chatMessage.chatImg).toString()));
            binding.fNameTxt.setText(chatMessage.chatName);
            binding.recentChatTxt.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.chatId;
                user.uName = chatMessage.chatName;
                user.img = chatMessage.chatImg;
                chatListener.onChatClicked(user);
            });
        }
    }

    private Bitmap getConvertImg(String encodedImg) {
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
