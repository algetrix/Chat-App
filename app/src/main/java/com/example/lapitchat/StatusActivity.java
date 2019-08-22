package com.example.lapitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private  Button mSavebtn;


    //Firebase database reference
    private DatabaseReference mStatusReference;
    private FirebaseUser mCurrentUser;

    //Progress Dialog
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase initialization
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus = findViewById(R.id.status_input);
        mStatus.getEditText().setText(status_value);

        mSavebtn = findViewById(R.id.status_save_btn);
        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save your changes");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();
                mStatusReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        }
                        else{
                            mProgress.hide();
                            Toast.makeText(getApplicationContext(), "There was some problem while saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
