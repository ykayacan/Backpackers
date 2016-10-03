package com.backpackers.android.services;

import com.google.api.client.http.HttpHeaders;
import com.google.firebase.iid.FirebaseInstanceId;

import com.backpackers.android.Constants;
import com.backpackers.android.util.PrefUtils;
import com.backpackers.android.util.ServerHelper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.IOException;

import timber.log.Timber;

public class FcmRegistrationIntentService extends IntentService {

    private static final String TAG = "FcmRegistrationIntentService";

    public FcmRegistrationIntentService() {
        super(TAG);
    }

    public static void start(Context context) {
        final Intent i = new Intent(context, FcmRegistrationIntentService.class);
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Make a call to Instance API
        FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();

        final String token = instanceId.getToken();

        PrefUtils.with(this).edit().putString(Constants.PREF_KEY_FCM_TOKEN, token).apply();
        char[] accessToken = PrefUtils.with(this)
                .getString(Constants.PREF_KEY_ACCESS_TOKEN, "").toCharArray();

        try {
            if (!TextUtils.isEmpty(token)) {
                sendTokenToServer(token, accessToken);
                PrefUtils.with(this).edit()
                        .putBoolean(Constants.PREF_KEY_SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (IOException e) {
            Timber.d(e, "Failed to complete token refresh");
            PrefUtils.with(this).edit()
                    .putBoolean(Constants.PREF_KEY_SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendTokenToServer(String token, char[] accessToken) throws IOException {
        ServerHelper.getYolooApi()
                .registrations()
                .register(token)
                .setRequestHeaders(
                        new HttpHeaders()
                                .setAuthorization("Bearer " + String.valueOf(accessToken)))
                .execute();
    }
}
