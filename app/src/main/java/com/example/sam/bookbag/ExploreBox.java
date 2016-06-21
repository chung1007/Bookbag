package com.example.sam.bookbag;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;

/**
 * Created by sam on 6/18/16.
 */
public class ExploreBox extends RelativeLayout {

    String title;
    String edition;
    String condition;
    String price;
    String userId;
    String ISBN;
    String bitmap;
    View box;

    public ExploreBox(Context context, String title, String edition, String condition, String price, String ISBN, String userId, String bitmap) {
        super(context);
        this.title = title;
        this.edition = edition;
        this.condition = condition;
        this.price = price;
        this.userId = userId;
        this.ISBN = ISBN;
        this.bitmap = bitmap;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        box = mInflater.inflate(R.layout.explorebox, this, true);

    }

    public View getBox() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        box = inflater.inflate(R.layout.explorebox, null);
        TextView boxTitle = (TextView) box.findViewById(R.id.exploreBoxTitle);
        TextView boxEdition = (TextView) box.findViewById(R.id.exploreBoxEdition);
        TextView boxCondition = (TextView) box.findViewById(R.id.exploreBoxCondition);
        TextView boxPrice = (TextView) box.findViewById(R.id.exploreBoxPrice);
        TextView userIdInBox = (TextView)box.findViewById(R.id.userId);
        TextView bookISBN = (TextView)box.findViewById(R.id.isbn);
        final ImageView boxImage = (ImageView) box.findViewById(R.id.exploreImageView);
        boxTitle.setText(title);
        boxEdition.setText(edition + " edition");
        boxCondition.setText(condition);
        boxPrice.setText(price);
        userIdInBox.setText(userId);
        bookISBN.setText(ISBN);
        Log.e("userId", userId);
        Log.e("title", title);
        boxImage.setImageBitmap(null);
        boxImage.destroyDrawingCache();
        boxImage.setImageResource(0);
        boxImage.setImageBitmap(StringToBitMap(bitmap));
        return box;
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}

