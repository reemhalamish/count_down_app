package halamish.reem.remember.view;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Re'em on 6/1/2017.
 */

public class AngelinaTextView extends TextView {
    public static Typeface getFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/angelina.TTF");
    }

    public AngelinaTextView(Context context) {
        super(context);
        init();
    }

    public AngelinaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AngelinaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AngelinaTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setTypeface(getFont(getContext()));
    }

}
