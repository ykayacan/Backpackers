package com.yoloo.android.backend.validator;

import com.google.api.client.util.Preconditions;
import com.google.api.server.spi.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class Validator {

    private static Validator sValidator;

    private List<Rule> rules = new ArrayList<>(0);

    public static Validator get() {
        if (sValidator == null) {
            sValidator = new Validator();
        }
        return sValidator;
    }

    private Validator() {
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public void addRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void validate() throws ServiceException {
        Preconditions.checkNotNull(this.rules, "Rule is empty.");
        for (Rule rule : rules) {
            rule.validate();
        }
    }
}
