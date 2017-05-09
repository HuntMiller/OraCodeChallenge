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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ora.hmill.oracodechallenge.R;
import com.ora.hmill.oracodechallenge.activity.MainActivity;
import com.ora.hmill.oracodechallenge.other.ChatMessage;
import com.ora.hmill.oracodechallenge.other.User;

import java.util.ArrayList;

//TODO: Make it so when the fragment loads for the first time the listview is displaying correctly

/**
 * Fragment for Message List
 */
public class MessageListFragment extends Fragment {
    private static final String TAG = "messagelistfragmentD";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public TextView mltv;
    public static ListView messageList;
    public static ArrayAdapter adapter;
    public static ArrayList al;

    public MessageListFragment() {
        // Required empty public constructor
    }

    public static MessageListFragment newInstance(String param1, String param2) {
        MessageListFragment fragment = new MessageListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_message_list, container, false);
        mltv = (TextView) v.findViewById(R.id.mltv);
        messageList = (ListView) v.findViewById(R.id.message_list_listview);
        al = new ArrayList();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, al);

        for(User u : MainActivity.users){
            getMessageFromFirebaseUser(MainActivity.currentUID, u.uid);
            adapter.notifyDataSetChanged();
        }

        messageList.setAdapter(adapter);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User u = new User();
                for(User user: MainActivity.users){
                    if(user.email.equals(adapter.getItem(position).toString())){
                        u.name = user.name;
                        u.email = user.email;
                        u.uid = user.uid;
                        u.nickname = user.nickname;
                        u.birthday = user.birthday;
                        u.aboutme = user.aboutme;
                    }
                }
                ((MainActivity)getActivity()).loadHomeFragment(MainActivity.TAG_MESSAGE_HISTORY, u);
            }
        });
        return v;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /*
    Get a list of users you have chat history with
     */
    public void getMessageFromFirebaseUser(String senderUid, String receiverUid) {
        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference();

        databaseReference.child("chats")
                .getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(room_type_1)) {
                            Log.d(TAG, "getMessageFromFirebaseUser: " + room_type_1 + " exists");
                            FirebaseDatabase.getInstance().getReference().child("chats").child(room_type_1)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            // Chat message is retreived.
                                            ChatMessage chat = dataSnapshot.getValue(ChatMessage.class);
                                            //Add the email of the person who isn't yourself to the listview
                                            //Only add them to the list once
                                            if (chat.receiverEmail.equals(MainActivity.currentEmail) && !al.contains(chat.senderEmail)) {
                                                adapter.add(chat.senderEmail);
                                            } else if (chat.senderEmail.equals(MainActivity.currentEmail) && !al.contains(chat.receiverEmail)) {
                                                adapter.add(chat.receiverEmail);
                                            }
                                            adapter.notifyDataSetChanged();
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
                            FirebaseDatabase.getInstance().getReference().child("chats").child(room_type_2)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            // Chat message is retreived.
                                            ChatMessage chat = dataSnapshot.getValue(ChatMessage.class);
                                            //Add the email of the person who isn't yourself to the listview
                                            //Only add them to the list once
                                            if (chat.receiverEmail.equals(MainActivity.currentEmail) && !al.contains(chat.senderEmail)) {
                                                adapter.add(chat.senderEmail);
                                            } else if (chat.senderEmail.equals(MainActivity.currentEmail) && !al.contains(chat.receiverEmail)) {
                                                adapter.add(chat.receiverEmail);
                                            }
                                            adapter.notifyDataSetChanged();
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

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to get message
                    }
                });
    }

}
