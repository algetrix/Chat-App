package com.example.lapitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mProfileDeclineReqBtn;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_DisplayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_request_btn);
        mProfileDeclineReqBtn = findViewById(R.id.profile_decline_btn);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mProfileDeclineReqBtn.setVisibility(View.GONE);

        if(user_id.equals(mCurrentUser.getUid())){
            Intent settingsIntent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                //--------------FRIEND LIST/ REQUEST FEATURE--------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mProfileDeclineReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setText("ACCEPT FRIEND REQUEST");
                            } else if (req_type.equals("sent")) {
                                mProfileDeclineReqBtn.setVisibility(View.GONE);
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");
                            }
                            mProgressDialog.dismiss();
                        }
                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("UNFRIEND");
                                        mProfileDeclineReqBtn.setVisibility(View.GONE);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                        });
                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                //---------NOT FRIEND STATE------------

                if(mCurrent_state.equals("not_friends")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                    .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCurrent_state = "req_sent";
                                        mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");

                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_LONG).show();

                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                mProfileSendReqBtn.setEnabled(true);
                //---------CANCEL REQUEST STATE------------
                if(mCurrent_state.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                                }
                                            });
                                }
                            });
                }
                //--------REQ ACCEPT STATE-----------
                if(mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mProfileSendReqBtn.setEnabled(true);
                                                                            mCurrent_state = "friends";
                                                                            mProfileSendReqBtn.setText("UNFRIEND");
                                                                            mProfileDeclineReqBtn.setVisibility(View.GONE);
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });

                        }
                    });
                }
                //-----------UNFRIEND STATE-------------
                if(mCurrent_state.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                                }
                                            });
                                }
                            });

                }
            }
        });
        mProfileDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProfileSendReqBtn.setEnabled(true);
                                                mCurrent_state = "not_friends";
                                                mProfileSendReqBtn.setText("SEND FRIEND REQUEST");
                                                mProfileDeclineReqBtn.setVisibility(View.GONE);

                                            }
                                        });
                            }
                        });
            }
        });
    }

}