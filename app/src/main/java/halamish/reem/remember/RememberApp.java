package halamish.reem.remember;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import halamish.reem.remember.firebase.FirebaseInitiationController;
import lombok.Getter;

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

    @SuppressLint("StaticFieldLeak")
    @Getter private static Context appContext;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        init(this);
    }

    public static void init(Context context) {
        LocalStorage.init(context);
        LocalRam.getManager().setUsername(LocalStorageUsernamePhone.getManager().getUserName());

        FirebaseInitiationController.init(context);

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_MODERATE) {
            LocalRam.getManager().removeAllImages();
            if (level >= TRIM_MEMORY_BACKGROUND) {
                LocalRam.getManager().removeAllThumbnails();
            }
        }
    }
}
