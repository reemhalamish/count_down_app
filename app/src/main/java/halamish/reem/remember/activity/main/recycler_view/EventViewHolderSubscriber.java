package halamish.reem.remember.activity.main.recycler_view;

import android.support.v4.content.ContextCompat;
import android.view.View;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 6/5/2017.
 */

class EventViewHolderSubscriber extends EventViewHolder {
    EventViewHolderSubscriber(View itemView) {
        super(itemView);
    }

    @Override
    void prepareIcon() {
        ivIcon.setImageResource(R.drawable.ic_supervisor_account_black_24dp);
        ivIcon.setColorFilter(ContextCompat.getColor(vMainItem.getContext(), R.color.purple500));
    }
}
