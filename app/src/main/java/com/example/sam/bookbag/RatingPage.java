package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by sam on 7/12/16.
 */
public class RatingPage extends AppCompatActivity {
    Intent intent;
    String sellerId;
    String sellerName;
    Integer userSmileCount;
    Integer userSadCount;
    Firebase rateDataBase;
    ArrayList<String> keys;
    ProfilePictureView ratePagePicture;
    TextView profilePageFirstName;
    TextView profilePageLastName;
    TextView smileCounter;
    TextView sadCounter;
    boolean alreadyRated = false;
    HashMap<String, Integer> keysAndValues;
    ImageView smile;
    ImageView sad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sellerprofilepage);
        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        intent = getIntent();
        Log.e("ratingpage", "started");
        sellerId = intent.getStringExtra("profileId");
        sellerName = intent.getStringExtra("profileName");
        rateDataBase = new Firebase(Constants.ratingDataBase);
        smile = (ImageView)findViewById(R.id.smile);
        sad = (ImageView)findViewById(R.id.sad);
        keys = new ArrayList<>();
        keysAndValues = new HashMap<>();
        setPageInfo();
        getCurrentRatings();
        listenForSmileClicked();
        listenForSadClicked();

    }

    public void getCurrentRatings(){
        rateDataBase.child(sellerId + "_" + sellerName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue().toString();
                keys.add(key);
                keysAndValues.put(key, Integer.parseInt(value));
                if(keys.size() == 2){
                    userSmileCount = keysAndValues.get("likes");
                    userSadCount = keysAndValues.get("dislikes");
                    Log.e("smiles", userSmileCount + " ");
                    Log.e("sads", userSadCount + " ");
                    smileCounter.setText(Integer.toString(userSmileCount));
                    sadCounter.setText(Integer.toString(userSadCount));
                    Log.e("setCounters", "true");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    public void setPageInfo(){
        ratePagePicture = (ProfilePictureView)findViewById(R.id.profileImageOfSeller);
        profilePageFirstName = (TextView)findViewById(R.id.profileFirstNameOfSeller);
        profilePageLastName = (TextView)findViewById(R.id.profileLastNameOfSeller);
        smileCounter = (TextView)findViewById(R.id.smileCounter);
        sadCounter = (TextView)findViewById(R.id.sadCounter);
        ratePagePicture.setProfileId(sellerId);
        String name[] = sellerName.split(" ");
        profilePageFirstName.setText(name[0]);
        profilePageLastName.setText(name[1]);

    }
    public void listenForSmileClicked(){
        smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alreadyRated) {
                    smileCounter.setText(Integer.toString(userSmileCount + 1));
                    rateDataBase.child(sellerId + "_" + sellerName).child("likes").setValue(Integer.parseInt(smileCounter.getText().toString()));
                    alreadyRated = true;
                } else {
                    toastMaker("You already rated!");
                }
            }
        });
    }
    public void listenForSadClicked(){
        sad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alreadyRated) {
                    sadCounter.setText(Integer.toString(userSadCount + 1));
                    rateDataBase.child(sellerId + "_" + sellerName).child("dislikes").setValue(Integer.parseInt(sadCounter.getText().toString()));
                    alreadyRated = true;
                } else {
                    toastMaker("You already rated!");
                }
            }
        });
    }
    public void toastMaker(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
