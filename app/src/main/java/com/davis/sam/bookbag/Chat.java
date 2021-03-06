package com.davis.sam.bookbag;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sam on 6/6/16.
 */
public class Chat extends Fragment {
    ListView chatListView;
    List<JSONObject> convoJson;
    ChatListAdapter adapter;
    Firebase chatDataBase;
    boolean newSellerDone;
    boolean newConvoDone = false;
    ArrayList<String>sellers;
    HashMap<String, String> conversations;
    ArrayList<String> messageKeyList;
    File messageDir;
    File newMessageDir;
    public static  String messageFromPage = "";
    PrintWriter file;
    PrintWriter newFile;
    Intent intent;

    public Chat() {

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.chat, container, false);
        Firebase.setAndroidContext(getContext());
        chatListView = (ListView) view.findViewById(R.id.chatListView);
        chatDataBase = new Firebase(Constants.chatDataBase);
        messageDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_chat");
        newMessageDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bookbag_newChat");
        sellers = new ArrayList<>();
        messageKeyList = new ArrayList<>();
        conversations = new HashMap<>();
        Log.e("Chat", "started");
        chatListView.setAdapter(null);
        checkPostFile();
        listenForChatBoxClicked();
        setNewSellerListener();
        if(FacebookLogin.firstTime) {
            chatDataBase.child(HomePage.userId).child("123456789_No Name").child("initialized").child("00:00:00 AM_No Name_00:00 AM").setValue("new messages initialized");
            FacebookLogin.firstTime = false;
        }
        setAllMessagesListener();
        setDeleteClickListener();
        return view;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    public void checkPostFile() {
        ArrayList<String> convoFiles = new ArrayList<>();
        File file = new File("/sdcard/Bookbag_chat");
        File list[] = file.listFiles();
        try {
            for (int i = 0; i < list.length; i++) {
                convoFiles.add(list[i].getName());
            }
        } catch (NullPointerException NPE) {
        }
        if (!convoFiles.isEmpty()) {
            Log.e("conversations", "there has been previous conversations");
            listPostNames(convoFiles);
        }

    }
    public String readFile(String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            return null;
        }
        String dataOfFile = "";
        String buf;
        try {
            while ((buf = file.readLine()) != null) {
                dataOfFile = dataOfFile.concat(buf + "\n");
            }
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            return null;
        }
        return dataOfFile;
    }

    public void listPostNames(ArrayList<String> postNames) {
        convoJson = new ArrayList<>();
        for (int i = 0; i < postNames.size(); i++) {
            String fileName = "sdcard/Bookbag_chat/" + postNames.get(i);
            Log.e("postNames", postNames.get(i));
            ArrayList<String> data = getData(postNames.get(i), fileName);
            try {
                JSONObject postDataRead = new JSONObject();
                postDataRead.put("sellerId", data.get(0));
                postDataRead.put("sellerName", data.get(1));
                postDataRead.put("bookName", data.get(2));
                postDataRead.put("message", data.get(3));
                Log.e("messageData", postDataRead.toString());
                Log.e("postData", data.get(2));
                convoJson.add(postDataRead);
            } catch (JSONException JSE) {
                Log.e("assign json", "failed");
            }

        }

        displayPostBoxes(convoJson);
    }

    public void displayPostBoxes(List<JSONObject> datapoints) {
        adapter = new ChatListAdapter(getContext(), datapoints);
        chatListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("boxes", "made");
    }
    //http://stackoverflow.com/questions/599161/best-way-to-convert-an-arraylist-to-a-string

    public ArrayList<String> getData(String fileName, String filePath) {
        Log.e("fileName", fileName);
        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> nameChar = new ArrayList<>();
        String splitName[] = fileName.split("_");
        String sellerNameNoSpace = splitName[0];
        String[] firstAndLast = sellerNameNoSpace.split("([-_ ]|(?<=[^-_ A-Z])(?=[A-Z]))");
        Log.e("first", firstAndLast[0]);
        Log.e("last", firstAndLast[1]);
        Log.e("splitName", splitName.toString());
        String sellerName = firstAndLast[0] + " " + firstAndLast[1];
        ArrayList<String> chars = new ArrayList<>();
        for (int i = 1; i < splitName.length; i++) {
            chars.add(splitName[i]);
        }
        String sellerId = chars.get(0);
        data.add(sellerId);
        data.add(sellerName);
        for (int i = 1; i < chars.size(); i++) {
            nameChar.add(chars.get(i));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameChar.size(); i++) {
            sb.append(nameChar.get(i));
            if (i != nameChar.size() - 1) {
                sb.append(" ");
            }
        }
        String nameOfBook = sb.toString();
        String test = nameOfBook + "test";
        Log.e("booknametest", test.toString());
        String message = readFile(filePath);
        data.add(nameOfBook);
        data.add(message);

        Log.e("splitName", chars.toString());
        return data;
    }

    public void listenForChatBoxClicked() {
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView sellerName = (TextView) view.findViewById(R.id.chatName);
                TextView sellerId = (TextView) view.findViewById(R.id.sellerId);
                TextView bookTitle = (TextView) view.findViewById(R.id.titleOfTextBook);
                Intent intent = new Intent(getContext(), ChatPage.class);
                intent.putExtra("sellerId", sellerId.getText().toString());
                intent.putExtra("sellerName", sellerName.getText().toString());
                intent.putExtra("bookName", bookTitle.getText().toString());
                intent.putExtra("isContinued", "continued");

                startActivity(intent);
            }
        });
    }

    public void setNewSellerListener() {
        chatDataBase.child(HomePage.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newSellerDone = true;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setAllMessagesListener() {
        chatDataBase.child(HomePage.userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chatDataBase.child(HomePage.userId).child("123456789_No Name").setValue(null);
                Log.e("newChatMate", "added!");
                String sellerKey = dataSnapshot.getKey();
                sellers.add(sellerKey);
                if (newSellerDone) {
                    String latestChatMate = sellers.get(sellers.size() - 1);
                    getNewChatTopic(latestChatMate);
                    newSellerDone = false;
                    sellers.clear();
                    Log.e("lastChatMate", latestChatMate);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getNewChatTopic(final String sellerKey) {
        chatDataBase.child(HomePage.userId).child(sellerKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("newBook", "added");
                String newTopic = dataSnapshot.getKey();
                checkForLatestMessage(sellerKey, newTopic);
                getMessages(sellerKey, newTopic);
                Log.e("newTopic", newTopic);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getMessages(final String newChatMate, final String newTopic) {
        chatDataBase.child(HomePage.userId).child(newChatMate).child(newTopic).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("new messages", "added");
                String messageKeys = dataSnapshot.getKey();
                Log.e("keys", messageKeys);
                String messages = dataSnapshot.getValue().toString();
                Log.e("messages", messages);
                messageKeyList.add(messageKeys);
                conversations.put(messageKeys, messages);
                //if (newConvoDone) {
                newConvoDone = false;
                Log.e("conversatons", conversations.toString());
                String lastMessageKey = messageKeyList.get(messageKeyList.size() - 1);
                String lastMessage = conversations.get(lastMessageKey);
                Log.e("lastKey", lastMessageKey);
                Log.e("lastMessage", lastMessage);
                if (!lastMessage.equals("sjvsdvbsdbv")) {
                    Log.e("lastMessage", "does not equal!");
                    writeToFileAndUpdate(newChatMate, newTopic, lastMessage, lastMessageKey);
                    Log.e("fromChat1", lastMessage);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void checkForLatestMessage(String chatMate, String newTopic) {
        chatDataBase.child(HomePage.userId).child(chatMate).child(newTopic).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newConvoDone = true;
                Log.e("newConvoDone", "is True");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void writeToFileAndUpdate(String sellerAndId, String messageKey, String message, String lastMessageKey) {
        Log.e("fromChat", message);
        Log.e("latestMessage1", message);
        Log.e("new convo", "writing to file!");
        Log.e("lastMessageKey", lastMessageKey);
        String[] sellerAndIdSplit = sellerAndId.split("_");
        String sellerId = sellerAndIdSplit[0];
        Log.e("sellerId", sellerId);
        String sellerName = sellerAndIdSplit[1];
        Log.e("sellerName", sellerName);
        String bookName = messageKey;
        Log.e("lastestBook", bookName);
        String fileName = "sdcard/Bookbag_chat/" + sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_");
        try {
            if (message.equals(messageFromPage) && !lastMessageKey.contains("_" + HomePage.userName + "_") || readFile(fileName) == null) {
                messageDir.mkdir();
                file = null;
                file = new PrintWriter(new FileOutputStream(new File(messageDir, (sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_")))));
                file.println(message);
                file.close();
                saveNewChats(sellerName, sellerId, bookName, message);
                refreshChatPage();
                Log.e("chat page", "refreshed");
            }
        } catch (IOException IOE) {
            Log.e("chat", "saving conv. failed");
        }
    }

    public void refreshChatPage() {
        chatListView.setAdapter(null);
        checkPostFile();
    }
    public void saveNewChats(String sellerName, String sellerId, String bookName, String message){
        newMessageDir.mkdir();
        newFile = null;
        try {
            newFile = new PrintWriter(new FileOutputStream(new File(newMessageDir, (sellerName.replace(" ", "") + "_" + sellerId + "_" + bookName.replace(" ", "_")))));
            newFile.println(message);
            newFile.close();
        }catch (IOException IOE){
            Log.e("file", "failed");
        }

    }
    public void setDeleteClickListener(){
        chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView sellerName = (TextView) view.findViewById(R.id.chatName);
                TextView sellerId = (TextView) view.findViewById(R.id.sellerId);
                TextView bookTitle = (TextView) view.findViewById(R.id.titleOfTextBook);
                String name = sellerName.getText().toString();
                String ID = sellerId.getText().toString();
                String book = bookTitle.getText().toString();
                String fileName = name.replace(" ", "") + "_" + ID + "_" + book.replace(" ", "_");
                String filePath = "sdcard/Bookbag_chat/" + fileName;
                conversationDeleteDialog(filePath, book, name, ID);
                return true;
            }
        });
    }
    public void conversationDeleteDialog(final String fileName, final String bookTitle, final String sellerName, final String sellerId){
        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure?")
                .setMessage("Delete this conversation?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File file =  new File(fileName);
                        file.delete();
                        chatDataBase.child(HomePage.userId).child(sellerId + "_" + sellerName).child(bookTitle).setValue(null);
                        refreshChatPage();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    @Override
    public void onDestroy(){
        Log.e("Chat", "killed");
        //this.startService(new Intent(this, BackgroundListeners.class));
        super.onDestroy();
    }
    @Override
    public void onPause() {
        super.onPause();
        checkPostFile();
        listenForChatBoxClicked();
        setNewSellerListener();
        setAllMessagesListener();
        setDeleteClickListener();
        Log.e("Chat", "paused");
    }
    @Override
    public void onResume(){
        super.onResume();
        checkPostFile();
        listenForChatBoxClicked();
        setNewSellerListener();
        setAllMessagesListener();
        setDeleteClickListener();
        Log.e("Chat", "paused");
    }


}