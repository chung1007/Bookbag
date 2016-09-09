package com.davis.sam.bookbag;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by sam on 6/15/16.
 */
public class WishListBox extends RelativeLayout {

    public WishListBox(Context context) {
        super(context);
        LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.explorebox, this, true);
    }
}


