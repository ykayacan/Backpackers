package com.backpackers.android.backend.validator.rule.notification;

import com.google.api.server.spi.response.ConflictException;

import com.googlecode.objectify.Key;
import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.validator.Rule;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class DeviceRegistrationConflictRule implements Rule<ConflictException> {

    private final String regId;

    public DeviceRegistrationConflictRule(String regId) {
        this.regId = regId;
    }

    @Override
    public void validate() throws ConflictException {
        final Key<?> key = ofy().load().type(RegistrationRecord.class)
                .filter("regId =", regId)
                .keys().first().now();

        if (key != null) {
            throw new ConflictException("Device " + regId + " already registered, skipping register");
        }
    }
}
