package halamish.reem.remember;

import android.content.Context;
import android.content.SharedPreferences;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Re'em on 5/19/2017.
 */

@AllArgsConstructor
public class LocalStorage {
    SharedPreferences sharedPreferences;
    @Getter static LocalStorage manager;
    static void init(Context context) {
        manager = new LocalStorage(context.getSharedPreferences(
                context.getApplicationContext().getClass().getCanonicalName(), Context.MODE_PRIVATE
        ));
    }

    public String getString(String key, String defaultRetVal) {
        return sharedPreferences.getString(key, defaultRetVal);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }
}
