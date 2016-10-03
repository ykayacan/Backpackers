package com.backpackers.android.data.model.upload;

public class Media {

    private String id;
    private String mime;
    private String length;
    private MediaDetail detail;

    public String getId() {
        return id;
    }

    public String getMime() {
        return mime;
    }

    public String getLength() {
        return length;
    }

    public MediaDetail getDetail() {
        return detail;
    }
}
