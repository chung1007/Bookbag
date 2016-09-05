package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {
    TextView next;
    Thread thread;

    public static TextView displayText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.previewpage);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        next = (TextView)findViewById(R.id.next);
        /*displayText = (TextView) findViewById(R.id.textDisplay);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Hero.otf");
        displayText.setTypeface(tf);*/
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
}




