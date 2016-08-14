package com.yoloo.android.backend.response;

public class WrappedResponse implements Response {

    private int code;
    private String message;
    private Object item;

    private WrappedResponse() {
    }

    private WrappedResponse(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.item = builder.item;
    }

    public static WrappedResponse.Builder builder() {
        return new WrappedResponse.Builder();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getItem() {
        return item;
    }

    public static final class Builder {
        private int code;
        private String message;
        private Object item;

        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setItem(Object item) {
            this.item = item;
            return this;
        }

        public WrappedResponse build() {
            return new WrappedResponse(this);
        }
    }
}
