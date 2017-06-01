package halamish.reem.remember;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import halamish.reem.remember.firebase.db.entity.User;
import halamish.reem.remember.firebase.storage.FirebaseStorageManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Re'em on 5/25/2017.
 *
 * manages the local items to save:
 *      bitmaps - for events, thumbnails+big
 *      the global username
 *      current user
 *
 */

public class LocalRam implements FirebaseStorageManager.OnPictureReadyCallback, FirebaseStorageManager.OnLowDensPictureReadyCallback {



    static interface OneTimeOnly {
        @SuppressLint("NewApi")
        default boolean oneTimeOnly() {return true;}
    }

    public interface OnNewThumbnailInserted extends OneTimeOnly {
        /**
         *
         * @param eventId
         * @param thumbnail
         * @return boolean value - if you want to unsubscribe the callback from the LocalRam
         */
        boolean thumbnailReady(String eventId, Bitmap thumbnail);
    }
    public interface OnNewImageInserted extends OneTimeOnly {

        /**
         *
         * @param eventId
         * @param image
         * @return boolean value - if you want to unsubscribe the callback from the LocalRam
         */
        boolean imageReady(String eventId, Bitmap image);
    }

    @Getter private static LocalRam manager = new LocalRam();

    @Getter @Setter private String username;
    @Getter @Setter private User user;
    private Map<String, Bitmap> eventIdToThumbnail;
    private Map<String, Bitmap> eventIdToPicture;
    private Collection<OnNewThumbnailInserted> thumbnailCallbackss;
    private Collection<OnNewImageInserted> imageCallbackss;



    private LocalRam() {
        eventIdToPicture = new HashMap<>();
        eventIdToThumbnail = new HashMap<>();
        thumbnailCallbackss = new HashSet<>();
        imageCallbackss = new HashSet<>();
    }

    public void registerNewCallback(OnNewThumbnailInserted thumbnailInsertedCallback) {
        if (!thumbnailCallbackss.contains(thumbnailInsertedCallback))
            thumbnailCallbackss.add(thumbnailInsertedCallback);
    }

    public void registerNewCallback(OnNewImageInserted newImageInsertedCallback){
        if (!imageCallbackss.contains(newImageInsertedCallback))
            imageCallbackss.add(newImageInsertedCallback);
    }
    public void removeCallback(OnNewThumbnailInserted thumbnailInsertedCallback) {
        thumbnailCallbackss.remove(thumbnailInsertedCallback);
    }
    public void removeCallback(OnNewImageInserted newImageInsertedCallback) {
        imageCallbackss.remove(newImageInsertedCallback);
    }

    @Nullable public Bitmap getThumbnail(String eventId) {
        Bitmap retVal = eventIdToThumbnail.get(eventId);
        if (retVal == null) {
            requestThumbnail(eventId);
        }

        return retVal;
    }
//    @Nullable public Bitmap getPicture(String eventId) {
//        Bitmap picture = eventIdToPicture.get(eventId);
//        if (picture == null)
//            requestPicture(eventId);
//
//        return picture;
//    }

    public void requestThumbnail(String eventId) {
        FirebaseStorageManager.getManager().getLowDensPictureByteArray(eventId, this);
    }

    public void requestThumbnail(String eventId, OnNewThumbnailInserted thumbnailInsertedCallback) {
        registerNewCallback(thumbnailInsertedCallback);
        requestThumbnail(eventId);
    }

    public void requestPicture(String eventId ) {
        FirebaseStorageManager.getManager().getPictureByteArray(eventId, this);
    }

    public void requestPicture(String eventId, OnNewImageInserted newImageInsertedCallback) {
        registerNewCallback(newImageInsertedCallback);
        requestPicture(eventId);
    }

    @AllArgsConstructor
    private class DecodeImageTask extends AsyncTask<Void, Void, Bitmap> {
        private byte[] picture;
        private boolean isThumbnail;
        private String eventId;

        @Override protected Bitmap doInBackground(Void... voids) {return BitmapFactory.decodeByteArray(picture, 0, picture.length);}

        @Override protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null)
                if (isThumbnail)
                    addThumbnail(eventId, bitmap);
                else
                    addImage(eventId, bitmap);
        }
    }

    @Override
    public void onLowDensPicReady(String eventId, byte[] picture, int height, int width) {
        new DecodeImageTask(picture, true, eventId).execute();
    }

    @Override
    public void onPicReady(String eventId, byte[] picture, int height, int width) {
        new DecodeImageTask(picture, false, eventId).execute();
    }

    public void addImage(String eventId, Bitmap picture) {
        if (eventId == null || picture == null) return;
        eventIdToPicture.put(eventId, picture);

        Handler handler = new Handler(Looper.getMainLooper());

        for (OnNewImageInserted callback : imageCallbackss) {
            handler.post(() -> {
                boolean callbackFinished = callback.imageReady(eventId, picture);
                if (callbackFinished) removeCallback(callback);
            });
        }
    }
    public void addThumbnail(String eventId, Bitmap picture) {
        if (eventId == null || picture == null) return;
        eventIdToThumbnail.put(eventId, picture);

        Handler handler = new Handler(Looper.getMainLooper());

        for (OnNewThumbnailInserted callback : thumbnailCallbackss) {
            handler.post(() -> {
                boolean callbackFinished = callback.thumbnailReady(eventId, picture);
                if (callbackFinished) removeCallback(callback);
            });
        }
    }

    public void removeImage(String eventId) {
        eventIdToPicture.remove(eventId);
    }
    public void removeThumbnail(String eventId) {
        eventIdToThumbnail.remove(eventId);
    }


    public void removeAllImages() {
        Set<String> eventIdsForPictures = eventIdToPicture.keySet();
        for (String eventId : eventIdsForPictures) {
            removeImage(eventId);
        }
    }
    public void removeAllThumbnails() {
        Set<String> eventIdsForThumbnails = eventIdToThumbnail.keySet();
        for (String eventId : eventIdsForThumbnails) {
            removeImage(eventId);
        }
    }
}
