package com.backpackers.android.backend.model.media;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.backpackers.android.backend.Config;
import com.backpackers.android.backend.util.MediaUtil;
import com.backpackers.android.backend.util.MimeUtil;

public class MediaObject {

    private Link servingUrl;
    private String mimeType;
    private Resolution resolution;

    private boolean crop;

    private MediaObject() {
    }

    public MediaObject(Link servingUrl, String mimeType, Resolution resolution) {
        this(servingUrl, mimeType, resolution, false);
    }

    public MediaObject(Link servingUrl, String mimeType, Resolution resolution, boolean crop) {
        this.servingUrl = servingUrl;
        this.mimeType = mimeType;
        this.resolution = resolution;
        this.crop = crop;
    }

    @JsonProperty("w")
    public int getWidth() {
        return resolution.getValue();
    }

    @JsonProperty("h")
    public int getHeight() {
        return resolution.getValue();
    }

    public String getUrl() {
        if (MimeUtil.isPhoto(mimeType)) {
            return MediaUtil.getImageUrl(servingUrl, resolution.getValue(), crop);
        } else {
            return servingUrl.getValue();
        }
    }

    public enum Resolution {
        LOW(Config.LOW_RESOLUTION),
        THUMB(Config.THUMB_RESOLUTION),
        STANDARD(Config.STANDARD_RESOLUTION);

        private final int value;

        Resolution(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
