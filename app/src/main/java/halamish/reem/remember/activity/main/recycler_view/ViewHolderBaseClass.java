package halamish.reem.remember.activity.main.recycler_view;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import halamish.reem.remember.firebase.db.entity.Event;

/**
 * Created by Re'em on 6/18/2017.
 */

public class ViewHolderBaseClass extends RecyclerView.ViewHolder {
    public ViewHolderBaseClass(View itemView) {
        super(itemView);
    }
    void initValues(Event event, Bitmap thumbnail){}
    void setListenerRow(View.OnClickListener listener){}
    void setListenerIcon(View.OnClickListener listener){}
}
