package com.uksw.khw.ngechat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccount;

    private FirebaseAuth myAuth;
    private DatabaseReference RootKev;

    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myAuth = FirebaseAuth.getInstance();
        RootKev = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        }
        else {
            dialog.setTitle("Creating New Account");
            dialog.setMessage("Please Wait, While We Are Creating New Account For You");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            myAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            sendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        }
                    });
        }
    }

    private void InitializeFields() {
    CreateAccountButton = (Button) findViewById(R.id.register_button);
    UserEmail = (EditText) findViewById(R.id.register_email);
    UserPassword = (EditText) findViewById(R.id.register_password);
    AlreadyHaveAccount = (TextView) findViewById(R.id.Already_have_account_link);

    dialog = new ProgressDialog(this);
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, loginActivity.class);
        startActivity(loginIntent);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//biar user kalo klik back button tidak kembali kehalaman sebelumnya
        startActivity(mainIntent);
        finish();
    }
}
