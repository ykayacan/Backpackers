package com.yoloo.android.backend.model.media.photo;

import com.google.appengine.tools.cloudstorage.GcsFilename;

import com.yoloo.android.backend.model.media.MediaObject;

public class LowPhotoRes extends MediaObject {

    private static final int SIZE = 306;

    private LowPhotoRes(GcsFilename gcsFilename) {
        super(gcsFilename);
    }

    public static LowPhotoRes from(GcsFilename gcsFilename) {
        return new LowPhotoRes(gcsFilename);
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
