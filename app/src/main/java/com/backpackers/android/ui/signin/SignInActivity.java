package com.backpackers.android.ui.signin;

import com.backpackers.android.R;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

public class SignInActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ProviderSignInFragment.newInstance(),
                        ProviderSignInFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        final int count = mFragmentManager.getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStack();
        }
    }
}
