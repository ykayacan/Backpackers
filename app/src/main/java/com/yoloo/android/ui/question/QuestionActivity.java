package com.yoloo.android.ui.question;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.yalantis.ucrop.UCrop;
import com.yoloo.android.R;
import com.yoloo.android.data.model.QuestionModel;
import com.yoloo.android.data.remote.QuestionService;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class QuestionActivity extends MvpActivity<QuestionView, QuestionPresenter>
        implements QuestionView, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "QuestionActivity";

    // Runtime permissions
    protected static final int RC_STORAGE_READ_ACCESS_PERM = 101;

    // Default image quality for JPEG image format
    private static final int COMPRESSION_QUALITY = 90;

    /**
     * Request code for the autocomplete activity. This will be used to identify results from the
     * autocomplete activity in onActivityResult.
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int REQUEST_SELECT_PICTURE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;

    // New cropped image name
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "cropped_image.jpg";

    private Uri mUri = null;

    @BindView(R.id.send)
    Button mSendButton;

    @BindView(R.id.preview)
    ImageView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        ButterKnife.bind(this);
    }

    @NonNull
    @Override
    public QuestionPresenter createPresenter() {
        return new QuestionPresenter(new QuestionService());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                if (uri != null) {
                    startCropActivity(uri);
                } else {
                    Toast.makeText(this, R.string.toast_cannot_retrieve_selected_image,
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                handleCropError(data);
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            handleCropResult(data);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            /*ThumbView thumbView = new ThumbView(getApplicationContext());
            thumbView.setImage(imageBitmap);*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @OnClick(R.id.pick)
    void addImage() {
        Log.i(TAG, "addImage: ");
        pickFromGalleryTask();
    }

    @AfterPermissionGranted(RC_STORAGE_READ_ACCESS_PERM)
    private void pickFromGalleryTask() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_SELECT_PICTURE);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this,
                    getString(R.string.permission_read_storage_rationale),
                    RC_STORAGE_READ_ACCESS_PERM,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void startCropActivity(@NonNull Uri uri) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(COMPRESSION_QUALITY);

        // new cropped image path
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(),
                SAMPLE_CROPPED_IMAGE_NAME));

        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(500, 500)
                .withOptions(options)
                .start(this);
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            setImage(resultUri);
        } else {
            Toast.makeText(this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setImage(Uri uri) {
        mUri = uri;
        mPreview.setImageURI(uri);
    }

    @OnClick(R.id.send)
    void send() {
        QuestionModel model = new QuestionModel();
        if (mUri != null) {
            model.setFile(new File(mUri.getPath()));
        }

        presenter.send(model);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
