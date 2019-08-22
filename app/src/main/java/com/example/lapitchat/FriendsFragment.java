package com.example.lapitchat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    private FirebaseRecyclerAdapter adapter;
    final String[] s = new String[1];

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        fetch();

        //Inflate the layout of the fragments
        return mMainView;
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        //final String userName = dataSnapshot.child("name").getValue().toString();
        //final String user_thumb = dataSnapshot.child("thumb_image").getValue().toString();
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, new SnapshotParser<Friends>() {
                            @NonNull
                            @Override
                            public Friends parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Friends(
                                        snapshot.child("date").getValue().toString());
                            }
                        })
                        .build();
        adapter = new FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder>(options) {
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_single_layout, viewGroup, false);

                        /*String list_use_id = getRef(i).getKey();
                        s[0] = list_use_id;*/
                return new FriendsFragment.FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model) {
                final String list_use_id = getRef(position).getKey();
                mUsersDatabase.child(list_use_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        //String userOnline = dataSnapshot.child("online").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String userOnLine =  dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnLine);
                        }
                        holder.setDate((model.getDate()));
                        holder.setName(userName);
                        holder.setThumb_image(userThumb, getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == 0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_use_id);
                                            startActivity(profileIntent);
                                        }
                                        if(which == 1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_use_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(adapter);
    }

    /*public String list_id(){
        return s[0];
    }*/

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView =itemView;
        }

        public void setDate(String date) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);
        }

        public void setName(String name){
            TextView userName = mView.findViewById(R.id.users_single_name);
            userName.setText(name);
        }
        public void setThumb_image(String thumb_image, Context cxt){
            CircleImageView userImage = mView.findViewById(R.id.users_single_image);
            Picasso.with(cxt).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage);
        }
        public void setUserOnline(String onlineStatus){
            ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);
            if(onlineStatus.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else{
                userOnlineView.setVisibility(View.GONE);
            }
        }
    }
}
