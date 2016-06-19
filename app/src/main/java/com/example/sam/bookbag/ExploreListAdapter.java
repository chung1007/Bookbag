package com.example.sam.bookbag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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

    public ExploreListAdapter(Context context, String edition, String condition, String price, String title, String userId) {
        super();
        this.context = context;
        this.edition = edition;
        this.condition = condition;
        this.price = price;
        this.title = title;
        this.userId = userId;
    }

    @Override
    public int getCount(){
        return 1;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            Log.e("convertView", "null");
            ExploreBox listBox = new ExploreBox(context, title, edition, condition, price, userId);
            listBox.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            convertView = listBox.getBox();

        }
        return convertView;
    }
}


