package com.backpackers.android.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by          Fabián Rosales Esquivel
 * Visit my web page   http://www.frosquivel.com
 * Visit my blog       http://www.frosquivel.com/blog
 * Created Date        on 5/15/16
 * This is an android library to take easy picture
 */
public class MagicalCamera {

    public static int TAKE_PHOTO = 0;
    public static int SELECT_PHOTO = 1;

    public static final int LANDSCAPE_CAMERA = 1;
    public static final int NORMAL_CAMERA = 3;

    //compress format public static variables
    public static Bitmap.CompressFormat JPEG = Bitmap.CompressFormat.JPEG;
    public static Bitmap.CompressFormat PNG = Bitmap.CompressFormat.PNG;
    public static Bitmap.CompressFormat WEBP = Bitmap.CompressFormat.WEBP;

    //the max of quality photo
    private int BEST_QUALITY_PHOTO = 4000;

    //Your own resize picture
    private int mResizePhoto;

    //the names of our photo
    private String mThePhotoName;
    private String mAnotherPhotoName;

    //my activity variable
    private Activity activity;

    //bitmap to set and get
    private Bitmap myPhoto;

    //my intent curret fragment (only use for fragments)
    Intent intentFragment;

    //THE CURRENT IMAGE PATH
    private static String imgPath;

    //the references properties photo
    private float latitude;
    private String latitudeReference;
    private float longitude;
    private String longitudeReference;
    private String dateTimeTakePhoto;
    private String imageLength;
    private String imageWidth;
    private String modelDevice;
    private String makeCompany;
    private String orientation;
    private String iso;
    private String dateStamp;

    private String mRealPath;
    //endregion

    //================================================================================
    // Accessors
    //================================================================================
    //region Getter and Setters
    public Intent getIntentFragment() {
        return intentFragment;
    }

    public void setMyPhoto(Bitmap myPhoto) {
        this.myPhoto = myPhoto;
    }

    public Bitmap getMyPhoto() {
        return myPhoto;
    }

    public int getResizePhoto() {
        return mResizePhoto;
    }

    public void setResizePhoto(int resizePhoto) {
        if (resizePhoto < BEST_QUALITY_PHOTO)
            this.mResizePhoto = resizePhoto;
        else
            this.mResizePhoto = BEST_QUALITY_PHOTO;
    }

