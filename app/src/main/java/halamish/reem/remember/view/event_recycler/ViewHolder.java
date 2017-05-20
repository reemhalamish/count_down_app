package halamish.reem.remember.view.event_recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import halamish.reem.remember.R;
import halamish.reem.remember.view.CountDownView;
import halamish.reem.remember.view.MaterialCircleImageView;

/**
 * Created by Re'em on 5/20/2017.
 *
 * "stupid" class. can be controlled
 */

class ViewHolder extends RecyclerView.ViewHolder {
    ImageView ivStar;
    CountDownView cdvDays;
    MaterialCircleImageView civPicture;
    TextView tvTitle;
    View vMainItem;
    boolean starStateOn;


    public ViewHolder(View itemView) {
        super(itemView);

        ivStar = (ImageView) itemView.findViewById(R.id.iv_item_recycler_event_star);
        cdvDays = (CountDownView) itemView.findViewById(R.id.cdv_item_recycler_event_countdown);
        civPicture = (MaterialCircleImageView) itemView.findViewById(R.id.civ_item_recycler_event_img);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_item_recycler_event_title);
        vMainItem = itemView;
    }

    /**
     * show the star full or empty
     * @param starOn
     */
    public void setStarState(boolean starOn) {
        if (starOn) {
            ivStar.setImageResource(R.drawable.ic_star_black_24dp);
        } else {
            ivStar.setImageResource(R.drawable.ic_star_border_black_24dp);
        }
        starStateOn = starOn;
    }


    void flipStarState() {setStarState(!starStateOn);}
}
