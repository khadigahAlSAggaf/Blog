package com.example.khadougal_saggaf.blogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.khadougal_saggaf.blogapp.BlogPost;
import com.example.khadougal_saggaf.blogapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {
    //Attribute
    public List<BlogPost> profile_blog_list;
    public Context context;


    //Constructor
    public ProfileRecyclerAdapter(List<BlogPost> blog_list) {
        this.profile_blog_list = blog_list;
    }

    @NonNull
    @Override
    public ProfileRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_item, parent, false);

        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecyclerAdapter.ViewHolder holder, int position) {

        //retrieve post description in list
        String descData = profile_blog_list.get(position).getDesc();
        holder.setDesc(descData);

        //retrieve image in list
        String image_uri = profile_blog_list.get(position).getImage_uri();
        String thumbUri = profile_blog_list.get(position).getImage_thumb(); //add

        holder.setImage(image_uri, thumbUri); //add

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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            //This views holds on activity - have actions -
            btn_blog_like = mView.findViewById(R.id.blog_like);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);

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

    }
}
