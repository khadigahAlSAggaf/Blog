package com.example.khadougal_saggaf.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class PostPage extends AppCompatActivity {

    private Toolbar postToolBar;
    private String post_id;
    private String user_id;
    private TextView post;
    private TextView username;
    private TextView date;
    private CircleImageView profileImage;
    private ImageView postImage;


    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);

        //ToolBar
        postToolBar = findViewById(R.id.post_toolBar);
        setSupportActionBar(postToolBar);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Post View Instance
        post = findViewById(R.id.postView_desc);
        username = findViewById(R.id.username_postView);
        date = findViewById(R.id.plog_date_postView);
        profileImage = findViewById(R.id.postView_userImage);
        postImage = findViewById(R.id.postView_image_post);


        post_id = getIntent().getExtras().getString("post_id");
        user_id = getIntent().getExtras().getString("user_id");

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Posts").document(post_id).get().addOnCompleteListener(PostPage.this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String postDescription = task.getResult().getString("desc");
                    String image = task.getResult().getString("image_uri");

                    Date postData = task.getResult().getDate("timeStamp");
                    long dateNumber = postData.getTime();
                    String dateString = DateFormat.format("MM/dd/yyyy", new Date(dateNumber)).toString();

                    post.setText(postDescription);
                    date.setText(dateString);

                    //replace the dummy image profile with the i,age that uploaded into firebase
                    RequestOptions placeholderRequest = new RequestOptions(); //define place holder
                    placeholderRequest.placeholder(R.drawable.profile);// link holder with dummy image profile
                    //load image
                    Glide.with(PostPage.this).setDefaultRequestOptions(placeholderRequest).load(image).into(postImage);


                }
            }
        });

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(PostPage.this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    username.setText(userName);
                    //replace the dummy image profile with the i,age that uploaded into firebase
                    RequestOptions placeholderRequest = new RequestOptions(); //define place holder
                    placeholderRequest.placeholder(R.drawable.profile);// link holder with dummy image profile
                    //load image
                    Glide.with(PostPage.this).setDefaultRequestOptions(placeholderRequest).load(userImage).into(profileImage);

                } else {

                    //Firebase Exception
                }
            }
        });

    } // END ON_CREATE


    // Link the menu with the layout needed..
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.post_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Define the action of each item of menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Toast.makeText(PostPage.this, "Edit icon clicked", Toast.LENGTH_LONG).show();
                return true;

            case R.id.delete:
                deletePost();
                return true;

            default:
                return false;
        }
    }

    public void deletePost() {
//deklee
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(PostPage.this);

        // Setting Dialog Title
        alertDialog2.setTitle("Confirm Delete...");

        // Setting Dialog Message
        alertDialog2.setMessage("Are you sure you want delete this post?");

        // Setting Icon to Dialog
        alertDialog2.setIcon(R.drawable.ic_delete_red);

        // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        firebaseFirestore.collection("Posts").document(post_id).delete();

                        Intent i=new Intent(PostPage.this,MainActivity.class);
                        Toast.makeText(PostPage.this, "Post deleted", Toast.LENGTH_LONG).show();

                        startActivity(i);

                    }
                });

        // Setting Negative "NO" Btn
        alertDialog2.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

        // Showing Alert Dialog
        alertDialog2.show();
    }

}
