package com.uksw.khw.ngechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccount;
    private EditText UserName, UserStatus;
    private CircleImageView UserProfile;
    //inisialisasi untuk menyimpan informasi user dari aplikasi ke firebase begitu sebaliknya
    private String currentUserId;
    private FirebaseAuth myAuth;
    private DatabaseReference RootKev;

    private static final int GalleryPick = 1;
    private StorageReference ProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        RootKev = FirebaseDatabase.getInstance().getReference();
        ProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        UpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });

        RetrieveUserInfo();

        UserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }



    private void InitializeFields() {
        UpdateAccount = (Button) findViewById(R.id.update_settings);
        UserName = (EditText) findViewById(R.id.set_user_name);
        UserStatus = (EditText) findViewById(R.id.set_status);
        UserProfile = (CircleImageView) findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Images");
                loadingBar.setMessage("Please wait, your profile image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                final Uri resultUri = result.getUri();
                final StorageReference filePath = ProfileImagesRef.child(currentUserId+ ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                RootKev.child("Users").child(currentUserId).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Profile image stored to database successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }
    }

    private void UpdateSetting() {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();


        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            HashMap<String, Object> ProfileMap = new HashMap<>();
            ProfileMap.put("uid", currentUserId);
            ProfileMap.put("name", setUserName);
            RootKev.child("Users").child(currentUserId).updateChildren(ProfileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Update profile successful", Toast.LENGTH_SHORT).show();//jika berhasil muncul pesan ini
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();// jika error muncul pesan ini
                            }
                        }
                    });
        } else {
            HashMap<String, Object> ProfileMap = new HashMap<>();
            ProfileMap.put("uid", currentUserId);
            ProfileMap.put("name", setUserName);
            ProfileMap.put("status", setStatus);
            //buat mengubah data di database
            RootKev.child("Users").child(currentUserId).updateChildren(ProfileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Update profile successful", Toast.LENGTH_SHORT).show();//jika berhasil muncul pesan ini
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();// jika error muncul pesan ini
                            }
                        }
                    });
        }
    }

//mengambil info dari database untuk di simpan pada aplikasi
    private void RetrieveUserInfo() {
        RootKev.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                        String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                        String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                        String retrieveImage = dataSnapshot.child("image").getValue().toString();

                        UserName.setText(retrieveUserName);
                        UserStatus.setText(retrieveStatus);
                        Picasso.get().load(retrieveImage).into(UserProfile);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);
                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();

                            UserName.setText(retrieveUserName);
                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Please set or update your profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToMainActivity () {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//biar user kalo klik back button tidak kembali kehalaman sebelumnya
        startActivity(mainIntent);
        finish();
    }

}
