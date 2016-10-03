package com.backpackers.android.backend.model.media.mediadetail;

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonSerialize;

import com.backpackers.android.backend.model.media.MediaObject;

public class MediaDetail {

    @JsonProperty("thumb")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private MediaObject thumb;

    @JsonProperty("low")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private MediaObject low;

    @JsonProperty("std")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private MediaObject standard;

    private MediaDetail() {
    }

    private MediaDetail(Builder builder) {
        this.low = builder.low;
        this.thumb = builder.thumb;
        this.standard = builder.standard;
    }

    public static MediaDetail.Builder builder() {
        return new MediaDetail.Builder();
    }

    public static final class Builder {
        private MediaObject low;
        private MediaObject thumb;
        private MediaObject standard;

        public Builder setLowImage(MediaObject low) {
            this.low = low;
            return this;
        }

        public Builder setThumbImage(MediaObject thumb) {
            this.thumb = thumb;
            return this;
        }

        public Builder setStandardImage(MediaObject standard) {
            this.standard = standard;
            return this;
        }

        public Builder setStandardVideo(MediaObject standard) {
            this.standard = standard;
            return this;
        }

        public MediaDetail build() {
            return new MediaDetail(this);
        }
    }
}
