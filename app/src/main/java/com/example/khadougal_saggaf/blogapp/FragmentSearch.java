package com.example.khadougal_saggaf.blogapp;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 */


public class FragmentSearch extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private EditText search_feild;
    private ImageView search_btn;
    private RecyclerView recyclerView;

    public FragmentSearch() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_search, container, false);

        search_btn = view.findViewById(R.id.search);
        search_feild = view.findViewById(R.id.search_editText);

        recyclerView=view.findViewById(R.id.result_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search=search_feild.getText().toString();
                firebaseUserSearch(search);
            }


        });

        return view;
    }

    private void firebaseUserSearch( String search) {

        final Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("desc").startAt(search).endAt(search+ "\uf8ff");
/*
        //Query firebaseSearchQuery = firebaseFirestore.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<BlogPost, ResultsViewHolder>(firstQuery) {
            @Override
            public void onBindViewHolder(ResultsViewHolder holder, int position, BlogPost model) {
                // Bind the Chat object to the ChatHolder
                // ...


                BlogPost.class,
                ResultsViewHolder.class,
                firstQuery;

            }

            @Override
            public ResultsViewHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                /*View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.message, group, false);*/

              //  return new ResultsViewHolder(view);
          //  }
        //};


    }

    public static class ResultsViewHolder extends RecyclerView.ViewHolder {

        public ResultsViewHolder(@NonNull View itemView) {
            super(itemView);
            View mView;
        }


    }


    }
