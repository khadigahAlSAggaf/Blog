package com.example.khadougal_saggaf.blogapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;

    public String userID;

    private FirebaseFirestore firebaseFirestore;

    public CommentsRecyclerAdapter(List<Comments> commentsList) {

        this.commentsList = commentsList;

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

        // Retrieve username/user image
        userID = commentsList.get(position).getUser_id();

        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.setAccountProfile(userName, userImage);

                } else {

                    //Firebase Exception
                }
            }
        });


        //retrieve date of comment
        if (commentsList.get(position).getTimestamp() != null) {

            long millisecond = commentsList.get(position).getTimestamp().getTime();

            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();

            holder.recive_date(dateString);
        }

    }

    @Override
    public int getItemCount() {
        if (commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        private TextView comment_message;
        private TextView username;
        private CircleImageView accout_image;
        private TextView comment_date;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message) {

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setAccountProfile(String name, String image) {
            username = mView.findViewById(R.id.comment_username);
            accout_image = mView.findViewById(R.id.comment_image);

            username.setText(name);

            //request optional to assign temporary image appear before while loading the image from db.
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.accountlist);

            //Glide used to set call image from db
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(image).into(accout_image);


        }

        public void recive_date(String date) {
            comment_date = mView.findViewById(R.id.comment_date);
            comment_date.setText(date);
        }


    }
}
