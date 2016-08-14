package com.yoloo.android.backend.response;

public final class WrappedString {

    private String uploadToken;

    private WrappedString() {
    }

    public WrappedString(String uploadToken) {
        this.uploadToken = uploadToken;
    }

    public String getUploadToken() {
        return uploadToken;
    }
}
