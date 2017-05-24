package halamish.reem.remember.activity_create_event;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import halamish.reem.remember.Util;

/**
 * Created by Re'em on 5/23/2017.
 */

public class WorkWithPicture {
    private static final int ASPECT_Y = 3;
    private static final int ASPECT_X = 4;
    private static final int SIZE_X = 600;
    private static final int SIZE_Y = 450;

    public interface OnPictureCroppedAndReadyCallback {
        void onPictureReady(Bitmap image);
    }

    private static final int REQ_GET_PICTURE = Util.uniqueIntNumber.incrementAndGet();
    private static final int REQ_CAMERA_CROP = Util.uniqueIntNumber.incrementAndGet();

    /**
     * They who calls getPictures(), Must they call  onResult() as well!
     * @param context
     */
    public static void getPictures(Activity context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // maybe     new Intent(Intent.ACTION_PICK);
        // maybe        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        context.startActivityForResult(intent, REQ_GET_PICTURE);
    }

    public static void onResult(int reqCode, int resCode, Intent data, Activity activity, OnPictureCroppedAndReadyCallback callback) {
        if (resCode != Activity.RESULT_OK || data == null) return;
        if (reqCode == REQ_GET_PICTURE) {
            Uri picture = data.getData();
            crop(activity, picture);
        } else if (reqCode == REQ_CAMERA_CROP) {
                getCroppedFromIntent(data, activity, callback);
        }
    }

    private static void getCroppedFromIntent(Intent data, Activity activity, OnPictureCroppedAndReadyCallback callback) {
        Uri uri = data.getData();
        ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = activity.getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                callback.onPictureReady(image);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void crop(Activity activity, Uri pictureUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
// this will open all images in the Galery
        intent.setDataAndType(pictureUri, "image/*");
        intent.putExtra("crop", "true");
// this defines the aspect ration
        intent.putExtra("aspectX", ASPECT_Y);
        intent.putExtra("aspectY", ASPECT_X);
// this defines the output bitmap size
        intent.putExtra("outputX", SIZE_X);
        intent.putExtra("outputY", SIZE_Y);
// true to return a Bitmap, false to directly save the cropped iamge
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, REQ_CAMERA_CROP);
    }
}
