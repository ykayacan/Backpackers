package com.backpackers.android.data.repository;

import com.backpackers.android.data.repository.remote.UploadService;

import java.io.File;
import java.util.List;

import okhttp3.Response;
import rx.Observable;

public class UploadRepository {

    private UploadService mService;

    public UploadRepository(UploadService service) {
        mService = service;
    }

    public Observable<Response> upload(final String userId, final List<File> files) {
        return mService.upload(userId, files);
    }
}
