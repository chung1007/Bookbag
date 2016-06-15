package com.example.sam.bookbag;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sam on 6/6/16.
 */
public class Sell extends Fragment {
    AutoCompleteTextView autoCompleteTextView;
    View view;
    Button postButton;
    ScrollView scrollView;
    private ListPopupWindow lpw;
    private String[] list;
    private static final int CAMERA_REQUEST = 1888;
    ImageView addPictureView;
    EditText className;
    EditText authorName;
    EditText ISBN;
    EditText condition;
    EditText price;
    EditText edition;
    ArrayList<EditText> dataList;
    EditText[] editTextList;
    boolean correctInfo;

    public Sell() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sell, container, false);
        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.condition);
        addPictureView = (ImageView) view.findViewById(R.id.textBookImageOne);
        postButton = (Button) view.findViewById(R.id.postButton);
        scrollView = (ScrollView)view.findViewById(R.id.postDataScrollView);
        setEdittextId();
        setConditionList();
        setPhotoAddListener();
        setPostClickListener();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void setConditionList() {
        list = new String[]{"Poor", "Fair", "Good", "New"};
        lpw = new ListPopupWindow(getContext());
        lpw.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, list));
        lpw.setAnchorView(autoCompleteTextView);
        lpw.setModal(true);
        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String item = list[position];
                autoCompleteTextView.setText(item);
                lpw.dismiss();
            }
        });
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                lpw.show();
                return true;
            }
        });
    }
    public void setPhotoAddListener(){
        addPictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
                Toast.makeText(getContext(), "Take photo Landscape!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startCamera() {

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            addPictureView.setImageBitmap(photo);
        }
    }
    public void setPostClickListener(){
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDataCompletion(editTextList);
                if (!correctInfo) {
                    //Do nothing
                } else {
                    sendTextBookPhoto();
                    sendPostData();
                    toastMaker("Posted!");
                    clearPostData();
                }
            }
        });
    }
    public void setEdittextId(){
        className = (EditText)view.findViewById(R.id.className);
        authorName = (EditText)view.findViewById(R.id.authorName);
        ISBN = (EditText)view.findViewById(R.id.ISBN);
        condition = (EditText)view.findViewById(R.id.condition);
        price = (EditText)view.findViewById(R.id.price);
        edition = (EditText)view.findViewById(R.id.edition);
        dataList = new ArrayList<>(Arrays.asList(className, authorName, ISBN, condition, price, edition));
        editTextList = new EditText[6];
        for (int i = 0; i < dataList.size(); i++){
            editTextList[i] = dataList.get(i);
            Log.e("size i", i + "");
            Log.e("editTextLength", editTextList.length + "");
        }
    }
    public void checkDataCompletion(EditText[] dataList){
        ArrayList<String> checkList = new ArrayList<>();
        for (int i = 0; i < dataList.length; i++){
            checkList.add(dataList[i].getText().toString());
            Log.e("length i", i + "");
            Log.e("dataList", dataList[i].getText().toString());
        }
        Log.e("checkList", checkList.toString());
        if(checkList.contains("")){
            toastMaker("Incomplete information!");
            correctInfo = false;
        }else{
            correctInfo = true;
        }

    }
    public void sendTextBookPhoto(){
        try {
            addPictureView.setDrawingCacheEnabled(true);
            addPictureView.buildDrawingCache();
            Bitmap bitmap = addPictureView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference imageKey = MyApplication.storageRef.child(HomePage.userId);
            UploadTask uploadTask = imageKey.child(className.getText().toString()).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //DO NOTHING
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //DO NOTHING
                }
            });
        }catch (NullPointerException NPE){
            toastMaker("You forgot some info!");
        }
    }
    public void sendPostData(){
        ArrayList<String> dataNames = new ArrayList<>(Arrays.asList("className", "authorName", "ISBN", "condition", "price", "edition"));
        DatabaseReference postKey = MyApplication.ref.child(HomePage.userId);
        for (int i = 0; i < editTextList.length; i++){
            postKey.child(className.getText().toString()).child(dataNames.get(i)).setValue(editTextList[i].getText().toString());
        }
    }
    public void clearPostData(){
        addPictureView.setImageResource(0);
        addPictureView.setImageResource(R.drawable.addpicture);
        for (int i = 0; i < editTextList.length; i++){
            editTextList[i].setText("");
        }
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }
    public void toastMaker(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

