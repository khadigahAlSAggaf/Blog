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
import com.google.firebase.firestore.DocumentReference;
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
import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    /* Adapter responsible for receiving data from List<> and then show it in recyclerView  */

    public List<BlogPost> blog_list;
    public Context context;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    //String postID;
    //String currentUserID;


    public BlogRecyclerAdapter(List<BlogPost> blog_list) { //contractor
        this.blog_list = blog_list;
    }
    //flowing method require for adapter

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //layout inflater responsible for inflate/hold view --> here connect with customize layout plog_list_item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plog_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        return new ViewHolder(view);
    }

    /* onBindViewHolder responsible for decide what appear post/image Post/image Account ..etc*/
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final String postID = blog_list.get(position).BlogpostID;
        final String currentUserID = firebaseAuth.getCurrentUser().getUid();

        //retrieve post description in list
        String descData = blog_list.get(position).getDesc();
        holder.setDesc(descData);

        //retrieve image in list
        String image_uri = blog_list.get(position).getImage_uri();
        holder.setImage(image_uri);

        //String userID = blog_list.get(position).getUser_id();
        final String user_id = blog_list.get(position).getUser_id();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        String userName = task.getResult().getString("name");
                        String userImage = task.getResult().getString("image");
                        holder.setupUser(userName, userImage);

                    } else {

                        //Firebase Exception
                    }
                }
            });

            //Time Feature***
            long millisecond = blog_list.get(position).getTimeStamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();

            holder.setTime(dateString);

            //Count Like
            firebaseFirestore.collection("Posts/" + postID + "/Likes").addSnapshotListener((MainActivity) context, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    //if (documentReference != null) {
                    //Log.d(TAG, "Error:" + e.getMessage());

                    if (!queryDocumentSnapshots.isEmpty()) {

                        int count = queryDocumentSnapshots.size();
                        holder.likeCount(count);

                    } else {
                        holder.likeCount(0);

                    }
                    //}

                }
            });

            //Count Comment
            firebaseFirestore.collection("Posts/" + postID + "/Comments").addSnapshotListener((MainActivity) context, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    //if (documentReference != null) {
                    //Log.d(TAG, "Error:" + e.getMessage());

                    if (!queryDocumentSnapshots.isEmpty()) {

                        int count = queryDocumentSnapshots.size();
                        holder.Countcomment(count);

                    } else {
                        holder.Countcomment(0);

                    }
                    //}

                }
            });

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

            //Comment Feature
            holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent commentIntent = new Intent(context, CommentsActivity.class);
                    commentIntent.putExtra("blog_post_id", postID);
                    context.startActivity(commentIntent);

                }
            });
        }

        //onClicked post
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "post ID:  " + postID, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(context, PostPage.class);
                intent.putExtra("post_id",postID);
                intent.putExtra("user_id",user_id);

                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    // in holder declare the items that need to retrieve
    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView; //to match the view
        private TextView desc;
        private ImageView blogImageView;
        private TextView userName;
        private CircleImageView userImage;
        private TextView date;
        public ImageView btn_blog_like;
        public TextView like_count;
        private ImageView blogCommentBtn;
        private TextView countComments;


        //this constructor reqiure for our viewHolder
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            mView = itemView;

            btn_blog_like = mView.findViewById(R.id.blog_like);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            //mView.setOnClickListener(this);

        }

        //retrieve post
        public void setDesc(String DescText) {
            desc = mView.findViewById(R.id.postView_desc);
            desc.setText(DescText);
        }

        //retrieve image using the uri downloader
        private void setImage(String downloaderUri) {
            blogImageView = mView.findViewById(R.id.postView_image_post);

            //request optional to assign temporary image appear before while loading the image from db.
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.postlist);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloaderUri).into(blogImageView);
        }

        private void setupUser(String name, String image) {
            userImage = mView.findViewById(R.id.postView_userImage);
            userName = mView.findViewById(R.id.username_postView);

            userName.setText(name);

            //request optional to assign temporary image appear before while loading the image from db.
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.accountlist);

            //Glide used to set call image from db
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(image).into(userImage);
        }

        //time
        private void setTime(String plogDate) {
            date = mView.findViewById(R.id.plog_date);
            date.setText(plogDate);

        }

        //like count
        private void likeCount(int countLike) {
            like_count = mView.findViewById(R.id.blog_like_count);
            like_count.setText(countLike + " Likes");
        }

        //Comment counter
        public void Countcomment(int count) {
            countComments = mView.findViewById(R.id.count_commnts);
            countComments.setText(count + "");
        }


        //@Override
        /*public void onClick(View view) {
            Toast.makeText(context,"here:  "+postID ,Toast.LENGTH_LONG).show();
            //Intent intent = new Intent(context, UserAccount.class);
            //intent.putExtra("postid",postID);
            //context.startActivity(intent);
        }
        */
    }
}
