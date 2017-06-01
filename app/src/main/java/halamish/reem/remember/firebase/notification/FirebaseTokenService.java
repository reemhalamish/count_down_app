package halamish.reem.remember.firebase.notification;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import halamish.reem.remember.LocalStorageUsernamePhone;
import halamish.reem.remember.LocalRam;
import halamish.reem.remember.firebase.FirebaseInitiationController;
import halamish.reem.remember.firebase.db.FirebaseDbManager;

/**
 * Created by Re'em on 5/20/2017.
 */

public class FirebaseTokenService extends FirebaseInstanceIdService {
    private final static String TAG = FirebaseTokenService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        updateTokenInServer(this);
    }

    public static void updateTokenInServer(Context context) {
        String username = LocalRam.getManager().getUsername();
        String phoneId = LocalStorageUsernamePhone.getManager().getPhoneConstId(context);
        String firebaseId = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, firebaseId);

        FirebaseInitiationController.getManager().whenReady(() -> FirebaseDbManager.getManager().uploadUserPhoneUpdate(username, phoneId, firebaseId));
    }
}
