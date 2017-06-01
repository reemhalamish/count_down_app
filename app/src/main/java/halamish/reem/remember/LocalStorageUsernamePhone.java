package halamish.reem.remember;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Re'em on 5/19/2017.
 *
 * is used to be a gate between the LocalStorage and the rest of the world
 */

@AllArgsConstructor
public class LocalStorageUsernamePhone {
    private static final java.lang.String KEY_USERNAME = "rememberapp@key_username";

    @Getter private static LocalStorageUsernamePhone manager  = new LocalStorageUsernamePhone();

    @SuppressLint("HardwareIds")
    public String getPhoneConstId(Context context) {

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
    }


}
