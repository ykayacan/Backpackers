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
    public static final String GCM_API_KEY = "AIzaSyCectOS_wSvb9rIsnLMfJH9F25yXrIfrJo";

    /**
     * Android client ID from Google Cloud console.
     */
    public static final String ANDROID_CLIENT_ID = "156968448687-2nqm11mo5b99jfaimqboltte4h1dd94o.apps.googleusercontent.com";

    /**
     * iOS client ID from Google Cloud console.
     */
    public static final String IOS_CLIENT_ID = "YOUR-IOS-CLIENT-ID";

    /**
     * Web client ID from Google Cloud console.
     */
    public static final String WEB_CLIENT_ID = "156968448687-8pamascp1u8kht9qrhonuq47kndst6k8.apps.googleusercontent.com";

    public static final String BASE64_CLIENT_ID = "MTU2OTY4NDQ4Njg3LThwYW1hc2NwMXU4a2h0OXFyaG9udXE0N2tuZHN0Nms4LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29t";

    /**
     * Audience ID used to limit access to some client to the API.
     */
    public static final String AUDIENCE_ID = WEB_CLIENT_ID;


    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    /**
     * API package name.
     */
    public static final String API_OWNER = "modal.backend.android.yoloo.com";

    /**
     * API package path.
     */
    public static final String API_PACKAGE_PATH = "";

    public static final long TOKEN_EXPIRES_IN = 86400; // 24 hours in seconds.

    private Constants() {
    }
}
