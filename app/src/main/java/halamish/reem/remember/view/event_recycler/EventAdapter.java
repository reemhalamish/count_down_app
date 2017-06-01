package halamish.reem.remember.view.event_recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.activity.main.ImageViewDialog;
import halamish.reem.remember.firebase.db.entity.Event;

import static android.graphics.BitmapFactory.decodeByteArray;

/**
 * Created by Re'em on 5/20/2017.
 *
 * the adapter for the recycler view.
 * controls the ViewHolder
 */

@SuppressLint("NewApi")
@SuppressWarnings("JavaDoc")
public class EventAdapter extends RecyclerView.Adapter<ViewHolder> implements LocalRam.OnNewThumbnailInserted {

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
    private Context context;
    private boolean isInEditMode;

    EventAdapter(List<Event> events, boolean isStarOnAllEvents, boolean isStarVisibleAllEvents, OnStarPress callbacks, Context context, boolean isInEditMode) {
        this.events = events;
        this.isStarOnAllEvents = isStarOnAllEvents;
        this.isStarVisibleAllEvents = isStarVisibleAllEvents;
        this.callbacks = callbacks;
        this.context = context;
        this.isInEditMode = isInEditMode;
        if (!isInEditMode) startThumbnailListening();
    }

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

        if (isInEditMode) return;
        Bitmap icon = LocalRam.getManager().getThumbnail(event.getUniqueId());
        if (icon != null) {
            holder.civPicture.setImageBitmap(icon);
            holder.civPicture.setOnClickListener(view -> {
                ImageViewDialog dialog = new ImageViewDialog(context);
                dialog.setCancelable(true);
                dialog.show(icon);
            });
        } else {
            holder.civPicture.setOnClickListener(null);
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
//        notifyItemRangeChanged(position, events.size());
    }

    void addEvent(Event toAdd) {
        events.add(0, toAdd);
        notifyItemInserted(0);
//        notifyItemRangeChanged(1, events.size());

        // todo not always working
    }

    /**
     * remove the prev event and insert the new one
     * @param prevEvent
     * @param updatedEvent
     */
    public void update(Event prevEvent, Event updatedEvent) {
        int prevEventIndex = events.indexOf(prevEvent);
        if (prevEventIndex == -1) return;

        events.remove(prevEventIndex);
        events.add(prevEventIndex, updatedEvent);
        notifyItemChanged(prevEventIndex);
    }



    public void startThumbnailListening() {
        LocalRam.getManager().registerNewCallback(this);
    }

    public void stopThumbnailListening() {
        LocalRam.getManager().removeCallback(this);
    }

    @Override
    public boolean thumbnailReady(String eventId, Bitmap thumbnail) {
        int eventIndex = -1;
        for (Event event : events)
            if (event.getUniqueId().equals(eventId))
                eventIndex = events.indexOf(event);
        if (eventIndex != -1){
            notifyItemChanged(eventIndex);
        }
        return false;
    }

}
