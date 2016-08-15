package com.yoloo.android.backend.util;

import com.google.common.net.MediaType;

public class MimeUtil {

    public static boolean isValidMime(String mimeType) {
        MediaType mime = MediaType.parse(mimeType);
        return mime.is(MediaType.ANY_IMAGE_TYPE) || mime.is(MediaType.ANY_VIDEO_TYPE);
    }

    public static boolean isVideo(String mimeType) {
        MediaType mime = MediaType.parse(mimeType);
        return mime.is(MediaType.ANY_VIDEO_TYPE);
    }

    public static boolean isPhoto(String mimeType) {
        MediaType mime = MediaType.parse(mimeType);
        return mime.is(MediaType.ANY_IMAGE_TYPE);
    }
}
