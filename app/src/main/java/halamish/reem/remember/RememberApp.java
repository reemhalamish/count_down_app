package halamish.reem.remember;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.notification.FirebaseTokenService;
import halamish.reem.remember.firebase.storage.FirebaseStorageManager;
import halamish.reem.remember.firebase.user.FirebaseUserManager;

/**
 * Created by Re'em on 5/17/2017.
 *
 *
 *  Your new email address is application.countdown@gmail.com
 *  the password is xphrvktjur which is hebrew for count-down (SFIRA LEAHOR)
 *
 */

public class RememberApp extends Application {
    private static final String TAG = RememberApp.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
        init(this);
    }

    public static void init(Context context) {
        Log.d(TAG, "before init");
        LocalStorage.init(context);
        LocalDB.init(context);
        Util.username = LocalDB.getManager().getUserName();

        FirebaseApp.initializeApp(context);
        FirebaseUserManager.init(context);
        FirebaseDbManager.init(context);
        FirebaseTokenService.updateTokenInServer();
        FirebaseStorageManager.init(context);

        Log.d(TAG, "after init");
    }
}
