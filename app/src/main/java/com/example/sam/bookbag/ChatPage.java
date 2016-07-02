package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.facebook.FacebookSdk;

/**
 * Created by sam on 7/2/16.
 */
public class ChatPage extends AppCompatActivity {
    Intent intent;
    String sellerId;
    String sellerName;
    String bookName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.messagingpage);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        intent = getIntent();
        sellerId = intent.getStringExtra("sellerId");
        sellerName = intent.getStringExtra("sellerName");
        bookName = intent.getStringExtra("bookName");
    }

}
