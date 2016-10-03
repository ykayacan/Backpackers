package com.backpackers.android.backend.response;

public class WrappedErrorResponse {

    private int code;
    private String message;

    private WrappedErrorResponse() {
    }

    private WrappedErrorResponse(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
    }

    public static WrappedErrorResponse.Builder builder() {
        return new WrappedErrorResponse.Builder();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static final class Builder {
        private int code;
        private String message;

        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public WrappedErrorResponse build() {
            return new WrappedErrorResponse(this);
        }
    }
}
