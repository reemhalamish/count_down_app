package halamish.reem.remember.view.event_recycler;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import halamish.reem.remember.R;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.PartiallyEventForGui;
import halamish.reem.remember.view.HeaderView;

/**
 * Created by Re'em on 5/20/2017.
 *
 * is used to display (and set listeners on) a list of events
 */

public class EventRecyclerViewWithHeader extends RelativeLayout {
    HeaderView mHeaderView;
    RecyclerView mRecyclerView;
    TextView mTvNothingHere;
    String mShowWhenNoObjects;
    EventAdapter mAdapter;
    boolean mStarVisible;
    boolean mStarOn;
    int mEditModeNumItems;

    public EventRecyclerViewWithHeader(Context context) {
        super(context);
        init(null);
    }


    public EventRecyclerViewWithHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EventRecyclerViewWithHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EventRecyclerViewWithHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    private void init(AttributeSet attributeSet) {
        inflate(getContext(), R.layout.view_recycler_with_header, this);
        mHeaderView = (HeaderView) findViewById(R.id.view_rcv_with_header_header);
        mRecyclerView = (RecyclerView) findViewById(R.id.view_rcv_with_header_rcv);
        mTvNothingHere = (TextView) findViewById(R.id.view_rcv_with_header_nothing);

        mRecyclerView.setHasFixedSize(true);

        if (attributeSet != null) {
            TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.EventRecyclerViewWithHeader);
            try {
                mTvNothingHere.setText(a.getText(R.styleable.EventRecyclerViewWithHeader_nothingText));
                mHeaderView.setTitle(a.getText(R.styleable.EventRecyclerViewWithHeader_headerText).toString());
                mStarVisible = a.getBoolean(R.styleable.EventRecyclerViewWithHeader_starVisible, false);
                mStarOn = a.getBoolean(R.styleable.EventRecyclerViewWithHeader_starOn, false);
                mEditModeNumItems = a.getInt(R.styleable.EventRecyclerViewWithHeader_editModeNumItems, 0);
            } finally {
                a.recycle();
            }
        }

        if (isInEditMode()) {
            List<Event> eventList = new ArrayList<>();
            for (int i = 0; i < mEditModeNumItems; i++) {
                eventList.add(new Event(new PartiallyEventForGui("2017/05/21 19:00", "Wedding!", "We are getting married :)" ,"reem.halamish@gmail.com", true, true)));
            }
            startWhenInfoAlreadyInXml(eventList, null);
        }
    }

    public void setHeaderText(String headerText) {
        mHeaderView.setTitle(headerText);
    }
    public void setHeaderText(int headerTextResId) {
        mHeaderView.setTitle(headerTextResId);
    }

    public void setNothing(String nothingText) {
        mTvNothingHere.setText(nothingText);
    }

    public void setNothing(int nothingTextResId) {
        mTvNothingHere.setText(nothingTextResId);
    }

    public void start(List<Event> data, boolean shouldStarVisible, boolean shouldStarBeOn, EventAdapter.OnStarPress callbacks) {
        List<Event> copy = new ArrayList<>(data);
        mAdapter = new EventAdapter(data, shouldStarBeOn, shouldStarVisible, generateCallbacks(callbacks));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        updateVisibility();
    }

    public void startWhenInfoAlreadyInXml(List<Event> data, EventAdapter.OnStarPress callbacks) {
        start(data, mStarVisible, mStarOn, callbacks);
    }


    private void updateVisibility() {
        if (mAdapter.getItemCount() == 0) {
            mRecyclerView.setVisibility(GONE);
            mTvNothingHere.setVisibility(VISIBLE);
        } else {
            mRecyclerView.setVisibility(VISIBLE);
            mTvNothingHere.setVisibility(GONE);
        }
    }

    public void addEvent(Event toAdd) {
        mAdapter.addEvent(toAdd);
        updateVisibility();
    }

    /**
     * generate new callbacks in case the callbacks are asking to remove items if user pressed the star
     *
     * because, maybe the item removal will cause the recycler view to have 0 rows,
     * so we should display the "nothing here" text instead
     *
     * @param originalCallbacks
     * @return
     */
    private EventAdapter.OnStarPress generateCallbacks(EventAdapter.OnStarPress originalCallbacks) {

        if (originalCallbacks == null
                ||
                !(
                        originalCallbacks.shouldRemoveWhenStarOffThanPressed()
                                ||
                                originalCallbacks.shouldRemoveWhenStarOnThanPressed()
                )
                ) {
            // no removing will happen, original callbacks are good
            return originalCallbacks;
        }

        // removing will happen. we will need to
        return new EventAdapter.OnStarPress() {
            boolean isRemovingWhenStarOnPressedOf = originalCallbacks.shouldRemoveWhenStarOnThanPressed();
            boolean isRemovingWhenStarOffPressedOn = originalCallbacks.shouldRemoveWhenStarOffThanPressed();

            @Override
            public void onStarOnPressedOff(Event event) {
                if (isRemovingWhenStarOnPressedOf) {
                    updateVisibility();
                }
                originalCallbacks.onStarOnPressedOff(event);
            }

            @Override
            public void onStarOffPressedOn(Event event) {
                if (isRemovingWhenStarOffPressedOn) {
                    updateVisibility();
                }
                originalCallbacks.onStarOffPressedOn(event);
            }

            @Override
            public void onPressView(Event event) {
                originalCallbacks.onPressView(event);
            }

            @Override
            public boolean shouldRemoveWhenStarOnThanPressed() {
                return isRemovingWhenStarOnPressedOf;
            }

            @Override
            public boolean shouldRemoveWhenStarOffThanPressed() {
                return isRemovingWhenStarOffPressedOn;
            }
        };
    }

    public void removeAt(int indexInHot) {
        mAdapter.removeAt(indexInHot);
    }
}
