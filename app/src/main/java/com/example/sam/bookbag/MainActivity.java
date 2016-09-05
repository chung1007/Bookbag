package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextView next;
    Thread thread;

    public static TextView displayText;
    ImageView icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.previewpage);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        next = (TextView)findViewById(R.id.next);
        icon = (ImageView)findViewById(R.id.companyLogo);
        generateRandomFontColor();
        forceClick();
    }
    public void next(View view){
        Intent go = new Intent(this, ActualMainActivity.class);
        startActivity(go);
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
    public void forceClick() {
        thread=  new Thread(){
            @Override
            public void run(){
                try {
                    synchronized(this){
                        wait(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                next.performClick();
                                next.setPressed(true);
                            }
                        });
                    }
                }
                catch(InterruptedException ex){
                }

                // TODO
            }
        };

        thread.start();
    }
    public void generateRandomFontColor(){
        Random r = new Random();
        int x = r.nextInt(7) + 1;
        Log.e("random", x + "");
        switch (x) {
            case 1:
                Log.e("case", "1");
                icon.setImageResource(0);
                icon.setImageResource(R.drawable.gradientappspreview1);
                break;
            case 2:
                Log.e("case", "2");
                icon.setImageResource(0);
                icon.setImageResource(R.drawable.gradientappspreview2);
                break;
            case 3:
                Log.e("case", "3");
                icon.setImageResource(0);
                icon.setImageResource(R.drawable.gradientappspreview3);
                break;
            case 4:
                Log.e("case", "4");
                icon.setImageResource(0);
                icon.setImageResource(R.drawable.gradientappspreview4);
                break;
            case 5:
                Log.e("case", "5");
                icon.setImageResource(0);
                icon.setImageResource(R.drawable.gradientappspreview5);
                break;
            case 6:
                Log.e("case", "6");
                icon.setImageResource(0);
                icon.setImageResource(R.drawable.gradientappspreview6);
                break;
        }
    }
}




