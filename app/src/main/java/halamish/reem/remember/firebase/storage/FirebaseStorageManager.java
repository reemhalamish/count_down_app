package halamish.reem.remember.firebase.storage;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;

import java.io.InputStream;

import lombok.Getter;

import static halamish.reem.remember.firebase.Helper.toFirebaseBranch;

/**
 * Created by Re'em on 5/23/2017.
 */

public class FirebaseStorageManager {
    private static final String BRANCH_IMGS = "imgs";
    private static final String BRANCH_EVENTS = "events";
    private static final String DEFAULT_PICTURE_EXT = ".jpg";

    public static interface OnStorageFinishedCallback {
        default void onError(Exception exception) {
            exception.printStackTrace();
        }
        void onFinished(Uri downloadUrl);
    }


    @Getter private static FirebaseStorageManager manager;

    public static void init(Context context) {
        manager = new FirebaseStorageManager();
    }

    private FirebaseStorage storage;
    private FirebaseStorageManager(){
        storage = FirebaseStorage.getInstance();
    }


    /**
     * uploads a picture to /imgs/evens/{event_id>.jpg
     * @param stream
     * @param eventId
     * @param callback
     */
    public void uploadPictureToEvent(InputStream stream, String eventId, OnStorageFinishedCallback callback) {
        uploadPictureToEvent(stream, eventId, DEFAULT_PICTURE_EXT, callback);

    }

    public void uploadPictureToEvent(InputStream stream, String eventId,String fileType, OnStorageFinishedCallback callback) {
        if (!fileType.startsWith(".")) fileType = "." + fileType;
        if (fileType.equals(".jpeg")) fileType = ".jpeg";
        upload(stream, toFirebaseBranch(BRANCH_IMGS, BRANCH_EVENTS, eventId + fileType), callback);

    }


    private void upload(InputStream stream, String fullPath, OnStorageFinishedCallback callback) {
        storage.getReference()
                .child(fullPath)
                .putStream(stream)
                .addOnFailureListener(callback::onError)
                .addOnCompleteListener(task -> callback.onFinished(task.getResult().getDownloadUrl()));
    }

}
