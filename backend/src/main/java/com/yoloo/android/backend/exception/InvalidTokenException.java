package com.yoloo.android.backend.exception;

import com.google.api.server.spi.ServiceException;

import com.yoloo.android.backend.response.Response;

public class InvalidTokenException extends ServiceException {
    private static final int CODE = Response.UNAUTHORIZED;
    private static final String MESSAGE = "Token is invalid.";

    public InvalidTokenException() {
        super(CODE, MESSAGE);
    }

    public InvalidTokenException(Throwable cause) {
        super(CODE, cause);
    }
}
