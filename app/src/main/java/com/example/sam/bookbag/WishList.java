package com.example.sam.bookbag;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
    StorageReference storageRef;
    ListView wishList;
    Button button1;
    Button button2;
    ImageView addWantItem;
    ImageView seeWishItems;
    File wishBookDir;
    PrintWriter file;
    View dialogDisplay;
    View infoView;
    ArrayList<Bitmap> displayBM;

    public WishList() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wishlist, container, false);
        wishList = (ListView)view.findViewById(R.id.userWishList);
        seeWishItems = (ImageView)view.findViewById(R.id.seeWishItems);
        button2 = (Button)view.findViewById(R.id.button2);
        button1 = (Button)view.findViewById(R.id.button1);
        displayBM = new ArrayList<>();
        storageRef = MyApplication.storageRef;
        addWantItem = (ImageView)view.findViewById(R.id.addWantItem);
        wishBookDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/wishes");
        dialogDisplay = View.inflate(getContext(), R.layout.suggestwantitem, null);
        checkPostFile();
        setWishesListener();
        listenForListItemClicked();
        setPlusButtonListener(dialogDisplay);
        setUnavailableItemListener();
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
    public void getFiles(String path, String otherPath) {
        ArrayList<String> postFiles = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_wishList/" + path);
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
            Log.e("postFiles", postFiles.toString());
            wishNames(postFiles, otherPath);
        }

    }
    public void wishNames(ArrayList<String> postNames, String path) {
        wishListWanted = new ArrayList<>();
        for (int i = 0; i < postNames.size(); i++) {
            String fileName =  "sdcard/Bookbag_wishList/" + path + "/" + postNames.get(i);
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
        if(datapoints.size()>0){
            button2.setTextColor(R.color.capsuleSelected);
            button1.setTextColor(Color.WHITE);
            wishList.setAdapter(null);
            wishList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPostFile();
                button2.setTextColor(R.color.capsuleSelected);
                button1.setTextColor(Color.WHITE);
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
    public void setWishesListener(){
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wishList.setAdapter(null);
                getFiles("wishExist", "wishExist");
                button1.setTextColor(R.color.capsuleSelected);
                button2.setTextColor(Color.WHITE);
            }
        });
    }
    public void setUnavailableItemListener(){
        seeWishItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFiles("wishes", "wishes");
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
                if(dialogView.getParent()!=null) {
                    ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                }
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
    public void listenForListItemClicked() {
        wishList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(button2.getCurrentTextColor()!=Color.BLACK){
                    Log.e("button", "clicked");
                    infoView = View.inflate(getContext(), R.layout.bookinfopage, null);
                    ImageView add = (ImageView)infoView.findViewById(R.id.addToWishList);
                    ((ViewGroup) add.getParent()).removeView(add);
                    TextView Id = (TextView) view.findViewById(R.id.userId);
                    TextView boxTitle = (TextView) view.findViewById(R.id.exploreBoxTitle);
                    String sellersId = Id.getText().toString();
                    String bookTitle = boxTitle.getText().toString();
                    String fileName = sellersId + "_" + (bookTitle.replace(" ", ""));
                    String content = readFile("/sdcard/Bookbag_wishList/existing/" + fileName);
                    ProfilePictureView profilePictureView;
                    profilePictureView = (ProfilePictureView) infoView.findViewById(R.id.sellerImage);
                    profilePictureView.setProfileId(sellersId);
                    for (int j = 0; j < 4; j++) {
                        setViewPictures(sellersId, bookTitle, Integer.toString(j + 1));
                        Log.e("j", j + "");
                    }
                    try {
                        JSONObject fileData = new JSONObject(content);
                        Iterator<String> keys = fileData.keys();
                        String firstKey = keys.next();
                        JSONObject jsonToRead = fileData.getJSONObject(firstKey);
                        String price = jsonToRead.getString("price");
                        String edition = jsonToRead.getString("edition");
                        String author = jsonToRead.getString("author");
                        String ISBN = jsonToRead.getString("ISBN");
                        String condition = jsonToRead.getString("condition");
                        String notes = jsonToRead.getString("notes");
                        String seller = jsonToRead.getString("seller");
                        ArrayList<String> viewDataList = new ArrayList<>(Arrays.asList(price, bookTitle, edition, author,
                                ISBN, condition, notes, seller));
                        setViewData(infoView, viewDataList);
                        setUpViewingDialog(infoView);
                    } catch (JSONException JSE) {
                        Log.e("item click listener", "json failed!");
                    }
                }
            }
        });
    }
    public void setViewData(View view, ArrayList<String> values){

        TextView price = (TextView)view.findViewById(R.id.priceDisplay);
        TextView bookTitle = (TextView)view.findViewById(R.id.nameDisplay);
        TextView edition = (TextView)view.findViewById(R.id.editionDisplay);
        TextView author = (TextView)view.findViewById(R.id.authorDisplay);
        TextView ISBN = (TextView)view.findViewById(R.id.ISBNDisplay);
        TextView condition = (TextView)view.findViewById(R.id.conditionDisplay);
        TextView notes = (TextView)view.findViewById(R.id.notesDisplay);
        TextView seller = (TextView)view.findViewById(R.id.sellerName);
        ArrayList<TextView> textviews = new ArrayList<>(Arrays.asList(price, bookTitle, edition,
                author, ISBN, condition, notes, seller));
        for (int i = 0; i < values.size(); i++){
            textviews.get(i).setText(values.get(i));
        }

    }

    public void setUpViewingDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("");
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();

    }
    public void setViewPictures(String userId, String title, String number){

        final StorageReference imageRef = storageRef.child(userId).child(title).child("image" + number);
        imageRef.getBytes(Constants.ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                Log.e("completion", "SUCCCESS!");

                imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.e("bytes", "SUCCESS");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        displayBM.add(bitmap);
                        Log.e("sizeafteradd", displayBM.size() + "");
                        Log.e("bitmap", bitmap.toString());
                        if (displayBM.size() == 4) {
                            ImageView bookOne = (ImageView) infoView.findViewById(R.id.imageDisplayOne);
                            ImageView bookTwo = (ImageView) infoView.findViewById(R.id.imageDisplayTwo);
                            ImageView bookThree = (ImageView) infoView.findViewById(R.id.imageDisplayThree);
                            ImageView bookFour = (ImageView) infoView.findViewById(R.id.imageDisplayFour);
                            bookOne.setImageBitmap(displayBM.get(0));
                            bookTwo.setImageBitmap(displayBM.get(1));
                            bookThree.setImageBitmap(displayBM.get(2));
                            bookFour.setImageBitmap(displayBM.get(3));
                            displayBM.clear();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.e("getting image", "failed");
                    }
                });
            }
        });
    }

}

