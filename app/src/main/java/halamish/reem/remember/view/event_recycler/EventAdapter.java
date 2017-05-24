package halamish.reem.remember.view.event_recycler;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import halamish.reem.remember.R;
import halamish.reem.remember.RememberApp;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.storage.FirebaseStorageManager;
import lombok.AllArgsConstructor;

import static android.graphics.BitmapFactory.decodeByteArray;

/**
 * Created by Re'em on 5/20/2017.
 *
 * the adapter for the recycler view.
 * controls the ViewHolder
 */

@SuppressLint("NewApi")
@SuppressWarnings("JavaDoc")
@AllArgsConstructor
public class EventAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = EventAdapter.class.getSimpleName();

    public interface OnStarPress {
        default void onPressView(Event event){}
        default void onStarOnPressedOff(Event event) {}
        default void onStarOffPressedOn(Event event) {}
    }


    private List<Event> events;
    private boolean isStarOnAllEvents;
    private boolean isStarVisibleAllEvents;
    private OnStarPress callbacks;
    private static Map<String, Bitmap> eventIdToBitmap = new HashMap<>();


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = View.inflate(parent.getContext(), R.layout.item_view_recycler_event, null);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.cdvDays.setCountdown((int) event.get_local_CountDownDays(), (int) event.get_local_CountDownHours());
        Bitmap icon = eventIdToBitmap.get(event.getUniqueId());
        if (icon != null) holder.civPicture.setImageBitmap(icon);
        else {
            FirebaseStorageManager.getManager().getLowDensPictureUrl(event.getUniqueId(), new FirebaseStorageManager.OnPictureReadyCallback() {
                @Override
                public void onPicReady(String eventId, byte[] picture, int height, int width) {
                    Log.d(TAG, "picture got in! for item " + position);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap icon = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                    eventIdToBitmap.put(eventId, icon);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }

        if (isStarVisibleAllEvents) {
            holder.ivStar.setVisibility(View.VISIBLE);
            holder.setStarState(isStarOnAllEvents); // show the star full or empty

            holder.ivStar.setOnClickListener(view -> {
                Event eventWhenPressed = events.get(holder.getAdapterPosition());
                if (holder.starStateOn) {
                    callbacks.onStarOnPressedOff(eventWhenPressed);
                } else {
                        callbacks.onStarOffPressedOn(eventWhenPressed);
                }

                holder.flipStarState(); // so that next time it will be opposite

            });

        } else { // isStarVisibleAllEvents is false
            holder.ivStar.setVisibility(View.GONE);
        }

        holder.vMainItem.setOnClickListener(view -> callbacks.onPressView(events.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    /**
     * removes the event from the list
     * @param event
     */
    void remove(Event event) {
        removeAt(events.indexOf(event));
    }

    /**
     * removes the object in that position
     * @param position
     */
    void removeAt(int position) {
        events.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, events.size());
    }

    void addEvent(Event toAdd) {
        events.add(0, toAdd);
        notifyItemInserted(0);
        notifyItemRangeChanged(1, events.size());
    }


}
