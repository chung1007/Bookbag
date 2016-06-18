package com.example.sam.bookbag;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 6/6/16.
 */
public class Explore extends Fragment {
    EditText searchBar;
    StorageReference storageRef;
    DatabaseReference ref;
    Map<String, String> keysAndValues;
    ArrayList<String> lastOfFirstKey;
    ArrayList<String> lastOfPostKey;
    ArrayList<String> checkFirstListening;
    ArrayList<String> checkPostListening;
    ListView exploreList;
    String condition;
    String edition;
    String price;
    String userId;
    String postKey;
    View exploreBox;
    LayoutInflater inflater;
    View box;
    final long ONE_MEGABYTE = 1024 * 1024;


    public Explore(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.explore, container, false);
        searchBar = (EditText)view.findViewById(R.id.searchBar);
        exploreList = (ListView)view.findViewById(R.id.exploreBoxList);
        keysAndValues =  new HashMap<>();
        lastOfFirstKey = new ArrayList<>();
        lastOfPostKey = new ArrayList<>();
        checkFirstListening = new ArrayList<>();
        checkPostListening = new ArrayList<>();
        storageRef = MyApplication.storageRef;
        ref = MyApplication.ref;
        checkIfFirstListeningIsDone();
        setFirebaseListener();
        searchBar.setCursorVisible(false);
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                searchBar.setCursorVisible(true);
            }
        });
        /*Drawable img = Explore.this.getContext().getResources().getDrawable(
                R.drawable.search);
        img.setBounds(0, 0, 0, searchBar.getMeasuredHeight());*/
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void checkIfFirstListeningIsDone(){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkFirstListening.add("Done");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    public void checkIfPostListeningIsDone(String firstKey){
        ref.child(firstKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {checkPostListening.add("Done");}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
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
           public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
           @Override
           public void onChildRemoved(DataSnapshot dataSnapshot) {}
           @Override
           public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
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
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    public void getPostData(final String userId, final String postKey){
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
                    Log.e("condition", condition);
                    Log.e("price", price);
                    exploreBox = createBox(postKey, edition, condition, price, userId);
                    ExploreListAdapter adapter = new ExploreListAdapter(getContext(), edition, condition, price, postKey, userId);
                    exploreList.setAdapter(adapter);
                    adapter.add(exploreBox);
                    adapter.notifyDataSetChanged();
                    keysAndValues.clear();
                }else {
                    //keep adding on to keys and values list;
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private View createBox(final String title, String edition, String condition, String price, final String userId) {
        if (getActivity() != null) {
            inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            box = inflater.inflate(R.layout.explorebox, null);
        }
        TextView boxTitle = (TextView)box.findViewById(R.id.exploreBoxTitle);
        TextView boxEdition = (TextView)box.findViewById(R.id.exploreBoxEdition);
        TextView boxCondition = (TextView)box.findViewById(R.id.exploreBoxCondition);
        TextView boxPrice = (TextView)box.findViewById(R.id.exploreBoxPrice);
        final ImageView boxImage = (ImageView)box.findViewById(R.id.exploreImageView);
        boxTitle.setText(title);
        boxEdition.setText(edition);
        boxCondition.setText(condition);
        boxPrice.setText(price);
        final StorageReference imageRef = storageRef.child(userId).child(title).child("image1");

        imageRef.getBytes(ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                Log.e("completion", "SUCCCESS!");

                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.e("bytes", "SUCCESS");
                        // Use the bytes to display the image
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        boxImage.setImageBitmap(null);
                        boxImage.destroyDrawingCache();
                        boxImage.setImageResource(0);
                        boxImage.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.e("userIdForImage", userId);
                        Log.e("titleForImage", title);
                        Log.e("getting image", "failed");
                    }
                });
            }
        });
        return box;
    }

    /*
    keysAndValues.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                               if (keysAndValues.size() == 7 ) {
                                   Log.e("firebase", "here are the post data");
                                   Log.e("dataList", keysAndValues.toString());
                                   condition = keysAndValues.get("condition");
                                   edition = keysAndValues.get("edition");
                                   price = keysAndValues.get("price");
                                   exploreBox = createBox(postKey, edition, condition, price, userId);
                                   ExploreListAdapter adapter = new ExploreListAdapter(getContext(), edition, condition, price, postKey, userId);
                                   exploreList.setAdapter(adapter);
                                   adapter.add(exploreBox);
                                   adapter.notifyDataSetChanged();
                                   keysAndValues.clear();
                                   //exploreList.addView(exploreBox);
                               }else {
                                   //keep adding to keysAndValues
                               }
     */
}
