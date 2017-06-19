package halamish.reem.remember.activity.main.recycler_view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.firebase.db.FirebaseDbException;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;
import halamish.reem.remember.firebase.db.entity.EventType;
import halamish.reem.remember.firebase.db.entity.User;
import halamish.reem.remember.firebase.storage.FirebaseStorageManager;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Created by Re'em on 5/20/2017.
 *
 * the adapter for the recycler view.
 * controls the ViewHolder
 */

@SuppressLint("NewApi")
@SuppressWarnings("JavaDoc")
public class EventAdapter extends RecyclerView.Adapter<ViewHolderBaseClass> implements LocalRam.OnNewThumbnailInserted, RowMovingTouchListener.ElementSwiped {
    private static final int TIME_SHOW_UNDO_MS = 5000;

    public interface EventAdapterCallbacks {
        void gotoViewActivity(Event event);
        void gotoEditActivity(Event event);
        void eventRemoved(Event event);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class EventIdToEventPair implements Comparable<EventIdToEventPair> {
        String eventId; Event event;

        @Override
        public int compareTo(@NonNull EventIdToEventPair other) {
            String event1date, event2date;
            if (eventId.equals(NOW_SPECIAL_STRING)) {
                event1date = Event.format(new Date());
            } else {
                event1date = event.dateTimeFormattedTogether();
            }

            if (other.eventId.equals(NOW_SPECIAL_STRING)) {
                event2date = Event.format(new Date());
            } else {
                event2date = other.event.dateTimeFormattedTogether();
            }

            return -event1date.compareTo(event2date); // the minus makes them sort from past to future
        }
    }


    private static final int TYPE_MINE = Util.uniqueIntNumber.incrementAndGet();
    private static final int TYPE_HOT = Util.uniqueIntNumber.incrementAndGet();
    private static final int TYPE_PRIVATE_SUBSCRIBER = Util.uniqueIntNumber.incrementAndGet();
    private static final int TYPE_NOW = Util.uniqueIntNumber.incrementAndGet();
    private static final int TYPE_UNDO = Util.uniqueIntNumber.incrementAndGet();

    @SuppressWarnings("unused")
    private static final String TAG = EventAdapter.class.getSimpleName();
    private static final String NOW_SPECIAL_STRING = "Event_Special_String_now";


    private List<EventIdToEventPair> mEvents;
    private Collection<Event> eventsToBeRemoved;
    private Context context;
    private final EventAdapterCallbacks callbacks;
    private boolean needToExit = false;


    public EventAdapter(Collection<Event> events, Context context, EventAdapterCallbacks callbacks, boolean isInEditMode) {
        mEvents = new ArrayList<>();
        for (Event event : events)
            mEvents.add(new EventIdToEventPair(event.uniqueId, event));
        mEvents.add(new EventIdToEventPair(NOW_SPECIAL_STRING, null));
        Collections.sort(mEvents);
        eventsToBeRemoved = new HashSet<>();

        this.context = context;
        this.callbacks = callbacks;
        if (!isInEditMode) {
            startThumbnailListening();
        }
    }



    @Override
    public int getItemViewType(int position) {
        // todo implementation will change when using "now"!
        if (position == getPositionForNow()) {
            return TYPE_NOW;
        }
        Event event = getFromPosition(position);
        assert event != null;

        if (eventsToBeRemoved.contains(event))
            return TYPE_UNDO;

        if (event.localGetType().equals(EventType.CREATOR))
            return TYPE_MINE;
        if (event.isPublic)
            return TYPE_HOT;
        return TYPE_PRIVATE_SUBSCRIBER;
    }

    private Event getFromPosition(int position) {
        return mEvents.get(position).event;
    }

    @Override
    public ViewHolderBaseClass onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NOW) {
            View row = LayoutInflater.from(context).inflate(R.layout.item_event_main_now, parent, false);
            return new ViewHolderBaseClass(row);
        }

