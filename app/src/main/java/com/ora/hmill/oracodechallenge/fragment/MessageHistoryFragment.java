/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ora.hmill.oracodechallenge.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ora.hmill.oracodechallenge.R;
import com.ora.hmill.oracodechallenge.activity.MainActivity;
import com.ora.hmill.oracodechallenge.other.ChatMessage;
import com.ora.hmill.oracodechallenge.other.CustomAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageHistoryFragment extends Fragment {
    private static final String TAG = "messagefragmentD";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private Button sendButton;
    private EditText toSend;
    private ListView message_listView;
    private ArrayList<ChatMessage> chatMessages;
    private CustomAdapter customAdapter;
    private ChatMessage cm;

    private OnFragmentInteractionListener mListener;

    public MessageHistoryFragment() {
        // Required empty public constructor
    }

    public static MessageHistoryFragment newInstance(String param1, String param2) {
        MessageHistoryFragment fragment = new MessageHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        scrollChatToBottom();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_message_history, container, false);
        toSend = (EditText) v.findViewById(R.id.to_send_editText);
        sendButton = (Button) v.findViewById(R.id.send_button);
        message_listView = (ListView) v.findViewById(R.id.message_listview);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChatMessageData();
                if(cm.receiverUid != null){
                    if(cm.message.length() > 0)
                        sendMessageToFirebaseUser(cm.receiverUid, cm);
                    toSend.setText("");
                    scrollChatToBottom();
                }
                else{
                    Toast.makeText(getContext(), "Encountered an error while trying to send this user a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chatMessages = new ArrayList();

        getChatMessageData();
        getMessages(cm.receiverUid, cm);

        customAdapter = new CustomAdapter(chatMessages, getContext());
        message_listView.setAdapter(customAdapter);
        message_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.timestamp);
                if(tv.getVisibility() == View.GONE) tv.setVisibility(View.VISIBLE);
                else tv.setVisibility(View.GONE);
            }
        });
        message_listView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

        return v;
    }

    public interface OnFragmentInteractionListener {
        void onMessageFragmentInteraction(Uri uri);
    }

    private void scrollChatToBottom(){
        message_listView.post(new Runnable(){
            public void run() {
                message_listView.setSelection(message_listView.getCount() - 1);
            }});
    }

    /*
    populate a ChatMessage with data from the selected user you are chatting with, etc
     */
    public void getChatMessageData(){
        cm = new ChatMessage();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

        cm.sender = MainActivity.currentUser;
        cm.senderEmail = MainActivity.currentEmail;
        cm.senderUid = MainActivity.currentUID;
        cm.senderURL = MainActivity.currentURL;

        cm.receiver = MainActivity.selectedUser.name;
        cm.receiverEmail = MainActivity.selectedUser.email;
        cm.receiverUid = MainActivity.selectedUser.uid;
        cm.receiverURL = MainActivity.selectedUser.url;

        cm.message = toSend.getText().toString();
        cm.timestamp = sdf.format(new java.util.Date());
    }

    /*
    Get message history & add to listview
     */
    public void getMessages(String sendTo, final ChatMessage chat) {
        final String room_type_1 = MainActivity.currentUID + "_" + sendTo;
        final String room_type_2 = sendTo + "_" + MainActivity.currentUID;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference();

        databaseReference.child("chats")
                .getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(room_type_1)) {
                            Log.d(TAG, "getMessageFromFirebaseUser: " + room_type_1 + " exists");
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("chats")
                                    .child(room_type_1)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            // Chat message is retreived.
                                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                                            customAdapter.add(chatMessage);
                                            customAdapter.notifyDataSetChanged();
                                            customAdapter.notifyDataSetInvalidated();
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
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Unable to get message.
                                        }
                                    });
                        } else if (dataSnapshot.hasChild(room_type_2)) {
                            Log.d(TAG, "getMessageFromFirebaseUser: " + room_type_2 + " exists");
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("chats")
                                    .child(room_type_2)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            // Chat message is retreived.
                                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                                            customAdapter.add(chatMessage);
                                            customAdapter.notifyDataSetChanged();
                                            customAdapter.notifyDataSetInvalidated();
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
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Unable to get message.
                                        }
                                    });
                        } else {
                            Log.d(TAG, "getMessageFromFirebaseUser: no such room exists");
                            getMessages(cm.receiverUid, cm);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to get message
                    }
                });
    }

    /*
    Send a message to another user
     */
    public void sendMessageToFirebaseUser(String sendTo, final ChatMessage chat) {
        final String room_type_1 = MainActivity.currentUID + "_" + sendTo;
        final String room_type_2 = sendTo + "_" + MainActivity.currentUID;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference();

        databaseReference.child("chats")
                .getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(room_type_1)) {
                            Log.d(TAG, "sendMessageToFirebaseUser: " + room_type_1 + " exists");
                            databaseReference.child("chats")
                                    .child(room_type_1)
                                    .child(String.valueOf(chat.timestamp))
                                    .setValue(chat);
                        } else if (dataSnapshot.hasChild(room_type_2)) {
                            Log.d(TAG, "sendMessageToFirebaseUser: " + room_type_2 + " exists");
                            databaseReference.child("chats")
                                    .child(room_type_2)
                                    .child(String.valueOf(chat.timestamp))
                                    .setValue(chat);
                        } else {
                            Log.d(TAG, "sendMessageToFirebaseUser: success");
                            databaseReference.child("chats")
                                    .child(room_type_1)
                                    .child(String.valueOf(chat.timestamp))
                                    .setValue(chat);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to send message.
                    }
                });
    }

}