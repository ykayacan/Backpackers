package com.backpackers.android.backend.model;

import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class RegistrationRecord {

    @Id
    private Long id;

    @Parent
    private Key<Account> userKey;

    @Index
    private String regId;

    private RegistrationRecord() {
    }

    public RegistrationRecord(Key<Account> userKey, String regId) {
        this.userKey = userKey;
        this.regId = regId;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }
}