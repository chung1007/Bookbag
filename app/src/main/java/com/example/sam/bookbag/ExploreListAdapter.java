package com.example.sam.bookbag;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sam on 6/16/16.
 */
public class ExploreListAdapter extends BaseAdapter {
    Context context;
    List<JSONObject> dataPoints;
    ArrayList<String> userIds;
    JSONObject postData;
    JSONObject postValues;
    String edition;
    String condition;
    String price;
    String title;
    String userId;
    String ISBN;
    String bitmap;
    public static boolean isprofile;

    public ExploreListAdapter(Context context, List<JSONObject> datapoints, ArrayList<String> userIds) {
        super();
        this.context = context;
        this.dataPoints = datapoints;
        this.userIds = userIds;
        postData = new JSONObject();
        postValues = new JSONObject();

    }

    @Override
    public int getCount(){
        return dataPoints.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return getItem(arg0);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
         return position;
        }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            Log.e("getView", "called");
            Log.e("convertView", "null");
            postData = dataPoints.get(position);
            Iterator<String> keys = postData.keys();
            String firstKey = keys.next();
            userId = userIds.get(position);
            try {
                postValues = postData.getJSONObject(firstKey);
                title = postValues.getString("title");
                edition = postValues.getString("edition");
                condition = postValues.getString("condition");
                price = postValues.getString("price");
                ISBN = postValues.getString("ISBN");
                bitmap = postValues.getString("bitmap");
            }catch (JSONException JSE){
                Log.e("getView", "JSON FAILED");
            }
            if(isprofile){
                ExploreBox.showX = true;

            }else{
                ExploreBox.showX = false;
            }
            try {
                ExploreBox listBox = new ExploreBox(context, title, edition, condition, price, ISBN, userId, bitmap);
                listBox.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                convertView = listBox.getBox();
                setXButtonClickListener(convertView, parent);
            }catch (NullPointerException NPE){
                Log.e("box", "null");
            }


        }
        return convertView;
    }
    public void setXButtonClickListener(final View view, final ViewGroup parent) {
        if (isprofile) {
            ImageView xButton = (ImageView) view.findViewById(R.id.XButton);
            xButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("X button", "clicked");
                    TextView title = (TextView) view.findViewById(R.id.exploreBoxTitle);
                    String titleName = title.getText().toString();
                    Log.e("item", titleName);
                    markItemAsSold(titleName, parent);

                }
            });
        }
    }
    public void markItemAsSold(final String bookTitle, ViewGroup parent){
        new AlertDialog.Builder(parent.getContext())
                .setTitle("Delete Listing?")
                .setMessage("")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.ref.child(HomePage.userId).child(bookTitle).setValue(null);
                        String fileName = "sdcard/Bookbag_explore/" + HomePage.userId + "_" + bookTitle.replace(" ", "");
                        String fileName2 = android.os.Environment.getExternalStorageDirectory() + "/Bookbag_wishList/existing/" + HomePage.userId + "_" + bookTitle.replace(" ", "");
                        File file = new File(fileName);
                        File file2 = new File(fileName2);
                        file.delete();
                        if (file2.exists()) {
                            file2.delete();
                            Log.e("file deleted", "in wishList");
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}



