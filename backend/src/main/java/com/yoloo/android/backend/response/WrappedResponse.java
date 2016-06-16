package com.yoloo.android.backend.response;

import com.google.api.server.spi.response.CollectionResponse;

import java.util.Collection;

public class WrappedResponse<T> extends CollectionResponse<T> {
    private final int status;
    private final String message;

    public static <T> WrappedResponse.Builder<T> builder() {
        return new WrappedResponse.Builder<>();
    }

    private WrappedResponse(int status, String message, Collection<T> items, String nextPageToken) {
        super(items, nextPageToken);
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public static class Builder<T> extends CollectionResponse.Builder<T> {
        private int status;
        private String message;
        private Collection<T> items;
        private String nextPageToken;

        public WrappedResponse.Builder<T> setStatus(int status) {
            this.status = status;
            return this;
        }

        public WrappedResponse.Builder<T> setMessage(String message) {
            this.message = message;
            return this;
        }

        public WrappedResponse.Builder<T> setItems(Collection<T> items) {
            this.items = items;
            return this;
        }

        public WrappedResponse.Builder<T> setNextPageToken(String nextPageToken) {
            this.nextPageToken = nextPageToken;
            return this;
        }

        public WrappedResponse<T> build() {
            return new WrappedResponse<>(this.status, this.message, this.items, this.nextPageToken);
        }
    }
}
