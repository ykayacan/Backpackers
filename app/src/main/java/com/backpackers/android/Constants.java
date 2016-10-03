package com.backpackers.android;

public class Constants {

    private static final String PRODUCTION_URL = "https://backpackers-app.appspot.com/_ah/api/";
    private static final String LOCAL_URL = "http://10.0.2.2:8080/_ah/api/";
    private static final String LOCAL_URL_GENY = "http://10.0.3.2:8080/_ah/api/";
    public static final String API_BASEURL = PRODUCTION_URL;

    public static final String API_UPLOAD_URL = "https://backpackers-app.appspot.com/upload";

    public static final String LOCAL_IMAGE_URL = "http://10.0.2.2:8080/_ah/";

    public static final String BASE64_CLIENT_ID = "NjM0MjE3NTM4Njc5LXNsZ3YwZ29nYjU0dnZlMTVrM3JpY2NjY2YxNmRzcDA2LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29t";

    public static final String PROVIDER_GOOGLE = "GOOGLE";
    public static final String PROVIDER_YOLOO = "YOLOO";
    public static final String PROVIDER_FACEBOOK = "FACEBOOK";

    public static final String PREF_KEY_ACCESS_TOKEN = "accessToken";
    public static final String PREF_KEY_REFRESH_TOKEN = "refreshToken";

    public static final String PREF_KEY_USER_ID = "userId";
    public static final String PREF_KEY_EMAIL = "email";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_PROFILE_IMAGE_URL = "profileImageUrl";
    public static final String PREF_KEY_PROVIDER = "provider";
    public static final String PREF_KEY_FCM_TOKEN = "fcm_token";
    public static final String PREF_KEY_SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    public static final String PREF_IS_BADGE_WELCOME_SHOWN = "KEY_IS_BADGE_WELCOME_SHOWN";
    public static final String PREF_IS_BADGE_LEVEL_1_SHOWN = "KEY_IS_BADGE_LEVEL_1_SHOWN";

    private Constants() {
    }
}
