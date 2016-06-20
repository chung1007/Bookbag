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

import java.util.ArrayList;

/**
 * Created by sam on 6/16/16.
 */
public class ExploreListAdapter extends BaseAdapter {
    Context context;
    String edition;
    String condition;
    String price;
    String title;
    String userId;
    RecyclerView.ViewHolder holder;

    public ExploreListAdapter(Context context, ArrayList<String> postDatas) {
        super();
        this.context = context;
        this.title = postDatas.get(0);
        this.edition = postDatas.get(1);
        this.condition = postDatas.get(2);
        this.price = postDatas.get(3);
        this.userId = postDatas.get(4);
    }

    @Override
    public int getCount(){
        return 10;
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
            Log.e("userIdInAdapter", userId);
            Log.e("makeBoxTitle", title);
            ExploreBox listBox = new ExploreBox(context, title, edition, condition, price, userId);
            listBox.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            convertView = listBox.getBox();

        }
        return convertView;
    }
}



