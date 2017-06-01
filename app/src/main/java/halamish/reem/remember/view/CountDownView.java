package halamish.reem.remember.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 5/16/2017.
 */

public class CountDownView extends RelativeLayout {
    private TextView tvCountdown;
    private TextView tvDaysHours;
    private TextView tvIn;

    public CountDownView(Context context) {
        super(context);
        init();
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private void init() {
        inflate(getContext(), R.layout.view_countdown , (ViewGroup) getRootView());
        tvCountdown = (TextView) findViewById(R.id.tv_view_countdown_num_days);
        tvDaysHours = (TextView) findViewById(R.id.view_countdown_days_hours);
        tvIn = (TextView) findViewById(R.id.view_countdown_in);
    }

    public void setCountdown(int days, int hours) {
        if (hours <= 96 && hours > -96) {
            tvCountdown.setText(String.valueOf(Math.abs(hours)));
            tvDaysHours.setText(R.string.hours);

            if (hours >= 0) {
                int color = ContextCompat.getColor(getContext(), R.color.accent);
                for (TextView view : Arrays.asList(tvIn, tvCountdown, tvDaysHours)) {
                    view.setTextColor(color);
                }

                tvIn.setText(R.string.in);
            } else { // hours are negative
                tvIn.setText(R.string.before);
                int color = ContextCompat.getColor(getContext(), R.color.divider);
                for (TextView view : Arrays.asList(tvIn, tvCountdown, tvDaysHours)) {
                    view.setTextColor(color);
                }
            }
        } else { // hours out of range - display days
            tvDaysHours.setText(R.string.days);

            if (days >= 0) {
                tvIn.setText(R.string.in);
            } else { // days are negative
                tvIn.setText(R.string.before);
                int color = ContextCompat.getColor(getContext(), R.color.divider);
                for (TextView view : Arrays.asList(tvIn, tvCountdown, tvDaysHours)) {
                    view.setTextColor(color);
                }
            }

            if (Math.abs(days) > 99) {
                tvCountdown.setText(R.string.plus99);
            } else {
                tvCountdown.setText(String.valueOf(Math.abs(days)));
            }
        }
    }
}
