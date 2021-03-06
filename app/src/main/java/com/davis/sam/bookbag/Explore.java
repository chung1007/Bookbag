package com.davis.sam.bookbag;



import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 6/6/16.
 */

//TODO save all the latest post data on sdcard file and on the onCreateView, populate adapter with those values.
public class Explore extends Fragment{

    EditText searchBar;
    StorageReference storageRef;
    ExploreListAdapter adapter;
    NotificationManager mNotificationManager;
    Spinner sortList;
    DatabaseReference ref;
    Map<String, String> keysAndValues;
    ArrayList<String> lastOfFirstKey;
    ArrayList<String> lastOfPostKey;
    ArrayList<String> checkFirstListening;
    ArrayList<String> checkPostListening;
    ArrayList<String> userIdlist;
    ArrayList<Bitmap> displayBM;
    ArrayList<String> postKeyNames = new ArrayList<>();
    List<JSONObject> dataPoints;
    JSONObject postData;
    JSONObject eachPostData;
    JSONObject displayPictures;
    ListView exploreList;
    View infoView;
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
    int counter;
    PrintWriter file;
    PrintWriter anotherFile;
    View view;

    public Explore() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("onThisScreen", "onCreateView");
        view = inflater.inflate(R.layout.explore, container, false);
        checkVersionAndAskPermission();
        exploreList = (ListView) view.findViewById(R.id.exploreBoxList);
        if(!FacebookLogin.firstTime) {
            checkPostFile();
        }
        termsOfService();
        exploreDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_explore");
        wishDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/existing");
        wishExistDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList/wishExist");
        //deleteAllFiles();
        initializeFiles();
        searchBar = (EditText) view.findViewById(R.id.searchBar);
        searchBar.setCursorVisible(false);
        sortList = (Spinner) view.findViewById(R.id.exploreSpinner);
        keysAndValues = new HashMap<>();
        lastOfFirstKey = new ArrayList<>();
        lastOfPostKey = new ArrayList<>();
        checkFirstListening = new ArrayList<>();
        checkPostListening = new ArrayList<>();
        displayBM = new ArrayList<>();
        storageRef = MyApplication.storageRef;
        ref = MyApplication.ref;
        setSearchBarListener();
        setSortItemListener();
        checkIfFirstListeningIsDone();
        setFirebaseListener();
        listenForListItemClicked();
        setSearchBarClickListener();
        checkForDeletes();
        //setDataBaseDeleteListener();
        return view;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onThisScreen", "onCreate");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        //getActivity().startService(new Intent(getContext(), Explore.class));
    }
    @Override
    public void onPause() {
        super.onPause();
        searchBar.setText("");

    }

    public void initializeFiles(){
        try {
            File testFile;
            testFile = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_wishList");
            testFile.mkdir();
            file = null;
            file = new PrintWriter(new FileOutputStream(new File(testFile, (""))));
            file.println("");
            file.close();
        }catch (IOException IOE){
            Log.e("file initializing", "failed");
        }
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
                if (!postKey.equals("Initialized")) {
                    postKeyNames.add(firstKey + "_" + postKey);
                }
                Log.e("postKeyNames", postKeyNames.toString());
                Log.e("lastOfPostKeyList", lastOfPostKey.toString());
                if (!FacebookLogin.firstTime && isStoragePermissionGranted()) {
                    if (!postKey.equals("Initialized")) {
                        getPostData(firstKey, postKey);
                    }
                }
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
                if (keysAndValues.size() == 12) {
                    condition = keysAndValues.get("condition");
                    price = keysAndValues.get("price");
                    edition = keysAndValues.get("edition");
                    ISBN = keysAndValues.get("ISBN");
                    bitmap = keysAndValues.get("bitmap1");
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
                        refreshExplorePage();
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
    public void deleteAllFiles(){
        File file1 = new File("/sdcard/Bookbag_explore");
        File file2 = new File("/sdcard/Bookbag_chat");
        File file3 = new File("/sdcard/Bookbag_wishList");
        File file4 = new File("/sdcard/Bookbag_newChat");
        File fileL1[] = file1.listFiles();
        File fileL2[] = file2.listFiles();
        File fileL3[] = file3.listFiles();
        File fileL4[] = file4.listFiles();
        deleteFilesInPath("/sdcard/Bookbag_explore", fileL1);
        deleteFilesInPath("/sdcard/Bookbag_chat", fileL2);
        deleteFilesInPath("/sdcard/Bookbag_wishList", fileL3);
        deleteFilesInPath("/sdcard/Bookbag_newChat", fileL4);

    }

    public void deleteFilesInPath(String path, File [] list){
            for (int i = 0; i < list.length; i++) {
                File deleteFile = new File(path + "/" + list[i].getName());
                deleteFile.delete();
            }
    }

    public void checkPostFile() {
        ArrayList<String> postFiles = new ArrayList<>();
        userIdlist = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_explore");
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                postFiles.add(list[i].getName());
                String splitName[] = list[i].getName().split("_");
                String userId = splitName[0];
                userIdlist.add(userId);
            }
        } catch (NullPointerException NPE) {
            if(!FacebookLogin.firstTime) {
                toastMaker("No posts currently");
            }
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
        adapter = new ExploreListAdapter(getContext(), datapoints, userIds);
        exploreList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("boxes", "made");
    }

    public void toastMaker(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void setSortItemListener() {
        sortList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Price")) {
                    casePriceChosen();
                } else if (selectedItem.equals("Recent")) {
                    checkPostFile();
                } else if (selectedItem.equals("Cond.")) {
                    caseConditionChosen();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });
    }
    public void setSearchBarClickListener(){
        searchBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchBar.setCursorVisible(true);
                searchBar.setGravity(Gravity.NO_GRAVITY);
                searchBar.setGravity(Gravity.CENTER_VERTICAL);
                return false;
            }
        });
    }

    public void setSearchBarListener() {
        searchBar.setGravity(Gravity.CENTER);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchBar.getText().toString().equals("")) {
                    searchBar.setCursorVisible(false);
                    searchBar.setGravity(Gravity.CENTER);
                    exploreList.setAdapter(null);
                    checkPostFile();
                    putDownKeyBoard();
                } else if (dataPoints != null) {
                    for (int j = 0; j < dataPoints.size(); j++) {
                        JSONObject jsonFirst = dataPoints.get(j);
                        Iterator<String> keys = jsonFirst.keys();
                        String firstKey = keys.next();
                        try {
                            JSONObject jsonUnderFirst = jsonFirst.getJSONObject(firstKey);
                            if (!jsonUnderFirst.getString("title").toLowerCase().contains(searchBar.getText().toString().toLowerCase())) {
                                dataPoints.remove(jsonFirst);
                                userIdlist.remove(firstKey);
                            }
                        } catch (JSONException JSE) {
                            Log.e("json in search", "FAILED");
                        }

                    }
                    exploreList.setAdapter(null);
                    displayPostBoxes(dataPoints, userIdlist);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public void casePriceChosen() {
        try {
            HashMap<String, String> mapToSort = new HashMap<>();
            for (int i = 0; i < dataPoints.size(); i++) {
                JSONObject jsonFirst = dataPoints.get(i);
                Iterator<String> keys = jsonFirst.keys();
                String firstKey = keys.next();
                try {
                    JSONObject jsonUnderFirst = jsonFirst.getJSONObject(firstKey);
                    String key = firstKey + "_" + jsonUnderFirst.getString("title");
                    Log.e("compare keys", key);
                    String value = jsonUnderFirst.getString("price");
                    mapToSort.put(key, value);
                } catch (JSONException JSE) {
                    Log.e("JSON in sorting", "FAILED");
                }
            }
            HashMap<String, String> sortedMap = sortByValues(mapToSort);
            List<JSONObject> sortedDataPoints = new ArrayList<>();
            List<String> sortedKeys = new ArrayList<>(sortedMap.keySet());
            List<String> sortedValues = new ArrayList<>(sortedMap.values());
            ArrayList<String> userIds = new ArrayList<>();
            List<String> list = new ArrayList<>();
            Log.e("mapToSort", mapToSort.toString());
            Log.e("sorted Map", sortedMap.toString());
            counter = 0;
            while (list.size() != dataPoints.size()) {
                for (int i = 0; i < dataPoints.size(); i++) {
                    JSONObject jsonFirst = dataPoints.get(i);
                    Iterator<String> keys = jsonFirst.keys();
                    String firstKey = keys.next();
                    try {
                        JSONObject jsonUnderFirst = jsonFirst.getJSONObject(firstKey);
                        String key = firstKey + "_" + jsonUnderFirst.getString("title");
                        String value = jsonUnderFirst.getString("price");
                        if (key.equals(sortedKeys.get(counter)) && value.equals(sortedValues.get(counter))) {
                            list.add(key);
                            sortedDataPoints.add(jsonFirst);
                            userIds.add(firstKey);
                            Log.e("addedKey", key);
                            sortedKeys.remove(sortedKeys.get(counter));
                            sortedValues.remove(sortedValues.get(counter));
                            if (sortedKeys.size() == 0) {
                                break;
                            }
                        } else {
                        }
                    } catch (JSONException JSE) {
                        Log.e("JSON in sorting", "FAILED");
                    }
                }
            }
            displayPostBoxes(sortedDataPoints, userIds);
            Log.e("matches", list.toString());
        }catch (NullPointerException NPE){
            Log.e("No items", "null");
        }
    }

    public void caseConditionChosen() {
        try {
            HashMap<String, String> mapToSort = new HashMap<>();
            HashMap<String, String> conditionValues = new HashMap<>();
            conditionValues.put("As New", "1");
            conditionValues.put("Fine", "2");
            conditionValues.put("Very Good", "3");
            conditionValues.put("Good", "4");
            conditionValues.put("Fair", "5");
            conditionValues.put("Poor", "6");
            for (int i = 0; i < dataPoints.size(); i++) {
                JSONObject jsonFirst = dataPoints.get(i);
                Iterator<String> keys = jsonFirst.keys();
                String firstKey = keys.next();
                try {
                    JSONObject jsonUnderFirst = jsonFirst.getJSONObject(firstKey);
                    String title = jsonUnderFirst.getString("title");
                    String condition = jsonUnderFirst.getString("condition");
                    String key = firstKey + "_" + title + "_" + condition;
                    String value = conditionValues.get(condition);
                    mapToSort.put(key, value);
                } catch (JSONException JSE) {
                    Log.e("JSON in sorting", "FAILED");
                }
            }
            Log.e("conditionMap", mapToSort.toString());
            HashMap<String, String> sortedMap = sortByValues(mapToSort);
            List<JSONObject> sortedDataPoints = new ArrayList<>();
            List<String> sortedKeys = new ArrayList<>(sortedMap.keySet());
            List<String> sortedValues = new ArrayList<>(sortedMap.values());
            ArrayList<String> userIds = new ArrayList<>();
            List<String> list = new ArrayList<>();
            Log.e("mapToSort", mapToSort.toString());
            Log.e("sorted Map", sortedMap.toString());
            counter = 0;
            while (list.size() != dataPoints.size()) {
                for (int i = 0; i < dataPoints.size(); i++) {
                    JSONObject jsonFirst = dataPoints.get(i);
                    Iterator<String> keys = jsonFirst.keys();
                    String firstKey = keys.next();
                    try {
                        JSONObject jsonUnderFirst = jsonFirst.getJSONObject(firstKey);
                        String title = jsonUnderFirst.getString("title");
                        String condition = jsonUnderFirst.getString("condition");
                        String key = firstKey + "_" + title + "_" + condition;
                        if (key.equals(sortedKeys.get(counter))) {
                            list.add(key);
                            sortedDataPoints.add(jsonFirst);
                            userIds.add(firstKey);
                            Log.e("addedKey", key);
                            sortedKeys.remove(sortedKeys.get(counter));
                            sortedValues.remove(sortedValues.get(counter));
                            if (sortedKeys.size() == 0) {
                                break;
                            }
                        } else {
                        }
                    } catch (JSONException JSE) {
                        Log.e("JSON in sorting", "FAILED");
                    }
                }
            }
            displayPostBoxes(sortedDataPoints, userIds);
            Log.e("matches", list.toString());
        }catch(NullPointerException NPE){
            Log.e("no listings", "null");
        }
    }

    public void listenForListItemClicked() {
        exploreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                infoView = View.inflate(getContext(), R.layout.bookinfopage, null);
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
                Intent intent = new Intent(getContext(), RatingPage.class);
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
                Intent intent = new Intent(getContext(), ChatPage.class);
                intent.putExtra("sellerId", sellerId);
                intent.putExtra("sellerName", sellerName);
                intent.putExtra("bookName", bookName);
                startActivity(intent);
            }
        });

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

    public void setWishListAddClicked(ImageView view, final String data, final String userId, final String postKey){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("wishIcon", "clicked");
                if (view.getTag() != null) {
                    ImageView addIcon = (ImageView) view.findViewById(R.id.addToWishList);
                    toastMaker("removed bookmark");
                    String fileName = userId + "_" + (postKey.replace(" ", ""));
                    File file = new File("sdcard/Bookbag_wishList/existing/" + fileName);
                    file.delete();
                    addIcon.setImageResource(R.drawable.addtowishlist);
                    addIcon.setTag(null);

                } else {
                    try {
                        file = null;
                        wishDir.mkdir();
                        file = new PrintWriter(new FileOutputStream(new File(wishDir, (userId + "_" + (postKey.replace(" ", ""))))));
                        file.println(data);
                        file.close();
                        toastMaker("Added as bookmark in Wishlist");
                        checkifAlreadyAdded(view, userId, postKey);
                    } catch (IOException IOE) {
                        Log.e("file", "NOT FOUND WISHLIST");
                    }
                }
            }
        });
    }

    public void setViewPictures(final String userId, final String title, String number){

        getPictures(userId, title);

            /*final StorageReference imageRef = storageRef.child(userId).child(title).child("image" + number);
            imageRef.getBytes(Constants.ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    Log.e("completion", "SUCCCESS!");

                    imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.e("bytes", "SUCCESS");
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inScaled = false;
                            options.inJustDecodeBounds = false;
                            options.inDither = false;
                            options.inSampleSize = 0;
                            options.inScaled = false;
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
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
                                saveImageToGallery(displayBM.get(1));
                                Log.e("image", "saved to gallery");
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
            });*/
        }
    public void getPictures(final String userId, final String postKey) {
        keysAndValues.clear();
        ref.child(userId).child(postKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String keys = dataSnapshot.getKey();
                String values = dataSnapshot.getValue().toString();
                keysAndValues.put(keys, values);
                if (keysAndValues.size() == 12) {
                    displayPictures = new JSONObject();
                    String bitmap1 = keysAndValues.get("bitmap1");
                    String bitmap2 = keysAndValues.get("bitmap2");
                    String bitmap3 = keysAndValues.get("bitmap3");
                    String bitmap4 = keysAndValues.get("bitmap4");
                    try {
                        displayPictures.put("bitmap1", bitmap1);
                        displayPictures.put("bitmap2", bitmap2);
                        displayPictures.put("bitmap3", bitmap3);
                        displayPictures.put("bitmap4", bitmap4);

                        ImageView bookOne = (ImageView) infoView.findViewById(R.id.imageDisplayOne);
                        ImageView bookTwo = (ImageView) infoView.findViewById(R.id.imageDisplayTwo);
                        ImageView bookThree = (ImageView) infoView.findViewById(R.id.imageDisplayThree);
                        ImageView bookFour = (ImageView) infoView.findViewById(R.id.imageDisplayFour);
                        try {
                            bookOne.setImageBitmap(StringToBitMap(displayPictures.getString("bitmap1")));
                            bookTwo.setImageBitmap(StringToBitMap(displayPictures.getString("bitmap2")));
                            bookThree.setImageBitmap(StringToBitMap(displayPictures.getString("bitmap3")));
                            bookFour.setImageBitmap(StringToBitMap(displayPictures.getString("bitmap4")));
                        } catch (JSONException JE) {
                            Log.e("displayPictures", "failed");
                        }
                    } catch (JSONException JE) {
                        Log.e("getting pictures", "failed");
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
    public void checkIfWishExists(String fileName, String bookTitle, String ISBN){
        String content = readFile("/sdcard/Bookbag_wishList/wishes/"+fileName);
        if(content!=null){
            Log.e("item", "inWishList!");
            mNotificationManager =
                    (NotificationManager)getContext(). getSystemService(Context.NOTIFICATION_SERVICE);
            int notifyID = 1;
            android.support.v4.app.NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(getContext())
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
    public void putDownKeyBoard(){
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public boolean isStoragePermissionGranted() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.e("permission", "Permission is granted");
                    return true;
                } else {

                    Log.e("Permission", "Permission is revoked");
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return false;
                }
            } else { //permission is automatically granted on sdk<23 upon installation
                Log.e("Permission", "Permission is granted");
                return true;
            }
        }catch (NullPointerException NPE){
            //whatever
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.e("Permission","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            Log.e("permission", "GRANTED");
        }
    }
    public void checkVersionAndAskPermission(){
        if(FacebookLogin.firstTime) {
            if (Build.VERSION.SDK_INT >= 21) {
                isStoragePermissionGranted();
            } else {
                //Do Nothing
            }
        }
    }

    public void deleteIfExists(String id, String bookName){
        File explore = new File(android.os.Environment.getExternalStorageDirectory() + "/Bookbag_explore/" + id + "_" + bookName.replace(" ", ""));
        File wish = new File(android.os.Environment.getExternalStorageDirectory() + "/Bookbag_wishList/existing/" + id + "_" + bookName.replace(" ", ""));
        if (explore.exists()) {
            Log.e("deleted Item", "existed in explore");
            explore.delete();
            exploreList.setAdapter(null);
            checkPostFile();
        }
        if (wish.exists()) {
            Log.e("deleted Item", "existed in wishlist");
            wish.delete();
        }
    }
    public void refreshExplorePage(){
        exploreList.setAdapter(null);
        checkPostFile();
    }
    public void saveImageToGallery(Bitmap bitmap){
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";
        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, "TEST.png");
            file.createNewFile();
            fOut = new FileOutputStream(file);

            getResizedBitmap(bitmap, 1000, 1000).compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());


        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());

        }
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    public Bitmap StringToBitMap(String encodedString){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inSampleSize = 0;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            byte [] encodeByte=Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, options);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
    public void termsOfService(){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        View terms = inflater.inflate(R.layout.termsofservice, null);
        SharedPreferences pref = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        if(!pref.contains("terms")){
            new android.app.AlertDialog.Builder(getContext())
                    .setView(terms)
                    .setTitle("")
                    .setMessage("")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putString("terms", "shown").apply();
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else{
            //don't do anything
        }
    }

    public void checkForDeletes(){
        File file = new File("/sdcard/Bookbag_explore");
        File list[] = file.listFiles();
        for (int i = 0; i < list.length; i++){
            if(!postKeyNames.contains(list[i].getName())){
                File toDeleteFile = new File("sdcard/Bookbag_explore/" + list[i].getName());
                toDeleteFile.delete();
                postKeyNames.clear();
                refreshExplorePage();
            }
        }
    }



}

