package com.yoloo.android.util;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import com.yoloo.android.Constants;
import com.yoloo.android.backend.modal.yolooApi.YolooApi;

import java.io.IOException;

public final class ServerHelper {

    private static YolooApi sYolooApi = null;

    private ServerHelper() {
    }

    public static YolooApi getYolooApi() {
        if (sYolooApi == null) {  // Only do this once
            sYolooApi = new YolooApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(Constants.API_BASEURL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                            // only enable GZip when connecting to remote server
                            final boolean enableGZip = Constants.API_BASEURL.startsWith("https:");
                            if (!enableGZip) {
                                request.setDisableGZipContent(true);
                            }
                        }
                    })
                    .setApplicationName("Yoloo")
                    .build();
        }
        return sYolooApi;
    }
}
