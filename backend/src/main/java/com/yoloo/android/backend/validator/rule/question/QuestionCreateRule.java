package com.yoloo.android.backend.validator.rule.question;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.android.backend.validator.Rule;

public class QuestionCreateRule implements Rule<BadRequestException> {

    private static final String LAT_LNG_PATTERN =
            "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$";

    private final String content;
    private final String location;
    private final String hashtags;

    public QuestionCreateRule(String content, String hashtags, String locations) {
        this.content = content;
        this.hashtags = hashtags;
        this.location = locations;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(content)) {
            throw new BadRequestException("Question content cannot be empty.");
        }
        if (Strings.isNullOrEmpty(location)) {
            throw new BadRequestException("Question location cannot be empty.");
        }
        if (Strings.isNullOrEmpty(hashtags)) {
            throw new BadRequestException("Question hashtag cannot be empty.");
        }
        /*if (!Pattern.compile(LAT_LNG_PATTERN).matcher(latLng).matches()) {
            throw new BadRequestException("Invalid latitude or longitude value.");
        }*/
    }
}
