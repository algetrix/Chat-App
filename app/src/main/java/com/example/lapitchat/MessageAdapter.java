package com.example.lapitchat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_single_layout, viewGroup, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_layout);
            profileImage = itemView.findViewById(R.id.message_profile_layout);
            displayName = itemView.findViewById(R.id.name_text_layout);
            messageImage = itemView.findViewById(R.id.message_image_layout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        mAuth = FirebaseAuth.getInstance();

        final Messages c = mMessageList.get(i);

        final String from_user = c.getFrom();
        final String message_type = c.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser() != null) {
                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    if (from_user.equals(current_user_id)) {
                        messageViewHolder.profileImage.setVisibility(View.GONE);
                        messageViewHolder.displayName.setGravity(Gravity.RIGHT | Gravity.END);
                        messageViewHolder.messageText.setGravity(Gravity.RIGHT | Gravity.END);
                        messageViewHolder.displayName.setText(name);
                        messageViewHolder.messageText.setText(c.getMessage());
                    }
                    else {
                        messageViewHolder.displayName.setText(name);
                        messageViewHolder.messageText.setText(c.getMessage());
                        Picasso.with(messageViewHolder.profileImage.getContext()).load(image)
                                .placeholder(R.drawable.default_avatar).into(messageViewHolder.profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            messageViewHolder.messageText.setText(c.getMessage());
            messageViewHolder.messageImage.setVisibility(View.INVISIBLE);


        }
        else {

            messageViewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(messageViewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(messageViewHolder.messageImage);

        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
