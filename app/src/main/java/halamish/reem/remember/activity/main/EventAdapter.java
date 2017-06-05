package halamish.reem.remember.activity.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventType;

/**
 * Created by Re'em on 5/20/2017.
 *
 * the adapter for the recycler view.
 * controls the ViewHolder
 */

@SuppressLint("NewApi")
@SuppressWarnings("JavaDoc")
public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> implements LocalRam.OnNewThumbnailInserted {

    private static final int TYPE_MINE = Util.uniqueIntNumber.incrementAndGet();
    private static final int TYPE_NOT_MINE = Util.uniqueIntNumber.incrementAndGet();
    private static final int TYPE_NOW = Util.uniqueIntNumber.incrementAndGet();

    private static final String TAG = EventAdapter.class.getSimpleName();


    private List<Event> events;
    private Context context;
    private boolean isInEditMode;

    EventAdapter(Collection<Event> events, Context context, boolean isInEditMode) {
        this.events = new ArrayList<>(events);
        this.context = context;
        this.isInEditMode = isInEditMode;
        if (!isInEditMode) startThumbnailListening();
    }



    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {
        // todo implementation will change when using "now"!
        if (position == getPositionForNow()) {
            return TYPE_NOW;
        }
        Event event = getFromPosition(position);
        if (event.localGetType().equals(EventType.CREATOR))
            return TYPE_MINE;
        return TYPE_NOT_MINE;
    }

    private Event getFromPosition(int position) {
        // todo implementation will change!
        return events.get(position);
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NOW) {
            View row = View.inflate(parent.getContext(), R.layout.item_event_now, null);
            return new EventViewHolder(row);
        }

        View row = View.inflate(parent.getContext(), R.layout.item_event_main, null);
        if (viewType == TYPE_MINE) {
            return new EventViewHolderEdit(row);
        } else {
            return new EventViewHolderStar(row);
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(EventViewHolder holderUnCasted, int position) {
        if (position==getPositionForNow()) return;

        Event event = getFromPosition(position);
        EventViewHolderNotNow holder = (EventViewHolderNotNow) holderUnCasted;
        holder.initValues(context, event, LocalRam.getManager().getThumbnail(event.getUniqueId()));

        if (event.localGetType().equals(EventType.CREATOR)) {
            EventViewHolderEdit holderEdit = (EventViewHolderEdit) holder;
            // todo if needed
        }
    }

    @Override
    public int getItemCount() {
        return events.size() + 1;
    }


    /**
     * removes the event from the list
     * @param event
     */
    void remove(Event event) {
        removeAt(getPosition(event.uniqueId));
    }

    private int getPosition(String eventUniqueId) {
        // todo how the implementation will change with "now" ?
        for (int i =0; i < events.size(); i++) {
            if (events.get(i).uniqueId.equals(eventUniqueId))
                return i;
        }
        return -1;
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
        // todo add in the right place!
        events.add(0, toAdd);
        notifyItemInserted(0);
//        notifyItemRangeChanged(1, events.size());

    }

    /**
     * remove the prev event and insert the new one
     * @param prevEvent
     * @param updatedEvent
     */
    public void update(Event prevEvent, Event updatedEvent) {
        int prevEventIndex = getPosition(prevEvent.uniqueId);
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
        int eventIndex = getPosition(eventId);
        if (eventIndex != -1){
            notifyItemChanged(eventIndex);
        }
        return false;
    }

    public int getPositionForNow() {
        // todo NOT LAST PLACE, change!
        return events.size();
    }
}
