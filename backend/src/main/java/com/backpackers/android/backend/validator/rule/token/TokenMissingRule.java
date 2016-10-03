package com.backpackers.android.backend.validator.rule.token;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.backpackers.android.backend.oauth2.GrantType;
import com.backpackers.android.backend.validator.Rule;

public class TokenMissingRule implements Rule<BadRequestException> {

    private final String token;
    private final GrantType type;

    public TokenMissingRule(String token, GrantType type) {
        this.token = token;
        this.type = type;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(token)) {
            throw new BadRequestException("Parameters missing from the request: " +
                    (type.toString().equals("password") ? "access_token" : "refresh_token"));
        }
    }
}