    private void setImgPath(String imgPath) {
        MagicalCamera.imgPath = imgPath;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getLatitudeReference() {
        return latitudeReference;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getLongitudeReference() {
        return longitudeReference;
    }

    public String getMakeCompany() {
        return makeCompany;
    }

    public String getModelDevice() {
        return modelDevice;
    }

    public String getDateTimeTakePhoto() {
        return dateTimeTakePhoto;
    }

    public String getImageLength() {
        return imageLength;
    }

    public String getImageWidth() {
        return imageWidth;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getIso() {
        return iso;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public String getRealPath() {
        return mRealPath;
    }
    //endregion

    //================================================================================
    // Constructs
    //================================================================================
    //region Construct
    public MagicalCamera(Activity activity, int resizePhoto) {
        if (resizePhoto < BEST_QUALITY_PHOTO)
            this.mResizePhoto = resizePhoto;
        else
            this.mResizePhoto = BEST_QUALITY_PHOTO;

        if (resizePhoto == 0) {
            this.mResizePhoto = 1;
        }
        this.activity = activity;
    }

    public MagicalCamera(Activity activity) {
        this.activity = activity;
        this.mResizePhoto = BEST_QUALITY_PHOTO;
    }
    //endregion

    //================================================================================
    // Principal Methods
    //================================================================================
    //region Take and Select photos

    /**
     * This method call the intent to take photo
     */
    public boolean takePhoto() {
        this.mThePhotoName = "MagicalCamera";
        this.mAnotherPhotoName = "MagicalCamera";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri FileUri = getPhotoFileUri(this.mThePhotoName, this.mAnotherPhotoName, this.activity);

        if (FileUri != null) {

            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUri);
            if (intent.resolveActivity(this.activity.getPackageManager()) != null) {
                this.activity.startActivityForResult(intent, TAKE_PHOTO);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This library call the intent to take photo
     */
    public boolean takeFragmentPhoto() {
        this.mThePhotoName = "MagicalCamera";
        this.mAnotherPhotoName = "MagicalCamera";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri FileUri = getPhotoFileUri(this.mThePhotoName, this.mAnotherPhotoName, this.activity);

        if (FileUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(this.mThePhotoName, this.mAnotherPhotoName, this.activity));
            if (intent.resolveActivity(this.activity.getPackageManager()) != null) {
                this.intentFragment = intent;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This call the intent to selected the picture
     *
     * @param headerName the header name of popUp
     */
    public boolean selectedPicture(String headerName) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        this.activity.startActivityForResult(
                Intent.createChooser(intent, (!headerName.equals("") ? headerName : "Magical Camera")),
                SELECT_PHOTO);

        return true;
    }

    /**
     * This call the intent to selected the picture
     */
    public boolean selectedFragmentPicture() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(this.activity.getPackageManager()) != null) {
            this.intentFragment = intent;
            return true;
        } else {
            return false;
        }
    }

    /**
     * This methods is called in the override method onActivityResult
     * for the respective activation, and this validate which of the intentn result be,
     * for example: if is selected file or if is take picture
     */
    public void resultPhoto(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                this.myPhoto = onSelectFromGalleryResult(data);
            } else if (requestCode == TAKE_PHOTO) {
                this.myPhoto = onTakePhotoResult();
            }

            if (this.myPhoto != null) {
                if (ifCameraLandScape(true) == LANDSCAPE_CAMERA) {
                    this.myPhoto = rotateImage(getMyPhoto(), 270);
                }
            }
        }
    }

    public void resultPhoto(int requestCode, int resultCode, Intent data, boolean doLandScape) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                this.myPhoto = onSelectFromGalleryResult(data);
            } else if (requestCode == TAKE_PHOTO) {
                this.myPhoto = onTakePhotoResult();
            }

            if (this.myPhoto != null) {
                if (ifCameraLandScape(doLandScape) == LANDSCAPE_CAMERA) {
                    this.myPhoto = rotateImage(getMyPhoto(), 270);
                }
            }
        }
    }

