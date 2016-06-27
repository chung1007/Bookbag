package com.example.sam.bookbag;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

/**
 * Created by sam on 6/6/16.
 */
public class WishList extends Fragment {
    List<JSONObject>dataPoints;
    List<JSONObject>wishListWanted;
    ArrayList<String>userIdlist;
    ExploreListAdapter adapter;
    WishListAdapter wishAdapter;
    ListView wishList;
    Button button1;
    Button button2;
    ImageView addWantItem;
    File wishBookDir;
    PrintWriter file;
    View dialogDisplay;

    public WishList() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wishlist, container, false);
        wishList = (ListView)view.findViewById(R.id.userWishList);
        button2 = (Button)view.findViewById(R.id.button2);
        button1 = (Button)view.findViewById(R.id.button1);
        addWantItem = (ImageView)view.findViewById(R.id.addWantItem);
        wishBookDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/wishes");
        dialogDisplay = View.inflate(getContext(), R.layout.suggestwantitem, null);
        checkPostFile();
        setBookMatchListener();
        setPlusButtonListener(dialogDisplay);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void checkPostFile() {
        ArrayList<String> postFiles = new ArrayList<>();
        userIdlist = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_wishList/existing");
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                postFiles.add(list[i].getName());
                String splitName[] = list[i].getName().split("_");
                String userId = splitName[0];
                userIdlist.add(userId);
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
    public void getFiles() {
        ArrayList<String> postFiles = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_wishList/wishes");
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                postFiles.add(list[i].getName());
            }
        } catch (NullPointerException NPE) {
            toastMaker("No posts currently");
        }
        Log.e("files", postFiles.toString());
        if (!postFiles.isEmpty()) {
            Log.e("post", "there has been previous posts!");
            Log.e("userId's", userIdlist.toString());
            wishNames(postFiles);
        }

    }
    public void wishNames(ArrayList<String> postNames) {
        wishListWanted = new ArrayList<>();
        for (int i = 0; i < postNames.size(); i++) {
            String fileName = "sdcard/Bookbag_wishList/wishes/" + postNames.get(i);
            String content = readFile(fileName);
            try {
                JSONObject postDataRead = new JSONObject(content);
                wishListWanted.add(postDataRead);
            } catch (JSONException JSE) {
                Log.e("assign json", "failed");
            }
        }
        displayWishBoxes(wishListWanted);
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
            String fileName = "sdcard/Bookbag_wishList/existing/" + postNames.get(i);
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
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button2.setTextColor(R.color.tabSelected);
                button1.setTextColor(Color.BLACK);
                wishList.setAdapter(null);
                wishList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
        Log.e("boxes", "made");
    }
    public void displayWishBoxes(List<JSONObject> datapoints) {
        wishAdapter = new WishListAdapter(getContext(), datapoints);
        wishList.setAdapter(null);
        wishList.setAdapter(wishAdapter);
        wishAdapter.notifyDataSetChanged();

        Log.e("boxes", "made");
    }
    public void setBookMatchListener(){
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFiles();
                button1.setTextColor(R.color.tabSelected);
                button2.setTextColor(Color.BLACK);
            }
        });
    }
    public void toastMaker(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    public void setPlusButtonListener(final View dialogView){
        addWantItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("");
                builder.setView(dialogView)
                        .setCancelable(false)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                EditText textBookName = (EditText) dialogView.findViewById(R.id.bookTheyWant);
                                EditText textBookISBN = (EditText) dialogView.findViewById(R.id.ISBNOfDesiredBook);
                                String bookName = textBookName.getText().toString();
                                String bookISBN = textBookISBN.getText().toString();
                                saveWantedBookInfo(bookName, bookISBN);
                            }
                        }).show();

            }
        });
    }
    public void saveWantedBookInfo(String bookName, String bookISBN){
        try {
            file = null;
            JSONObject wantedBookInfo = new JSONObject();
            try {
                wantedBookInfo.put("bookName", bookName);
                wantedBookInfo.put("bookISBN", bookISBN);
            }catch (JSONException JSE){
                Log.e("wantedBookJson", "failed");
            }

            wishBookDir.mkdir();
            file = new PrintWriter(new FileOutputStream(new File(wishBookDir, (bookName.replace(" ", "_")+"_"+ bookISBN))));
            file.println(wantedBookInfo);
            file.close();
        } catch (IOException IOE) {
            Log.e("file", "NOT FOUND");
        }
    }

}

