package halamish.reem.remember.activity.main.recycler_view;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import halamish.reem.remember.R;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.view.CountDownView;
import halamish.reem.remember.view.MaterialCircleImageView;

/**
 * Created by Re'em on 5/20/2017.
 *
 * "stupid" class. can be controlled
 */

abstract class EventViewHolder extends ViewHolderBaseClass {
    CountDownView cdvDays;
    MaterialCircleImageView civPicture;
    TextView tvTitle;
    View vMainItem;
    ImageView ivIcon;


    EventViewHolder(View itemView) {
        super(itemView);
        cdvDays = (CountDownView) itemView.findViewById(R.id.cdv_item_main_countdown);
        civPicture = (MaterialCircleImageView) itemView.findViewById(R.id.civ_item_main_img);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_item_main_title);
        ivIcon = (ImageView) itemView.findViewById(R.id.iv_item_main_end_icon);
        vMainItem = itemView;
    }

    @Override
    void setListenerRow(View.OnClickListener listener) {
        vMainItem.setOnClickListener(listener);
    }

    @Override
    void setListenerIcon(View.OnClickListener listener) {
        ivIcon.setOnClickListener(listener);
    }

    abstract void prepareIcon();

    @Override
    void initValues(Event event, Bitmap thumbnail) {
        prepareIcon();
        cdvDays.setCountdown((int) event.localGetCountDownDays(), (int) event.localGetCountDownHours());
        tvTitle.setText(event.getTitle());
        if (thumbnail != null) {
            civPicture.setImageBitmap(thumbnail);
        }
    }

}
