package com.uksw.khw.ngechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class loginActivity extends AppCompatActivity {

    //untuk mengambil user dari firebase
    private FirebaseAuth myAuth;

    private ProgressDialog dialog;
    //inisialisasi dari button,edittext,textview agar bisa dilakukan proses
    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgetPasswordLink;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            //mengirim user ke registrasi activity
            public void onClick(View v)
            {
                sendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLogin();
            }
        });

    }

    private void UserLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            myAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.setTitle("Sign In");
                            dialog.setMessage("Please Wait");
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.show();

                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(loginActivity.this, "Logged in Successful", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(loginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
        }
    }

    //agar inisialisasi kita dapat bekerja
    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        NeedNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        dialog = new ProgressDialog(this);
    }


    //melempar user ke kelas main activity
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(loginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//biar user kalo klik back button tidak kembali kehalaman sebelumnya
        startActivity(mainIntent);
        finish();
    }
    //melempar User ke registrasi
    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(loginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}