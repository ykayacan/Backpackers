package com.yoloo.android.backend.response;

public final class WrappedString {

    private String uploadTicket;

    private WrappedString() {
    }

    public WrappedString(String uploadTicket) {
        this.uploadTicket = uploadTicket;
    }

    public String getUploadTicket() {
        return uploadTicket;
    }
}
