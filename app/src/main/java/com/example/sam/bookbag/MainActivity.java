package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {

    public static TextView displayText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.previewpage);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        try{
            Thread.sleep(2000);
            Intent intent = new Intent(this, ActualMainActivity.class);
            startActivity(intent);
        }catch (InterruptedException IE){
            Log.e("go to main", "failed");
        }
        /*displayText = (TextView) findViewById(R.id.textDisplay);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Hero.otf");
        displayText.setTypeface(tf);*/
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


