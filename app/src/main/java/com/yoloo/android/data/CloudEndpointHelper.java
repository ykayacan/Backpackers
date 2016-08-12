package com.yoloo.android.data;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import com.yoloo.android.Constants;
import com.yoloo.android.backend.modal.yolooApi.YolooApi;

import java.io.IOException;

public final class CloudEndpointHelper {

    private static YolooApi sYolooApi = null;

    private CloudEndpointHelper() {
    }

    public static YolooApi getYolooApi() {
        if (sYolooApi == null) {  // Only do this once
            sYolooApi = new YolooApi.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(Constants.API_BASEURL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                            request.setDisableGZipContent(true);
                            //request.setRequestHeaders(new HttpHeaders().)
                        }
                    })
                    .build();
        }
        return sYolooApi;
    }

    public static YolooApi getYolooApi(String token) {
        if (sYolooApi == null) {  // Only do this once
            sYolooApi = new YolooApi.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(Constants.API_BASEURL)
                    .build();
        }
        return sYolooApi;
    }
}
