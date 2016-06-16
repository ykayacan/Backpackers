package com.yoloo.android.backend.exception;

import com.google.api.server.spi.ServiceException;

import com.yoloo.android.backend.response.Response;

public class AlreadyFoundException extends ServiceException {
    private static final int CODE = Response.BAD_REQUEST;

    public AlreadyFoundException(String message) {
        super(CODE, message);
    }

    public AlreadyFoundException(Throwable cause) {
        super(CODE, cause);
    }

    public AlreadyFoundException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}
