package com.yoloo.android.ui.signup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.yoloo.android.R;
import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.repository.AccountRepository;
import com.yoloo.android.data.repository.TokenRepository;
import com.yoloo.android.data.repository.remote.TokenService;
import com.yoloo.android.data.repository.remote.UserService;
import com.yoloo.android.util.Prefs;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class YolooSignUpActivity extends MvpActivity<YolooSignUpView, YolooSignUpPresenter>
        implements YolooSignUpView {

    @BindView(R.id.signUpEmailEt)
    TextInputEditText mEmailEt;

    @BindView(R.id.signUpEmailTil)
    TextInputLayout mEmailTil;

    @BindView(R.id.signUpUsernameEt)
    TextInputEditText mUsernameEt;

    @BindView(R.id.signUpUsernameTil)
    TextInputLayout mUsernameTil;

    @BindView(R.id.signUpPasswordEt)
    TextInputEditText mPasswordEt;

    @BindView(R.id.signUpPasswordTil)
    TextInputLayout mPasswordTil;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rootViewCL)
    ConstraintLayout mRootView;

    @BindInt(android.R.integer.config_shortAnimTime)
    int mShortAnimTime;

    @BindViews({
            R.id.signUpProgressPb,
            R.id.signUpEmailTil,
            R.id.signUpUsernameTil,
            R.id.signUpPasswordTil,
            R.id.signUpBtn,
    })
    View[] mViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoloo_sign_up);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPasswordEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.signUpUsernameEt || id == EditorInfo.IME_NULL) {
                    attemptSignUp();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public YolooSignUpPresenter createPresenter() {
        return new YolooSignUpPresenter(
                new AccountRepository(new UserService()),
                new TokenRepository(new TokenService()));
    }

    @Override
    public void onShowProgressDialog(boolean show) {
        showProgress(show);
    }

    @Override
    public void onNavigateToHome() {
        Timber.d("Navigating to home.");
    }

    @Override
    public void onSaveUser(Account account) {
        Prefs.with(this, "user")
                .edit()
                .putString("id", account.getId())
                .putString("email", account.getEmail())
                .putString("usename", account.getUsername())
                .putString("profileImageUrl", account.getProfileImageUrl())
                .putString("provider", account.getProvider())
                .apply();
    }

    @Override
    public void onSaveToken(Token token) {
        Prefs.with(this, "token")
                .edit()
                .putString("accessToken", token.getAccessToken())
                .putString("refreshToken", token.getRefreshToken())
                .apply();
    }

    @Override
    public void onEmailExistsError() {
        Snackbar.make(mRootView, R.string.error_email_already_taken, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onUsernameExistsError() {
        Snackbar.make(mRootView, R.string.error_username_already_taken, Snackbar.LENGTH_SHORT)
                .show();
    }

    /**
     * Attempts to sign in or signUp the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @OnClick(R.id.signUpBtn)
    void attemptSignUp() {
        // Reset errors.
        mEmailTil.setError(null);
        mUsernameTil.setError(null);
        mPasswordTil.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailEt.getText().toString();
        String username = mUsernameEt.getText().toString();
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

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameTil.setError(getString(R.string.error_field_required));
            focusView = mUsernameTil;
            cancel = true;
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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            getPresenter().signUp(username, email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the signUp form.
     */
    private void showProgress(final boolean show) {
        ButterKnife.apply(mViews, new ButterKnife.Action<View>() {
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
}

