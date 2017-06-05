package halamish.reem.remember.view;

import android.content.Context;
import android.util.AttributeSet;

import de.hdodenhof.circleimageview.CircleImageView;
import halamish.reem.remember.R;

/**
 * Created by Re'em on 5/19/2017.
 */

public class MaterialCircleImageView extends CircleImageView {
    public MaterialCircleImageView(Context context) {
        super(context);
        init();
    }

    public MaterialCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.circle_color_light);
        if (isInEditMode())
            setImageResource(R.color.purple500);
    }
}
