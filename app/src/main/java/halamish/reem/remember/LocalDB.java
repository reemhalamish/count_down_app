package halamish.reem.remember;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Re'em on 5/19/2017.
 */

@AllArgsConstructor
public class LocalDB {
    private static final java.lang.String KEY_USERNAME = "rememberapp@key_username";

    @Getter private static LocalDB manager;
    private Context context;
    public static void init(Context context) {
        manager = new LocalDB(context);
    }

    @SuppressLint("HardwareIds")
    public String getPhoneConstId() {

        String androidId = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String androidTelephonId = "1346abc";

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)androidTelephonId.hashCode() << 8));
        return deviceUuid.toString();
    }

    public String getUserName() {
        return LocalStorage.getManager().getString(KEY_USERNAME, null);
    }

    public void setUserName(String newUsername) {
        LocalStorage.getManager().putString(KEY_USERNAME, newUsername);
        Util.username = newUsername;
    }


}
