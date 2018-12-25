package com.example.khadougal_saggaf.blogapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Toolbar mainToolBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainToolBar=findViewById(R.id.main_toolBar);
        setSupportActionBar(mainToolBar);

        getSupportActionBar().setTitle("Photo Blog");
    }



    @Override
    protected void onStart(){
        super.onStart();
        sendToLogIn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logOut:
                logOut();
                return true;

            case R.id.action_setting_btn:
                startActivity(new Intent(MainActivity.this,UserAccount.class));
                return true;

            default:return false;
        }
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        sendToLogIn();

    }

    private void sendToLogIn(){
        FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null){
            startActivity(new Intent(MainActivity.this,LogIn.class));
            finish();
        }
    }
}
