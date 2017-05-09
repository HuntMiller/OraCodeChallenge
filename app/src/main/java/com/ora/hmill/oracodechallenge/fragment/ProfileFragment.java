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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ora.hmill.oracodechallenge.R;
import com.ora.hmill.oracodechallenge.activity.MainActivity;
import com.ora.hmill.oracodechallenge.other.User;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Fragment for Profile
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "profilefragmentD";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private GoogleApiClient mGoogleApiClient;
    private TextView name, email, uid;
    private EditText nickname, birthday, aboutme;
    private Button delete, save;
    private FirebaseAuth mAuth;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView pic = (ImageView) v.findViewById(R.id.profile_picture_imageView);
        name = (TextView) v.findViewById(R.id.profile_name_textView);
        email = (TextView) v.findViewById(R.id.profile_email_textView);
        uid = (TextView) v.findViewById(R.id.profile_uuid_textView);
        nickname = (EditText) v.findViewById(R.id.nickname_editText);
        birthday = (EditText) v.findViewById(R.id.birthday_editText);
        aboutme = (EditText) v.findViewById(R.id.about_me_editText);
        delete = (Button) v.findViewById(R.id.delete_account_button);
        save = (Button) v.findViewById(R.id.save_changes_button);

        new DownloadImageTask(pic).execute(mAuth.getCurrentUser().getPhotoUrl().toString());
        getYourData();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /*
    Get data for your profile
     */
    private void getYourData(){
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                //get all users that arent you
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    //this is you
                    if (TextUtils.equals(user.uid, mAuth.getCurrentUser().getUid())) {
                        name.setText(user.name);
                        email.setText(user.email);
                        uid.setText(user.uid);
                        nickname.setText(user.nickname);
                        birthday.setText(user.birthday);
                        aboutme.setText(user.aboutme);
                        Log.d(TAG, "Updated your profile data");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*
    Update database with your new info
     */
    private void updateUser() {
        Log.d(TAG, "Updating user");
        User user = new User(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getPhotoUrl().toString(),
                mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getEmail(),
                nickname.getText().toString(), birthday.getText().toString(), aboutme.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
        Toast.makeText(getContext(), "Updated your profile", Toast.LENGTH_SHORT).show();
    }

    /*
    Delete user from database
     */
    private void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User account deleted.");
                    Toast.makeText(getContext(), "User account deleted!", Toast.LENGTH_LONG).show();
                    signOut();
                } else {
                    Toast.makeText(getContext(), "Please reauthenticate your account to delete it.", Toast.LENGTH_LONG).show();
                    signOut();
                }
            }
        });
    }

    /*
    Sign out of firebase and Google
     */
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        mGoogleApiClient = MainActivity.mGoogleApiClient;
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        getActivity().finish();
                    }
                });
    }

    /*
    Download image from url
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
