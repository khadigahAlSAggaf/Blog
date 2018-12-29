package com.example.khadougal_saggaf.blogapp;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolBar;
    private FloatingActionButton addPost;
    private String Current_userID;

    //firebase instance
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ToolBar
        mainToolBar = findViewById(R.id.main_toolBar);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("Photo Blog");

        //Firebase Instance
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //onClick floatingPoint Button
        addPost = findViewById(R.id.floatingActionButton);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, newPostActivity.class));
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        sendToLogIn();
    }


    // Link the menu with the layout needed..
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Define the action of each item of menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logOut:
                logOut();
                return true;

            case R.id.action_setting_btn:
                startActivity(new Intent(MainActivity.this, UserAccount.class));
                return true;

            default:
                return false;
        }
    }

    //LogOut method
    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        sendToLogIn();

    }

    /* sendToLogIn method called by onStart method witch work while application starting
     * it check if user already logIn -doesn't logOut yet- so it's start app directly to main page -Home-
     * but if it logOut it display register/LogIn page.
     *
     * and if the user already logIn, but doesn't configure setup image and name, the app open direct to setup page.
     * how to sheck the setup is done or no?
     * it's connected to the document of this user, and check if it's data -image & name- found or not,
     * so if it's found then continue to home page, but if doesn't exist so, the setup page display.
     * */
    private void sendToLogIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LogIn.class));
            finish();
        } else {
            Current_userID = firebaseAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(Current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            startActivity(new Intent(MainActivity.this, UserAccount.class));
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }
}
