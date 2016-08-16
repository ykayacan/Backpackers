package com.yoloo.android.backend.model.media.photo;

import com.google.appengine.tools.cloudstorage.GcsFilename;

import com.yoloo.android.backend.model.media.MediaObject;

public class NormalPhotoRes extends MediaObject {

    private static final int SIZE = 650;

    public NormalPhotoRes(GcsFilename gcsFileName) {
        super(gcsFileName);
    }

    public static NormalPhotoRes from(GcsFilename gcsFilename) {
        return new NormalPhotoRes(gcsFilename);
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
        return setupUrl(false, SIZE);
    }
}
