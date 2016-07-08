package com.example.sam.bookbag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by sam on 6/26/16.
 */
public class ChatListAdapter extends BaseAdapter {
    Context context;
    List<JSONObject> messageData;

    public ChatListAdapter(Context context, List<JSONObject> messageData){
        super();
        this.context = context;
        this.messageData = messageData;
    }
    @Override
    public int getCount(){

        return messageData.size();
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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View chatBox = inflater.inflate(R.layout.chatbox, null);
            TextView sellerName = (TextView)chatBox.findViewById(R.id.chatName);
            TextView latestMessage = (TextView)chatBox.findViewById(R.id.latestMessage);
            TextView bookName = (TextView)chatBox.findViewById(R.id.titleOfTextBook);
            TextView sellerId = (TextView)chatBox.findViewById(R.id.sellerId);
            ImageView newMessageIndicator = (ImageView)chatBox.findViewById(R.id.newMessageIndicator);
            ProfilePictureView sellerPicture = (ProfilePictureView)chatBox.findViewById(R.id.chatImage);
            JSONObject bookJson = messageData.get(position);
            try {
                String name = bookJson.getString("sellerName").replace(" ", "");
                String id = bookJson.getString("sellerId");
                String book = bookJson.getString("bookName").replace(" ", "_");
                String fileName = name+"_"+id+"_"+book;
                String filePath = "sdcard/Bookbag_newChat/"+fileName;
                if(readFile(filePath)!=null){
                    Log.e("indicator", "visible");
                }else {
                    newMessageIndicator.setVisibility(View.GONE);
                }
                sellerPicture.setProfileId(bookJson.getString("sellerId"));
                sellerName.setText(bookJson.getString("sellerName"));
                latestMessage.setText(bookJson.getString("message"));
                sellerId.setText(bookJson.getString("sellerId"));
                bookName.setText(bookJson.getString("bookName"));
                convertView = chatBox;
            }catch (JSONException JSE){
                Log.e("wishListBox", "JSON FAILED");
            }
        }
        return convertView;
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

}
