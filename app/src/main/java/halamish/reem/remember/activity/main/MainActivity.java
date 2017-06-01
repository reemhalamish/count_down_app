package halamish.reem.remember.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.activity.create_event.CreateEditEventActivity;
import halamish.reem.remember.activity.view_event.ViewEventActivity;
import halamish.reem.remember.firebase.FirebaseInitiationController;
import halamish.reem.remember.firebase.db.FirebaseDbException;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;
import halamish.reem.remember.view.event_recycler.EventAdapter;
import halamish.reem.remember.view.event_recycler.EventRecyclerViewWithHeader;

@SuppressWarnings("JavaDoc")
public class MainActivity extends AppCompatActivity implements FirebaseInitiationController.OnFirebaseReadyToStartCallback {

    private static final int IDX_NOT_IN_LIST = -1;
    private static final int REQUEST_CREATE_NEW_EVENT = Util.uniqueIntNumber.incrementAndGet();
    private static final int REQUEST_VIEW_EVENT = Util.uniqueIntNumber.incrementAndGet();
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DELAY_ADDING_NEW_EVENTS_MS = 500;
    private static final int DELAY_NOTHING_MS = 0;
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

        FirebaseInitiationController.getManager().whenReady(this);

    }

    /**
     * if found duplicate between "hot" and "subscribed" events, remove it from hot
     */
    private void removeDuplicatesHotSubscribed() {
        if (mListHotEvents == null || mListSubscribedEvents == null) return;

        List<Event> duplicates = new ArrayList<>();
        for (Event event : mListSubscribedEvents) {
            if (mListHotEvents.contains(event)) {
                duplicates.add(event);
            }
        }
        for (Event event : duplicates) {
            mListHotEvents.remove(event);
            mViewHotEvents.remove(event);
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
    private void goToViewEventActivity(Event event) {
        Intent viewEvent = new Intent(this, ViewEventActivity.class);
        viewEvent.putExtra(ViewEventActivity.INPUT_EVENT, event);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.
                        makeSceneTransitionAnimation(this, mFab, "fab");

        startActivityForResult(viewEvent, REQUEST_VIEW_EVENT, options.toBundle());
        // for result because maybe the user will subscribe or edit the event!
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
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_VIEW_EVENT) {
            Event updatedEvent = (Event) data.getSerializableExtra(ViewEventActivity.OUTPUT_EVENT);
            switch (data.getStringExtra(ViewEventActivity.OUTPUT_ACTION)) {
                case ViewEventActivity.OUTPUT_ACTION_EDITED:
                    handleEventEdited(updatedEvent);
                    break;
                case ViewEventActivity.OUTPUT_ACTION_SUBSCRIBED:
                    boolean isNowSubscribed = data.getBooleanExtra(ViewEventActivity.OUTPUT_IS_EVENT_SUBSCRIBED, true);
                    if (isNowSubscribed) {
                        handleNewSubscription(updatedEvent);
                    } else {
                        // was subscribed - not anymore!
                        handleUnsubscribe(updatedEvent);
                    }
                    break;
            }
        }

        if (requestCode == REQUEST_CREATE_NEW_EVENT) {
            Event eventFromCreatedActivity =
                    (Event) data.getSerializableExtra(CreateEditEventActivity.OUTPUT_EVENT);

            handleNewCreatedEvent(eventFromCreatedActivity);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleUnsubscribe(Event updatedEvent) {
        //update the LocalRam - remove eventId from user.map<eventSubscribed : notificationPolicy>
        LocalRam.getManager().getUser().eventSubscribed.remove(updatedEvent.getUniqueId());

        // update Firebase db
        FirebaseDbManager
                .getManager()
                .reqUnsubscribe(
                        LocalRam.getManager().getUsername(),
                        updatedEvent.getUniqueId(),
                        null
                );

        Event eventToRemoveFromSubscribed = null;
        for (Event event :mListSubscribedEvents) {
            if (event.equals(updatedEvent)) {
                eventToRemoveFromSubscribed = event;
                break;
            }
        }
        if (eventToRemoveFromSubscribed != null) {
            mListSubscribedEvents.remove(eventToRemoveFromSubscribed);
            mViewSubscribedEvents.remove(eventToRemoveFromSubscribed);
        } else {
            // event already removed. don't do anything else for updating the UI
            return;
        }

        if (updatedEvent.isPublic()) {
            new Handler().postDelayed(() -> {
                mListHotEvents.add(updatedEvent);
                mViewHotEvents.addEvent(updatedEvent);
            }, DELAY_ADDING_NEW_EVENTS_MS);

        }

        checkIfSubscribedNeedToShow(); // maybe the event subscribed was the only only one in "subscribed" section, and the "hot" section is empty!
    }

    private void handleNewSubscription(Event updatedEvent) {
        //update the LocalRam - add to user.map<eventSubscribed : notificationPolicy>
        LocalRam.getManager().getUser().eventSubscribed.put(updatedEvent.getUniqueId(), updatedEvent.get_local_subscriberNtfcPolicy());

        // update Firebase db
        FirebaseDbManager
                .getManager()
                .reqSubscribe(
                        updatedEvent.getUniqueId(),
                        updatedEvent.weeklyAlertDay(),
                        LocalRam.getManager().getUsername(),
                        EventNotificationPolicy.fromString(updatedEvent.get_local_subscriberNtfcPolicy()),
                        null
                );

        // if event is in the subscrobed already, don't do anything else!
        if (mListSubscribedEvents.contains(updatedEvent)) return;

        // remove if exists in "hot events"
        Event eventInHotEvents = null;
        for (Event event :mListHotEvents) {
            if (event.equals(updatedEvent)) {
                eventInHotEvents = event;
                break;
            }
        }
        if (eventInHotEvents != null) {
            mListHotEvents.remove(eventInHotEvents);
            mViewHotEvents.remove(eventInHotEvents);
        }


        // finally - add
        int delayAnimationBeforeAdding;
        if (eventInHotEvents != null) { // so wait with the adding, until it's gone from there
            delayAnimationBeforeAdding = DELAY_ADDING_NEW_EVENTS_MS;
        } else {
            delayAnimationBeforeAdding = DELAY_NOTHING_MS;
        }

        new Handler().postDelayed(() -> {
            mListSubscribedEvents.add(updatedEvent);
            mViewSubscribedEvents.addEvent(updatedEvent);

        }, delayAnimationBeforeAdding);
    }

    private void handleEventEdited(Event updatedEvent) {
        Event prevEvent = null;
        for (Event prevEventIterate : mListMyEvents) {
            if (prevEventIterate.equals(updatedEvent)) {
                prevEvent = prevEventIterate;
                break;
            }
        }
        if (prevEvent == null) Log.e(TAG, "couldn't find edited event!");
        else {
            mListMyEvents.remove(prevEvent);
            mListHotEvents.add(updatedEvent);
            mViewMyEvents.update(prevEvent, updatedEvent);

        }
    }

    private void handleNewCreatedEvent(Event eventFromCreatedActivity) {
        EventNotificationPolicy policy = EventNotificationPolicy.fromString(eventFromCreatedActivity.getCreatorNtfcPolicy());
        addNewEventUi(eventFromCreatedActivity);

        Snackbar snackbar = Snackbar.make(mMainView, getString(R.string.new_event_created), BaseTransientBottomBar.LENGTH_LONG);
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event != DISMISS_EVENT_ACTION) {
                    addNewEventDb(eventFromCreatedActivity, policy);
                }
            }
        });

        // todo add here snckbar option to "DISMISS" in which it will call the Storage Manager and cancel the pictures upload

        snackbar.show();
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


    @Override
    protected void onStart() {
        super.onStart();
        mViewMyEvents.onStart();
        mViewHotEvents.onStart();
        mViewSubscribedEvents.onStart();
    }

    /**
     * now we can start asking for updates
     */
    @Override
    public void onFirebaseReady() {
        String username = LocalRam.getManager().getUsername();
        FirebaseDbManager manager = FirebaseDbManager.getManager();

        // fab - create new event
        mFab.setVisibility(View.VISIBLE);
        mFab.setOnClickListener(view -> {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this, mFab, "fab");

            Intent createNewEventIntent = new Intent(MainActivity.this, CreateEditEventActivity.class);
            createNewEventIntent.putExtra(CreateEditEventActivity.INPUT_IS_NEW, true);
            createNewEventIntent.putExtra(CreateEditEventActivity.INPUT_EVENT, Event.createNewHalfFilled());

            startActivityForResult(createNewEventIntent, REQUEST_CREATE_NEW_EVENT, options.toBundle());
        });


        manager.requestDownloadAllEventsUserCreated(username, eventsUserCreated -> {
            mListMyEvents = eventsUserCreated;
            mViewMyEvents.startWhenInfoAlreadyInXml(
                    eventsUserCreated,
                    this,
                    new EventAdapter.OnStarPress() {
                        public void onPressView(Event event) {
                            goToViewEventActivity(event);}
                    }
            );
            mViewMyEvents.setVisibility(View.VISIBLE);
            removeDuplicatesHotMy();
            updateProgressBarIfNeeded();

        });


        manager.requestDownloadAllEventsUserIsSubscribedTo(username, eventsUserSubscribed -> {
            mListSubscribedEvents = eventsUserSubscribed;
            removeDuplicatesHotSubscribed();
            mViewSubscribedEvents.startWhenInfoAlreadyInXml(mListSubscribedEvents, this, new EventAdapter.OnStarPress() {
                @Override
                public void onPressView(Event event) {
                    goToViewEventActivity(event);
                }

                @Override
                public void onStarOnPressedOff(Event event) {
                    if (event.isPublic()) {
                        handleUnsubscribe(event);
                        return;
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.sure_wanna_unsubscribe)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                                handleUnsubscribe(event);
                                dialogInterface.dismiss();
                            })
                            .show();
                }
            });
            checkIfSubscribedNeedToShow();
            updateProgressBarIfNeeded();
        });



        manager.requestDownloadHotEvents(hotEvents -> {
            mListHotEvents = hotEvents;
            mViewHotEvents.startWhenInfoAlreadyInXml(hotEvents, this, new EventAdapter.OnStarPress() {
                @Override
                public void onPressView(Event event) {
                    goToViewEventActivity(event);
                }

                @Override
                public void onStarOffPressedOn(Event event) {
                    handleNewSubscription(event);
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
