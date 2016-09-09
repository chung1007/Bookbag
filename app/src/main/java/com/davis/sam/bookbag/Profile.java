package com.davis.sam.bookbag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sam on 6/6/16.
 */
public class Profile extends Fragment {
    ArrayList<String> userIdlist;
    List<JSONObject> dataPoints;
    ExploreListAdapter adapter;
    ProfileListAdapter profileAdapter;
    public static ListView profileList;
    ImageView list;
    EditText searchBar;
    boolean listShowing = false;
    DatabaseReference ref;
    boolean deletingDone = false;
    ArrayList<String> rateKeys;
    ArrayList<String> rateKeysToShow;
    Firebase rateDataBase;
    boolean isProfilelist;
    View view;

    public Profile() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile, container, false);
        Firebase.setAndroidContext(getContext());
        initializations(view);
        setListeners();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void checkPostFile() {
        ArrayList<String> postFiles = new ArrayList<>();
        userIdlist = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_explore");
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                if (list[i].getName().contains(HomePage.userId)) {
                    postFiles.add(list[i].getName());
                    String splitName[] = list[i].getName().split("_");
                    String userId = splitName[0];
                    userIdlist.add(userId);
                }
            }
        } catch (NullPointerException NPE) {
            toastMaker("No posts currently");
        }
        Log.e("files", postFiles.toString());
        if (!postFiles.isEmpty()) {
            Log.e("post", "there has been previous posts!");
            Log.e("userId's", userIdlist.toString());
            listPostNames(postFiles, userIdlist);
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

    public void listPostNames(ArrayList<String> postNames, ArrayList<String> userIds) {
        dataPoints = new ArrayList<>();
        for (int i = 0; i < postNames.size(); i++) {
            String fileName = "sdcard/Bookbag_explore/" + postNames.get(i);
            String content = readFile(fileName);
            try {
                JSONObject postDataRead = new JSONObject(content);
                dataPoints.add(postDataRead);
            } catch (JSONException JSE) {
                Log.e("assign json", "failed");
            }

        }
        Collections.reverse(dataPoints);
        Collections.reverse(userIds);
        displayPostBoxes(dataPoints, userIds);
    }

    public void displayPostBoxes(List<JSONObject> datapoints, ArrayList<String> userIds) {
        adapter = new ExploreListAdapter(getContext(), datapoints, userIds);
        profileList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("boxes", "made");
    }

    public void toastMaker(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void setShowListClickedListener() {
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listShowing == false) {
                    ExploreListAdapter.isprofile = true;
                    checkPostFile();
                    isProfilelist = false;
                    try {
                        ref.child(HomePage.userId).child("Initialized").setValue(null);
                    } catch (NullPointerException NPE) {
                        Log.e("Initialized", "already deleted");
                    }
                    listShowing = true;
                    searchBar.setText("My Listings");
                    searchBar.setGravity(Gravity.CENTER);
                    searchBar.setFocusable(false);
                    searchBar.setFocusableInTouchMode(false);
                    putDownKeyBoard();
                } else {
                    profileList.setAdapter(null);
                    listShowing = false;
                    searchBar.setText("");
                    searchBar.setGravity(Gravity.CENTER);
                    searchBar.setFocusableInTouchMode(true);

                }

            }
        });
    }

    public void setSearchBarClickListener() {
        searchBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!searchBar.getText().toString().equals("My Listings")) {
                    searchBar.setCursorVisible(true);
                    searchBar.setGravity(Gravity.NO_GRAVITY);
                    searchBar.setGravity(Gravity.CENTER_VERTICAL);

                }
                return false;
            }
        });
    }

    /*public void listItemClickListener() {
        profileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("delete", "clicked");
                TextView boxTitle = (TextView) view.findViewById(R.id.exploreBoxTitle);
                String bookTitle = boxTitle.getText().toString();
                markItemAsSold(bookTitle);
                return true;
            }
        });
    }*/
    public void setDataBaseListener(){
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();
                Log.e("deleted userid", userId);
                getNameOfDeletedItem(userId);
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

    public void getNameOfDeletedItem(final String userId){
        ref.child(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String deletedItem = dataSnapshot.getKey();
                deleteIfExists(userId, deletedItem);
                Log.e("deleted item", deletedItem);
                refreshPage();
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
        if(!id.equals(HomePage.userId)) {
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
    }
    public void getAllRateKeys(){
        rateDataBase.addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                rateKeys.add(dataSnapshot.getKey());
                Log.e("rateKeys", rateKeys.toString());
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
    public void initializations(View view){
        rateDataBase = new Firebase(Constants.ratingDataBase);
        profileList = (ListView) view.findViewById(R.id.profileList);
        list = (ImageView) view.findViewById(R.id.userListings);
        searchBar = (EditText) view.findViewById(R.id.profileSearchBar);
        ref = MyApplication.ref;
        ref.child(HomePage.userId).child("Initialized").setValue("listeners initialized");
        rateKeys = new ArrayList<>();
        rateKeysToShow = new ArrayList<>();
        getAllRateKeys();
        searchBar.setCursorVisible(false);
        searchBar.setText("");
    }

    public void setListeners(){
        setShowListClickedListener();
        setSearchBarClickListener();
        //listItemClickListener();
        setDataBaseListener();
        setProfileSearchBarlistener();
        profileBoxClickListener();
    }

    public void setProfileSearchBarlistener(){
        searchBar.setGravity(Gravity.CENTER);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchBar.getText().toString().equals("")) {
                    profileList.setAdapter(null);
                    searchBar.setCursorVisible(false);
                    searchBar.setGravity(Gravity.CENTER);
                    rateKeysToShow.clear();
                    putDownKeyBoard();
                } else if (searchBar.getText().toString().replace(" ", "").length() > 3 && !searchBar.getText().toString().equals("My Listings")) {
                    for (int i = 0; i < rateKeys.size(); i++) {
                        if ((rateKeys.get(i)).toLowerCase().contains("_" + ((searchBar.getText().toString()).toLowerCase()))) {
                            if (!rateKeysToShow.contains(rateKeys.get(i))) {
                                rateKeysToShow.add(rateKeys.get(i));
                            }
                        }
                    }
                    Log.e("rateKeysToShow", rateKeysToShow.toString());
                    Collections.reverse(rateKeysToShow);
                    profileAdapter = new ProfileListAdapter(getContext(), rateKeysToShow);
                    profileList.setAdapter(null);
                    profileList.setAdapter(profileAdapter);
                    profileAdapter.notifyDataSetChanged();
                    isProfilelist = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void putDownKeyBoard(){
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void profileBoxClickListener(){
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isProfilelist){
                    //Don't do anything
                }else{
                    TextView sellerName = (TextView)view.findViewById(R.id.sellerProfileName);
                    TextView sellerId = (TextView)view.findViewById(R.id.sellerProfileId);
                    String name = sellerName.getText().toString();
                    String ID = sellerId.getText().toString();
                    Intent intent = new Intent(getContext(), RatingPage.class);
                    intent.putExtra("profileName", name);
                    intent.putExtra("profileId", ID);
                    startActivity(intent);
                    Log.e("profileName", name);
                    Log.e("profileId", ID);
                }
            }
        });
    }
    public void refreshPage(){
        profileList.setAdapter(null);
        checkPostFile();
    }

    @Override
    public void onPause() {
        super.onPause();
        searchBar.setText("");
        listShowing = false;
    }


}
