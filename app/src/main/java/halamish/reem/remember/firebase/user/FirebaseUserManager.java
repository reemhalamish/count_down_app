package halamish.reem.remember.firebase.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lombok.Getter;


/**
 * Created by Re'em on 5/17/2017.
 *
 * is used to store the actual user
 */


public class FirebaseUserManager {
    public interface OnUserConnectedCallback {
        void onUserConnected();
    }

    private static final String TAG = FirebaseUserManager.class.getSimpleName();
    @Getter private static FirebaseUserManager manager;
    public static void init(OnUserConnectedCallback callback) {
        manager = new FirebaseUserManager(callback);
    }

    private FirebaseAuth mAuth;

    private FirebaseUserManager(OnUserConnectedCallback callback) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            connectAnonymously(callback);
        } else {
            callback.onUserConnected();
        }
    }



    private void connectAnonymously(OnUserConnectedCallback callback) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        callback.onUserConnected();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                    }
                });
    }


}
