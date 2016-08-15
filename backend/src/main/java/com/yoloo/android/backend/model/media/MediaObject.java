package com.yoloo.android.backend.model.media;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.tools.cloudstorage.GcsFilename;

public abstract class MediaObject {

    private final GcsFilename gcsFileName;

    private final ImagesService is = ImagesServiceFactory.getImagesService();

    protected MediaObject(GcsFilename gcsFileName) {
        this.gcsFileName = gcsFileName;
    }

    @JsonProperty("w")
    public abstract int getWidth();

    @JsonProperty("h")
    public abstract int getHeight();

    public abstract String getUrl();

    protected String fileName(GcsFilename gcsFilename) {
        return "/gs/" + gcsFilename.getBucketName() + "/" + gcsFilename.getObjectName();
    }

    protected String setupUrl(boolean crop, int size) {
        String servingUrl = is.getServingUrl(ServingUrlOptions.Builder
                .withGoogleStorageFileName(fileName(gcsFileName)));

        if (size > 0) {
            servingUrl = servingUrl.concat("=s" + size);
        }

        if (crop) {
            servingUrl = servingUrl.concat("-c");
        }

        return servingUrl;
    }
}
