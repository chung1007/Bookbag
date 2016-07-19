package com.example.sam.bookbag;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

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
import java.util.HashMap;
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

    public BackgroundListeners(){

    }
    IBinder mBinder;
    boolean mAllowRebind;

    @Override
    public void onCreate() {
        Log.e("oncreate", "of Background");
        ref = MyApplication.ref;
        exploreDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_explore");
        wishDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/existing");
        wishExistDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/wishExist");
        keysAndValues = new HashMap<>();
        lastOfFirstKey = new ArrayList<>();
        lastOfPostKey = new ArrayList<>();
        checkFirstListening = new ArrayList<>();
        checkPostListening = new ArrayList<>();
        ref = MyApplication.ref;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("started", "1");
        checkIfFirstListeningIsDone();
        setFirebaseListener();
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
                    Log.e("map keys", keysAndValues.keySet().toString());
                    Log.e("map values", keysAndValues.values().toString());
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


}
