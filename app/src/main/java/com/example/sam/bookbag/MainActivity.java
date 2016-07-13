package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {

    public static TextView displayText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        displayText = (TextView) findViewById(R.id.textDisplay);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Hero.otf");
        displayText.setTypeface(tf);
    }

}


