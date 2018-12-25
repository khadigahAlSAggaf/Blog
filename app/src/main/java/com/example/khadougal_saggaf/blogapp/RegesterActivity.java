package com.example.khadougal_saggaf.blogapp;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegesterActivity extends AppCompatActivity {
    private Button createAccount;
    private Button logIn;

    private EditText Reg_Email;
    private EditText Reg_Password;
    private EditText Reg_ConfiremPassword;

    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regester);

        //Firebase instance
        auth=FirebaseAuth.getInstance();

        progressBar=findViewById(R.id.Reg_progressBar);

        createAccount=findViewById(R.id.CreateAccount_button);
        logIn=findViewById(R.id.AlreadyHaveAccount_button);

        Reg_Password=findViewById(R.id.Reg_password_editText);
        Reg_Email=findViewById(R.id.Reg_Email_editText);
        Reg_ConfiremPassword=findViewById(R.id.ConfiremPass_editText);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegesterActivity.this,LogIn.class));
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=Reg_Email.getText().toString().trim();
                String password=Reg_Password.getText().toString().trim();
                String confiremPass=Reg_ConfiremPassword.getText().toString().trim();

                if(!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(password)&& !TextUtils.isEmpty(confiremPass)){
                  if(password.equals(confiremPass)){
                      progressBar.setVisibility(View.VISIBLE);

                      auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                          @Override
                          public void onComplete(@NonNull Task<AuthResult> task) {

                              if(task.isSuccessful()){
                                startActivity(new Intent(RegesterActivity.this,UserAccount.class));
                              }else{
                                  String erroeMessage=task.getException().getMessage();
                                  Toast.makeText(RegesterActivity.this,erroeMessage,Toast.LENGTH_LONG).show();

                              }
                              progressBar.setVisibility(View.INVISIBLE);

                          }
                      });

                  }else{
                      Toast.makeText(RegesterActivity.this,"confirm password and password dosn't match",Toast.LENGTH_LONG).show();
                  }
                }
            }
        });




    }
    @Override
    protected void onStart(){
        super.onStart();
        sendToMain();
    }

    protected void sendToMain(){
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!=null){
            startActivity(new Intent(RegesterActivity.this,MainActivity.class));
            finish();
        }
    }
}
