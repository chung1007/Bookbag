package com.example.sam.bookbag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by sam on 6/16/16.
 */
public class ExploreListAdapter extends ArrayAdapter<View> {
    Context context;
    String edition;
    String condition;
    String price;
    String title;
    String userId;

    public ExploreListAdapter(Context context, String edition, String condition, String price, String title, String userId) {
        super(context, R.layout.explorebox);
        this.context = context;
        this.edition = edition;
        this.condition = condition;
        this.price = price;
        this.title = title;
        this.userId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View box = inflater.inflate(R.layout.explorebox, null);
        TextView boxTitle = (TextView) box.findViewById(R.id.exploreBoxTitle);
        TextView boxEdition = (TextView) box.findViewById(R.id.exploreBoxEdition);
        TextView boxCondition = (TextView) box.findViewById(R.id.exploreBoxCondition);
        TextView boxPrice = (TextView) box.findViewById(R.id.exploreBoxPrice);
        final ImageView boxImage = (ImageView) box.findViewById(R.id.exploreImageView);
        boxTitle.setText(title);
        boxEdition.setText(edition);
        boxCondition.setText(condition);
        boxPrice.setText(price);

        MyApplication.storageRef.child(userId).child(title).child("image1").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                boxImage.setImageBitmap(null);
                boxImage.destroyDrawingCache();
                boxImage.setImageResource(0);
                boxImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
        return box;
    }
}


