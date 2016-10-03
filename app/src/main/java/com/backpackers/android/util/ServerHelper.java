package com.backpackers.android.util;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import com.backpackers.android.Constants;
import com.backpackers.android.backend.modal.yolooApi.YolooApi;

import okhttp3.OkHttpClient;

public final class ServerHelper {

    private static YolooApi sYolooApi = null;
    private static OkHttpClient sOkHttpClient = null;

    private ServerHelper() {
    }

    public static YolooApi getYolooApi() {
        if (sYolooApi == null) {  // Only do this once
            sYolooApi = new YolooApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(Constants.API_BASEURL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> request) {
                            // only enable GZip when connecting to remove server
                            final boolean enableGZip = Constants.API_BASEURL.startsWith("https:");
                            if (!enableGZip) {
                                request.setDisableGZipContent(true);
                            }
                        }
                    })
                    .setApplicationName("Backpackers")
                    .build();
        }
        return sYolooApi;
    }

    public static OkHttpClient getOkHttpClient() {
        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient();
        }
        return sOkHttpClient;
    }
}
