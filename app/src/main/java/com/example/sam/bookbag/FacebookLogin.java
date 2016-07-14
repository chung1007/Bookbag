package com.example.sam.bookbag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by sam on 6/4/16.
 */
public class FacebookLogin extends Fragment {

    private TextView mTextDetails;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private LoginButton mButtonLogin;
    private AccessToken accessToken;
    public static Profile profile;
    DatabaseReference ref;
    Firebase ratingsDataBase;
    public static boolean firstTime;
    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            try {
                Log.e("Login", "onSuccess");
                accessToken = loginResult.getAccessToken();
                loginResult.getRecentlyGrantedPermissions();
                profile = Profile.getCurrentProfile();
                if (profile != null) {
                    Log.e("user name", profile.getFirstName());
                    firstTime = true;
                    sendUserData(profile);
                    sendExtras();
                    Log.e("profile", "!=Null");
                } else {
                    mTextDetails.setText("Searching...");
                    Log.e("displayText", "Searching");
                }
            } catch (NullPointerException NPE) {
                Log.e("Profile", "isNull");
                Log.e("User ID", loginResult.getAccessToken().getUserId());
            }
        }

        @Override
        public void onCancel() {
            Log.e("Login", "onCancel");
        }

        @Override
        public void onError(FacebookException e) {
            Log.e("Login", "onError " + e);
        }
    };

    public FacebookLogin() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getContext());
        Firebase.setAndroidContext(getContext());
        ref = MyApplication.ref;
        mCallbackManager = CallbackManager.Factory.create();
        setupTokenTracker();
        setupProfileTracker();
        mTokenTracker.startTracking();
        mProfileTracker.startTracking();
        Log.e("Trackers", "are Tracking");
        if (Profile.getCurrentProfile() != null){
            firstTime = false;
            Intent homePage = new Intent(getContext(), HomePage.class);
            homePage.putExtra("userName", Profile.getCurrentProfile().getName());
            homePage.putExtra("userId", Profile.getCurrentProfile().getId());
            Log.e("userIdBefore", Profile.getCurrentProfile().getId());
            displayToast("Welcome Back " + Profile.getCurrentProfile().getName() + "!");
            startActivity(homePage);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.facebook_login, container, false);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setupTextDetails(view);
        setupLoginButton(view);
        mButtonLogin.setReadPermissions("public_profile");

    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            //constructWelcomeMessage(profile.getName());
        } catch (NullPointerException NPE) {
            Log.e("currentProfile", "NULL");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
        Log.e("Trackers", "onStop");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setupTextDetails(View view) {
        mTextDetails = (TextView) view.findViewById(R.id.textDisplay);
    }

    private void setupTokenTracker() {
        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.e("AccessToken", "" + currentAccessToken);
                accessToken = currentAccessToken;
            }
        };
    }

    private void setupProfileTracker() {
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                try {
                    if (currentProfile == null){
                        MainActivity.displayText.setText("");
                    }else if(currentProfile != null){
                        profile = currentProfile;
                    }
                } catch (NullPointerException NPE) {
                    Log.e("New Profile", "NULL");
                }
            }
        };
    }

    private void setupLoginButton(View view) {
        mButtonLogin = (LoginButton) view.findViewById(R.id.login_button);
        mButtonLogin.setFragment(this);
        mButtonLogin.registerCallback(mCallbackManager, mFacebookCallback);
        mButtonLogin.setReadPermissions("public_profile");
        mButtonLogin.setReadPermissions("email");
        Log.e("public profile", "requested");
    }

    public void displayToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void sendExtras(){
        Intent homePage = new Intent(getContext(), HomePage.class);
        homePage.putExtra("userName", profile.getName());
        homePage.putExtra("userId", profile.getId());
        Log.e("userId", profile.getId());
        displayToast("Welcome " + profile.getName() + "!");
        startActivity(homePage);
    }
    public void sendUserData(Profile profile){
        ratingsDataBase = new Firebase(Constants.ratingDataBase);
        MyApplication.ref.child(profile.getId()).child("Initialized").setValue("listeners initialized");
        ratingsDataBase.child(profile.getId()+"_"+profile.getName()).child("likes").setValue(Integer.parseInt("0"));
        ratingsDataBase.child(profile.getId()+"_"+profile.getName()).child("dislikes").setValue(Integer.parseInt("0"));
        Log.e("Firebase", "sent Data");
    }
}



