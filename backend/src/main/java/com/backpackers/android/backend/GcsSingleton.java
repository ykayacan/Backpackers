package com.backpackers.android.backend;

import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

public class GcsSingleton {

    private static GcsService gcsService = null;

    public static GcsService getGcsService() {
        if (gcsService == null) {
            gcsService = GcsServiceFactory
                    .createGcsService(new RetryParams.Builder()
                            .initialRetryDelayMillis(10)
                            .retryMaxAttempts(10)
                            .totalRetryPeriodMillis(15000)
                            .build());
        }
        return gcsService;
    }
}
