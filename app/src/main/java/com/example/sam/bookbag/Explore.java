package com.example.sam.bookbag;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
public class Explore extends Fragment {
    EditText searchBar;
    StorageReference storageRef;
    ExploreListAdapter adapter;
    Spinner sortList;
    DatabaseReference ref;
    Map<String, String> keysAndValues;
    ArrayList<String> lastOfFirstKey;
    ArrayList<String> lastOfPostKey;
    ArrayList<String> checkFirstListening;
    ArrayList<String> checkPostListening;
    ArrayList<String> userIdlist;
    List<JSONObject> dataPoints;
    JSONObject postData;
    JSONObject eachPostData;
    ListView exploreList;
    String condition;
    String edition;
    String price;
    String ISBN;
    String bitmap;
    File dir;
    int counter;
    PrintWriter file;
    public Explore(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("onThisScreen", "onCreateView");
        View view = inflater.inflate(R.layout.explore, container, false);
        exploreList = (ListView)view.findViewById(R.id.exploreBoxList);
        checkPostFile();
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_explore");
        searchBar = (EditText)view.findViewById(R.id.searchBar);
        sortList = (Spinner)view.findViewById(R.id.exploreSpinner);
        keysAndValues =  new HashMap<>();
        lastOfFirstKey = new ArrayList<>();
        lastOfPostKey = new ArrayList<>();
        checkFirstListening = new ArrayList<>();
        checkPostListening = new ArrayList<>();
        storageRef = MyApplication.storageRef;
        ref = MyApplication.ref;
        setSearchBarListener();
        setSortItemListener();
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
                if (keysAndValues.size() == 8) {
                    Log.e("map keys", keysAndValues.keySet().toString());
                    Log.e("map values", keysAndValues.values().toString());
                    condition = keysAndValues.get("condition");
                    price = keysAndValues.get("price");
                    edition = keysAndValues.get("edition");
                    ISBN = keysAndValues.get("ISBN");
                    bitmap = keysAndValues.get("bitmap");
                    Log.e("key ISBN", ISBN);
                    try {
                        postData = new JSONObject();
                        eachPostData = new JSONObject();
                        eachPostData.put("title", postKey);
                        eachPostData.put("edition", edition);
                        eachPostData.put("condition", condition);
                        eachPostData.put("price", price);
                        eachPostData.put("ISBN", ISBN);
                        eachPostData.put("bitmap", bitmap);
                        postData.put(userId, eachPostData);
                    } catch (JSONException JSE) {
                        Log.e("JSON", "FAILED");
                    }
                    try {
                        file = null;
                        dir.mkdir();
                        file = new PrintWriter(new FileOutputStream(new File(dir, (userId + "_" + (postKey.replace(" ", ""))))));
                        file.println(postData);
                        file.close();
                    } catch (IOException IOE) {
                        Log.e("file", "NOT FOUND");
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
    public void checkPostFile(){
        ArrayList<String> postFiles = new ArrayList<>();
        userIdlist = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_explore" );
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                postFiles.add(list[i].getName());
                String splitName[] = list[i].getName().split("_");
                String userId = splitName[0];
                userIdlist.add(userId);
            }
        }catch (NullPointerException NPE){
            toastMaker("No posts currently");
        }
        Log.e("files", postFiles.toString());
        if(!postFiles.isEmpty()){
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
    public void listPostNames(ArrayList<String> postNames, ArrayList<String> userIds){
        dataPoints = new ArrayList<>();
        for (int i = 0; i < postNames.size(); i++){
            String fileName = "sdcard/Bookbag_explore/" + postNames.get(i);
            String content = readFile(fileName);
            try {
                JSONObject postDataRead = new JSONObject(content);
                dataPoints.add(postDataRead);
            }catch (JSONException JSE){
                Log.e("assign json", "failed");
            }

        }
        Collections.reverse(dataPoints);
        Collections.reverse(userIds);
        displayPostBoxes(dataPoints, userIds);
    }

    public void displayPostBoxes(List<JSONObject> datapoints, ArrayList<String> userIds){
        adapter = new ExploreListAdapter(getContext(), datapoints, userIds);
        exploreList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("boxes", "made");
    }
    public void toastMaker(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    public void setSortItemListener(){
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
    public void setSearchBarListener(){

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchBar.getText().toString().equals("")) {
                    exploreList.setAdapter(null);
                    checkPostFile();
                } else {
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
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
    public void casePriceChosen(){
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
        while (list.size() != dataPoints.size()){
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
                        if(sortedKeys.size()==0){
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
    }
    public void caseConditionChosen(){
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
        while (list.size() != dataPoints.size()){
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
                        if(sortedKeys.size()==0){
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
    }
}
