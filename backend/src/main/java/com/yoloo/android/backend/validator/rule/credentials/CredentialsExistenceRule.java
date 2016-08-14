package com.yoloo.android.backend.validator.rule.credentials;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;

import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.validator.Rule;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class CredentialsExistenceRule implements Rule<ServiceException> {

    private final String[] values;

    public CredentialsExistenceRule(String[] values) {
        this.values = values;
    }

    @Override
    public void validate() throws ServiceException {
        if (values.length != 3) {
            throw new BadRequestException("Invalid credentials.");
        }

        if (isUserExists()) {
            throw new ConflictException("Username or email is already taken.");
        }
    }

    private boolean isUserExists() {
        return ofy().load().type(Account.class)
                .filter("email =", values[2])
                .keys().first().now() != null;
    }
}
