package com.backpackers.android.ui.timeline;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.backpackers.android.framework.base.BaseMvpDataView;

public interface TimelineView extends BaseMvpDataView<CollectionResponseAbstractPost> {

    void onLikeSuccessful();

    void onLikeFailed();

    void onDislikeSuccessful();

    void onDislikeFailed();

}
