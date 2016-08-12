package com.yoloo.android.backend.validator.rule.common;

import com.google.api.server.spi.response.NotFoundException;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.validator.Rule;

import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class NotFoundRule implements Rule<NotFoundException> {

    private static final Logger logger = Logger.getLogger(NotFoundRule.class.getSimpleName());

    private final Class<?> type;
    private final String websafeKey;

    public NotFoundRule(final Class<?> type, final String websafeKey) {
        this.type = type;
        this.websafeKey = websafeKey;
    }

    @Override
    public void validate() throws NotFoundException {
        final Key<?> key = ofy().load().type(type)
                .filter("__key__ =", Key.create(websafeKey))
                .keys().first().now();

        if (key == null) {
            throw new NotFoundException("Could not find " +
                    type.getSimpleName().toLowerCase() + " with ID: " + websafeKey);
        }
    }
}
