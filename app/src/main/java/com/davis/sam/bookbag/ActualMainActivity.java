package com.davis.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.facebook.FacebookSdk;

/**
 * Created by sam on 9/4/16.
 */
public class ActualMainActivity extends AppCompatActivity {

    public static TextView displayText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        displayText = (TextView) findViewById(R.id.textDisplay);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Hero.otf");
        displayText.setTypeface(tf);
    }
    @Override
    public void onDestroy(){
        Log.e("MainActivity", "killed");
        //this.startService(new Intent(this, BackgroundListeners.class));
        Intent i = new Intent(this, BackgroundListeners.class);
        i.putExtra("userId", HomePage.userId);
        i.putExtra("userName", HomePage.userName);
        startService(i);
        Log.e("started", "service");
        super.onDestroy();
    }

}
