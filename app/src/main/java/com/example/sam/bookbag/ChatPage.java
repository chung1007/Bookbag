package com.example.sam.bookbag;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.facebook.FacebookSdk;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sam on 7/2/16.
 */
public class ChatPage extends AppCompatActivity {
    Intent intent;
    String sellerId;
    String sellerName;
    String bookName;
    EditText messageBox;
    TextView back;
    TextView chatMateName;
    public boolean isContinued = false;
    Firebase messageRoom;
    Button sendButton;
    String time;
    Boolean done;
    LinearLayout messagePage;
    ArrayList<String> messageKeys;
    Map<String, String> keysAndMessages;
    ScrollView scroll;
    File messageDir;
    PrintWriter file;
    String messageTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.messagingpage);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        done = false;
        messageRoom = new Firebase(Constants.chatDataBase);
        messageKeys = new ArrayList<>();
        keysAndMessages = new HashMap<>();
        scroll = (ScrollView)findViewById(R.id.messageScrollView);
        messagePage = (LinearLayout)findViewById(R.id.messagePage);
        intent = getIntent();
        sellerId = intent.getStringExtra("sellerId");
        sellerName = intent.getStringExtra("sellerName");
        bookName = intent.getStringExtra("bookName");
        checkIfBoxClicked();
        messageBox = (EditText)findViewById(R.id.messageBox);
        chatMateName = (TextView)findViewById(R.id.chatMateName);
        sendButton  = (Button)findViewById(R.id.sendButton);
        messageDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_chat");
        setPageInfo();
        listenForSendClicked();
        time = getCurrentTime();
        checkIfNewMessageIsDone(bookName);
        listenForNewMessages(bookName);
        messageRoom.child(HomePage.userId).child(sellerId+"_"+sellerName).child(bookName).child(time).setValue("sjvsdvbsdbv");
        HomePage.viewPager.setCurrentItem(1);
        chateMateNameClicked();
    }
    public void setPageInfo(){
        Log.e("firstTime!", "true");
        if(isContinued == false) {
            messageBox.setText("Hi! I am interested in buying " + bookName + ".");
        }
        chatMateName.setText(sellerName);
        back = (TextView)findViewById(R.id.backFromChat);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomePage.viewPager.setCurrentItem(3);
                finish();

            }
        });
    }
    @Override
    public void onBackPressed() {
        HomePage.viewPager.setCurrentItem(3);
    }
    public void listenForSendClicked(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString();
                if (message.equals("")) {
                    //Do Nothing
                } else {
                    messageBox.setText("");
                    messageRoom.child(HomePage.userId).child(sellerId + "_" + sellerName).child(bookName).child(getCurrentTime() + "_" + HomePage.userName + "_" + messageTime).setValue(message);
                    messageRoom.child(sellerId).child(HomePage.userId + "_" + HomePage.userName).child(bookName).child(getCurrentTime() + "_" + HomePage.userName + "_" + messageTime).setValue(message);
                }
            }
        });
    }
    public String getCurrentTime(){
        String delegate = "hh:mm:ss aaa";
        String delegate2 = "hh:mm aaa";
        String date = (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
        messageTime = (String) DateFormat.format(delegate2, Calendar.getInstance().getTime());
        return date;
    }
    public void checkIfNewMessageIsDone(String bookName){
        messageRoom.child(HomePage.userId).child(sellerId+"_"+sellerName).child(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("data", "done reading");
                done = true;
                Log.e("done", done.toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    public void listenForNewMessages(final String bookName){
        messageRoom.child(HomePage.userId).child(sellerId+"_"+sellerName).child(bookName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("listening", "for new messages");
                String key = dataSnapshot.getKey();
                String message = dataSnapshot.getValue().toString();
                messageKeys.add(key);
                keysAndMessages.put(key, message);
                Log.e("done after listening", done.toString());
                if (done) {
                    Log.e("done", " is true");
                    messageRoom.child(HomePage.userId).child(sellerId + "_" + sellerName).child(bookName).child(time).setValue(null);
                    String latestMessage = messageKeys.get(messageKeys.size() - 1);
                    String newMessage = keysAndMessages.get(latestMessage);
                    if (newMessage.equals("sjvsdvbsdbv")) {
                        Log.e("message", "was test");

                    } else {
                        addMessage(latestMessage, newMessage);
                        Log.e("latestKey", latestMessage);
                        Log.e("newMessage", newMessage);
                        writeToFile(newMessage);
                    }
                    scrollDown();

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
    public void addMessage(String key, String message){
        if(key.contains(HomePage.userName)){
            Log.e("message", "user's");
            View messageBox = getUserMessageBox(key, message);
            messagePage.addView(messageBox);
        }else if(message.equals("test")) {
            //Do nothing
        }else{
            View otherMessageBox = getOtherMessageBox(key, message);
            messagePage.addView(otherMessageBox);
            Log.e("message", "not users");
        }
    }
    public View getUserMessageBox(String key, String message){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View userMessage = inflater.inflate(R.layout.usermessage, null);
        TextView messageText = (TextView)userMessage.findViewById(R.id.usersMessageBox);
        TextView timeStamp = (TextView)userMessage.findViewById(R.id.timeStamp);
        messageText.setText(message);
        key = key.substring(Math.max(key.length() - 8, 0));
        timeStamp.setText(key);
        Log.e("timeStamp", key);
        return userMessage;
    }
    public View getOtherMessageBox(String key, String message){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View otherMessage = inflater.inflate(R.layout.othermessage, null);
        TextView messageText = (TextView)otherMessage.findViewById(R.id.otherMessageBox);
        TextView timeStamp = (TextView)otherMessage.findViewById(R.id.otherTimeStamp);
        messageText.setText(message);
        key = key.substring(Math.max(key.length() - 8, 0));
        timeStamp.setText(key);
        return otherMessage;
    }
    public void writeToFile(String message){

        try {
            messageDir.mkdir();
            file = null;
            file = new PrintWriter(new FileOutputStream(new File(messageDir, (sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_")))));
            file.println(message);
            Log.e("fromChatPage", message);
            file.close();
            Chat.messageFromPage = message;
        }catch(IOException IOE){
            Log.e("chat", "saving conv. failed");
        }
    }

    public void checkIfBoxClicked(){
        String continued = intent.getStringExtra("isContinued");
        if(continued!=null){
            isContinued = true;
        }else{
            isContinued = false;
        }
    }
    public void scrollDown(){
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    public void chateMateNameClicked(){
        chatMateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RatingPage.class);
                intent.putExtra("profileId", sellerId);
                intent.putExtra("profileName", sellerName);
                startActivity(intent);

            }
        });
    }

}
