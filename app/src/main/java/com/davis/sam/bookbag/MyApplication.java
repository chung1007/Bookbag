package com.davis.sam.bookbag;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sam on 5/31/16.
 */
public class MyApplication extends Application {
    public static FirebaseDatabase dataBase;
    public static DatabaseReference ref;
    public static FirebaseStorage storage;
    public static StorageReference storageRef;

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        printHashKey();
        initializeFirebase();
        setInstabug();
    }
    public void printHashKey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.sam.bookbag",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("Bookbag KeyHash: ", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
    public void setInstabug(){
        new Instabug.Builder(this, "8acf9a491975145b686561255f5e3410")
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .build();
    }

    public void initializeFirebase(){
        dataBase = FirebaseDatabase.getInstance();
        ref = dataBase.getReference();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://bookbag-7c9b3.appspot.com");

    }

}
