package com.uksw.khw.ngechat;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;

    private FirebaseAuth myAuth;
    private DatabaseReference RootKev;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAuth = FirebaseAuth.getInstance();
        RootKev = FirebaseDatabase.getInstance().getReference();


        InitializeFields();
    }


    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ngeCHAT");

        mViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = myAuth.getCurrentUser();

        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            updateUserStatus("Online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = myAuth.getCurrentUser();

        if (currentUser != null){
            updateUserStatus("Offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser != null){
            updateUserStatus("Offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserId = myAuth.getCurrentUser().getUid();
        //buat save informasi user ke firebase
        RootKev.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
                } else {
                    sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }
    //untuk mengurus bagian optional menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout) {

            updateUserStatus("Offline");

            myAuth.signOut();
            sendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_setting) {
            sendUserToSettingActivity();
        }
        if (item.getItemId() == R.id.main_find_friends) {

            sendUserToFindFriendsActivity();

        }

        return true;
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, loginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void sendUserToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingIntent);
    }


    private void sendUserToFindFriendsActivity() {
        Intent FindFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FindFriendsIntent);
    }


    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat(" MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(" hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = myAuth.getCurrentUser().getUid();

        RootKev.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }

}