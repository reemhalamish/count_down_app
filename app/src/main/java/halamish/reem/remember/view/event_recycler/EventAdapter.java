package halamish.reem.remember.view.event_recycler;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import halamish.reem.remember.R;
import halamish.reem.remember.firebase.db.entity.Event;
import lombok.AllArgsConstructor;

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

    public interface OnStarPress {
        default void onPressView(Event event){}
        default void onStarOnPressedOff(Event event) {}
        default void onStarOffPressedOn(Event event) {}
    }


    private List<Event> events;
    private boolean isStarOnAllEvents;
    private boolean isStarVisibleAllEvents;
    private OnStarPress callbacks;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = View.inflate(parent.getContext(), R.layout.item_view_recycler_event, null);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.cdvDays.setCountdown((int) event.getCountDownDays(), (int) event.getCountDownHours());

        // todo holder.civPicture.setImage(from firebase storage somehow)

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
    }


}
