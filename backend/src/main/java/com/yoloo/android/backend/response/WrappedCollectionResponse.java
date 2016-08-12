package com.yoloo.android.backend.response;

import com.google.api.server.spi.response.CollectionResponse;

import java.util.Collection;

public class WrappedCollectionResponse<T> extends CollectionResponse<T> implements Response {

    private final int statusCode;
    private final Status status;
    private final String message;

    private WrappedCollectionResponse(Builder<T> builder) {
        super(builder.items, builder.nextPageToken);
        this.statusCode = builder.statusCode;
        this.status = builder.status;
        this.message = builder.message;
    }

    public static <T> WrappedCollectionResponse.Builder<T> builder() {
        return new WrappedCollectionResponse.Builder<>();
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    public static class Builder<T> extends CollectionResponse.Builder<T> {
        private int statusCode;
        private Status status;
        private String message;
        private Collection<T> items;
        private String nextPageToken;

        public WrappedCollectionResponse.Builder<T> setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public WrappedCollectionResponse.Builder<T> setStatus(Status status) {
            this.status = status;
            return this;
        }

        public WrappedCollectionResponse.Builder<T> setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public WrappedCollectionResponse.Builder<T> setItems(Collection<T> items) {
            this.items = items;
            return this;
        }

        @Override
        public WrappedCollectionResponse.Builder<T> setNextPageToken(String nextPageToken) {
            this.nextPageToken = nextPageToken;
            return this;
        }

        public WrappedCollectionResponse<T> build() {
            return new WrappedCollectionResponse<>(this);
        }
    }
}
