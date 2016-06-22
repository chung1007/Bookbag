package com.example.sam.bookbag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
            ExploreBox listBox = new ExploreBox(context, title, edition, condition, price, ISBN, userId, bitmap);
            listBox.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            convertView = listBox.getBox();

        }
        return convertView;
    }
}