        if (viewType == TYPE_UNDO) {
            return new UndoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_event_main_undo, parent, false));
        }

        View row = LayoutInflater.from(context).inflate(R.layout.item_event_main, parent, false);
        if (viewType == TYPE_MINE) {
            return new EventViewHolderEdit(row);
        } else if (viewType == TYPE_PRIVATE_SUBSCRIBER) {
            return new EventViewHolderSubscriber(row);
        } else {
            return new EventViewHolderStar(row);
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {ViewHolder.itemView} to reflect the item at the given
     * position.
     * <p>
     * @param holderUnCasted   The ViewHolder which should be updated to represent the contents of the
     *                          item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolderBaseClass holderUnCasted, int position) {
        if (position==getPositionForNow()) return;

        Event event = getFromPosition(position);

        if (eventsToBeRemoved.contains(event)) {
            UndoViewHolder holder = (UndoViewHolder) holderUnCasted;
            holder.tvUndo.setOnClickListener(view -> {
                eventsToBeRemoved.remove(event);
                notifyItemChanged(holder.getAdapterPosition());
            });
        } else { // regular event

            EventViewHolder holder = (EventViewHolder) holderUnCasted;
            holder.initValues(event, LocalRam.getManager().getThumbnail(event.getUniqueId()));

            if (LocalRam.getManager().thumbnailExist(event.uniqueId))
            holder.civPicture.setOnClickListener(view -> {
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(LocalRam.getManager().getThumbnail(event.uniqueId));
                new AlertDialog.Builder(context)
                        .setView(imageView)
                        .create()
                        .show();
            });

            holderUnCasted.setListenerRow((view) -> callbacks.gotoViewActivity(event));

            EventType eventType = event.localGetType();
            if (eventType == EventType.CREATOR) {
                holderUnCasted.setListenerIcon(view -> callbacks.gotoEditActivity(event));
            } else if (eventType == EventType.PRIVATE_SUBSCRIBER) {

                holderUnCasted.setListenerIcon(view -> callbacks.gotoViewActivity(event));
            } else {
                // hot event
                holderUnCasted.setListenerIcon(view -> {
                    boolean isSubscribed = LocalRam.getManager().getUser().isSubscribed(event.uniqueId);
                    boolean newSubscribed = !isSubscribed;
                    EventViewHolderStar holderStar = (EventViewHolderStar) holderUnCasted;
                    holderStar.setStarState(newSubscribed);
                    if (newSubscribed) {
                        FirebaseDbManager.getManager().reqSubscribe(event.uniqueId, event.weeklyAlertDay(), LocalRam.getManager().getUsername(), EventNotificationPolicy.NOTIFY_WEEKLY);
                        LocalRam.getManager().getUser().addSubscription(event.uniqueId, EventNotificationPolicy.NOTIFY_WEEKLY.toString());
                    } else {
                        FirebaseDbManager.getManager().reqUnsubscribe(LocalRam.getManager().getUsername(), event.uniqueId);
                        LocalRam.getManager().getUser().removeSubscription(event.uniqueId);
                    }
                });
            }

        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }


    /**
     * removes the event from the list
     * @param event
     */
    void remove(Event event) {
        removeAt(getPosition(event.uniqueId));
    }

    private int getPosition(String eventUniqueId) {
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).eventId.equals(eventUniqueId))
                return i;
        }
       return -1;
    }

    /**
     * removes the object in that position
     * @param position
     */
    void removeAt(int position) {
        mEvents.remove(position);
        notifyItemRemoved(position);
    }

    public void addEvent(Event toAdd) {
        EventIdToEventPair pair = new EventIdToEventPair(toAdd.uniqueId, toAdd);
        mEvents.add(pair);
        Collections.sort(mEvents);
        int position = mEvents.indexOf(pair);
        notifyItemInserted(position);
    }

    /**
     * insert a new event in a position where an old event was
     * if you can't find the old event, insert anyway
     * @param updatedEvent
     */
    public void update(Event updatedEvent) {
        int eventIndex = getPosition(updatedEvent.uniqueId);
        if (eventIndex == -1) {
            addEvent(updatedEvent);
            return;
        }

        mEvents.get(eventIndex).event = updatedEvent;
        notifyItemChanged(eventIndex);
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

    int getPositionForNow() {
        return mEvents.indexOf(new EventIdToEventPair(NOW_SPECIAL_STRING, null));
    }
    void unstar(Event event) {

    }

    public void notifyItemChanged(Event event) {
        int position = getPosition(event.getUniqueId());
        notifyItemChanged(position);
    }


    void markToBeRemoved(int position) {
        Event event = getFromPosition(position);
        eventsToBeRemoved.add(event);
        notifyItemChanged(position);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (needToExit) return;
            if (eventsToBeRemoved.contains(event)) {
                removeEventIncludingAll(event);
            }
        }, TIME_SHOW_UNDO_MS);
    }

    /**
     * do ASAP all the things that normally you could have wait for
     * plus, remove all listeners you we registered
     */
    public void onActivityDestroyed(){
        needToExit = true;
        for (Event event: eventsToBeRemoved){
            removeEventIncludingAll(event);
        }
    }

    /** removes from firebase, from the local RAM and from the adapter list */
    private void removeEventIncludingAll(Event event) {
        User user = LocalRam.getManager().getUser();
        remove(event);
        eventsToBeRemoved.remove(event);
        EventType type = event.localGetType();
        switch (type) {
            case CREATOR:
                try {
                    FirebaseStorageManager.getManager().deleteAllRelated(event.uniqueId);
                    FirebaseDbManager.getManager().reqDeleteEvent(event.uniqueId);
                } catch (FirebaseDbException.NotEventCreator ignored) {}
                break;
            case PRIVATE_SUBSCRIBER:
                FirebaseDbManager.getManager().reqUnsubscribe(LocalRam.getManager().getUsername(), event.uniqueId);
                user.removeSubscription(event.uniqueId);
                break;
            case HOT_EVENT:
                if (LocalRam.getManager().getUser().isSubscribed(event.uniqueId)) {
                    FirebaseDbManager.getManager().reqUnsubscribe(LocalRam.getManager().getUsername(), event.uniqueId);
                    user.removeSubscription(event.uniqueId);
                }

                FirebaseDbManager.getManager().reqHideHotEventFromUesr(LocalRam.getManager().getUsername(), event.uniqueId);
                LocalRam.getManager().getUser().addHidden(event.uniqueId);
        }
    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder) {
        markToBeRemoved(viewHolder.getAdapterPosition());
    }

}
