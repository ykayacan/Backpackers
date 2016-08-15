package com.yoloo.android.backend.model.media;

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonSerialize;

import com.yoloo.android.backend.model.media.photo.LowPhotoRes;
import com.yoloo.android.backend.model.media.photo.NormalPhotoRes;
import com.yoloo.android.backend.model.media.photo.OriginalPhotoRes;
import com.yoloo.android.backend.model.media.photo.ThumbPhotoRes;

public class Resolution {

    @JsonProperty("pLow")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private LowPhotoRes lowPhotoRes;

    @JsonProperty("pNormal")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private NormalPhotoRes normalPhotoRes;

    @JsonProperty("pOrig")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private OriginalPhotoRes originalPhotoRes;

    @JsonProperty("pThumb")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private ThumbPhotoRes thumbPhotoRes;

    @JsonProperty("vNormal")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private NormalVideoRes normalVideoRes;

    public Resolution(LowPhotoRes lowPhotoRes, NormalPhotoRes normalPhotoRes,
                      OriginalPhotoRes originalPhotoRes, ThumbPhotoRes thumbPhotoRes) {
        this.lowPhotoRes = lowPhotoRes;
        this.normalPhotoRes = normalPhotoRes;
        this.originalPhotoRes = originalPhotoRes;
        this.thumbPhotoRes = thumbPhotoRes;
    }

    public Resolution(NormalVideoRes normalVideoRes) {
        this.normalVideoRes = normalVideoRes;
    }

    private Resolution() {
    }
}
