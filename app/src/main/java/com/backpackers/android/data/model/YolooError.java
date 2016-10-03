package com.backpackers.android.data.model;

import java.util.List;

public class YolooError {

    private int code;
    private List<Error> errors;
    private String message;

    public int getCode() {
        return code;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public String getMessage() {
        return message;
    }
}
