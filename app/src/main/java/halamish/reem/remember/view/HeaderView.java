package halamish.reem.remember.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 5/19/2017.
 */

public class HeaderView extends RelativeLayout {
    TextView tvTitle;
    public HeaderView(Context context) {
        super(context);
        init(null);
    }


    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.view_header, this);
        tvTitle = (TextView) findViewById(R.id.tv_header_title);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HeaderView);
            CharSequence t = a.getText(R.styleable.HeaderView_android_text);
            tvTitle.setText(t);
            a.recycle();
        }
    }

    public void setTitle(String newTitle) {
        tvTitle.setText(newTitle);
    }

    public void setTitle(int newTitleResId) {
        tvTitle.setText(newTitleResId);
    }

}
