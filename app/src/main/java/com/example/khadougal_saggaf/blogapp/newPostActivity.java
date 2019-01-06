package com.example.khadougal_saggaf.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import id.zelory.compressor.Compressor;


public class newPostActivity extends AppCompatActivity {

    Toolbar newPost_toolBar;
    ProgressBar new_post_progressBar;


    private ImageView image_new_post;
    private EditText editText_new_post_desc;
    private Button add_post_button;

    private Uri post_image_uri = null;
    private String userID;

    //Firebase Instance
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    //File Compressed
    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPost_toolBar = findViewById(R.id.newPost_toolBar);
        setSupportActionBar(newPost_toolBar);
        getSupportActionBar().setTitle("Photo Blog");

        new_post_progressBar = findViewById(R.id.progressBar_new_post);

        image_new_post = findViewById(R.id.new_post_image);
        editText_new_post_desc = findViewById(R.id.editText_newPost_desc);
        add_post_button = findViewById(R.id.button_addPost);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userID = firebaseAuth.getCurrentUser().getUid();

        image_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(newPostActivity.this);
            }
        });

        /* setup_Button onClick action, in this step the image that picked from gallery/camera
         * will upload into Storage Reference..
         * 1- Creating new file into
         * 2- Saved each image with userID+.jpg
         * 3- Compress image
         * 4- Get Image path UriDowloader,to user later
         * UriDowloader witch is path of image in firestore Storage
         * */

        add_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String newPost = editText_new_post_desc.getText().toString();

                if (!TextUtils.isEmpty(newPost) && post_image_uri != null) {
                    new_post_progressBar.setVisibility(View.VISIBLE);

                    String randoKey = UUID.randomUUID().toString();


                    byte[] imageData = CompressPhoto(image_new_post); // call compressed method


                    final StorageReference filePath = storageReference.child("post_image").child(randoKey + ".jpg"); // create child of post_image in storage


                    UploadTask uploadTask = filePath.putBytes(imageData); // upload task with image that compressed
                    //UploadTask uploadTask = filePath.putFile(post_image_uri);

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

                                storeFirestore(task, newPost); //task take the image task uploaded, newPost is the post written

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


    }//END ON-CREATE


    /* storeFirestore store data into firestore database..
     * after uploading image into storage now each data must store into backend database firestore..
     * 1- Creating new collection -root- into fireStore DB
     * */
    private void storeFirestore(@NonNull Task<Uri> task, String newPost) {

        Uri downloadUri = task.getResult();

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("image_uri", downloadUri.toString());
        postMap.put("desc", newPost);
        postMap.put("userId", userID);
        postMap.put("timeStamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(newPostActivity.this, "Yessssss, Post Added Successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(newPostActivity.this, MainActivity.class));

                } else {
                    Toast.makeText(newPostActivity.this, "Post Doesn't Uploaded", Toast.LENGTH_LONG).show();

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

                post_image_uri = result.getUri();
                image_new_post.setImageURI(post_image_uri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    /* CompressPhoto, compress the image resolution and quality..
     * github library (Compressor)
     * */
    private byte[] CompressPhoto(ImageView image_new_post) {
        byte[] imageData;
        File newImageFile = new File(post_image_uri.getPath());
        try {

            compressedImageFile = new Compressor(newPostActivity.this)
                    .setMaxHeight(100)
                    .setMaxWidth(100)
                    .setQuality(1)
                    .compressToBitmap(newImageFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return imageData = baos.toByteArray();
    }


}//END CLASS
