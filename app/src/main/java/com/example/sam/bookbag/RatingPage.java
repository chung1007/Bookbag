package com.example.sam.bookbag;

import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Created by sam on 7/12/16.
 */
public class RatingPage extends AppCompatActivity {
    Intent intent;
    StorageReference storageRef;
    String sellerId;
    String sellerName;
    Integer userSmileCount;
    Integer userSadCount;
    Firebase rateDataBase;
    ArrayList<String> keys;
    ArrayList<Bitmap> displayBM;
    ProfilePictureView ratePagePicture;
    TextView profilePageFirstName;
    TextView profilePageLastName;
    TextView smileCounter;
    TextView sadCounter;
    TextView noListings;
    HashMap<String, Integer> keysAndValues;
    ImageView smile;
    ImageView sad;
    ListView activeListing;
    ArrayList<String> userIdlist;
    ExploreListAdapter adapter;
    List<JSONObject> dataPoints;
    View infoView;
    PrintWriter file;
    PrintWriter anotherFile;
    File wishDir;
    File wishExistDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sellerprofilepage);
        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SpannableString s = new SpannableString("Profile");
        s.setSpan(new TypefaceSpan(this, "Roboto.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(null);
        setUpTitle();
        intent = getIntent();
        Log.e("ratingpage", "started");
        sellerId = intent.getStringExtra("profileId");
        sellerName = intent.getStringExtra("profileName");
        rateDataBase = new Firebase(Constants.ratingDataBase);
        smile = (ImageView)findViewById(R.id.smile);
        sad = (ImageView)findViewById(R.id.sad);
        activeListing = (ListView)findViewById(R.id.sellerActiveListings);
        noListings = (TextView)findViewById(R.id.noListingsText);
        keys = new ArrayList<>();
        displayBM = new ArrayList<>();
        keysAndValues = new HashMap<>();
        wishDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/existing");
        wishExistDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/wishExist");
        storageRef = MyApplication.storageRef;
        setPageInfo();
        checkPostFile();
        getCurrentRatings();
        listenForSmileClicked();
        listenForSadClicked();
        listenForListItemClicked();

    }

    public void getCurrentRatings(){
        rateDataBase.child(sellerId + "_" + sellerName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue().toString();
                keys.add(key);
                keysAndValues.put(key, Integer.parseInt(value));
                if (keys.size() == 2) {
                    userSmileCount = keysAndValues.get("likes");
                    userSadCount = keysAndValues.get("dislikes");
                    Log.e("smiles", userSmileCount + " ");
                    Log.e("sads", userSadCount + " ");
                    smileCounter.setText(Integer.toString(userSmileCount));
                    sadCounter.setText(Integer.toString(userSadCount));
                    Log.e("setCounters", "true");
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
    public void setPageInfo(){
        ratePagePicture = (ProfilePictureView)findViewById(R.id.profileImageOfSeller);
        profilePageFirstName = (TextView)findViewById(R.id.profileFirstNameOfSeller);
        profilePageLastName = (TextView)findViewById(R.id.profileLastNameOfSeller);
        smileCounter = (TextView)findViewById(R.id.smileCounter);
        sadCounter = (TextView)findViewById(R.id.sadCounter);
        ratePagePicture.setProfileId(sellerId);
        String name[] = sellerName.split(" ");
        profilePageFirstName.setText(name[0]);
        profilePageLastName.setText(name[1]);

    }
    public void listenForSmileClicked(){
        smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveRate(sellerId)) {
                    smileCounter.setText(Integer.toString(userSmileCount + 1));
                    rateDataBase.child(sellerId + "_" + sellerName).child("likes").setValue(Integer.parseInt(smileCounter.getText().toString()));
                }else{
                    toastMaker("You already rated!");
                }
            }
        });
    }
    public void listenForSadClicked(){
        sad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveRate(sellerId)) {
                    sadCounter.setText(Integer.toString(userSadCount + 1));
                    rateDataBase.child(sellerId + "_" + sellerName).child("dislikes").setValue(Integer.parseInt(sadCounter.getText().toString()));
                }else {
                    toastMaker("You already rated!");
                }
            }
        });
    }
    public void toastMaker(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    public void checkPostFile() {
        ArrayList<String> postFiles = new ArrayList<>();
        userIdlist = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_explore");
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                if (list[i].getName().contains(sellerId)) {
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
        ExploreListAdapter.isprofile = false;
        adapter = new ExploreListAdapter(this, datapoints, userIds);
        activeListing.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("boxes", "made");
        if(adapter.isEmpty()){
            noListings.setVisibility(View.VISIBLE);
            Log.e("noListings", "set visible true");
        }else{
            noListings.setVisibility(View.INVISIBLE);
            Log.e("noListings", "set visible false");
        }
    }
    public void listenForListItemClicked() {
        activeListing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                infoView = View.inflate(getApplicationContext(), R.layout.bookinfopage, null);
                TextView Id = (TextView) view.findViewById(R.id.userId);
                TextView boxTitle = (TextView) view.findViewById(R.id.exploreBoxTitle);
                ImageView wishListAdd = (ImageView) infoView.findViewById(R.id.addToWishList);
                String sellersId = Id.getText().toString();
                String bookTitle = boxTitle.getText().toString();
                String fileName = sellersId + "_" + (bookTitle.replace(" ", ""));
                String content = readFile("/sdcard/Bookbag_explore/" + fileName);
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
                    checkifAlreadyAdded(infoView, sellersId, bookTitle);
                    setViewData(infoView, viewDataList);
                    setUpViewingDialog(infoView);
                    setWishListAddClicked(wishListAdd, content, sellersId, bookTitle);
                    setContactSellerListener(infoView, sellersId, seller, bookTitle);
                    setProfilePictureClickListener(profilePictureView, sellersId, seller);
                } catch (JSONException JSE) {
                    Log.e("item click listener", "json failed!");
                }
            }
        });
    }
    public void setProfilePictureClickListener(ProfilePictureView view, final String sellerId, final String sellerName){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RatingPage.class);
                intent.putExtra("profileId", sellerId);
                intent.putExtra("profileName", sellerName);
                startActivity(intent);
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
    public void setContactSellerListener(View view, final String sellerId, final String sellerName, final String bookName){
        Button contactSeller = (Button)view.findViewById(R.id.contactSellerButton);
        contactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatPage.class);
                intent.putExtra("sellerId", sellerId);
                intent.putExtra("sellerName", sellerName);
                intent.putExtra("bookName", bookName);
                startActivity(intent);
            }
        });

    }

    public void setUpViewingDialog(View view) {

        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();

    }

    public void setWishListAddClicked(ImageView view, final String data, final String userId, final String postKey){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("wishIcon", "clicked");
                if (view.getTag() != null) {
                    toastMaker("Already in Wist List!");
                    Log.e("item", "already in list!");
                } else {
                    try {
                        file = null;
                        wishDir.mkdir();
                        file = new PrintWriter(new FileOutputStream(new File(wishDir, (userId + "_" + (postKey.replace(" ", ""))))));
                        file.println(data);
                        file.close();
                        toastMaker("Added to WishList!");
                        checkifAlreadyAdded(view, userId, postKey);
                    } catch (IOException IOE) {
                        Log.e("file", "NOT FOUND WISHLIST");
                    }
                }
            }
        });
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
    public void checkifAlreadyAdded(View view, String sellersId, String bookTitle){
        ImageView addIcon = (ImageView)view.findViewById(R.id.addToWishList);
        String fileName = sellersId+"_"+(bookTitle.replace(" ", ""));
        String content = readFile("/sdcard/Bookbag_wishList/existing/"+fileName);
        if(content!=null){
            Log.e("item", "inWishList!");
            addIcon.setImageResource(R.drawable.addedicon);
            addIcon.setTag(R.drawable.addedicon);
        }else{
            //Do nothing
        }

    }
    public boolean saveRate(String sellerId){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(pref.contains(sellerId)){
            Log.e("already rated!", "true");
            return false;
        }else{
            Log.e("already rated!", "false");
            editor.putString(sellerId, "testing");
            editor.apply();
            return true;
        }

    }
    public class TypefaceSpan extends MetricAffectingSpan {
        /** An <code>LruCache</code> for previously loaded typefaces. */
        private LruCache<String, Typeface> sTypefaceCache =
                new LruCache<String, Typeface>(12);

        private Typeface mTypeface;

        /**
         * Load the {@link Typeface} and apply to a {@link Spannable}.
         */
        public TypefaceSpan(Context context, String typefaceName) {
            mTypeface = sTypefaceCache.get(typefaceName);

            if (mTypeface == null) {
                mTypeface = Typeface.createFromAsset(context.getApplicationContext()
                        .getAssets(), String.format("fonts/%s", typefaceName));

                // Cache the loaded Typeface
                sTypefaceCache.put(typefaceName, mTypeface);
            }
        }

        @Override
        public void updateMeasureState(TextPaint p) {
            p.setTypeface(mTypeface);

            // Note: This flag is required for proper typeface rendering
            p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setTypeface(mTypeface);

            // Note: This flag is required for proper typeface rendering
            tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
    public void setUpTitle(){
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.actionbar, null);
        TextView titleTextView = (TextView) v.findViewById(R.id.appTitle);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto.ttf");
        titleTextView.setTypeface(tf);
        this.getSupportActionBar().setCustomView(v);
    }

}
