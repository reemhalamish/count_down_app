package halamish.reem.remember.activity.main;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 6/5/2017.
 */

public class EventViewHolderEdit extends EventViewHolderNotNow {
    public EventViewHolderEdit(View itemView) {
        super(itemView);
    }

    @Override
    void prepareIcon(Context context) {
        ivIcon.setImageResource(R.drawable.ic_edit_black_24dp);
        ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent));
    }
}
