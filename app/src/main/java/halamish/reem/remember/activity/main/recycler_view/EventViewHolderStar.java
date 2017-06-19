package halamish.reem.remember.activity.main.recycler_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.View;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.firebase.db.entity.Event;

/**
 * Created by Re'em on 6/5/2017.
 */

public class EventViewHolderStar extends EventViewHolder {
    private boolean starStateOn;

    public EventViewHolderStar(View itemView) {
        super(itemView);
        this.starStateOn = false;
    }

    /**
     * show the star full or empty
     * @param starOn
     */
    public void setStarState(boolean starOn) {
        starStateOn = starOn;
        prepareIcon();
    }

    public void flipStarState() {setStarState(!starStateOn);}

    @Override
    void prepareIcon() {
        Context context = vMainItem.getContext();
        if (starStateOn) {
            ivIcon.setImageResource(R.drawable.ic_star_black_24dp);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.yellow800));
        } else {
            ivIcon.setImageResource(R.drawable.ic_star_border_black_24dp );
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.divider));
        }
    }


    @Override
    void initValues(Event event, Bitmap thumbnail) {
        super.initValues(event, thumbnail);
        setStarState(LocalRam.getManager().getUser().isSubscribed(event.uniqueId));
    }
}
