package halamish.reem.remember.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 5/22/2017.
 */

public class ViewUtil {
    public static Drawable getVectorAsset(Context context, int vectorResId) {
        Drawable icon;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            icon = VectorDrawableCompat.create(context.getResources(),vectorResId, context.getTheme());
        } else {
            icon = context.getResources().getDrawable(vectorResId, context.getTheme());
        }
        return icon;
    }
}
