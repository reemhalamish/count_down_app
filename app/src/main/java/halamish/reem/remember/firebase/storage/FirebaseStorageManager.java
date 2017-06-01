package halamish.reem.remember.firebase.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import halamish.reem.remember.LocalRam;
import lombok.Getter;

import static halamish.reem.remember.firebase.Helper.toFirebaseBranch;

/**
 * Created by Re'em on 5/23/2017.
 *
 * storage looks like:
 *
 * imgs/
 *      events/
 *              {event_id}/
 *                      original/
 *                          picture.{extension, mostly jpg}
*                       low_density/
 *                          picture.{extension, mostly jpg}
 *               ...
 *
 */

public class FirebaseStorageManager {
    private static final String BRANCH_IMGS = "imgs";
    private static final String BRANCH_EVENTS = "events";
    private static final String BRANCH_ORIGINAL = "original";
    private static final String BRANCH_LOW_DENS = "low_density";
    private static final String PICTURE = "picture";
    private static final String DEFAULT_PICTURE_EXT = ".jpg";
    public static final int LOW_DENS_WIDTH = 320;
    public static final int LOW_DENS_HEIGHT = 320;

    public static interface OnStorageError {
        default void onError(Exception exception) {
            exception.printStackTrace();
        }
    }

    public static interface OnStorageFinishedCallback extends OnStorageError {
        void onFinished(Uri downloadUrl);
    }


    @Getter private static FirebaseStorageManager manager = new FirebaseStorageManager();

    private FirebaseStorage storage;
    private FirebaseStorageManager(){
        storage = FirebaseStorage.getInstance();
    }



    public void uploadPictureToEvent(byte[] bigPicture, byte[] lowDensImage, String eventId, String extension, OnStorageFinishedCallback callback) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        extension = extension.toLowerCase();
        if (extension.equals(".jpeg")) extension = ".jpg";
        if (!extension.equals(DEFAULT_PICTURE_EXT)) throw new IllegalStateException("Not supporting pictures with other formats than jpg");

        String fullPathBigImg = toFirebaseBranch(BRANCH_IMGS, BRANCH_EVENTS, eventId, BRANCH_ORIGINAL, PICTURE + DEFAULT_PICTURE_EXT);
        storage.getReference()
                .child(fullPathBigImg)
                .putBytes(bigPicture)
                .addOnFailureListener(callback::onError)
                .addOnCompleteListener(task -> {
                    Uri finishedUriOriginalDens = task.getResult().getDownloadUrl();

                    // upload the small image as well than finish
                    String fullPathLowDens = toFirebaseBranch(BRANCH_IMGS, BRANCH_EVENTS, eventId, BRANCH_LOW_DENS, PICTURE + DEFAULT_PICTURE_EXT);
                    storage
                            .getReference()
                            .child(fullPathLowDens)
                            .putBytes(lowDensImage)
                            .addOnFailureListener(callback::onError)
                            .addOnCompleteListener(task1 -> callback.onFinished(finishedUriOriginalDens));
                });
    }

    public String getPathPictureOriginal(String eventId) {
        return toFirebaseBranch(BRANCH_IMGS, BRANCH_EVENTS, eventId, BRANCH_ORIGINAL, PICTURE + DEFAULT_PICTURE_EXT);
    }

    public String getPathPictureLowDens(String eventId) {
        return toFirebaseBranch(BRANCH_IMGS, BRANCH_EVENTS, eventId, BRANCH_LOW_DENS, PICTURE + DEFAULT_PICTURE_EXT);
    }

    public static interface OnPictureReadyCallback extends OnStorageError {
        void onPicReady(String eventId, byte[] picture, int height, int width);
    }
    public static interface OnLowDensPictureReadyCallback extends OnStorageError {
        void onLowDensPicReady(String eventId, byte[] picture, int height, int width);
    }

    public void getLowDensPictureByteArray(String eventId, OnLowDensPictureReadyCallback callback){
        storage
                .getReference()
                .child(getPathPictureLowDens(eventId))
                .getBytes(LOW_DENS_WIDTH * LOW_DENS_HEIGHT)
                .addOnSuccessListener(bytes -> callback.onLowDensPicReady(eventId, bytes, LOW_DENS_HEIGHT, LOW_DENS_WIDTH))
                .addOnFailureListener(callback::onError);
    }

    public void getPictureByteArray(String eventId, OnPictureReadyCallback callback){
        storage
                .getReference()
                .child(getPathPictureLowDens(eventId))
                .getBytes(LOW_DENS_WIDTH * LOW_DENS_HEIGHT)
                .addOnSuccessListener(bytes -> callback.onPicReady(eventId, bytes, LOW_DENS_HEIGHT, LOW_DENS_WIDTH))
                .addOnFailureListener(callback::onError);
    }


}
