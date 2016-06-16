package com.yoloo.android.backend;

/**
 * API Keys, Client Ids and Audience Ids for accessing APIs and configuring
 * Cloud Endpoints.
 * When you deploy your solution, you need to use your own API Keys and IDs.
 * Please refer to the documentation for this sample for more details.
 */
public final class Constants {

    /**
     * Google Cloud Messaging API key.
     */
    public static final String GCM_API_KEY = "YOUR-GCM-API-KEY";

    /**
     * Android client ID from Google Cloud console.
     */
    public static final String ANDROID_CLIENT_ID = "369106561777-r0jg5h20eeuhho2gha25e5m2rmf66pd8.apps.googleusercontent.com";

    /**
     * iOS client ID from Google Cloud console.
     */
    public static final String IOS_CLIENT_ID = "YOUR-IOS-CLIENT-ID";

    /**
     * Web client ID from Google Cloud console.
     */
    public static final String WEB_CLIENT_ID = "369106561777-8cqi3ujggnjc723tqgmrm7i1ip5v215q.apps.googleusercontent.com";

    /**
     * Audience ID used to limit access to some client to the API.
     */
    public static final String AUDIENCE_ID = WEB_CLIENT_ID;


    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    /**
     * API package name.
     */
    public static final String API_OWNER =
            "modal.backend.android.yoloo.com";
    /**
     * API package path.
     */
    public static final String API_PACKAGE_PATH = "";

    private Constants() {
    }
}
