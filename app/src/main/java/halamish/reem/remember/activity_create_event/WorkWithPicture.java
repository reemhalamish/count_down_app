package halamish.reem.remember.activity_create_event;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

import halamish.reem.remember.Util;
import halamish.reem.remember.firebase.storage.FirebaseStorageManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static halamish.reem.remember.firebase.storage.FirebaseStorageManager.LOW_DENS_HEIGHT;
import static halamish.reem.remember.firebase.storage.FirebaseStorageManager.LOW_DENS_WIDTH;

/**
 * Created by Re'em on 5/23/2017.
 */

public class WorkWithPicture {
    private static final String EXTENSION = "jpg";
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int ASPECT_Y = 1;
    private static final int ASPECT_X = 1;
    private static final String TAG = WorkWithPicture.class.getSimpleName();
    private static final int QUALITY_PRCNTG = 90;

    public interface OnPictureCroppedAndReadyCallback {
        void onPictureReady(Bitmap image);
    }

    private static final int REQ_GET_PICTURE = Util.uniqueIntNumber.incrementAndGet();
//    private static final int REQ_CAMERA_CROP = Util.uniqueIntNumber.incrementAndGet();



    private OnPictureCroppedAndReadyCallback callback;
    @Getter private Bitmap actualPicture;
    @Getter private Bitmap lowDensityPicture;

    WorkWithPicture(OnPictureCroppedAndReadyCallback callback) {
        this.callback = callback;
    }

    /**
     * They who calls getPictures(), Must they call  onResult() as well!
     * @param context
     */
    void getPictures(Activity context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // maybe     new Intent(Intent.ACTION_PICK);
        // maybe        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        context.startActivityForResult(intent, REQ_GET_PICTURE);
    }

    void onResult(int reqCode, int resCode, Intent data, AppCompatActivity activity) {
        if (resCode != Activity.RESULT_OK || data == null) return;
        if (reqCode == REQ_GET_PICTURE) {
            getCroppedFromIntent(data, activity, callback);
        }
//        }
//        if (reqCode == REQ_GET_PICTURE) {
//            Uri picture = data.getData();
//            crop(activity, picture);
//        } else if (reqCode == REQ_CAMERA_CROP) {
//                getCroppedFromIntent(data, activity, callback);
//        }
    }

    private void getCroppedFromIntent(Intent data, Activity activity, OnPictureCroppedAndReadyCallback callback) {
        Uri uri = data.getData();
        ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = activity.getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                actualPicture = image;
                callback.onPictureReady(image);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap toLowDensity(Bitmap image) {
        return Bitmap.createScaledBitmap(image, LOW_DENS_WIDTH, LOW_DENS_HEIGHT, true);
    }

    private byte[] originalAsByteArr() {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        actualPicture.compress(COMPRESS_FORMAT, QUALITY_PRCNTG, bao); // bmp is bitmap from user image file
        return bao.toByteArray();
    }


    private byte[] lowDensAsByteArr() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        lowDensityPicture.compress(COMPRESS_FORMAT, QUALITY_PRCNTG, bao); // bmp is bitmap from user image file
        return bao.toByteArray();
    }

//
//    private static void crop(Activity activity, Uri pictureUri) {
//        Intent intent = new Intent("com.android.camera.action.CROP");
//// this will open all images in the Galery
//        intent.setDataAndType(pictureUri, "image/*");
//        intent.putExtra("crop", "true");
//// this defines the aspect ration
//        intent.putExtra("aspectX", ASPECT_Y);
//        intent.putExtra("aspectY", ASPECT_X);
//// this defines the output bitmap size
//        intent.putExtra("outputX", LOW_DENS_WIDTH);
//        intent.putExtra("outputY", LOW_DENS_HEIGHT);
//// true to return a Bitmap, false to directly save the cropped iamge
//        intent.putExtra("return-data", true);
//        activity.startActivityForResult(intent, REQ_CAMERA_CROP);
//    }

    @AllArgsConstructor
    private class ProcessImagesTask extends AsyncTask<Void, Void, Void> {
        final String eventId;

        @Override
        protected Void doInBackground(Void... voids) {
            if (actualPicture == null) return null;
            lowDensityPicture = toLowDensity(actualPicture);
            FirebaseStorageManager
                    .getManager()
                    .uploadPictureToEvent(
                            originalAsByteArr(),
                            lowDensAsByteArr(),
                            eventId,
                            EXTENSION,
                            downloadUrl -> Log.d(TAG, "pictures uploaded!")
                    );

            return null;

        }
    }

    void uploadPicturesQuietlyInBg(String eventId) {
        ProcessImagesTask task = new ProcessImagesTask(eventId);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
            task.execute();
        else
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
