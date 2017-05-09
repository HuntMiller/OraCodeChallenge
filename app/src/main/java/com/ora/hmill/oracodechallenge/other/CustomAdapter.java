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

package com.ora.hmill.oracodechallenge.other;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ora.hmill.oracodechallenge.R;
import com.ora.hmill.oracodechallenge.activity.MainActivity;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<ChatMessage> implements View.OnClickListener{

    private ArrayList<ChatMessage> chatMessageArrayList;
    Context mContext;

    // View lookup
    private static class ViewHolder {
        LinearLayout messageContainer;
        TextView timeStamp;
        TextView message;
    }

    public CustomAdapter(ArrayList<ChatMessage> chatM, Context context) {
        super(context, R.layout.row_item, chatM);
        this.chatMessageArrayList = chatM;
        this.mContext=context;
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        ChatMessage chatMessage=(ChatMessage)object;

        switch (v.getId())
        {
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ChatMessage chatMessage = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.messageContainer = (LinearLayout) convertView.findViewById(R.id.messageContainer);
            viewHolder.message = (TextView) convertView.findViewById(R.id.message);
            viewHolder.timeStamp = (TextView) convertView.findViewById(R.id.timestamp);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        //This is a message you sent
        if(chatMessage.senderUid.equals(MainActivity.currentUID)){
            viewHolder.messageContainer.setGravity(Gravity.RIGHT);
            viewHolder.message.setBackgroundResource(R.drawable.sent_message_bg);
            viewHolder.message.setTextColor(Color.WHITE);
        }
        //This is a message you received
        else{
            viewHolder.messageContainer.setGravity(Gravity.LEFT);
            viewHolder.message.setBackgroundResource(R.drawable.received_message_bg);
            viewHolder.message.setTextColor(Color.BLACK);
        }
        viewHolder.message.setText(chatMessage.message);
        viewHolder.timeStamp.setText(chatMessage.timestamp);
        // Return the completed view to render on screen
        return convertView;
    }
}