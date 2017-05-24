package halamish.reem.remember.activity_main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.activity_create_event.CreateEventActivity;
import halamish.reem.remember.firebase.db.FirebaseDbException;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;
import halamish.reem.remember.firebase.db.entity.PartiallyEventForGui;
import halamish.reem.remember.firebase.db.entity.User;
import halamish.reem.remember.view.event_recycler.EventAdapter;
import halamish.reem.remember.view.event_recycler.EventRecyclerViewWithHeader;

public class MainActivity extends AppCompatActivity implements FirebaseDbManager.OnDbReadyCallback<User> {

    private static final int IDX_NOT_IN_LIST = -1;
    private static final int REQUEST_CREATE_NEW_EVENT = Util.uniqueIntNumber.incrementAndGet();
    ProgressBar mProgressBar;
    List<Event> mListMyEvents, mListHotEvents, mListSubscribedEvents;
    FloatingActionButton mFab;
    View mMainView;

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

        mFab.setOnClickListener(view -> {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this, mFab, "fab");

            startActivityForResult(
                    new Intent(MainActivity.this, CreateEventActivity.class),
                    REQUEST_CREATE_NEW_EVENT,
                    options.toBundle()
            );
        });
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
     * if found duplicate between "hot" and "i created" events, remove it from hot
     */
    private void removeDuplicatesHotMy() {
        if (mListHotEvents == null || mListMyEvents == null) return;

        for (Event event : mListMyEvents) {
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
        // todo! maybe different version for my events (which i can edit) and other people's events (which i can only subscribe?)
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE_NEW_EVENT && resultCode == RESULT_OK) {
            PartiallyEventForGui fromNewActivity =
                    (PartiallyEventForGui) data.getSerializableExtra(CreateEventActivity.NEW_CREATED_EVENT);
            Event newbie = new Event(fromNewActivity);
            EventNotificationPolicy policy = EventNotificationPolicy.fromString(fromNewActivity.getPolicy());
            addNewEventUi(newbie);

            Snackbar snackbar = Snackbar.make(mMainView, getString(R.string.new_event_created), BaseTransientBottomBar.LENGTH_SHORT);
            snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    if (event != DISMISS_EVENT_ACTION) {
                        addNewEventDb(newbie, policy);
                    }
                }
            });

            // todo add here snckbar option to "DISMISS" in which it will call the Storage Manager and cancel the pictures upload

            snackbar.show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * is called when it's time to add the event in the db
     * @param newbie the event
     * @param policy the policy
     */
    private void addNewEventDb(Event newbie, EventNotificationPolicy policy) {
        try {
            FirebaseDbManager.getManager().uploadNewEvent(newbie, policy, null);
        } catch (FirebaseDbException.NotEventCreator notEventCreator) {
            Snackbar.make(mMainView, R.string.something_went_wrong, BaseTransientBottomBar.LENGTH_SHORT).show();
        }

    }
    private void addNewEventUi(Event newbie) {
        mListMyEvents.add(0, newbie);
        mViewMyEvents.addEvent(newbie);
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
        mFab = (FloatingActionButton) findViewById(R.id.fab_main);
        mMainView = findViewById(R.id.activity_main);
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
        String username = Util.username;
        FirebaseDbManager manager = FirebaseDbManager.getManager();



        manager.requestDownloadAllEventsUserCreated(username, eventsUserCreated -> {
            mListMyEvents = eventsUserCreated;
            mViewMyEvents.startWhenInfoAlreadyInXml(
                    eventsUserCreated,
                    new EventAdapter.OnStarPress() {
                        public void onPressView(Event event) {goToEventActivity(event);}
                    }
            );
            mViewMyEvents.setVisibility(View.VISIBLE);
            removeDuplicatesHotMy();
            updateProgressBarIfNeeded();

        });


        manager.requestDownloadAllEventsUserIsSubscribedTo(username, eventsUserSubscribed -> {
            mListSubscribedEvents = eventsUserSubscribed;
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



        manager.requestDownloadHotEvents(hotEvents -> {
            mListHotEvents = hotEvents;
            mViewHotEvents.startWhenInfoAlreadyInXml(hotEvents, new EventAdapter.OnStarPress() {
                @Override
                public void onPressView(Event event) {
                    goToEventActivity(event);
                }

                @Override
                public void onStarOffPressedOn(Event event) {
                    mListHotEvents.remove(event);
                    mViewHotEvents.remove(event);
                    mListSubscribedEvents.add(event); // they maintain two different lists!
                    mViewSubscribedEvents.addEvent(event); // they maintain two different lists!
                }
            });

            removeDuplicatesHotSubscribed();
            removeDuplicatesHotMy();
            updateProgressBarIfNeeded();
            checkIfSubscribedNeedToShow();
            if (hotEvents.size() > 0) {mViewHotEvents.setVisibility(View.VISIBLE);}

        });
    }
}
