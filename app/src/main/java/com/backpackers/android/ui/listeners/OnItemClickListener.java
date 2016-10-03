package com.backpackers.android.ui.listeners;

import android.view.View;

public interface OnItemClickListener<T> {

    void onItemClick(View v, T t);
}
