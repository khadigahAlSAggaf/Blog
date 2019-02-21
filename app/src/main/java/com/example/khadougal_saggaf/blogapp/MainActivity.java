package com.example.khadougal_saggaf.blogapp;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolBar;
    private FloatingActionButton addPost;
    private BottomNavigationView Bottom_Main_Nav;

    //Fragment
    private FragmentHome fragmentHome;
    private FragmentNotivication fragmentNotivication;
    private FragmentAccount fragmentAccount;


    private String Current_userID;

    //firebase instance
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ToolBar
        mainToolBar = findViewById(R.id.post_toolBar);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("Photo Blog");

        //Fragment Instance
        fragmentHome = new FragmentHome();
        fragmentAccount = new FragmentAccount();
        fragmentNotivication = new FragmentNotivication();

        //Firebase Instance
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Display the Home in first
        //replaceFragment(fragmentHome);
        initializeFragment();

        //onClick floatingPoint Button
        addPost = findViewById(R.id.floatingActionButton);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, newPostActivity.class));
            }
        });

        Bottom_Main_Nav = findViewById(R.id.main_Button_Nav);

        Bottom_Main_Nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.main_container);
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        replaceFragment(fragmentHome,currentFragment);
                        return true;

                    /*case R.id.nav_notivication:
                        //replaceFragment(fragmentNotivication);
                        startActivity(new Intent(MainActivity.this,otherPage.class));

                        return true;*/

                    case R.id.nav_account:
                        replaceFragment(fragmentNotivication,currentFragment);
                        //startActivity(new Intent(MainActivity.this,otherPage.class));

                        return true;

                    default:
                        return false;
                }

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
     * how to check the setup is done or no?
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


    private void replaceFragment(Fragment fragment,Fragment currentFragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if(fragment == fragmentHome){

            //fragmentTransaction.hide(fragmentAccount);
            fragmentTransaction.hide(fragmentNotivication);

        }

        /*if(fragment == fragmentAccount){

            fragmentTransaction.hide(fragmentHome);
            fragmentTransaction.hide(fragmentNotivication);

        }
        */

        if(fragment == fragmentNotivication){

            fragmentTransaction.hide(fragmentHome);
            //fragmentTransaction.hide(fragmentAccount);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

        /*fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();*/

    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, fragmentHome);
        fragmentTransaction.add(R.id.main_container, fragmentNotivication);
        //fragmentTransaction.add(R.id.main_container, accountFragment);

        fragmentTransaction.hide(fragmentNotivication);
        //fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

    }


}
