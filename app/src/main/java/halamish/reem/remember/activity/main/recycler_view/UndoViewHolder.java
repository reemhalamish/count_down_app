package halamish.reem.remember.activity.main.recycler_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
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

class UndoViewHolder extends ViewHolderBaseClass {
    TextView tvUndo;

    UndoViewHolder(View itemView) {
        super(itemView);
        tvUndo = (TextView) itemView.findViewById(R.id.tv_item_undo);
    }
    void setListenerIcon(View.OnClickListener listener){
        tvUndo.setOnClickListener(listener);
    }
}
