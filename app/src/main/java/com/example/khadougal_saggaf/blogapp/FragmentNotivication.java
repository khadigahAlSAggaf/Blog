package com.example.khadougal_saggaf.blogapp;


import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */

public class FragmentNotivication extends Fragment {

    private CircleImageView profile_image;
    private Uri mainImageURI;

    private TextView user_name;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DocumentSnapshot lastVisible;

    private String current_user;
    private RecyclerView post_list_view;
    private List<BlogPost> blog_list;
    private ProfileRecyclerAdapter profileRecyclerAdapter;

    private ImageView likeImage;

    private boolean isFirstPageFirstLoad = true;

    //Bio maximum 24 litters

    public FragmentNotivication() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_notivication, container, false);

        blog_list = new ArrayList<>();
        post_list_view = view.findViewById(R.id.profile_view);

        profileRecyclerAdapter = new ProfileRecyclerAdapter(blog_list); //adapter take arrayList

        post_list_view.setLayoutManager(new LinearLayoutManager(getActivity())); //decide shape of list
        post_list_view.setAdapter(profileRecyclerAdapter); //join recyclerView with recyclerAdapter


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        profile_image = view.findViewById(R.id.postView_userImage);

        user_name = view.findViewById(R.id.user_name);

        //likeImage = view.findViewById(R.id.like);


        /* Display image/data witch uploaded into firestore/Storage by:
         * 1- Retrieving the document of the user by UserId
         * 2- Checking if the data/name/image exist
         * 3- Use Glide (glide) library to  loading and caching image
         * 4- Text is retrieving smoothly using setText..
         * */
        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");


                        //replace the dummy image profile with the i,age that uploaded into firebase
                        RequestOptions placeholderRequest = new RequestOptions(); //define place holder
                        placeholderRequest.placeholder(R.drawable.profile);// link holder with dummy image profile
                        //load image
                        Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest).load(image).into(profile_image);

                        //put the name from DB into it'is place
                        user_name.setText(name);
                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(getActivity(), "Error while retrieving data" + error, Toast.LENGTH_LONG).show();
                }

            }
        });

        /*likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                alert.setTitle("Bio");
                alert.setMessage("Maximum 39 letter");

                // Set an EditText view to get user input

                final EditText input = new EditText(getContext());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Do something with value!
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();

            }
        });

*/
        // Retrieve user post
        myPost();


        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onDetach() {
        // this will make you scroll all the way down
        isFirstPageFirstLoad = true;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        // this will rearrange them in desending order
        isFirstPageFirstLoad = true;
        super.onAttach(context);
    }


    public void myPost() {

        if (firebaseAuth.getCurrentUser() != null) {


            //Query second = firebaseFirestore.collection("Posts");
            CollectionReference firstQuery = firebaseFirestore.collection("Posts");

            Query second = firstQuery.whereEqualTo("userId", firebaseAuth.getCurrentUser().getUid())
                    .orderBy("timeStamp", Query.Direction.DESCENDING);
            //Query firstQuery = firebaseFirestore.collection("Posts").document("user_id");


            //snapshot help us to retrieve the data in realTime with order by last to old pots
            // call getActivity in fragment or this in activity while using addSnapshotListenter
            // to attach data to activity only if activity is launch and stop while not
            second.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    //for loop to check for document changes
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String BlogpostID = doc.getDocument().getId();
                                BlogPost blogPostFromDB = doc.getDocument().toObject(BlogPost.class).withID(BlogpostID);

                                blog_list.add(blogPostFromDB);


                                profileRecyclerAdapter.notifyDataSetChanged(); //this to monitoring any change happen to list
                            }
                        }
                    }
                }

            });

        }
    }

}
