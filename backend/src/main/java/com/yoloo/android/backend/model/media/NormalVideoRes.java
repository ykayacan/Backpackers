package com.yoloo.android.backend.model.media;

import com.google.appengine.tools.cloudstorage.GcsFilename;

public class NormalVideoRes {

    private final GcsFilename gcsFileName;

    private String url;

    private NormalVideoRes(GcsFilename gcsFilename) {
        this.gcsFileName = gcsFilename;
    }

    public static NormalVideoRes from(GcsFilename gcsFilename) {
        return new NormalVideoRes(gcsFilename);
    }

    public String getUrl() {
        return "https://storage.googleapis.com/" +
                gcsFileName.getBucketName() + "/" +
                gcsFileName.getObjectName();
    }
}
