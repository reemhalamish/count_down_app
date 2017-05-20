package halamish.reem.remember.firebase.notification;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import halamish.reem.remember.Const;
import halamish.reem.remember.LocalDB;
import halamish.reem.remember.firebase.db.FirebaseDbManager;

/**
 * Created by Re'em on 5/20/2017.
 */

public class FirebaseTokenService extends FirebaseInstanceIdService {
    private final static String TAG = FirebaseTokenService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        updateTokenInServer();
    }

    public static void updateTokenInServer() {
        String username = Const.username;
        String phoneId = LocalDB.getManager().getPhoneConstId();
        String firebaseId = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, firebaseId);
        FirebaseDbManager.getManager().uploadUserPhoneUpdate(username, phoneId, firebaseId);
    }
}
