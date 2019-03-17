package com.example.khadougal_saggaf.blogapp;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {
    //Attribute
    public List<BlogPost> profile_blog_list;
    public Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;



    //Constructor
    public ProfileRecyclerAdapter(List<BlogPost> blog_list) {
        this.profile_blog_list = blog_list;
    }

    @NonNull
    @Override
    public ProfileRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProfileRecyclerAdapter.ViewHolder holder, int position) {
        final String postID = profile_blog_list.get(position).BlogpostID;
        final String currentUserID = firebaseAuth.getCurrentUser().getUid();

        //retrieve post description in list
        String descData = profile_blog_list.get(position).getDesc();
        holder.setDesc(descData);

        //retrieve image in list
        String image_uri = profile_blog_list.get(position).getImage_uri();
        String thumbUri = profile_blog_list.get(position).getImage_thumb(); //add

        holder.setImage(image_uri, thumbUri); //add



        //Get Like
        firebaseFirestore.collection("Posts/" + postID + "/Likes").document(currentUserID).addSnapshotListener((MainActivity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    //Log.d(TAG, "Error:" + e.getMessage());

                    if (documentSnapshot.exists()) {

                        //holder.setLike();
                        holder.btn_blog_like.setImageResource(R.drawable.ic_favorite_red);

                    } else {
                        holder.btn_blog_like.setImageResource(R.drawable.ic_favorite_gray);

                    }
                } else {
                    Log.d(TAG, "Error:" + e.getMessage());
                }

            }
        });

        //Like Feature
        holder.btn_blog_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + postID + "/Likes").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timeStamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + postID + "/Likes").document(currentUserID).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Posts/" + postID + "/Likes").document(currentUserID).delete();

                        }

                    }
                });
            }
        });


        //Count Like
        firebaseFirestore.collection("Posts/" + postID + "/Likes").addSnapshotListener((MainActivity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                //if (documentReference != null) {
                //Log.d(TAG, "Error:" + e.getMessage());
                if (queryDocumentSnapshots != null) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        int count = queryDocumentSnapshots.size();
                        holder.likeCount(count);

                    } else {
                        holder.likeCount(0);

                    }
                }

            }
        });

        //Count Comment
        firebaseFirestore.collection("Posts/" + postID + "/Comments").addSnapshotListener((MainActivity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                //if (documentReference != null) {
                //Log.d(TAG, "Error:" + e.getMessage());
                if (queryDocumentSnapshots != null) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        int count = queryDocumentSnapshots.size();
                        holder.Countcomment(count);

                    } else {
                        holder.Countcomment(0);

                    }
                }

            }
        });

        //Time Feature***
        if (profile_blog_list.get(position).getTimeStamp() != null) {

            long millisecond = profile_blog_list.get(position).getTimeStamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();

            holder.setTime(dateString);
        }

        //Comment Feature
        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", postID);
                context.startActivity(commentIntent);

            }
        });

        //onClicked post
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "post ID:  " + postID, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(context, PostPage.class);
                intent.putExtra("post_id", postID);
                intent.putExtra("user_id", currentUserID);

                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        if (profile_blog_list != null) {

            return profile_blog_list.size();

        } else {
            return 0;
        }
    }


    // ViewHolder Class
    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView; //to match the view
        public ImageView btn_blog_like;
        private ImageView blogCommentBtn;
        private TextView desc;
        private ImageView blogImageView;
        public TextView like_count;
        private TextView countComments;
        private TextView date;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            //This views holds on activity - have actions -
            btn_blog_like = mView.findViewById(R.id.profile_blog_like);
            blogCommentBtn = mView.findViewById(R.id.profile_blog_comment_icon);

        }


        //retrieve post
        public void setDesc(String DescText) {
            desc = mView.findViewById(R.id.profile_postView_desc);

            if (DescText.length() > 200) {
                String subDescription = DescText.substring(0, 200);
                desc.setText(subDescription + "..........");

            } else {
                desc.setText(DescText);
            }
        }

        //retrieve image using the uri downloader
        private void setImage(String downloaderUri, String thubo) {
            blogImageView = mView.findViewById(R.id.profile_postView_image_post);

            //request optional to assign temporary image appear before while loading the image from db.
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.postlist);

            //Glide.with(context).applyDefaultRequestOptions(requestOptions).load(thubo).into(blogImageView);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloaderUri).thumbnail(
                    Glide.with(context).load(thubo)
            ).into(blogImageView);

        }

        //Like count
        private void likeCount(int countLike) {
            like_count = mView.findViewById(R.id.profile_blog_like_count);
            like_count.setText(countLike + " Likes");
        }

        //Comment counter
        public void Countcomment(int count) {
            countComments = mView.findViewById(R.id.profile_count_commnts);
            countComments.setText(count + "");
        }

        //Time post
        private void setTime(String plogDate) {
            date = mView.findViewById(R.id.plog_date);
            date.setText(plogDate);

        }

    }
}
