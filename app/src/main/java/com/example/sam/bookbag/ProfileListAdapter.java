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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 7/12/16.
 */
public class ProfileListAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> profiles;
    public ProfileListAdapter(Context context, ArrayList<String> profiles){
        super();
        this.context = context;
        this.profiles = profiles;
    }
    @Override
    public int getCount(){

        return profiles.size();
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
            View profileBox = inflater.inflate(R.layout.sellerprofilebox, null);
            ProfilePictureView sellerView = (ProfilePictureView)profileBox.findViewById(R.id.sellerProfileImage);
            TextView sellerNameTextView = (TextView)profileBox.findViewById(R.id.sellerProfileName);
            String sellerProfile = profiles.get(position);
            String profileInfo[] = sellerProfile.split("_");
            String sellerId = profileInfo[0];
            String sellerName = profileInfo[1];
            sellerView.setProfileId(sellerId);
            sellerNameTextView.setText(sellerName);
            convertView = profileBox;
        }
        return convertView;
    }

}
