package com.yoloo.android.backend.validator.rule.common;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.android.backend.validator.Rule;

public class IdValidationRule implements Rule<BadRequestException> {

    // Numbers between 1 and 9.
    //private static final String PATTERN = "^[1-9]{1,45}$";

    private final String websafeKey;

    public IdValidationRule(final String websafeKey) {
        this.websafeKey = websafeKey;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(websafeKey)) {
            throw new BadRequestException(
                    "Given item id " + websafeKey + " is invalid.");
        }
        /*if (!Pattern.compile(PATTERN).matcher(String.valueOf(id)).matches()) {
            throw new BadRequestException(
                    "Given item id " + this.id + " is invalid.");
        }*/
    }
}
