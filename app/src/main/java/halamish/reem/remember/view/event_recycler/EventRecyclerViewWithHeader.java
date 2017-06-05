package halamish.reem.remember.view.event_recycler;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import halamish.reem.remember.R;
import halamish.reem.remember.RememberApp;
import halamish.reem.remember.activity.main.MainActivity;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.view.HeaderView;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

/**
 * Created by Re'em on 5/20/2017.
 *
 * is used to display (and set listeners on) a list of events
 */

public class EventRecyclerViewWithHeader extends RelativeLayout {
    private static final int REMOVAL_UPDATE_DELAY_MS = MainActivity.DELAY_UI_CHANGE_EVENTS_MS;
    HeaderView mHeaderView;
    RecyclerView mRecyclerView;
    TextView mTvNothingHere;
    EventAdapter mAdapter;
    boolean mStarVisible;
    boolean mStarOn;
    int mEditModeNumItems;
    Map<String, Bitmap> eventIdToBitmap;

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

//        mRecyclerView.setHasFixedSize(true);

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
                eventList.add(Event.createMock());
            }
            startWhenInfoAlreadyInXml(eventList, null, null);
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

    public void start(List<Event> data, Context context, boolean shouldStarVisible, boolean shouldStarBeOn, EventAdapter.OnStarPress callbacks) {
        List<Event> copy = new ArrayList<>(data);
        mAdapter = new EventAdapter(copy, shouldStarBeOn, shouldStarVisible, callbacks, context, isInEditMode());
        eventIdToBitmap = new HashMap<>();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false){
                    @Override public boolean supportsPredictiveItemAnimations() {return true;}
                });
        mRecyclerView.setItemAnimator(new FadeInAnimator());

        updateVisibility();
    }

    public void startWhenInfoAlreadyInXml(List<Event> data, Context context, EventAdapter.OnStarPress callbacks) {
        start(data, context, mStarVisible, mStarOn, callbacks);
    }


    private void updateVisibility() {
        if (mAdapter.getItemCount() == 0) {
//            mRecyclerView.setVisibility(GONE);
            mTvNothingHere.setVisibility(VISIBLE);
        } else {
//            mRecyclerView.setVisibility(VISIBLE);
            mTvNothingHere.setVisibility(GONE);
        }
    }

    public void addEvent(Event toAdd) {
        mAdapter.addEvent(toAdd);
        updateVisibility();
    }

    public void removeAt(int indexInHot) {
        mAdapter.removeAt(indexInHot);
        new Handler(Looper.myLooper()).postDelayed(this::updateVisibility, REMOVAL_UPDATE_DELAY_MS);
    }

    public void remove(Event event) {
        mAdapter.remove(event);
        new Handler(Looper.myLooper()).postDelayed(this::updateVisibility, REMOVAL_UPDATE_DELAY_MS);
    }

    /**
     * is called when the activity's onStart() is called
     */
    public void onStart() {
        if (mAdapter != null) {
            // it can be null if there was time until the recyclerView was created, and onStart was already called
            mAdapter.startThumbnailListening();
        }
    }

    /**
     * is called when the activity's onStop() is called
     */
    public void onStop() {
        mAdapter.stopThumbnailListening();

    }

    /**
     * remove the prev event and insert the new one
     *
     * @param prevEvent
     * @param updatedEvent
     */
    public void update(Event prevEvent, Event updatedEvent) {
        mAdapter.update(prevEvent, updatedEvent);
    }
}
