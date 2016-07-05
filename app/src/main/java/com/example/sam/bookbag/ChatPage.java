package com.example.sam.bookbag;

import android.app.Activity;
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
import android.widget.ListView;
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
        messageBox = (EditText)findViewById(R.id.messageBox);
        chatMateName = (TextView)findViewById(R.id.chatMateName);
        sendButton  = (Button)findViewById(R.id.sendButton);
        messageDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_chat");
        setPageInfo();
        listenForSendClicked();
        time = getCurrentTime();
        checkIfNewMessageIsDone(bookName);
        listenForNewMessages(bookName);
        messageRoom.child(HomePage.userId).child(bookName).child(time).setValue("test");
    }
    public void setPageInfo(){
        Log.e("firstTime!", "true");
        if(messagePage.getChildAt(0)==null) {
            messageBox.setText("Hi! i am interested in buying " + bookName + ".");
        }
        chatMateName.setText(sellerName);
        back = (TextView)findViewById(R.id.backFromChat);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                HomePage.viewPager.setCurrentItem(3);
            }
        });
    }
    public void listenForSendClicked(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString();
                if(message.equals("")){
                    //Do Nothing
                }else {
                    messageBox.setText("");
                    messageRoom.child(HomePage.userId).child(bookName).child(getCurrentTime() + "_" + HomePage.userName + "_" + messageTime).setValue(message);
                    messageRoom.child(sellerId).child(bookName).child(getCurrentTime() + "_" + HomePage.userName + "_" + messageTime).setValue(message);
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
        messageRoom.child(HomePage.userId).child(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
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
        messageRoom.child(HomePage.userId).child(bookName).addChildEventListener(new ChildEventListener() {
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
                    messageRoom.child(HomePage.userId).child(bookName).child(time).setValue(null);
                    String latestMessage = messageKeys.get(messageKeys.size() - 1);
                    String newMessage = keysAndMessages.get(latestMessage);
                    writeToFile(newMessage);
                    addMessage(latestMessage, newMessage);
                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    Log.e("latestKey", latestMessage);
                    Log.e("newMessage", newMessage);
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
            file = new PrintWriter(new FileOutputStream(new File(messageDir, (sellerName.replace(" ", "") + "_" + bookName.replace(" ", "_")))));
            file.println(message);
            file.close();
        }catch(IOException IOE){
            Log.e("chat", "saving conv. failed");
        }
    }
    public String readFile(String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            return null;
        }
        String dataOfFile = "";
        String buf;
        try {
            while ((buf = file.readLine()) != null) {
                dataOfFile = dataOfFile.concat(buf + "\n");
            }
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            return null;
        }
        return dataOfFile;
    }
    /*String name = sellerName.replace(" ", "") + "_" + bookName.replace(" ", "_");
    String splitName[] = name.split("_");
    ArrayList<String> chars = new ArrayList<>();
    for(int i = 1; i < splitName.length; i++){
        chars.add(splitName[i]);
    }
    Log.e("splitName", chars.toString());*/

}
