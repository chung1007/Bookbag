package com.example.sam.bookbag;

import android.app.Activity;
import android.content.Context;
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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListPopupWindow;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import java.io.IOException;
import java.net.URL;
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
    ImageView textBookImageOne;
    ImageView textBookImageTwo;
    ImageView textBookImageThree;
    ImageView textBookImageFour;
    String toCheckWith;
    EditText className;
    EditText authorName;
    EditText ISBN;
    EditText condition;
    EditText price;
    EditText edition;
    EditText notes;
    ArrayList<EditText> dataList;
    EditText[] editTextList;
    ImageView[] photoList;
    Bitmap imageOneBitmap;
    int photoCounter;
    boolean correctInfo;
    Toast toast;

    public Sell() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sell, container, false);
        if (FacebookLogin.firstTime){
            toastMaker("Remember to take all four pictures!");
            FacebookLogin.firstTime = false;
        }
        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.condition);
        textBookImageOne = (ImageView) view.findViewById(R.id.textBookImageOne);
        textBookImageTwo = (ImageView)view.findViewById(R.id.textBookImageTwo);
        textBookImageThree = (ImageView)view.findViewById(R.id.textBookImageThree);
        textBookImageFour = (ImageView)view.findViewById(R.id.textBookImageFour);
        postButton = (Button) view.findViewById(R.id.postButton);
        scrollView = (ScrollView)view.findViewById(R.id.postDataScrollView);
        toCheckWith = textBookImageOne.getDrawable().toString();
        setEdittextId();
        setConditionList();
        setPhotoList();
        setPhotoAddListener(textBookImageOne, 0);
        setPhotoAddListener(textBookImageTwo, 1);
        setPhotoAddListener(textBookImageThree, 2);
        setPhotoAddListener(textBookImageFour, 3);
        setPostClickListener();
        //tabChangeListener();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoCounter = 1;
    }

    public void setConditionList() {
        list = new String[]{"As New", "Fine", "Very Good", "Good", "Fair", "Poor"};
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

        condition.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                lpw.show();
            }
        });
    }
    public void setPhotoAddListener(ImageView view, final int requestCode){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera(requestCode);
                Toast.makeText(getContext(), "Take photo Landscape!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startCamera(int requestCode) {

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, requestCode);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK)
        {
            imageOneBitmap = (Bitmap) data.getExtras().get("data");
            textBookImageOne.setImageBitmap(imageOneBitmap);
        }else if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            textBookImageTwo.setImageBitmap(photo);
        }else if (requestCode == 2 && resultCode == Activity.RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            textBookImageThree.setImageBitmap(photo);
        }else if (requestCode == 3 && resultCode == Activity.RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            textBookImageFour.setImageBitmap(photo);
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
                    for (int i = 0; i < photoList.length; i++) {
                        sendTextBookPhoto(photoList[i]);
                    }
                    sendPostData();
                    toastMaker("Posted!");
                    clearPostData();
                    if(toast!=null){
                        HomePage.tabLayout.getTabAt(2);
                    }
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
        notes = (EditText)view.findViewById(R.id.notes);
        dataList = new ArrayList<>(Arrays.asList(className, authorName, ISBN, condition, price, edition, notes));
        editTextList = new EditText[7];
        for (int i = 0; i < dataList.size(); i++){
            editTextList[i] = dataList.get(i);

        }
    }

    public void setPhotoList(){
        photoList = new ImageView[]{textBookImageOne, textBookImageTwo, textBookImageThree, textBookImageFour};
    }
    public void checkDataCompletion(EditText[] dataList){
        ArrayList<String> dataCheckList = new ArrayList<>();
        ArrayList<String> photoCheckList = new ArrayList<>();
        for (int i = 0; i < dataList.length; i++){
            dataCheckList.add(dataList[i].getText().toString());

        }
        for (int i = 0; i < photoList.length; i++){
            photoCheckList.add(photoList[i].getDrawable().toString());
        }
        if(dataCheckList.contains("")){
            toastMaker("Incomplete information!");
            correctInfo = false;
        }else if(photoCheckList.contains(toCheckWith)){
            toastMaker("You forgot to fill in the photos!");
            correctInfo = false;
        }else{
            correctInfo = true;

        }

    }
    public void sendTextBookPhoto(ImageView imageView){
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            final Bitmap bitmap = imageView.getDrawingCache();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference imageKey = MyApplication.storageRef.child(HomePage.userId);
            UploadTask uploadTask = imageKey.child(className.getText().toString()).child("image" + Integer.toString(photoCounter)).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //DO NOTHING
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
        photoCounter++;
        imageView.setImageBitmap(null);
        imageView.destroyDrawingCache();
    }

    public void sendPostData(){
        ArrayList<String> dataNames = new ArrayList<>(Arrays.asList("className", "authorName", "ISBN", "condition", "price", "edition", "notes"));
        DatabaseReference postKey = MyApplication.ref.child(HomePage.userId);
        for (int i = 0; i < editTextList.length; i++){
            String dataValue = editTextList[i].getText().toString();
            if (dataValue.charAt(dataValue.length() - 1)==' '){
                Log.e("Title", "has space at the end");
                dataValue = dataValue.replace(dataValue.substring(dataValue.length()-1), "");
                Log.e("no space test", dataValue + "test");
            }
            Log.e("no space test2", dataValue + "test");
            postKey.child(className.getText().toString()).child(dataNames.get(i)).setValue(dataValue);
        }
        postKey.child(className.getText().toString()).child("bitmap").setValue(BitMapToString(imageOneBitmap));
        postKey.child(className.getText().toString()).child("seller").setValue(HomePage.userName);

    }
    public void clearPostData(){
        for (int i = 0; i < photoList.length; i++){
            photoList[i].setImageResource(0);
            photoList[i].setImageResource(R.drawable.addpicture);
        }
        for (int i = 0; i < editTextList.length; i++){
            editTextList[i].setText("");
        }
        scrollView.fullScroll(ScrollView.FOCUS_UP);
        price.setText("$");
        photoCounter = 1;
    }
    public void toastMaker(String message){
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

}

