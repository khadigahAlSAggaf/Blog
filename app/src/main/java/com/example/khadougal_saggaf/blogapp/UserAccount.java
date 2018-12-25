package com.example.khadougal_saggaf.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAccount extends AppCompatActivity {
    Toolbar setUpToolBar;
    CircleImageView setupProfileImage;
    private Uri mainImageURI = null;

    private String userID;
    private EditText setup_Name;
    private Button setup_Button;
    private boolean isChange;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressBar ProgressBar_setUp_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        setUpToolBar = findViewById(R.id.setUpToolBar);
        setSupportActionBar(setUpToolBar);
        getSupportActionBar().setTitle("Account Setup");

        setupProfileImage = findViewById(R.id.setup_image);
        setup_Name = findViewById(R.id.userName);
        setup_Button = findViewById(R.id.setup_button);
        ProgressBar_setUp_profile = findViewById(R.id.setUp_profile_ProgressBar);

        //firebase instance
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();


        setup_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_name = setup_Name.getText().toString();
                ProgressBar_setUp_profile.setVisibility(View.VISIBLE);

                if(isChange){
                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {

                    //to get cureent user data from database
                    userID = firebaseAuth.getCurrentUser().getUid();


                    //in storage firebase create root called profile images , collect each user id + image
                    final StorageReference image_path = storageReference.child("profile_images").child(userID + ".jpg");

                    UploadTask uploadTask = image_path.putFile(mainImageURI);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return image_path.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                storeFirestore(task, user_name);

                            } else {
                                // Handle failures
                                Toast.makeText(UserAccount.this, "noooo" + "****", Toast.LENGTH_LONG).show();
                                ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }else{
                    storeFirestore(null,user_name);
                }

                }
            }

            private void storeFirestore(@NonNull Task<Uri>task,String user_name) {
                Uri downloadUri;

                if(task!=null) {
                     downloadUri = task.getResult();
                }else {
                     downloadUri = mainImageURI;

                }
                Map<String, String> userMap = new HashMap<>();

                //create values name , image inside users collection
                userMap.put("name", user_name);
                userMap.put("image", downloadUri.toString());

                firebaseFirestore.collection("Users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserAccount.this, "Account Setting is Updated", Toast.LENGTH_LONG).show();

                            startActivity(new Intent(UserAccount.this, MainActivity.class));
                            finish();

                        } else {
                            String errer = task.getException().getMessage();
                            Toast.makeText(UserAccount.this, errer + "****", Toast.LENGTH_LONG).show();

                        }
                    }
                });


                ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);
                //Toast.makeText(UserAccount.this,"yesssss"+"****",Toast.LENGTH_LONG).show();

            }
        });

        ProgressBar_setUp_profile.setVisibility(View.VISIBLE);
        setup_Button.setEnabled(false);
        //to Rerive the image and name and fixed them in page after added to firebase
        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        //to choose new image if click
                        mainImageURI=Uri.EMPTY.parse(image);
                        //to place the dummy image profile with the i,age that uploaded into firebase
                        RequestOptions placeholderRequest = new RequestOptions(); //define [lace holder
                        placeholderRequest.placeholder(R.drawable.profile);// link holder with dummy image profile
                        //load image
                        Glide.with(UserAccount.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupProfileImage);

                        //put the name from DB into it'is place
                        setup_Name.setText(name);

                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(UserAccount.this, "(Firestore Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
                ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);
                setup_Button.setEnabled(true);



            }
        });


        setupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(UserAccount.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Request Permission
                        ActivityCompat.requestPermissions(UserAccount.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        Toast.makeText(UserAccount.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    } else {
                        BringCropImage();
                    }
                } else {
                    BringCropImage();
                }
            }
        });
    }

    private void BringCropImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1,1)
                .start(UserAccount.this);
    }

    //Cropping image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                setupProfileImage.setImageURI(mainImageURI);
                isChange=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
