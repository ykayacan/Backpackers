package com.yoloo.android.backend.validator.rule;

import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.android.backend.request.FeedRequest;
import com.yoloo.android.backend.validator.Rule;

public class FeedRequestRule implements Rule<BadRequestException> {

    private final FeedRequest request;

    public FeedRequestRule(FeedRequest request) {
        this.request = request;
    }

    @Override
    public void validate() throws BadRequestException {
        this.request.validate();
    }
}
