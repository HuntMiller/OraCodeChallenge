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

package com.ora.hmill.oracodechallenge.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ora.hmill.oracodechallenge.R;
import com.ora.hmill.oracodechallenge.fragment.MessageHistoryFragment;
import com.ora.hmill.oracodechallenge.fragment.MessageListFragment;
import com.ora.hmill.oracodechallenge.fragment.ProfileFragment;
import com.ora.hmill.oracodechallenge.fragment.SelectUserToMessageFragment;
import com.ora.hmill.oracodechallenge.fragment.WelcomeFragment;
import com.ora.hmill.oracodechallenge.other.Constants;
import com.ora.hmill.oracodechallenge.other.DownloadImage;
import com.ora.hmill.oracodechallenge.other.User;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private Handler mHandler;
    private NavigationView navigationView;
    private View headerView;
    private TextView name_TextView, email_TextView;
    private ImageView profile_pic;
    private FloatingActionButton fab;

    public static String CURRENT_TAG;
    public static int navigationIndex;
    public static FirebaseAuth mAuth;
    public static String currentUser, currentEmail, currentUID, currentURL, currentNick, currentBirthday, currentAboutMe;
    public static GoogleApiClient mGoogleApiClient;
    public static List<User> users = new ArrayList<>();
    public static User selectedUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Check if user just logged in
        checkIfLogin();
        //get user data
        getUserInfo();
        //setup Navigation header and view
        navSetup();
        //Toggle FAB for screen rotations
        toggleFab();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        //if not on messages, back returns to message fragment
        if (navigationIndex != Constants.INDEX_MESSAGE_LIST) {
            navigationIndex = Constants.INDEX_MESSAGE_LIST;
            CURRENT_TAG = Constants.TAG_MESSAGE_LIST;
            loadHomeFragment(true);
            return;
        }
        if (navigationIndex == Constants.INDEX_MESSAGE_LIST && !drawer.isDrawerOpen(GravityCompat.START)) {
            this.finishAffinity();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // show menu only when home fragment is selected
        if (navigationIndex == Constants.INDEX_MESSAGE_LIST) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    /*
    check if the user just logged in, if they did display welcome, if not keep displaying what was being displayed
     */
    public void checkIfLogin() {
        Intent i = getIntent();
        //If user just logged in make sure app is on message list, then set to false so screen rotations don't mess things up
        if (i.getBooleanExtra("login", false)) {
            //Set to welcome screen
            navigationIndex = Constants.INDEX_WELCOME;
            CURRENT_TAG = Constants.TAG_WELCOME;
            loadHomeFragment(false);
            setToolbarTitle("Welcome " + mAuth.getCurrentUser().getDisplayName());
            i.putExtra("login", false);
        }
    }

    /*
    init views, set them up, etc
     */
    public void initWidgets() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        name_TextView = (TextView) headerView.findViewById(R.id.drawer_name_textview);
        email_TextView = (TextView) headerView.findViewById(R.id.drawer_email_textview);
        profile_pic = (ImageView) headerView.findViewById(R.id.drawer_imageView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        mHandler = new Handler();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationIndex = Constants.INDEX_SELECT_USER_TO_MSG;
                CURRENT_TAG = Constants.TAG_SELECT_USER_TO_MESSAGE;
                loadHomeFragment(false);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


    }


    /*
    Firebase <Start>
     */

    public void updateYourDatabaseInfo(User user) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(user.uid)
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            // failed to add user
                        }
                    }
                });
    }

    /*
    Check if a user is registed in the database, if not add them
     */
    public void checkIfRegistered() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {
                    // User does not exist in database yet, add them
                    User user = new User();
                    user.name = currentUser;
                    user.email = currentEmail;
                    user.uid = currentUID;
                    user.nickname = "";
                    user.birthday = "";
                    user.aboutme = "";
                    updateYourDatabaseInfo(user);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*
    Get a list of all users in the database, get data of yourself too
     */
    public void getUserInfo() {
        try {
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser().getDisplayName();
            currentEmail = mAuth.getCurrentUser().getEmail();
            currentUID = mAuth.getCurrentUser().getUid();
            currentURL = mAuth.getCurrentUser().getPhotoUrl().toString();

            checkIfRegistered();
            FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    users.clear();
                    Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                    //get all users that arent you
                    while (dataSnapshots.hasNext()) {
                        DataSnapshot dataSnapshotChild = dataSnapshots.next();
                        User user = dataSnapshotChild.getValue(User.class);
                        //Not you, populate users list
                        if (!TextUtils.equals(user.uid, currentUID)) {
                            users.add(user);
                            Log.d(Constants.TAG, "Populating user list with: " + user.email);
                        }
                        //this is you
                        if (TextUtils.equals(user.uid, mAuth.getCurrentUser().getUid())) {
                            currentNick = user.nickname;
                            currentBirthday = user.birthday;
                            currentAboutMe = user.aboutme;
                            updateYourDatabaseInfo(user);
                            Log.d(Constants.TAG, "Updated your profile data");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            Log.d(Constants.TAG, "navigation null: " + e);
        }
    }

    /*
    sign out of firebase and google
     */
    public void signOut() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        finish();
                    }
                });
    }

    /*
    Firebase </Stop>
     */


    /*
    Navigation / Toolbar <Start>
     */

    //Get info of signed in user, load it into navigation header, set nav index, load fragment, update user data in firebase in case it changed
    public void navSetup() {
        loadNavigationHeader();
        setUpNavigationView();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.message_list) {

        } else if (id == R.id.profile) {

        } else if (id == R.id.logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
    Load the users data into the navigation header
     */
    private void loadNavigationHeader() {
        name_TextView.setText(currentUser);
        email_TextView.setText(currentEmail);
        //download to pic ImageView
        DownloadImage dlImage = new DownloadImage(profile_pic);
        //begin image download from URL
        dlImage.execute(currentURL);
    }

    private void setToolbarTitle(String title) {
        String defTitle = "error";
        switch (navigationIndex) {
            case Constants.INDEX_MESSAGE_HISTORY:
                defTitle = title;
                break;
            case Constants.INDEX_WELCOME:
                defTitle = title;
                break;
        }
        if (!defTitle.equals("error"))
            getSupportActionBar().setTitle(defTitle);
    }

    /*
    Set title of toolbar from a constant
     */
    private void setToolbarTitle() {
        String title = "error";
        switch (navigationIndex) {

            case Constants.INDEX_MESSAGE_LIST:
                title = Constants.MESSAGE_LIST_TOOLBAR_TITLE;
                break;
            case Constants.INDEX_PROFILE:
                title = Constants.PROFILE_TOOLBAR_TITLE;
                break;
            case Constants.INDEX_LOGOUT:
                title = Constants.LOGOUT_TOOLBAR_TITLE;
                break;
            case Constants.INDEX_SELECT_USER_TO_MSG:
                title = Constants.SELECT_USER_TO_MESSAGE_TOOLBAR_TITLE;
                break;
        }
        if (!title.equals("error"))
            getSupportActionBar().setTitle(title);
    }

    /*
    Set which fragment is shown as checked in the nav drawer
     */
    private void selectNavMenu() {
        navigationView.getMenu().getItem(navigationIndex).setChecked(true);
    }

    /*
    Takes care of navigation through nav drawer
     */
    public void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.message_list:
                        navigationIndex = Constants.INDEX_MESSAGE_LIST;
                        CURRENT_TAG = Constants.TAG_MESSAGE_LIST;
                        break;
                    case R.id.profile:
                        navigationIndex = Constants.INDEX_PROFILE;
                        CURRENT_TAG = Constants.TAG_PROFILE;
                        break;
                    case R.id.logout:
                        navigationIndex = Constants.INDEX_LOGOUT;
                        signOut();
                        break;
                    default:
                        navigationIndex = Constants.INDEX_MESSAGE_LIST;
                }

                //Check if the item is in checked state or not, if not make it checked
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment(true);

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes. obvious
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open. also obvious
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    // show or hide the fab
    private void toggleFab() {
        if (navigationIndex == Constants.INDEX_MESSAGE_LIST && CURRENT_TAG == Constants.TAG_MESSAGE_LIST) {
            fab.show();
        } else{
            fab.hide();
        }

    }
    /*
    Navigation / Toolbar </Stop>
     */



    /*
    General Fragment <Start>
     */


    /*
    Call when loading fragment from another fragment (message fragment)
     */
    public void loadHomeFragment(String tag, User user) {
        if (tag == Constants.TAG_MESSAGE_HISTORY) {
            selectedUser = user;
            navigationIndex = Constants.INDEX_MESSAGE_HISTORY;
            CURRENT_TAG = tag;
            setToolbarTitle(user.email);
        }
        loadHomeFragment(false);
    }

    /*
     * Returns respected fragment that user
     * selected from navigation menu
     */
    public void loadHomeFragment(boolean isNavigation) {
        //If the item is an item on the navigation menu, set it as selected
        if (isNavigation) {
            // selecting appropriate nav menu item
            selectNavMenu();
        }

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        //Fade animation for fragment transitions
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commit();

            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();

    }

    private Fragment getHomeFragment() {
        switch (navigationIndex) {
            case Constants.INDEX_WELCOME:
                WelcomeFragment welcomeFragment = new WelcomeFragment();
                return welcomeFragment;
            case Constants.INDEX_MESSAGE_LIST:
                MessageListFragment messageListFragment = new MessageListFragment();
                return messageListFragment;
            case Constants.INDEX_PROFILE:
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;
            case Constants.INDEX_SELECT_USER_TO_MSG:
                SelectUserToMessageFragment selectUserToMessageFragment = new SelectUserToMessageFragment();
                return selectUserToMessageFragment;
            case Constants.INDEX_MESSAGE_HISTORY:
                MessageHistoryFragment messageHistoryFragment = new MessageHistoryFragment();
                return messageHistoryFragment;
            default:
                return new MessageListFragment();
        }
    }

    /*
    General Fragment </Stop>
     */
}

