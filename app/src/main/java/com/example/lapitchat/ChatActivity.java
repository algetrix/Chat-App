package com.example.lapitchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUSerId;


    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileView;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;


    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private static final int GALLERY_PICK = 1;

    //Storage Reference
    private  StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(mChatUser);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUSerId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        Log.d("uid", mChatUser);
        String userName = getIntent().getStringExtra("user_name");
        Log.d("uname", userName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //getSupportActionBar().setTitle(userName);


        //---------Custom List bar items----------
        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_last_seen);
        mProfileView = findViewById(R.id.custom_bar_image);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView =findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        //----------IMAGE STORAGE------------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        loadMessages();

        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if(online.equals("true")){
                    mLastSeenView.setText("online");
                }
                else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    Log.d("Online", online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }
                Picasso.with(getBaseContext()).load(image).placeholder(R.drawable.default_avatar).into(mProfileView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUSerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chataddMap = new HashMap();
                    chataddMap.put("seen", false);
                    chataddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUSerMap = new HashMap();
                    chatUSerMap.put("Chat/" + mCurrentUSerId + "/" + mChatUser, chataddMap);
                    chatUSerMap.put("Chat/" + mChatUser + "/" + mCurrentUSerId, chataddMap);

                    mRootRef.updateChildren(chatUSerMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final String current_user_ref = "messages/" + mCurrentUSerId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUSerId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUSerId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();
            final StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String download_url = uri.toString();

                                Map messageMap = new HashMap();
                                messageMap.put("message", download_url);
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUSerId);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                mChatMessageView.setText("");
                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if(databaseError != null){
                                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUSerId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                messagesList.add(itemPos++, message);
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                }
                else{
                    mPrevKey = mLastKey;
                }
                if(itemPos == 1){
                    mLastKey = messageKey;
                }
                Log.d("TOTALKEYS", "Last Key: " + mLastKey + "  |   Previous Key" + mPrevKey + "    |   Message Key" + messageKey);
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUSerId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;
                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUSerId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUSerId;

            DatabaseReference user_messsage_push = mRootRef.child("messages").child(mCurrentUSerId).child(mChatUser).push();
            String push_id = user_messsage_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUSerId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}