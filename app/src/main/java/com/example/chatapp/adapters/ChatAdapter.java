package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ReceivedMsgItemContainerBinding;
import com.example.chatapp.databinding.SentMsgItemContainerBinding;
import com.example.chatapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final Bitmap receiverPfp;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessageList, Bitmap receiverPfp, String senderId) {
        this.chatMessageList = chatMessageList;
        this.receiverPfp = receiverPfp;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMsgViewHolder(
                    SentMsgItemContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false)
            );
        } else {
            return new ReceivedMsgViewHolder(
                    ReceivedMsgItemContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMsgViewHolder) holder).setData(chatMessageList.get(position));
        } else {
            ((ReceivedMsgViewHolder) holder).setData(chatMessageList.get(position), receiverPfp);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMsgViewHolder extends RecyclerView.ViewHolder {
        private final SentMsgItemContainerBinding binding;

        SentMsgViewHolder(SentMsgItemContainerBinding sentMsgItemContainerBinding) {
            super(sentMsgItemContainerBinding.getRoot());
            binding = sentMsgItemContainerBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.sentMsg.setText(chatMessage.message);
            binding.timeStampTxt.setText(chatMessage.timeStamp);
        }
    }

    static class ReceivedMsgViewHolder extends RecyclerView.ViewHolder {
        private final ReceivedMsgItemContainerBinding binding;

        ReceivedMsgViewHolder(ReceivedMsgItemContainerBinding receivedMsgItemContainerBinding) {
            super(receivedMsgItemContainerBinding.getRoot());
            binding = receivedMsgItemContainerBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverPfp) {
            binding.receivedTxtMsg.setText(chatMessage.message);
            binding.txtTimeStamp.setText(chatMessage.timeStamp);
            binding.profileImg.setImageBitmap(receiverPfp);
        }
    }
}
