package halamish.reem.remember.activity_main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.List;

import halamish.reem.remember.Const;
import halamish.reem.remember.R;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.User;
import halamish.reem.remember.view.event_recycler.EventAdapter;
import halamish.reem.remember.view.event_recycler.EventRecyclerViewWithHeader;

public class MainActivity extends AppCompatActivity implements FirebaseDbManager.OnDbReadyCallback<User> {

    private static final int IDX_NOT_IN_LIST = -1;
    ProgressBar mProgressBar;
    List<Event> mListMyEvents, mListHotEvents, mListSubscribedEvents;

    EventRecyclerViewWithHeader mViewMyEvents, mViewHotEvents, mViewSubscribedEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        for (View view : Arrays.asList(mViewHotEvents, mViewMyEvents, mViewSubscribedEvents)) {
            view.setVisibility(View.INVISIBLE);
        }

        FirebaseDbManager.getManager().requestUserDownload(this);
    }

    /**
     * if found duplicate between "hot" and "subscribed" events, remove it from hot
     */
    private void removeDuplicatesHotSubscribed() {
        if (mListHotEvents == null || mListSubscribedEvents == null) return;

        for (Event event : mListSubscribedEvents) {
            int indexInHot = mListHotEvents.indexOf(event);
            if (indexInHot > IDX_NOT_IN_LIST) {
                mListHotEvents.remove(indexInHot);
                mViewHotEvents.removeAt(indexInHot);
            }
        }
    }

    /**
     * is attached to the onClickListeners of the events shown
     * @param event
     */
    private void goToEventActivity(Event event) {
        //// TODO: 5/20/2017 implement this
    }

    /**
     * "subscribed" should only appear if there are hot events,
     * or if there are subscribed events.
     *
     * don't show empty "subscribed - just press star on the hot!" with no hot events
     */
    private void checkIfSubscribedNeedToShow() {
        boolean somethingInSubscribed = mListSubscribedEvents != null && mListSubscribedEvents.size() > 0;
        boolean somethingInHot = mListHotEvents != null && mListHotEvents.size() > 0;

        if (somethingInSubscribed || somethingInHot) {
            mViewSubscribedEvents.setVisibility(View.VISIBLE);
        } else {
            mViewSubscribedEvents.setVisibility(View.GONE);
        }
    }

    private void findViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.pb_main);
        mViewHotEvents = (EventRecyclerViewWithHeader) findViewById(R.id.erv_main_hot_events);
        mViewMyEvents = (EventRecyclerViewWithHeader) findViewById(R.id.erv_main_my_events);
        mViewSubscribedEvents = (EventRecyclerViewWithHeader) findViewById(R.id.erv_main_subscribed_events);
    }

    private void updateProgressBarIfNeeded() {
        if (mListMyEvents != null && mListHotEvents != null && mListSubscribedEvents != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * now we can start asking for updates
     * @param valueFromFirebase
     */
    @Override
    public void onDatabaseFinishedWorking(User valueFromFirebase) {
        String username = Const.username;
        FirebaseDbManager manager = FirebaseDbManager.getManager();



        manager.requestDownloadAllEventsUserCreated(username, valueFromFirebase1 -> {
            mListMyEvents = valueFromFirebase1;
            mViewMyEvents.startWhenInfoAlreadyInXml(valueFromFirebase1, this::goToEventActivity);
            mViewMyEvents.setVisibility(View.VISIBLE);
            updateProgressBarIfNeeded();

        });


        manager.requestDownloadAllEventsUserIsSubscribedTo(username, valueFromFirebase1 -> {
            mListSubscribedEvents = valueFromFirebase1;
            removeDuplicatesHotSubscribed();
            mViewSubscribedEvents.startWhenInfoAlreadyInXml(mListSubscribedEvents, new EventAdapter.OnStarPress() {
                @Override
                public void onPressView(Event event) {
                    goToEventActivity(event);
                }

                @Override
                public void onStarOnPressedOff(Event event) {
                    // todo add dialog "are you sure you want to unsubscirbe?"
                }
            });
            checkIfSubscribedNeedToShow();
            updateProgressBarIfNeeded();
        });



        manager.requestDownloadHotEvents(valueFromFirebase1 -> {
            mListHotEvents = valueFromFirebase1;
            mViewHotEvents.startWhenInfoAlreadyInXml(valueFromFirebase1, new EventAdapter.OnStarPress() {
                @Override
                public void onPressView(Event event) {
                    goToEventActivity(event);
                }

                @Override
                public boolean shouldRemoveWhenStarOffThanPressed() {
                    return true;
                }

                @Override
                public void onStarOffPressedOn(Event event) {
                    mListHotEvents.remove(event);
                    mListSubscribedEvents.add(event); // they maintain two different lists!
                    mViewSubscribedEvents.addEvent(event); // they maintain two different lists!
                }
            });
            if (valueFromFirebase1.size() > 0) {mViewHotEvents.setVisibility(View.VISIBLE);}
            updateProgressBarIfNeeded();
            checkIfSubscribedNeedToShow();

        });
    }
}
