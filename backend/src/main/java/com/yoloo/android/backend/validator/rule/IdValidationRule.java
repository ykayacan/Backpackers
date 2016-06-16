package com.yoloo.android.backend.validator.rule;

import com.yoloo.android.backend.exception.InvalidIdException;
import com.yoloo.android.backend.validator.Rule;

public class IdValidationRule implements Rule<InvalidIdException> {

    private final long id;

    public IdValidationRule(final long id) {
        this.id = id;
    }

    @Override
    public void validate() throws InvalidIdException {
        if (this.id <= 0) {
            throw new InvalidIdException(
                    "Given item id " + this.id + " is invalid.");
        }
    }
}
