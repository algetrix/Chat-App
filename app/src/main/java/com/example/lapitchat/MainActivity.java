package com.example.lapitchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.protobuf.DescriptorProtos;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ViewPager mViewPager;
    private SectionPagerAdapter msectionPagerAdapter;
    private TabLayout mTabLayout;

    private Toolbar mToolbar;

    private DatabaseReference mUserRef;

    //Firestore init
    private FirebaseFirestore mFsUserRef;
    private FirebaseFirestore mFsUserRef1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapit Chat");

        //Tabs
        mViewPager = findViewById(R.id.main_tabPager);
        msectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(msectionPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mFsUserRef = FirebaseFirestore.getInstance();
        }


    }
    @Override
    public void onStart() {

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //If current user is null it means user is not logged in
        if(currentUser == null){
            sendToStart();
        }
        else{
            HashMap<String, String> userOnlineMap = new HashMap<>();
            userOnlineMap.put("online", "true");
            mUserRef.child("online").setValue("true");
            mFsUserRef.collection("Users").document(mAuth.getCurrentUser().getUid()).set(userOnlineMap, SetOptions.merge());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            FieldValue timestamp = FieldValue.serverTimestamp();
            mFsUserRef.collection("Users").document(mAuth.getCurrentUser().getUid()).update("online", timestamp);
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            mUserRef.child("online").setValue(false);
            sendToStart();
        }
        if(item.getItemId() == R.id.main_settings_btn);{
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_all_btn){
            Intent settingsIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }
}