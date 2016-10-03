package com.backpackers.android.backend.util;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;

import com.backpackers.android.backend.Config;
import com.backpackers.android.backend.model.media.Metadata;

public class MediaUtil {

    private static final ImagesService is = ImagesServiceFactory.getImagesService();

    public static String getImageUrl(final Link servingUrl, final int value, final boolean crop) {
        String newUrl = servingUrl.getValue();
        if (value > 0) {
            newUrl = newUrl.concat("=s" + value);
        }

        if (crop) {
            newUrl = newUrl.concat("-c");
        }

        return newUrl;
    }

    public static Link getServingUrl(final Metadata metadata) {
        final String mimeType = metadata.getMimeType();
        if (MimeUtil.isPhoto(mimeType)) {
            return new Link(is.getServingUrl(ServingUrlOptions.Builder
                    .withGoogleStorageFileName("/gs/" + metadata.getBucketName() +
                            "/" + metadata.getObjectName())));
        } else if (MimeUtil.isVideo(mimeType)) {
            return new Link("https://storage.googleapis.com/" +
                    metadata.getBucketName() + "/" +
                    metadata.getObjectName());
        } else {
            throw new IllegalArgumentException("Unsupported mime type.");
        }
    }

    public static GcsFilename createGcsFilename(final String mimeType, final String websafeUserId) {
        if (MimeUtil.isPhoto(mimeType)) {
            return new GcsFilename(Config.MEDIA_BUCKET + "/" + websafeUserId,
                    RandomGenerator.INSTANCE.generate() + "." + extractExtension(mimeType));
        } else if (MimeUtil.isVideo(mimeType)) {
            return new GcsFilename(Config.MEDIA_BUCKET + "/" + websafeUserId,
                    RandomGenerator.INSTANCE.generate() + "." + extractExtension(mimeType));
        } else {
            throw new IllegalArgumentException("Unsupported mime type.");
        }
    }

    public static String extractExtension(String mimeType) {
        return mimeType.substring(mimeType.indexOf("/") + 1);
    }
}
