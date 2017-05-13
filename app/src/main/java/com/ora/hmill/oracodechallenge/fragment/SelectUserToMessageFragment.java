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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ora.hmill.oracodechallenge.R;
import com.ora.hmill.oracodechallenge.activity.MainActivity;
import com.ora.hmill.oracodechallenge.other.Constants;
import com.ora.hmill.oracodechallenge.other.User;

import java.util.ArrayList;

/**
 * Fragment for Selecting a user to chat with (even if you don't have previous conversation history)
 */
public class SelectUserToMessageFragment extends Fragment {
    private static final String TAG = "slctmsgtargetfragmentD";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private ListView userList;
    private ArrayAdapter adapter;
    private ArrayList al = new ArrayList();

    private OnFragmentInteractionListener mListener;

    public SelectUserToMessageFragment() {
        // Required empty public constructor
    }

    public static SelectUserToMessageFragment newInstance(String param1, String param2) {
        SelectUserToMessageFragment fragment = new SelectUserToMessageFragment();
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
        View v = inflater.inflate(R.layout.fragment_select_user_to_message, container, false);
        userList = (ListView) v.findViewById(R.id.select_user_listView);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, al);
        userList.setAdapter(adapter);

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User u = new User();
                for(User user: MainActivity.users){
                    Log.d(TAG, user.email + " " + adapter.getItem(position).toString());
                    if(user.email.equals(adapter.getItem(position).toString())){
                        u.name = user.name;
                        u.email = user.email;
                        u.uid = user.uid;
                        u.nickname = user.nickname;
                        u.birthday = user.birthday;
                        u.aboutme = user.aboutme;
                        u.url = user.url;
                    }
                }
                ((MainActivity)getActivity()).loadHomeFragment(Constants.TAG_MESSAGE_HISTORY, u);
            }
        });

        //Populate listview with Firebase Users
        for(User u : MainActivity.users){
            adapter.add(u.email);
            adapter.notifyDataSetChanged();
            adapter.notifyDataSetInvalidated();
        }
        adapter.notifyDataSetInvalidated();

        return v;
    }

    public interface OnFragmentInteractionListener {
        void onMessageFragmentInteraction(Uri uri);
    }

}
