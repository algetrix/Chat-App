package com.example.lapitchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.firebase.ui.common.ChangeEventType;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseRecyclerAdapter adapter;
    private FirebaseRecyclerAdapter fsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        linearLayoutManager = new LinearLayoutManager(this);
        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(linearLayoutManager);
        fetch();

    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
            .getReference()
            .child("Users");

        FirebaseFirestore fsDb = FirebaseFirestore.getInstance();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, new SnapshotParser<Users>() {
                            @NonNull
                            @Override
                            public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Users(
                                        snapshot.child("name").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("status").getValue().toString(),
                                        snapshot.child("thumb_image").getValue().toString());
                            }
                        })
                        .build();

        FirebaseRecyclerOptions<Users> fsOptions =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, new SnapshotParser<Users>() {
                            @NonNull
                            @Override
                            public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Users(
                                        snapshot.child("name").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("status").getValue().toString(),
                                        snapshot.child("thumb_image").getValue().toString());
                            }
                        })
                        .build();


        adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                holder.setDisplayName(model.getName());
                holder.setUserImage(model.getImage(), getApplicationContext());
                holder.setUserStatus(model.getStatus());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);
            }

        };
        mUsersList.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDisplayName(String name) {
            TextView userNameView = mView.findViewById(R.id.users_single_name);
            userNameView.setText(name);
        }
        public void setUserImage(String thumb_image, Context ctx) {
            CircleImageView userImage = mView.findViewById(R.id.users_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage);
        }
        public void setUserStatus(String status) {
            TextView userStatus = mView.findViewById(R.id.user_single_status);
            userStatus.setText(status);
        }

    }
}
