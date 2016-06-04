package com.example.sam.bookbag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    private Profile profile;
    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            try {
                Log.e("Login", "onSuccess");
                accessToken = loginResult.getAccessToken();
                loginResult.getRecentlyGrantedPermissions();
                profile = Profile.getCurrentProfile();
                Log.e("Profile", "after .getCurrentProfile");
                if (profile != null) {
                    Log.e("user name", profile.getFirstName());
                    constructWelcomeMessage(profile.getName());
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
        mCallbackManager = CallbackManager.Factory.create();
        setupTokenTracker();
        setupProfileTracker();
        mTokenTracker.startTracking();
        mProfileTracker.startTracking();
        Log.e("Trackers", "are Tracking");
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
            Profile profile = Profile.getCurrentProfile();
            constructWelcomeMessage(profile.getName());
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

    private void constructWelcomeMessage(String userName) {
        MainActivity.displayText.setText("Welcome " + userName);
    }
}



