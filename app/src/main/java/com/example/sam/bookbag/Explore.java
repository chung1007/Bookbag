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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
    LinearLayout exploreList;
    String condition;
    String edition;
    String price;
    String userId;
    String postKey;
    View exploreBox;
    LayoutInflater inflater;

    public Explore(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.explore, container, false);
        searchBar = (EditText)view.findViewById(R.id.searchBar);
        exploreList = (LinearLayout)view.findViewById(R.id.exploreBoxList);
        keysAndValues =  new HashMap<>();
        storageRef = MyApplication.storageRef;
        ref = MyApplication.ref;
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

   public void setFirebaseListener(){
       ref.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               userId = dataSnapshot.getKey();
               Log.e("userId", userId);
               ref.child(userId).addChildEventListener(new ChildEventListener() {
                   @Override
                   public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                       postKey = dataSnapshot.getKey();
                       ref.child(userId).child(postKey).addChildEventListener(new ChildEventListener() {
                           @Override
                           public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                               keysAndValues.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                               Log.e("dataList", keysAndValues.toString());
                               if(keysAndValues.size() == 7){
                                   condition = keysAndValues.get("condition");
                                   edition = keysAndValues.get("edition");
                                   price = keysAndValues.get("price");
                                   exploreBox = createBox(postKey, edition, condition, price, userId);
                                   exploreList.addView(exploreBox);
                               }else{
                                   //do nothing
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

    private View createBox(String title, String edition, String condition, String price, String userId) {
        if (getActivity() != null) {
            inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        }
        View box = inflater.inflate(R.layout.explorebox, null);
        TextView boxTitle = (TextView)box.findViewById(R.id.exploreBoxTitle);
        TextView boxEdition = (TextView)box.findViewById(R.id.exploreBoxEdition);
        TextView boxCondition = (TextView)box.findViewById(R.id.exploreBoxCondition);
        TextView boxPrice = (TextView)box.findViewById(R.id.exploreBoxPrice);
        final ImageView boxImage = (ImageView)box.findViewById(R.id.exploreImageView);
        boxTitle.setText(title);
        boxEdition.setText(edition);
        boxCondition.setText(condition);
        boxPrice.setText(price);

        storageRef.child(userId).child(title).child("image1").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
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
            }
        });
        return box;
    }
}
