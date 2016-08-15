package com.yoloo.android.backend.model.media.photo;

import com.google.appengine.tools.cloudstorage.GcsFilename;

import com.yoloo.android.backend.model.media.MediaObject;

public class OriginalPhotoRes extends MediaObject {

    public OriginalPhotoRes(GcsFilename gcsFileName) {
        super(gcsFileName);
    }

    public static OriginalPhotoRes from(GcsFilename gcsFilename) {
        return new OriginalPhotoRes(gcsFilename);
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public String getUrl() {
        return setupUrl(false, 0);
    }
}
