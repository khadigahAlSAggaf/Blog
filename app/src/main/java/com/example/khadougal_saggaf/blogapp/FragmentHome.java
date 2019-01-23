package com.example.khadougal_saggaf.blogapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHome extends Fragment {

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    DocumentSnapshot lastVisible;

    private RecyclerView post_list_view;
    private List<BlogPost> blog_list;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private boolean isFirstPageFirstLoad = true;

    public FragmentHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate view in it's activity
        View view = inflater.inflate(R.layout.fragment_fragment_home, container, false);

        blog_list = new ArrayList<>();
        post_list_view = view.findViewById(R.id.plog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list); //adapter take arrayList

        post_list_view.setLayoutManager(new LinearLayoutManager(getActivity())); //decide shape of list
        post_list_view.setAdapter(blogRecyclerAdapter); //join recyclerView with recyclerAdapter


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {


            post_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {
                        String desc = lastVisible.getString("desc");
                        //Toast.makeText(getContext(), "Reach Bottom.. " + desc, Toast.LENGTH_LONG).show();
                        loadMorePage();
                    }
                }
            });


            //.orderBy("timeStamp", Query.Direction.DESCENDING).limit(3)

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timeStamp", Query.Direction.DESCENDING).limit(3);

            //snapshot help us to retrieve the data in realTime with order by last to old pots
            // call getActivity in fragment or this in activity while using addSnapshotListenter
            // to attach data to activity only if activity is launch and stop while not
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    if (isFirstPageFirstLoad) {
                        // Get the last visible document
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    }

                    if (e != null) {
                        Log.d(TAG, "Error:" + e.getMessage());
                    } else {

                        //for loop to check for document changes
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {


                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String BlogpostID = doc.getDocument().getId();
                                BlogPost blogPostFromDB = doc.getDocument().toObject(BlogPost.class).withID(BlogpostID);
                                if (isFirstPageFirstLoad) {

                                    blog_list.add(blogPostFromDB);

                                } else {

                                    blog_list.add(0, blogPostFromDB);

                                }
                                blogRecyclerAdapter.notifyDataSetChanged(); //this to monitoring any change happen to list
                            }
                        }

                    }
                    isFirstPageFirstLoad = false;
                }

            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePage() {

        Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        //snapshot help us to retrieve the data in realTime with order by last to old pots
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    // Get the last visible document
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    //for loop to check for document changes
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String BlogpostID = doc.getDocument().getId();

                            BlogPost blogPostFromDB = doc.getDocument().toObject(BlogPost.class).withID(BlogpostID);
                            blog_list.add(blogPostFromDB);

                            blogRecyclerAdapter.notifyDataSetChanged(); //this to monitoring any change happen to list
                        }
                    }
                } //else {
                  //  Log.d(TAG, "Error Message Found ****************:" + e.getMessage());
                //}
            }

        });
    }

}
