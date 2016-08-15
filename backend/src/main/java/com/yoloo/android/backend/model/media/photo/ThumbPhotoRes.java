package com.yoloo.android.backend.model.media.photo;

import com.google.appengine.tools.cloudstorage.GcsFilename;

import com.yoloo.android.backend.model.media.MediaObject;

public class ThumbPhotoRes extends MediaObject {

    private static final int SIZE = 150;

    public ThumbPhotoRes(GcsFilename gcsFileName) {
        super(gcsFileName);
    }

    public static ThumbPhotoRes from(GcsFilename gcsFilename) {
        return new ThumbPhotoRes(gcsFilename);
    }

    @Override
    public int getWidth() {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE;
    }

    @Override
    public String getUrl() {
        return setupUrl(true, SIZE);
    }
}
