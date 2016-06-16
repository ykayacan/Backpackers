package com.yoloo.android.backend.exception;

import com.google.api.server.spi.ServiceException;

import com.yoloo.android.backend.response.Response;

public class InvalidIdException extends ServiceException {
    private static final int CODE = Response.BAD_REQUEST;

    public InvalidIdException(String statusMessage) {
        super(CODE, statusMessage);
    }

    public InvalidIdException(Throwable cause) {
        super(CODE, cause);
    }

    public InvalidIdException(String statusMessage, Throwable cause) {
        super(CODE, statusMessage, cause);
    }
}
