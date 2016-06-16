package com.yoloo.android.backend.response;

public class Response {

    /**
     * Response to a successful GET, PUT, PATCH or DELETE.
     * Can also be used for a POST that doesn't result in a creation.
     */
    public static final int OK = 200;

    /**
     * Response to a POST that results in a creation.
     * Should be combined with a Location header pointing to the location of the new resource.
     */
    public static final int CREATED = 201;

    /**
     * Response to a successful request that won't be returning a body (like a DELETE request).
     */
    public static final int NO_CONTENT = 204;

    /**
     * The request is malformed, such as if the body does not parse.
     */
    public static final int BAD_REQUEST = 400;

    /**
     * When no or invalid authentication details are provided.
     * Also useful to trigger an auth popup if the API is used from a browser.
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * When a non-existent resource is requested.
     */
    public static final int NOT_FOUND = 404;
}
