package com.example.sam.bookbag;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 6/6/16.
 */

//TODO save all the latest post data on sdcard file and on the onCreateView, populate adapter with those values.
public class Explore extends Fragment {
    EditText searchBar;
    StorageReference storageRef;
    DatabaseReference ref;
    Map<String, String> keysAndValues;
    ArrayList<String> lastOfFirstKey;
    ArrayList<String> lastOfPostKey;
    ArrayList<String> checkFirstListening;
    ArrayList<String> checkPostListening;
    JSONObject postData;
    JSONObject eachPostData;
    ListView exploreList;
    String condition;
    String edition;
    String price;
    File dir;
    PrintWriter file;
    public Explore(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("onThisScreen", "onCreateView");
        View view = inflater.inflate(R.layout.explore, container, false);
        checkPostFile();
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag");
        searchBar = (EditText)view.findViewById(R.id.searchBar);
        exploreList = (ListView)view.findViewById(R.id.exploreBoxList);
        keysAndValues =  new HashMap<>();
        lastOfFirstKey = new ArrayList<>();
        lastOfPostKey = new ArrayList<>();
        checkFirstListening = new ArrayList<>();
        checkPostListening = new ArrayList<>();
        postData  = new JSONObject();
        eachPostData = new JSONObject();
        storageRef = MyApplication.storageRef;
        ref = MyApplication.ref;
        checkIfFirstListeningIsDone();
        setFirebaseListener();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onThisScreen", "onCreate");
    }

    public void checkIfFirstListeningIsDone(){
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
    public void checkIfPostListeningIsDone(String firstKey){
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
               Log.e("lastOfFirstKeyList", lastOfFirstKey.toString());
               //if (!checkFirstListening.isEmpty()) {
               checkFirstListening.clear();
               Log.e("lastFirstKey", lastOfFirstKey.get((lastOfFirstKey.size() - 1)));
               Log.e("lastFirstKeySize", lastOfFirstKey.size() + "");
               afterUserIdAdded(lastOfFirstKey.get(lastOfFirstKey.size() - 1));
               // }
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
    public void afterUserIdAdded(final String firstKey){
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
                if (keysAndValues.size() == 7) {
                    Log.e("map keys", keysAndValues.keySet().toString());
                    Log.e("map values", keysAndValues.values().toString());
                    condition = keysAndValues.get("condition");
                    price = keysAndValues.get("price");
                    edition = keysAndValues.get("edition");
                    try {
                        eachPostData.put("title", postKey);
                        eachPostData.put("edition", edition);
                        eachPostData.put("condition", condition);
                        eachPostData.put("price", price);
                        postData.put(userId, eachPostData);
                        Log.e("postData", postData.toString());
                    }catch (JSONException JSE){
                        Log.e("JSON", "FAILED");
                    }
                    try {
                        file = null;
                        dir.mkdir();
                        file = new PrintWriter(new FileOutputStream(new File(dir, (userId + "_" + postKey))));
                        file.println(postData);
                        file.close();
                    }catch (IOException IOE){
                        Log.e("file", "NOT FOUND");
                    }
                   /* ExploreListAdapter adapter = new ExploreListAdapter(getContext(), edition, condition, price, postKey, userId);
                    exploreList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();*/
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
    public void checkPostFile(){
        ArrayList postFiles = new ArrayList<String>();
        File file = new File("/sdcard/Bookbag" );
        File list[] = file.listFiles();
        for( int i=0; i< list.length; i++)
        {
            postFiles.add( list[i].getName() );
        }
        Log.e("files", postFiles.toString());
        if(!postFiles.isEmpty()){
            Log.e("post", "there has been previous posts!");
        }

    }
}
