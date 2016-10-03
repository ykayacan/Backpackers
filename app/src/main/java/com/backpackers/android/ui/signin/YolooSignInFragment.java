package com.backpackers.android.ui.signin;

import com.backpackers.android.Constants;
import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.data.repository.TokenRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.data.repository.remote.TokenService;
import com.backpackers.android.data.repository.remote.UserService;
import com.backpackers.android.framework.base.BaseMvpFragment;
import com.backpackers.android.ui.home.HomeActivity;
import com.backpackers.android.util.KeyboardUtils;
import com.backpackers.android.util.PrefUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class YolooSignInFragment extends BaseMvpFragment<SignInView, SignInPresenter> implements
        SignInView {

    public static final int SIGN_IN = 1;
    public static final int SIGN_UP = 2;

    private static final String BUNDLE_MODE = "mode";

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.edit_email)
    TextInputEditText mEmailEt;

    @BindView(R.id.layout_edit_email)
    TextInputLayout mEmailTil;

    @BindView(R.id.edit_username)
    TextInputEditText mUsernameEt;

    @BindView(R.id.layout_edit_username)
    TextInputLayout mUsernameTil;

    @BindView(R.id.edit_password)
    TextInputEditText mPasswordEt;

    @BindView(R.id.layout_edit_password)
    TextInputLayout mPasswordTil;

    @BindView(R.id.content_root)
    CoordinatorLayout mRootView;

    @BindView(R.id.btn_sign_in_or_sign_up)
    Button mSingInOrSignUpBtn;

    @BindInt(android.R.integer.config_shortAnimTime)
    int mShortAnimTime;

    @BindViews({
            R.id.pb_loading,
            R.id.layout_edit_email,
            R.id.layout_edit_username,
            R.id.layout_edit_password,
            R.id.btn_sign_in_or_sign_up
    })
    View[] mSignUpViews;

    @BindViews({
            R.id.pb_loading,
            R.id.layout_edit_email,
            R.id.layout_edit_password,
            R.id.btn_sign_in_or_sign_up,
    })
    View[] mSignInViews;

    private Unbinder mUnbinder;

    private int mMode;

    public YolooSignInFragment() {
        // Required empty public constructor
    }

    public static YolooSignInFragment newInstance(@IntRange(from = 1, to = 2) int mode) {
        final YolooSignInFragment fragment = new YolooSignInFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_MODE, mode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMode = getArguments().getInt(BUNDLE_MODE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_yoloo_sign_in, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        setupToolbar();

        int stringRes = mMode == SIGN_IN ? R.string.action_sign_in : R.string.action_sign_up;

        mPasswordEt.setImeActionLabel(getString(stringRes), EditorInfo.IME_ACTION_DONE);

        mPasswordEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || EditorInfo.IME_ACTION_UNSPECIFIED == id) {
                    attemptSignIn();
                    return true;
                }
                return false;
            }
        });

        if (mMode == SIGN_IN) {
            mUsernameTil.setVisibility(View.GONE);
        }

        mSingInOrSignUpBtn.setText(getString(stringRes));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public SignInPresenter createPresenter() {
        return new SignInPresenter(
                new UserRepository(new UserService()),
                new TokenRepository(new TokenService()));
    }

    @Override
    public void onShowProgress(boolean show, boolean isSignIn) {
        if (isSignIn) {
            showSignInProgress(show);
        } else {
            showSignUpProgress(show);
        }
    }

    @Override
    public void onEmailExistsError(@AuthProvider int provider) {
        Snackbar.make(mRootView, R.string.error_email_already_taken, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onUsernameExistsError() {
        Snackbar.make(mRootView, R.string.error_username_already_taken, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onNetworkError() {
        Snackbar.make(mRootView, R.string.error_network_not_found, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUnauthorized() {
        PrefUtils.with(getContext()).edit().clear().apply();
        Snackbar.make(mRootView, R.string.error_user_not_found, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onInvalidEmail() {
        Snackbar.make(mRootView, R.string.error_invalid_email, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onInvalidPassword() {
        Snackbar.make(mRootView, R.string.error_invalid_password, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveUser(Account account) {
        PrefUtils.with(getContext()).edit()
                .putString(Constants.PREF_KEY_USER_ID, account.getId())
                .putString(Constants.PREF_KEY_EMAIL, account.getEmail())
                .putString(Constants.PREF_KEY_USERNAME, account.getUsername())
                .putString(Constants.PREF_KEY_PROFILE_IMAGE_URL, account.getProfileImageUrl())
                .putString(Constants.PREF_KEY_PROVIDER, account.getProvider())
                .apply();
    }

    @Override
    public void onSaveToken(Token token) {
        PrefUtils.with(getContext()).edit()
                .putString(Constants.PREF_KEY_ACCESS_TOKEN, token.getAccessToken())
                .putString(Constants.PREF_KEY_REFRESH_TOKEN, token.getRefreshToken())
                .apply();
    }

    @Override
    public void onSuccess() {
        HomeActivity.startHomeActivity(getActivity());
    }

    /**
     * Attempts to sign in or signUp the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @OnClick(R.id.btn_sign_in_or_sign_up)
    void attemptSignIn() {
        // Reset errors.
        mEmailTil.setError(null);
        if (mMode == SIGN_UP) {
            mUsernameTil.setError(null);
        }
        mPasswordTil.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailEt.getText().toString();
        String username = null;
        if (mMode == SIGN_UP) {
            username = mUsernameEt.getText().toString();
        }
        String password = mPasswordEt.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailTil.setError(getString(R.string.error_field_required));
            focusView = mEmailTil;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailTil.setError(getString(R.string.error_invalid_email));
            focusView = mEmailTil;
            cancel = true;
        }

        if (mMode == SIGN_UP) {
            // Check for a valid username.
            if (TextUtils.isEmpty(username)) {
                mUsernameTil.setError(getString(R.string.error_field_required));
                focusView = mUsernameTil;
                cancel = true;
            }
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordTil.setError(getString(R.string.error_field_required));
            focusView = mPasswordTil;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordTil.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordTil;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            KeyboardUtils.hideKeyboard(getActivity(), mPasswordEt);

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (mMode == SIGN_IN) {
                getPresenter().signIn(email, password);
            } else {
                // TODO: 26.09.2016 Fix locale
                getPresenter().signUp(username, email, password,
                        Locale.getDefault().getDisplayLanguage());
            }
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the signUp form.
     */
    private void showSignUpProgress(final boolean show) {
        ButterKnife.apply(mSignUpViews, new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull final View view, int index) {
                if (view instanceof ProgressBar) {
                    view.setVisibility(show ? View.VISIBLE : View.GONE);
                    view.animate().setDuration(mShortAnimTime).alpha(
                            show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
                } else {
                    view.setVisibility(show ? View.GONE : View.VISIBLE);
                    view.animate().setDuration(mShortAnimTime).alpha(
                            show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    /**
     * Shows the progress UI and hides the signUp form.
     */
    private void showSignInProgress(final boolean show) {
        ButterKnife.apply(mSignInViews, new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull final View view, int index) {
                if (view instanceof ProgressBar) {
                    view.setVisibility(show ? View.VISIBLE : View.GONE);
                    view.animate().setDuration(mShortAnimTime).alpha(
                            show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
                } else {
                    view.setVisibility(show ? View.GONE : View.VISIBLE);
                    view.animate().setDuration(mShortAnimTime).alpha(
                            show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            // Display back arrow
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(mMode == SIGN_IN
                    ? R.string.title_activity_sign_in
                    : R.string.title_activity_sign_up);
        }
    }
}
