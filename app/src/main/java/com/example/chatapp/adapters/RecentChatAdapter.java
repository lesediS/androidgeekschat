package com.example.chatapp.adapters;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.RecentChatItemContainerBinding;
import com.example.chatapp.models.ChatMessage;

import java.util.List;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessageList;

    public RecentChatAdapter(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                RecentChatItemContainerBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        RecentChatItemContainerBinding binding;

        ConversionViewHolder(RecentChatItemContainerBinding recentChatItemContainerBinding) {
            super(recentChatItemContainerBinding.getRoot());
            binding = recentChatItemContainerBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imgProfile.setImageBitmap(getConvertImg(getConvertImg(chatMessage.chatImg).toString()));
            binding.fNameTxt.setText(chatMessage.chatName);
            binding.recentChatTxt.setText(chatMessage.message);
        }
    }

    private Bitmap getConvertImg(String encodedImg) {
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
