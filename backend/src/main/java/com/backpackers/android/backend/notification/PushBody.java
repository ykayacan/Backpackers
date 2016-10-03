package com.backpackers.android.backend.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushBody {

    private String to;
    private List<String> registration_ids;
    private String condition;
    private String priority;
    private Map<String, String> notification = new HashMap<>();
    private Map<String, String> data = new HashMap<>();

    public PushBody addNotification(String key, String value) {
        notification.put(key, value);
        return this;
    }

    public PushBody addData(String key, String value) {
        data.put(key, value);
        return this;
    }

    public PushBody setTo(String to) {
        this.to = to;
        return this;
    }

    public void setRegistration_ids(List<String> registration_ids) {
        this.registration_ids = registration_ids;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
