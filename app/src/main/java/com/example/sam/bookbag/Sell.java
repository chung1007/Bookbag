package com.example.sam.bookbag;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

/**
 * Created by sam on 6/6/16.
 */
public class Sell extends Fragment {
    AutoCompleteTextView autoCompleteTextView;
    View view;
    private ListPopupWindow lpw;
    private String[] list;

    public Sell(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sell, container, false);
        autoCompleteTextView = (AutoCompleteTextView)view.findViewById(R.id.Condition);
        setConditionList();
        return view;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setConditionList(){
        list = new String[] { "Poor", "Fair", "Good", "New" };
        lpw = new ListPopupWindow(getContext());
        lpw.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, list));
        lpw.setAnchorView(autoCompleteTextView);
        lpw.setModal(true);
        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String item = list[position];
                autoCompleteTextView.setText(item);
                lpw.dismiss();
            }
        });
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                lpw.show();
                return true;
            }
        });
    }
}