    /**
     * This method obtain the path of the picture selected, and convert this in the
     * phsysical path of the image, and decode the file with the respective options,
     * resize the file and change the quality of photos selected.
     *
     * @param data the intent data for take the photo path
     * @return return a bitmap of the photo selected
     */
    @SuppressWarnings("deprecation")
    private Bitmap onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = this.activity.managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(column_index);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);
        bm = resizePhoto(bm, this.mResizePhoto, true);
        getPhotoFileUri(selectedImagePath);
        if (bm != null)
            return bm;
        else
            return null;
    }

    /**
     * Save the photo in memory bitmap, resize and return the photo
     *
     * @return the bitmap of the respective photo
     */
    public Bitmap onTakePhotoResult() {
        Uri takenPhotoUri = getPhotoFileUri(this.mThePhotoName, this.mAnotherPhotoName, this.activity);
        // by this point we have the camera photo on disk
        if (takenPhotoUri != null) {
            Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
            takenImage = resizePhoto(takenImage, this.mResizePhoto, true);
            return takenImage;
        } else {
            return null;
        }
    }
    //endregion

    //================================================================================
    // Save Photo in device
    //================================================================================
    //region Save Photo in device

    /**
     * This library write the file in the device storage or sdcard
     *
     * @param bitmap                  the bitmap that you need to write in device
     * @param photoName               the photo name
     * @param directoryName           the directory that you need to create the picture
     * @param format                  the format of the photo, maybe png or jpeg
     * @param autoIncrementNameByDate is this variable is active the system create
     *                                the photo with a number of the date, hour, and second to diferenciate this
     * @return return true if the photo is writen
     */
    private boolean writePhotoFile(Bitmap bitmap, String photoName, String directoryName,
                                   Bitmap.CompressFormat format, boolean autoIncrementNameByDate) {

        if (bitmap == null) {
            return false;
        } else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(format, 100, bytes);

            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = df.format(Calendar.getInstance().getTime());

            if (format == PNG) {
                photoName = (autoIncrementNameByDate) ? photoName + "_" + date + ".png" : photoName + ".png";
            } else if (format == JPEG) {
                photoName = (autoIncrementNameByDate) ? photoName + "_" + date + ".jpeg" : photoName + ".jpeg";
            } else if (format == WEBP) {
                photoName = (autoIncrementNameByDate) ? photoName + "_" + date + ".webp" : photoName + ".webp";
            }

            File wallpaperDirectory;

            try {
                wallpaperDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/" + directoryName + "/");
            } catch (Exception ev) {
                try {
                    wallpaperDirectory = Environment.getExternalStorageDirectory();
                } catch (Exception ex) {
                    try {
                        wallpaperDirectory = Environment.getDataDirectory();
                    } catch (Exception e) {
                        wallpaperDirectory = Environment.getRootDirectory();
                    }
                }
            }

            if (wallpaperDirectory != null) {
                if (!wallpaperDirectory.exists()) {
                    wallpaperDirectory.exists();
                    wallpaperDirectory.mkdirs();
                }

                File f = new File(wallpaperDirectory, photoName);
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    this.activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + f.getAbsolutePath())));

                    try {
                        //Update the System
                        Uri u = Uri.parse(f.getAbsolutePath());
                        this.activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, u));
                    } catch (Exception ex) {
                    }

                    return true;
                } catch (Exception ev) {
                    return false;
                }

            } else {
                return false;
            }
        }
    }

    /**
     * ***********************************************
     * This methods save the photo in memory device
     * with diferents params
     * **********************************************
     */
    public boolean savePhotoInMemoryDevice(Bitmap bitmap, String photoName, boolean autoIncrementNameByDate) {
        return writePhotoFile(bitmap, photoName, "MAGICAL CAMERA", PNG, autoIncrementNameByDate);
    }

    public boolean savePhotoInMemoryDevice(Bitmap bitmap, String photoName, Bitmap.CompressFormat format, boolean autoIncrementNameByDate) {
        return writePhotoFile(bitmap, photoName, "MAGICAL CAMERA", format, autoIncrementNameByDate);
    }

    public boolean savePhotoInMemoryDevice(Bitmap bitmap, String photoName, String directoryName, boolean autoIncrementNameByDate) {

        return writePhotoFile(bitmap, photoName, directoryName, PNG, autoIncrementNameByDate);
    }

    public boolean savePhotoInMemoryDevice(Bitmap bitmap, String photoName, String directoryName,
                                           Bitmap.CompressFormat format, boolean autoIncrementNameByDate) {
        return writePhotoFile(bitmap, photoName, directoryName, format, autoIncrementNameByDate);
    }
    //endregion

    //===============================================================================
    // Utils methods, resize and get Photo Uri and others
    //================================================================================
    //region Utils
    public static int ifCameraLandScape(boolean doRotate) {
        if(doRotate) {
            if (rotateIfLandScapeCamera()) {
                return LANDSCAPE_CAMERA;
            } else {
                return NORMAL_CAMERA;
            }
        }else{
            return 0;
        }
    }

    /**
     * Rotate the image if the device camera is land scape
     * @return
     */
    private static boolean rotateIfLandScapeCamera() {
        return Build.BRAND.toLowerCase().equals("samsung") || Build.BRAND.toLowerCase().equals("sony");
    }

    /**
     * Rotate the bitmap if the image is in landscape camera
     * @param source
     * @param angle
     * @return
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return retVal;
    }

    /**
     * This method resize the photo
     *
     * @param realImage    the bitmap of image
     * @param maxImageSize the max image size percentage
     * @param filter       the filter
     * @return a bitmap of the photo rezise
     */
    private static Bitmap resizePhoto(Bitmap realImage, float maxImageSize,
                                      boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width,
                height, filter);
    }

    //validate if the string isnull or empty
    private boolean notNullNotFill(String validate) {
        return validate != null && !validate.trim().equals("");
    }

    // Returns the Uri for a photo stored on memory device
    // the real URI for show the information of the photo
    // select photos
    private Uri getPhotoFileUri(String fileDir) {
        File mediaStorageDir = null;
        mediaStorageDir = new File("", fileDir);

        Uri imgUri = Uri.fromFile(mediaStorageDir);
        setImgPath(mediaStorageDir.getAbsolutePath());

        this.mRealPath = mediaStorageDir.getPath();
        try {
            getImageInformation();
        } catch (Exception ex) {
        }
        return imgUri;
    }

    //================================================================================
    // Get URI photo for selected photos of device
    //================================================================================
    // Returns the Uri for a photo stored on memory device
    private Uri getPhotoFileUri(String fileName, String fileDir, Context context) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileDir);

            return getUriFiles(mediaStorageDir, fileName);
        } else {
            File mediaStorageDir = new File(
                    context.getFilesDir(), fileDir);
            return getUriFiles(mediaStorageDir, fileName);
        }
    }

    // return the real URI from files
    private Uri getUriFiles(File mediaStorageDir, String fileName) {

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        try {
            return getUriAuxiliar(mediaStorageDir.getPath() + File.separator + fileName);
        } catch (Exception ev) {
            try {
                return getUriAuxiliar(Environment.getExternalStorageDirectory() + "/DCIM/", fileName);
            } catch (Exception ex) {
                try {
                    return getUriAuxiliar(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "");
                } catch (Exception e) {
                    try {
                        return getUriAuxiliar(Environment.getDataDirectory() + "");
                    } catch (Exception ef) {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * Obtain the Uri from file (like an auxiliar method)
     * @param direction
     * @param nameFile
     * @return
     */
    private Uri getUriAuxiliar(String direction, String nameFile) {
        try {
            final File file = new File(direction, nameFile);
            final Uri imgUri = Uri.fromFile(file);

            setImgPath(file.getAbsolutePath());
            mRealPath = imgUri.getPath();

            try {
                getImageInformation();
            } catch (Exception ignored) {
            }

            return imgUri;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Obtain the Uri from file (like an auxiliar method)
     * @param direction
     * @return
     */
    private Uri getUriAuxiliar(String direction) {
        try {
            final File file = new File(direction);
            final Uri uri = Uri.fromFile(file);

            setImgPath(file.getAbsolutePath());
            mRealPath = uri.getPath();
            return uri;
        } catch (Exception ex) {
            return null;
        }
    }

    // Returns true if external storage for photos is available
    private static boolean isExternalStorageAvailable() {
        final String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    //================================================================================
    // Exif interface methods
    //================================================================================
    private ExifInterface getAllFeatures() {
        if (!mRealPath.equals("")) {
            ExifInterface exif;
            try {
                exif = new ExifInterface(mRealPath);
                return exif;
            } catch (IOException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean getImageInformation() {
        try {
            ExifInterface exif = getAllFeatures();
            if (exif != null) {

                float[] latLong = new float[2];
                try{
                    exif.getLatLong(latLong);
                    latitude = latLong[0];
                    longitude = latLong[1];
                }catch(Exception ex){}


                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF))) {
                    latitudeReference = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF))) {
                    longitudeReference = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_DATETIME))) {
                    dateTimeTakePhoto = exif.getAttribute(ExifInterface.TAG_DATETIME);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_ORIENTATION))) {
                    orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_ISO))) {
                    iso = exif.getAttribute(ExifInterface.TAG_ISO);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP))) {
                    dateStamp = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH))) {
                    imageLength = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH))) {
                    imageWidth = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_MODEL))) {
                    modelDevice = exif.getAttribute(ExifInterface.TAG_MODEL);
                }

                if (notNullNotFill(exif.getAttribute(ExifInterface.TAG_MAKE))) {
                    makeCompany = exif.getAttribute(ExifInterface.TAG_MAKE);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    //================================================================================
    // Conversion Methods
    //================================================================================
    public static byte[] bitmapToBytes(Bitmap bitmap, Bitmap.CompressFormat format) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static Bitmap bytesToBitmap(byte[] byteArray, Bitmap.CompressFormat format) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);
        return bitmap;
    }

    public static String bytesToStringBase64(byte[] byteArray) {
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static byte[] stringBase64ToBytes(String stringBase64) {
        return Base64.decode(stringBase64, Base64.DEFAULT);
    }
}