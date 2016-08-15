package com.example.sam.bookbag;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 7/18/16.
 */
public class BackgroundListeners extends Service {
    DatabaseReference ref;
    ArrayList<String> checkFirstListening;
    ArrayList<String> checkPostListening;
    ArrayList<String> lastOfFirstKey;
    ArrayList<String> lastOfPostKey;
    Map<String, String> keysAndValues;
    String condition;
    String edition;
    String price;
    String ISBN;
    String bitmap;
    String author;
    String notes;
    String seller;
    File exploreDir;
    File wishDir;
    File wishExistDir;
    PrintWriter file;
    PrintWriter anotherFile;
    JSONObject postData;
    JSONObject eachPostData;
    NotificationManager mNotificationManager;
    Firebase chatDataBase;
    boolean newSellerDone;
    boolean newConvoDone = false;
    ArrayList<String>sellers;
    HashMap<String, String> conversations;
    ArrayList<String> messageKeyList;
    File messageDir;
    File newMessageDir;
    PrintWriter oldfile;
    PrintWriter newFile;
    String date;

    public BackgroundListeners(){

    }
    IBinder mBinder;
    boolean mAllowRebind;

    @Override
    public void onCreate() {
        Log.e("oncreate", "of Background");
        Firebase.setAndroidContext(this);
        ref = MyApplication.ref;
        chatDataBase = new Firebase("https://scorching-heat-6663.firebaseio.com/");
        exploreDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_explore");
        wishDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/existing");
        wishExistDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/wishExist");
        keysAndValues = new HashMap<>();
        lastOfFirstKey = new ArrayList<>();
        lastOfPostKey = new ArrayList<>();
        checkFirstListening = new ArrayList<>();
        checkPostListening = new ArrayList<>();
        chatDataBase = new Firebase(Constants.chatDataBase);
        messageDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_chat");
        newMessageDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_newChat");
        sellers = new ArrayList<>();
        messageKeyList = new ArrayList<>();
        conversations = new HashMap<>();
        /*if(FacebookLogin.firstTime) {
            chatDataBase.child(userId).child("123456789_No Name").child("initialized").child("00:00:00 AM_No Name_00:00 AM").setValue("new messages initialized");
            FacebookLogin.firstTime = false;
        }*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Log.e("started", "1");
        if(!pref.contains("userId") && !pref.contains("userName")) {
            try {
                String userIdFromIntent = intent.getStringExtra("userId");
                String userNameFromIntent = intent.getStringExtra("userName");
                editor.putString("userId", userIdFromIntent);
                editor.putString("userName", userNameFromIntent);
                editor.apply();
                checkIfFirstListeningIsDone();
                setFirebaseListener();
                setNewSellerListener(userIdFromIntent);
                setAllMessagesListener(userIdFromIntent, userNameFromIntent);
            }catch (NullPointerException NPE){
                Log.e("user", "hasn't logged in to app yet!");
            }
        }else{
            Log.e("userId", pref.getString("userId", null));
            Log.e("userName", pref.getString("userName", null));
            checkIfFirstListeningIsDone();
            setFirebaseListener();
            setNewSellerListener(pref.getString("userId", null));
            setAllMessagesListener(pref.getString("userId", null), pref.getString("userName", null));
        }
        Log.e("firebase", "listening!");
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {}
    @Override
    public void onDestroy() {}
    public void ExploreListeners(){

    }
    public void setAddedPostsListener(ArrayList<String> previousKeys){
        Log.e("previousKeys", previousKeys.toString());
        for(int i =0; i < previousKeys.size(); i++){
            afterUserIdAdded(previousKeys.get(i));
            Log.e("changes to prev keys", "listening");
        }
    }

    public void checkIfFirstListeningIsDone() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkFirstListening.add("Done");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkIfPostListeningIsDone(String firstKey) {
        ref.child(firstKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkPostListening.add("Done");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setFirebaseListener() {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                lastOfFirstKey.clear();
                String firstKey = dataSnapshot.getKey();
                lastOfFirstKey.add(firstKey);
                setAddedPostsListener(lastOfFirstKey);
                Log.e("lastOfFirstKeyList", lastOfFirstKey.toString());
                if (!checkFirstListening.isEmpty()) {
                    checkFirstListening.clear();
                    Log.e("lastFirstKey", lastOfFirstKey.get((lastOfFirstKey.size() - 1)));
                    Log.e("lastFirstKeySize", lastOfFirstKey.size() + "");
                    afterUserIdAdded(lastOfFirstKey.get(lastOfFirstKey.size() - 1));
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
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void afterUserIdAdded(final String firstKey) {
        ref.child(firstKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                checkIfPostListeningIsDone(firstKey);
                lastOfPostKey.clear();
                String postKey = dataSnapshot.getKey();
                lastOfPostKey.add(postKey);
                Log.e("lastOfPostKeyList", lastOfPostKey.toString());
                if (!checkPostListening.isEmpty()) {
                    checkPostListening.clear();
                    Log.e("lastPostKeySize", lastOfPostKey.size() + "");
                    Log.e("lastPostKey", lastOfPostKey.get(lastOfPostKey.size() - 1));
                    getPostData(firstKey, lastOfPostKey.get(lastOfPostKey.size() - 1));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String deletedItem = dataSnapshot.getKey();
                deleteIfExists(firstKey, deletedItem);
                Log.e("deleted item", deletedItem);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getPostData(final String userId, final String postKey) {
        ref.child(userId).child(postKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String keys = dataSnapshot.getKey();
                String values = dataSnapshot.getValue().toString();
                keysAndValues.put(keys, values);
                if (keysAndValues.size() == 9) {
                    condition = keysAndValues.get("condition");
                    price = keysAndValues.get("price");
                    edition = keysAndValues.get("edition");
                    ISBN = keysAndValues.get("ISBN");
                    bitmap = keysAndValues.get("bitmap");
                    author = keysAndValues.get("authorName");
                    notes = keysAndValues.get("notes");
                    seller = keysAndValues.get("seller");
                    Log.e("key ISBN", ISBN);
                    try {
                        postData = new JSONObject();
                        eachPostData = new JSONObject();
                        eachPostData.put("title", postKey);
                        eachPostData.put("edition", edition);
                        eachPostData.put("condition", condition);
                        eachPostData.put("price", price);
                        eachPostData.put("ISBN", ISBN);
                        eachPostData.put("author", author);
                        eachPostData.put("notes", notes);
                        eachPostData.put("bitmap", bitmap);
                        eachPostData.put("seller", seller);
                        postData.put(userId, eachPostData);
                    } catch (JSONException JSE) {
                        Log.e("JSON", "FAILED");
                    }
                    try {
                        exploreDir.mkdir();
                        String fileName = postKey.replace(" ", "_") + "_" + ISBN;
                        file = null;
                        file = new PrintWriter(new FileOutputStream(new File(exploreDir, (userId + "_" + (postKey.replace(" ", ""))))));
                        file.println(postData);
                        file.close();
                        checkIfWishExists(fileName, postKey, ISBN);
                    } catch (IOException IOE) {
                        Log.e("file", "NOT FOUND EXPLORE");
                    }
                    keysAndValues.clear();
                } else {
                    //keep adding on to keys and values list;
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
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void deleteIfExists(String id, String bookName){
        File explore = new File(android.os.Environment.getExternalStorageDirectory() + "/Bookbag_explore/" + id + "_" + bookName.replace(" ", ""));
        File wish = new File(android.os.Environment.getExternalStorageDirectory() + "/Bookbag_wishList/existing/" + id + "_" + bookName.replace(" ", ""));
        if (explore.exists()) {
            Log.e("deleted Item", "existed in explore");
            explore.delete();

        }
        if (wish.exists()) {
            Log.e("deleted Item", "existed in wishlist");
            wish.delete();
        }
    }
    public void checkIfWishExists(String fileName, String bookTitle, String ISBN){
        String content = readFile("/sdcard/Bookbag_wishList/wishes/"+fileName);
        if(content!=null){
            Log.e("item", "inWishList!");
            mNotificationManager =
                    (NotificationManager)this. getSystemService(Context.NOTIFICATION_SERVICE);
            int notifyID = 1;
            android.support.v4.app.NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("A Wish List item is available!")
                    .setContentText(bookTitle + " is on sale")
                    .setSmallIcon(R.drawable.notification);
            mNotificationManager.notify(
                    notifyID,
                    mNotifyBuilder.build());
            try {
                Log.e("wishExistsFile", "start");
                try {
                    JSONObject existingWishData = new JSONObject();
                    existingWishData.put("bookName", bookTitle);
                    existingWishData.put("bookISBN", ISBN);
                    wishExistDir.mkdir();
                    anotherFile = null;
                    anotherFile =  new PrintWriter(new FileOutputStream(new File(wishExistDir, (bookTitle.replace(" ", "_") + "_" + (ISBN)))));
                    anotherFile.println(existingWishData);
                    anotherFile.close();
                }catch (JSONException JSE){
                    Log.e("existing", "Failed");
                }
                File file = new File("sdcard/Bookbag_wishList/wishes/"+bookTitle.replace(" ", "_")+"_"+ISBN);
                file.delete();
                Log.e("wishExistsFile", "end");
            }catch (IOException IOE){
                Log.e("wishExists", "failed");
            }
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setNewSellerListener(String userId) {
        chatDataBase.child(userId).addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                newSellerDone = true;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setAllMessagesListener(final String userId, final String userName) {
        chatDataBase.child(userId).addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                chatDataBase.child(userId).child("123456789_No Name").setValue(null);
                Log.e("newChatMate", "added!");
                String sellerKey = dataSnapshot.getKey();
                sellers.add(sellerKey);
                getNewChatTopic(sellerKey, userId, userName);
                if (newSellerDone) {
                    String latestChatMate = sellers.get(sellers.size() - 1);
                    getNewChatTopic(latestChatMate, userId, userName);
                    newSellerDone = false;
                    sellers.clear();
                    Log.e("lastChatMate", latestChatMate);
                }
            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getNewChatTopic(final String sellerKey, final String userId, final String userName) {
        chatDataBase.child(userId).child(sellerKey).addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Log.e("newBook", "added");
                String newTopic = dataSnapshot.getKey();
                checkForLatestMessage(sellerKey, newTopic, userId);
                getMessages(sellerKey, newTopic, userId, userName);
                Log.e("newTopic", newTopic);
            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getMessages(final String newChatMate, final String newTopic, String userId, final String userName) {
        chatDataBase.child(userId).child(newChatMate).child(newTopic).addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Log.e("new messages", "added");
                String messageKeys = dataSnapshot.getKey();
                Log.e("keys", messageKeys);
                String messages = dataSnapshot.getValue().toString();
                Log.e("messages", messages);
                messageKeyList.add(messageKeys);
                conversations.put(messageKeys, messages);
                //if (newConvoDone) {
                newConvoDone = false;
                Log.e("conversatons", conversations.toString());
                String lastMessageKey = messageKeyList.get(messageKeyList.size() - 1);
                String lastMessage = conversations.get(lastMessageKey);
                Log.e("lastKey", lastMessageKey);
                Log.e("lastMessage", lastMessage);
                if (!lastMessage.equals("sjvsdvbsdbv")) {
                    writeToFileAndUpdate(newChatMate, newTopic, lastMessage, lastMessageKey, userName);
                }

            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void checkForLatestMessage(String chatMate, String newTopic, String userId) {
        chatDataBase.child(userId).child(chatMate).child(newTopic).addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                newConvoDone = true;
                Log.e("newConvoDone", "is True");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void writeToFileAndUpdate(String sellerAndId, String messageKey, String message, String lastMessageKey, String userName) {
        Log.e("new convo", "writing to file!");
        Log.e("lastMessageKey", lastMessageKey);
        String[] sellerAndIdSplit = sellerAndId.split("_");
        String sellerId = sellerAndIdSplit[0];
        Log.e("sellerId", sellerId);
        String sellerName = sellerAndIdSplit[1];
        Log.e("sellerName", sellerName);
        String bookName = messageKey;
        Log.e("lastestBook", bookName);
        String fileName = "sdcard/Bookbag_chat/"+sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_");
        try {
            if(!lastMessageKey.contains("_"+userName+"_") || readFile(fileName) == null) {
                String delegate = "hh:mm:ss aaa";
                String delegate2 = "hh:mm aaa";
                String date = (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
                String dateOfLastKey = lastMessageKey.substring(0, Math.min(lastMessageKey.length(), 8));
                String dateNumber = date.substring(0, Math.min(date.length(), 8));
                Log.e("dateOflastKey", dateOfLastKey);
                Log.e("date", date);
                if(Integer.parseInt(dateNumber.replace(":", ""))-Integer.parseInt(dateOfLastKey.replace(":", "")) < 3) {
                    messageDir.mkdir();
                    oldfile = null;
                    oldfile = new PrintWriter(new FileOutputStream(new File(messageDir, (sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_")))));
                    oldfile.println(message);
                    Log.e("fromBackChat", message);
                    oldfile.close();
                    newMessageNotification(sellerName, message);
                    saveNewChats(sellerName, sellerId, bookName, message);
                    Log.e("chat page", "refreshed");
                }
            }
        } catch (IOException IOE) {
            Log.e("chat", "saving conv. failed");
        }
    }

    public void saveNewChats(String sellerName, String sellerId, String bookName, String message){
        newMessageDir.mkdir();
        newFile = null;
        try {
            newFile = new PrintWriter(new FileOutputStream(new File(newMessageDir, (sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_")))));
            newFile.println(message);
            newFile.close();
        }catch (IOException IOE){
            Log.e("file", "failed");
        }

    }
    public void newMessageNotification(String person, String newMessage){
        mNotificationManager =
                (NotificationManager)this. getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 1;
        android.support.v4.app.NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("New message from "+ person)
                .setContentText(newMessage)
                .setSmallIcon(R.drawable.notification);
        mNotificationManager.notify(
                notifyID,
                mNotifyBuilder.build());
    }


}

