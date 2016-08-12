package com.yoloo.android.data.remote;

import com.yoloo.android.backend.modal.yolooApi.YolooApi;
import com.yoloo.android.backend.modal.yolooApi.model.Question;
import com.yoloo.android.data.CloudEndpointHelper;
import com.yoloo.android.data.model.QuestionModel;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class QuestionService {

    public static final MediaType MEDIA_TYPE_IMAGE
            = MediaType.parse(com.google.common.net.MediaType.ANY_IMAGE_TYPE.toString());

    public Observable<Question> create(final QuestionModel model) {
        return Observable.create(new Observable.OnSubscribe<Question>() {
            @Override
            public void call(Subscriber<? super Question> subscriber) {
                YolooApi api = CloudEndpointHelper.getYolooApi();
                final OkHttpClient client = new OkHttpClient();

                Timber.d("BURADA");

                /*Request request = new Request.Builder()
                        .url(Constants.API_BASEURL + "yolooApi/v1/questions")
                        .post(RequestBody.save(MEDIA_TYPE_IMAGE, model.getFile()))
                        .build();*/


                /*try {
                    *//*subscriber.onNext(api.questions().add(
                            "lol,ok,passport",
                            "23.5,45.5667",
                            "Izmir",
                            "Test message",
                            "Test title"
                    ).execute());*//*
                    //Response response = client.newCall(request).execute();
                    //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }*/
            }
        });
    }
}