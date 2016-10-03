package com.backpackers.android.ui.location;

import com.backpackers.android.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LocationFragment extends Fragment {

    private static final String KEY_IS_SEARCH_MODE = "IS_SEARCH_MODE";

    private boolean mIsSearchMode;

    public LocationFragment() {
        // Required empty public constructor
    }

    public static LocationFragment newInstance(boolean isSearchMode) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_SEARCH_MODE, isSearchMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsSearchMode = getArguments().getBoolean(KEY_IS_SEARCH_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

}
