package com.example.sam.bookbag;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by sam on 6/26/16.
 */
public class WishListAdapter extends BaseAdapter {
    Context context;
    List<JSONObject> wantedBookInfo;

    public WishListAdapter(Context context, List<JSONObject> wantedBookInfo){
        super();
        this.context = context;
        this.wantedBookInfo = wantedBookInfo;
    }
    @Override
    public int getCount(){

        return wantedBookInfo.size();
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
            View wishListBox = inflater.inflate(R.layout.wanted_list_display, null);
            TextView bookNameDisplay = (TextView)wishListBox.findViewById(R.id.bookNameDisplay);
            TextView bookISBNDisplay = (TextView)wishListBox.findViewById(R.id.bookISBNDisplay);
            JSONObject bookJson = wantedBookInfo.get(position);
            try {
                String bookName = bookJson.getString("bookName");
                String bookISBN = bookJson.getString("bookISBN");
                bookNameDisplay.setText(bookName);
                bookISBNDisplay.setText(bookISBN);
                convertView = wishListBox;
            }catch (JSONException JSE){
                Log.e("wishListBox", "JSON FAILED");
            }
        }
        return convertView;
    }

}
