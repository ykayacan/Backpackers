package com.backpackers.android.ui.notification;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseNotification;
import com.backpackers.android.framework.base.BaseMvpDataView;

public interface NotificationView extends BaseMvpDataView<CollectionResponseNotification> {
    void onFollowedBack();

    void onAlreadyFollowedError();
}
