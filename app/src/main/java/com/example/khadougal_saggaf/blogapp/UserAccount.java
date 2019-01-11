package com.example.khadougal_saggaf.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAccount extends AppCompatActivity {

    private Toolbar setUpToolBar;
    private ProgressBar ProgressBar_setUp_profile;

    private CircleImageView setupProfileImage;
    private Uri mainImageURI = null;

    private EditText setup_Name;
    private Button setup_Button;
    private boolean isChange;
    private String userID;

    //Firebase Instances
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        //Tool Bar
        setUpToolBar = findViewById(R.id.setUpToolBar);
        setSupportActionBar(setUpToolBar);
        getSupportActionBar().setTitle("Account Setup");

        //Progress Bar
        ProgressBar_setUp_profile = findViewById(R.id.setUp_profile_ProgressBar);


        setupProfileImage = findViewById(R.id.user_plog_image);
        setup_Name = findViewById(R.id.userName);
        setup_Button = findViewById(R.id.setup_button);

        //Firebase instance
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();


        /* setup_Button onClick action, in this step the image that picked from gallery/camera
         * will upload into Storage Reference..
         * 1- Creating new file into
         * 2- Saved each image with userID+.jpg
         * 3- Get Image path UriDowloader,to user later
         * UriDowloader witch is path of image in firestore Storage
         * */
        setup_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_name = setup_Name.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) { //both image and name are requires

                    ProgressBar_setUp_profile.setVisibility(View.VISIBLE);

                    if (isChange) {

                        userID = firebaseAuth.getCurrentUser().getUid(); //get userId

                        //create root called profile images, then save each image as image with name userID.jpg
                        final StorageReference image_path = storageReference.child("profile_images").child(userID + ".jpg");

                        UploadTask uploadTask = image_path.putFile(mainImageURI);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                //this work to get the URI that reserve at firestore for specific image
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name);

                                } else {
                                    // Handle failures
                                    Toast.makeText(UserAccount.this, "Image cannot Uploaded", Toast.LENGTH_LONG).show();
                                    ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    } else {
                        storeFirestore(null, user_name);
                    }
                }
            }

        });

        ProgressBar_setUp_profile.setVisibility(View.VISIBLE);
        setup_Button.setEnabled(false);



        /* Display image/data witch uploaded into firestore/Storage by:
         * 1- Retrieving the document of the user by UserId
         * 2- Checking if the data/name/image exist
         * 3- Use Glide (glide) library to  loading and caching image
         * 4- Text is retrieving smoothly using setText..
         * */
        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageURI = Uri.EMPTY.parse(image); //to choose new image if click after display

                        //replace the dummy image profile with the i,age that uploaded into firebase
                        RequestOptions placeholderRequest = new RequestOptions(); //define place holder
                        placeholderRequest.placeholder(R.drawable.profile);// link holder with dummy image profile
                        //load image
                        Glide.with(UserAccount.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupProfileImage);

                        //put the name from DB into it'is place
                        setup_Name.setText(name);
                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(UserAccount.this, "Error while retrieving data" + error, Toast.LENGTH_LONG).show();
                }
                ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);
                setup_Button.setEnabled(true);

            }
        });


        /* setupProfileImage onClick action, it picker image from gallery,camera...etc
         * It's requesting user permission to access galley while the phone SDK 21> ,otherwise permission open automatic.
         * */
        setupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(UserAccount.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Request permission dialog
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


    /* storeFirestore store data into firestore database..
     * after uploading image into storage now each data must store into backend database firestore..
     * 1- Createing new collection -root- into fireStore DB
     * */
    private void storeFirestore(@NonNull Task<Uri> task, String user_name) {
        Uri downloadUri;

        if (task != null) {
            downloadUri = task.getResult(); //upload new image into storage

        } else {

            downloadUri = mainImageURI; //image already uploaded
        }

        Map<String, String> userMap = new HashMap<>();
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
                    String error = task.getException().getMessage();
                    Toast.makeText(UserAccount.this, error + "Something wrong happen while upload image", Toast.LENGTH_LONG).show();
                }
            }
        });

        ProgressBar_setUp_profile.setVisibility(View.INVISIBLE);

    }

    /* BringCropImage first step of (Android-Image-Cropper) library from gitHub to Cropping image
     * to start picker image fom gallery, camera..etc
     * */
    private void BringCropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1,1)

                .start(UserAccount.this);
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

                mainImageURI = result.getUri(); //result of cropping
                setupProfileImage.setImageURI(mainImageURI); //assign image profile with Uri cropping
                isChange = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
