package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.UserItemContainerBinding;
import com.example.chatapp.models.User;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserItemContainerBinding binding = UserItemContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserInfo(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        UserItemContainerBinding binding;
        UserViewHolder(UserItemContainerBinding containerBinding) {
            super(containerBinding.getRoot());
            binding = containerBinding;
        }

        //TODO: Might need to fix fetchUsers() in UsersActivity to only include the below details and not everything
        void setUserInfo(User user){
            binding.usernameTxt.setText(user.uName);
            binding.fNameTxt.setText(user.fName);
            binding.imgProfile.setImageBitmap(userImg(user.img));
        }
    }

    private Bitmap userImg(String encodedImg){
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

