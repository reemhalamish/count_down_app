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
    private static final String TAG = FirebaseUserManager.class.getSimpleName();
    @Getter private static FirebaseUserManager manager;
    public static void init(Context rememberApp) {
        manager = new FirebaseUserManager();
    }

    private FirebaseAuth mAuth;

    private FirebaseUserManager() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            connectAnonymously();
        }
    }



    private void connectAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                        }
                    }
                });
    }


}
