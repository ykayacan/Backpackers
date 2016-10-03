package com.backpackers.android.data.repository.remote;

import com.backpackers.android.Constants;
import com.backpackers.android.util.ServerHelper;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;

public class UploadService {

    private static final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/jpeg");

    public Observable<Response> upload(final String userId, final List<File> files) {
        return Observable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                final MultipartBody.Builder body =
                        new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);

                // For each file add it to the builder.
                for (File file : files) {
                    body.addFormDataPart("file", file.getName(),
                            RequestBody.create(MEDIA_TYPE_IMAGE, file));
                }

                final Request request = new Request.Builder()
                        .url(Constants.API_UPLOAD_URL + "?token=" + userId)
                        .post(body.build())
                        .build();

                return ServerHelper.getOkHttpClient().newCall(request).execute();
            }
        });
    }
}
