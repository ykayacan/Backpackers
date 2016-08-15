package com.yoloo.android.backend.model;

import com.google.appengine.api.datastore.Link;

public class Image {

    private int width;

    private int height;

    private Link url;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Link getUrl() {
        return url;
    }

    public void setUrl(Link url) {
        this.url = url;
    }
}
