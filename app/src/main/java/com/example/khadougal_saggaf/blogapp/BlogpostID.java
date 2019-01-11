package com.example.khadougal_saggaf.blogapp;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.annotations.NotNull;

public class BlogpostID {
    @Exclude
    public String BlogpostID;

    public <T extends BlogpostID> T withID(@NotNull final String id){
        this.BlogpostID=id;
        return (T)this;
    }
}
