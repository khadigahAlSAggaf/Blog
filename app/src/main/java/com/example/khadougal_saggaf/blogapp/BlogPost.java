package com.example.khadougal_saggaf.blogapp;


import android.provider.ContactsContract;

import java.util.Date;

public class BlogPost extends BlogpostID {

    public String userId;
    public String image_uri;
    public String desc;
    public Date timeStamp;
    public String image_thumb;


    public BlogPost() {
    }


    public BlogPost(String user_id, String image_uri, String desc, Date timeStamp,String image_thumb) {
        this.userId = user_id;
        this.image_uri = image_uri;
        this.desc = desc;
        this.timeStamp = timeStamp;
        this.image_thumb=image_thumb;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public String getUser_id() {
        return userId;
    }


    public void setUser_id(String user_id) {
        this.userId = user_id;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}

