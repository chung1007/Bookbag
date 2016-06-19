package com.example.sam.bookbag;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
    View box;

    public ExploreBox(Context context, String title, String edition, String condition, String price, String userId) {
        super(context);
        this.title = title;
        this.edition = edition;
        this.condition = condition;
        this.price = price;
        this.userId = userId;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        box = mInflater.inflate(R.layout.explorebox, this, true);

    }

    public View getBox() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        box = inflater.inflate(R.layout.explorebox, null);
        StorageReference storageRef = MyApplication.storageRef;
        TextView boxTitle = (TextView) box.findViewById(R.id.exploreBoxTitle);
        TextView boxEdition = (TextView) box.findViewById(R.id.exploreBoxEdition);
        TextView boxCondition = (TextView) box.findViewById(R.id.exploreBoxCondition);
        TextView boxPrice = (TextView) box.findViewById(R.id.exploreBoxPrice);
        final ImageView boxImage = (ImageView) box.findViewById(R.id.exploreImageView);
        boxTitle.setText(title);
        boxEdition.setText(edition);
        Log.e("edition", edition);
        boxCondition.setText(condition);
        boxPrice.setText(price);
        final StorageReference imageRef = storageRef.child(userId).child(title).child("image1");
        Log.e("e.BoxUserId", userId);
        Log.e("e.BoxTitle", title);
        new Thread() {
            @Override
            public void run() {
                imageRef.getBytes(Constants.ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        Log.e("completion", "SUCCCESS!");

                        imageRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Log.e("bytes", "SUCCESS");
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
                                Log.e("userIdForImage", userId);
                                Log.e("titleForImage", title);
                                Log.e("getting image", "failed");
                            }
                        });
                    }
                });
            }
        }.start();
        return box;

    }

}

