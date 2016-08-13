package com.yoloo.android.backend.controller;

import com.googlecode.objectify.Key;

import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class LikeManager {

    private static final Logger logger = Logger.getLogger(LikeManager.class.getSimpleName());

    private <T> T getLikeableEntity(Class<T> type, String likeableEntityWebsafeKey) {
        return ofy().load().type(type)
                .filter("__key__ =", Key.create(likeableEntityWebsafeKey)).first().now();
    }

}
