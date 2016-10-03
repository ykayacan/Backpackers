package com.backpackers.android.data.model.upload;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaResponse {

    @SerializedName("items")
    private List<Media> medias;

    public List<Media> getMedias() {
        return medias;
    }
}
