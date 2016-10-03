package com.backpackers.android.ui.profile;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.framework.base.BaseMvpView;

public interface ProfileView extends BaseMvpView {

    void onProfile(Account account);

    void onUserFollowed();

    void onUserUnFollowed();

    void onShowUploadNotification();

    void onDismissUploadNotification();
}
