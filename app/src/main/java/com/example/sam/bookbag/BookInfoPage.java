package com.example.sam.bookbag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sam on 6/22/16.
 */
public class BookInfoPage extends Fragment{

    public BookInfoPage(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            Bundle bundle = this.getArguments();
            String myString = bundle.getString("title");
        }catch (NullPointerException NPE){
            Log.e("argument", "IS NULL");
        }
        Log.e("new fragment", "got");
        return inflater.inflate(R.layout.bookinfopage, container, false);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
