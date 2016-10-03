package com.backpackers.android.ui.location;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;

import com.backpackers.android.R;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.search.adapter.LocationAdapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class LocationActivity extends AppCompatActivity implements
        OnItemClickListener<PlaceLikelihood> {

    public static final String EXTRA_LOCATION_NAME = "EXTRA_LOCATION_NAME";
    public static final String EXTRA_LATITUDE = "EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE = "EXTRA_LONGITUDE";

    @BindView(R.id.list_location)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.layout_open_location)
    LinearLayout mOpenLocationSettingsLayout;

    private boolean mFirstTime = true;

    private LocationAdapter mAdapter;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Awareness.API)
                .build();

        setupToolbar();

        if (isLocationEnabled()) {
            mOpenLocationSettingsLayout.setVisibility(View.GONE);
        } else {
            mOpenLocationSettingsLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

        setupRecyclerView();

        getNearbyPlaces();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirstTime) {
            mFirstTime = false;
        } else {
            if (isLocationEnabled()) {
                mOpenLocationSettingsLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);

                getNearbyPlaces();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.text_location_turn_on)
    void openLocationSetttings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // Display back arrow
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setItemAnimator(animator);

        mAdapter = new LocationAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getNearbyPlaces() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Awareness.SnapshotApi.getPlaces(mGoogleApiClient)
                .setResultCallback(new ResultCallback<PlacesResult>() {
                    @Override
                    public void onResult(@NonNull PlacesResult placesResult) {
                        if (!placesResult.getStatus().isSuccess()) {
                            Timber.e("Could not get places. %s", placesResult.getStatus().getStatusMessage());
                            return;
                        }

                        List<PlaceLikelihood> placeLikelihoods = placesResult.getPlaceLikelihoods();

                        for (PlaceLikelihood p : placeLikelihoods) {
                            Timber.d("Place name: %s", p.getPlace().getName());
                        }

                        mAdapter.setItems(placeLikelihoods);
                    }
                });
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return !(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

    }

    @Override
    public void onItemClick(View v, PlaceLikelihood placeLikelihood) {
        final Intent i = new Intent();

        i.putExtra(EXTRA_LOCATION_NAME, placeLikelihood.getPlace().getName());
        i.putExtra(EXTRA_LATITUDE, placeLikelihood.getPlace().getLatLng().latitude);
        i.putExtra(EXTRA_LONGITUDE, placeLikelihood.getPlace().getLatLng().longitude);

        setResult(RESULT_OK, i);

        finish();
    }
}
