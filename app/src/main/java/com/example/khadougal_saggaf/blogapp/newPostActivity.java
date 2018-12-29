package com.example.khadougal_saggaf.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;


public class newPostActivity extends AppCompatActivity {

    Toolbar newPost_toolBar;
    ProgressBar new_post_progressBar;

    private ImageView image_new_post;
    private EditText editText_new_post_desc;
    private Button add_post_button;

    private Uri post_image_uri=null;
    private String userID;

    //Firebase Instance
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPost_toolBar=findViewById(R.id.newPost_toolBar);
        setSupportActionBar(newPost_toolBar);
        getSupportActionBar().setTitle("Photo Blog");

        new_post_progressBar=findViewById(R.id.progressBar_new_post);

        image_new_post=findViewById(R.id.new_post_image);
        editText_new_post_desc=findViewById(R.id.editText_newPost_desc);
        add_post_button=findViewById(R.id.button_addPost);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference=FirebaseStorage.getInstance().getReference();
        userID=firebaseAuth.getCurrentUser().getUid();

        image_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setMinCropResultSize(512,512)
                        .start(newPostActivity.this);
            }
        });


        add_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String newPost=editText_new_post_desc.getText().toString();

                if(!TextUtils.isEmpty(newPost)&& post_image_uri!=null){
                    new_post_progressBar.setVisibility(View.VISIBLE);

                    String randoKey=FieldValue.serverTimestamp().toString();//generate random key
                    final StorageReference filePath=storageReference.child("post_image").child(randoKey+".jpg");

                    UploadTask uploadTask = filePath.putFile(post_image_uri);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            //this work to get the URI that reserve at firestore for specific image
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                storeFirestore(task, newPost);

                            } else {
                                // Handle failures
                                Toast.makeText(newPostActivity.this, "Image cannot Uploaded", Toast.LENGTH_LONG).show();
                                //ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                }

            }
        });

    }//end onCreate


    /* storeFirestore store data into firestore database..
     * after uploading image into storage now each data must store into backend database firestore..
     * 1- Createing new collection -root- into fireStore DB
     * */
    private void storeFirestore(@NonNull Task<Uri> task, String newPost) {

        Uri downloadUri=task.getResult();

        Map<String, String> postMap = new HashMap<>();
        postMap.put("image_uri", downloadUri.toString());
        postMap.put("desc",newPost);
        postMap.put("userId",userID);
        postMap.put("timeStamp",FieldValue.serverTimestamp().toString());

        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    Toast.makeText(newPostActivity.this,"Post Added Successfully",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(newPostActivity.this,MainActivity.class));

                }else{
                    Toast.makeText(newPostActivity.this,"errorrrrreeee***",Toast.LENGTH_LONG).show();

                }
            }
        });

        new_post_progressBar.setVisibility(View.INVISIBLE);

    }

    /* This method by (Android-Image-Cropper) library from gitHub to Cropping image
     * image that chosen from gallery or camera..
     * the result of cropping image is URI, then replace the dummy image profile with this Uri image.
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                post_image_uri=result.getUri();
                image_new_post.setImageURI(post_image_uri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
