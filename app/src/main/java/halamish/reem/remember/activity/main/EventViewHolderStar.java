package halamish.reem.remember.activity.main;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 6/5/2017.
 */

public class EventViewHolderStar extends EventViewHolderNotNow {
    private boolean starStateOn;

    public EventViewHolderStar(View itemView) {
        super(itemView);
        this.starStateOn = false;
    }

    /**
     * show the star full or empty
     * @param starOn
     */
    public void setStarState(boolean starOn, Context context) {
        starStateOn = starOn;
        prepareIcon(context);
    }

    public void flipStarState() {setStarState(!starStateOn, vMainItem.getContext());}

    @Override
    void prepareIcon(Context context) {
        if (starStateOn) {
            ivIcon.setImageResource(R.drawable.ic_star_black_24dp);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.yellow800));
        } else {
            ivIcon.setImageResource(R.drawable.ic_star_border_black_24dp );
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.divider));
        }
    }
}
