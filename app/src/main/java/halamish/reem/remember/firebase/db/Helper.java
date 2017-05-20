package halamish.reem.remember.firebase.db;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Re'em on 5/20/2017.
 */

public class Helper {

    /**
     * generate a new map to upload stuff to firebase DB
     * @param input
     * @return
     */
    static Map<String, Object> toFirebaseMap(Map<String, ?> input) {
        Map<String, Object> retVal = new HashMap<>();
        if (input != null) {
            for (Map.Entry<String, ?> entry : input.entrySet()) {
                retVal.put(entry.getKey(), entry.getValue());
            }
        }
        return retVal;
    }

    /**
     * convert "branch1", "username", "some_key" ----> "/branch1/username/some_key"
     * @param values
     * @return
     */
    static String toFirebaseBranch(String... values) {
        String retVal = "";
        for (String branch : values) {
            retVal += "/" + branch;
        }
        return retVal;
    }

    static boolean checkInternet(Context context){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
