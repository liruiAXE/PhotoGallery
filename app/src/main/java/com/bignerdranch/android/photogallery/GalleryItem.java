package com.bignerdranch.android.photogallery;

import android.net.Uri;

/**
 * Created by dlw on 2016/8/6.
 */
public class GalleryItem {
    private String title;
    private String id;
    private String url_s;
    private String owner;
    public Uri getPhotoPageUri(){
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build();
    }
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String toString(){
        return title;
    }

    public String getmCaption() {
        return title;
    }

    public void setmCaption(String mCaption) {
        this.title = mCaption;
    }

    public String getmUrl() {
        return url_s;
    }

    public void setmUrl(String mUrl) {
        this.url_s = mUrl;
    }

    public String getmId() {
        return id;
    }

    public void setmId(String mId) {
        this.id = mId;
    }
}
