package com.example.sam.bookbag;

import android.content.Context;
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
            ProfilePictureView sellerPicture = (ProfilePictureView)chatBox.findViewById(R.id.chatImage);
            JSONObject bookJson = messageData.get(position);
            try {
                sellerPicture.setProfileId(bookJson.getString("sellerId"));
                sellerName.setText(bookJson.getString("sellerName"));
                latestMessage.setText(bookJson.getString("message"));
                bookName.setText(bookJson.getString("bookName"));
                convertView = chatBox;
            }catch (JSONException JSE){
                Log.e("wishListBox", "JSON FAILED");
            }
        }
        return convertView;
    }

}
