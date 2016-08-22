package com.example.sam.bookbag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 6/6/16.
 */
public class HomePage extends AppCompatActivity {
    private Toolbar toolbar;
    public static TabLayout tabLayout;
    public static CustomViewPager viewPager;
    public static String userId;
    public static String userName;
    Intent getUserInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.viewpager);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getExtras();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("");
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Hero.otf");
        TextView title = (TextView)toolbar.findViewById(R.id.toolbar_title);
        title.setTypeface(tf);
        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setPagingEnabled(false);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setTabIcons();
        setTabListener();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Explore(), "");
        adapter.addFragment(new WishList(), "");
        adapter.addFragment(new Sell(), "");
        adapter.addFragment(new Chat(), "");
        adapter.addFragment(new Profile(), "");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void setTabIcons() {
        final int[] ICONS = new int[]{
                R.drawable.search,
                R.drawable.wishlist,
                R.drawable.sell,
                R.drawable.chat,
                R.drawable.profile};
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
        tabLayout.getTabAt(4).setIcon(ICONS[4]);
        for (int i = 0; i < 5; i++) {
            tabLayout.getTabAt(i).getIcon().setTint(Color.WHITE);
        }
        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.tabSelected);
        tabLayout.getTabAt(0).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        //menu.findItem(R.id.userName).setTitle(userName);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void getExtras() {
        getUserInfo = getIntent();
        userName = getUserInfo.getStringExtra("userName");
        userId = getUserInfo.getStringExtra("userId");
        Log.e("userId", userId);

    }
    public void setTabListener(){
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.tabSelected);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                        if(tabLayout.getSelectedTabPosition() == 1){
                            Log.e("WishList", "selected");
                            giveDeleteTutorial();


                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.tabNotSelected);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
    }
    @Override
    public void onBackPressed(){
        finishAffinity();
    }

    public static class NoPageTransformer implements ViewPager.PageTransformer {
        public void transformPage(View view, float position) {
            if (position < 0) {
                view.setScrollX((int)((float)(view.getWidth()) * position));
            } else if (position > 0) {
                view.setScrollX(-(int) ((float) (view.getWidth()) * -position));
            } else {
                view.setScrollX(0);
            }

        }
    }
    public void giveDeleteTutorial(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(!pref.contains("tutorial")){
            editor.putString("tutorial", "given").apply();
            new AlertDialog.Builder(this)
                    .setTitle("Tutorial: Deleting items")
                    .setMessage("Hold down items to delete")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else{
            //don't do anything
        }
    }

}
