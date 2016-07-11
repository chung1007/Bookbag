package com.example.sam.bookbag;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    ListView profileList;
    ImageView list;
    EditText searchBar;
    boolean listShowing = false;
    DatabaseReference ref;
    boolean deletingDone = false;


    public Profile() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);
        profileList = (ListView) view.findViewById(R.id.profileList);
        list = (ImageView) view.findViewById(R.id.userListings);
        searchBar = (EditText) view.findViewById(R.id.profileSearchBar);
        ref = MyApplication.ref;
        ref.child(HomePage.userId).child("Initialized").setValue("listeners initialized");
        searchBar.setCursorVisible(false);
        setShowListClickedListener();
        setSearchBarClickListener();
        listItemClickListener();
        setDataBaseListener();
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
                    checkPostFile();
                    try {
                        ref.child(HomePage.userId).child("Initialized").setValue(null);
                    } catch (NullPointerException NPE) {
                        Log.e("Initialized", "already deleted");
                    }
                    listShowing = true;
                } else {
                    profileList.setAdapter(null);
                    listShowing = false;
                }

            }
        });
    }

    public void setSearchBarClickListener() {
        searchBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchBar.setCursorVisible(true);
                return false;
            }
        });
    }

    public void listItemClickListener() {
        profileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView boxTitle = (TextView) view.findViewById(R.id.exploreBoxTitle);
                String bookTitle = boxTitle.getText().toString();
                markItemAsSold(bookTitle);
                return false;
            }
        });
    }
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
    public void listenUntilDoneDeleting(String userId){
        ref.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deletingDone = true;
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
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void markItemAsSold(final String bookTitle){
        new AlertDialog.Builder(getContext())
                .setTitle("Mark as sold?")
                .setMessage("Delete from listings?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ref.child(HomePage.userId).child(bookTitle).setValue(null);
                        String fileName = "sdcard/Bookbag_explore/"+HomePage.userId+"_"+bookTitle.replace(" ", "");
                        String fileName2 = android.os.Environment.getExternalStorageDirectory() + "/Bookbag_wishList/existing/"+HomePage.userId+"_"+bookTitle.replace(" ", "");
                        File file = new File(fileName);
                        File file2 = new File(fileName2);
                        file.delete();
                        if(file2.exists()){
                            file2.delete();
                            Log.e("file deleted", "in wishList");
                        }
                        profileList.setAdapter(null);
                        checkPostFile();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
}
