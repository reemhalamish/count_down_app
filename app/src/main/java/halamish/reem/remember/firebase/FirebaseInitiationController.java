package halamish.reem.remember.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;

import java.util.Collection;
import java.util.HashSet;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.User;
import halamish.reem.remember.firebase.notification.FirebaseTokenService;
import halamish.reem.remember.firebase.storage.FirebaseStorageManager;
import halamish.reem.remember.firebase.user.FirebaseUserManager;
import lombok.Getter;

/**
 * Created by Re'em on 6/1/2017.
 *
 * controlls the initation of the firebase components
 */

public class FirebaseInitiationController implements FirebaseUserManager.OnUserConnectedCallback, FirebaseDbManager.OnDbReadyCallback<User> {


    public interface OnFirebaseReadyToStartCallback {
        void onFirebaseReady();
    }

    @Getter private static boolean isReady = false;
    @Getter private static FirebaseInitiationController manager = new FirebaseInitiationController();
    private Collection<OnFirebaseReadyToStartCallback> onReadyCallbacks = new HashSet<>();


    public static void init(Context context) {
        FirebaseApp.initializeApp(context);
        FirebaseUserManager.init(manager);
    }

    @Override
    public void onUserConnected() {
        FirebaseDbManager.init(this);
    }

    @Override
    public void onDatabaseFinishedWorking(User valueFromFirebase) {
        LocalRam.getManager().setUser(valueFromFirebase);
        isReady = true;
        for (OnFirebaseReadyToStartCallback callback : onReadyCallbacks) {
            callback.onFirebaseReady();
        }
        onReadyCallbacks = new HashSet<>(); // deleted all of them
    }



    public void addCallback(OnFirebaseReadyToStartCallback callback) {
        onReadyCallbacks.add(callback);
    }
    public void removeCallback(OnFirebaseReadyToStartCallback callback) {
        onReadyCallbacks.remove(callback);
    }

    public void whenReady(OnFirebaseReadyToStartCallback callback) {
        if (isReady)
            callback.onFirebaseReady();
        else {
            addCallback(callback);
        }
    }


}
