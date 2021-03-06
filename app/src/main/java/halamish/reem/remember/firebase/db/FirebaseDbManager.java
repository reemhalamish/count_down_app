package halamish.reem.remember.firebase.db;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import halamish.reem.remember.LocalStorageUsernamePhone;
import halamish.reem.remember.LocalRam;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;
import halamish.reem.remember.firebase.db.entity.User;
import lombok.Getter;

import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestDeleteEventUpload;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestHideEventUpload;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestNewEventUpload;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestUpdateEventUpload;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestUpdatePolicyUpload;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestUserPhoneUpload;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestUserSubscribeEvent;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestUserUnsubscribeDeletedEvents;
import static halamish.reem.remember.firebase.db.UpdatesGenerator.requestUserUnsubscribeEvent;

/**
 * Created by Re'em on 5/17/2017.
 *
 *
 * that's the manager. this is the gateway between the users (e.g. the activities etc) to the db internet calls
 *
 * uses to helper classes:
 *  Helper - which helps with some helper functions
 *  UpdatesGenerator - which helps with creating the updates (and structuring the data) for firebase DB
 *
 */

@SuppressWarnings("ALL")
public class FirebaseDbManager {
    private static final String TAG = FirebaseDbManager.class.getSimpleName();

    static final String BRANCH_USERS = "user";
    static final String BRANCH_USERS_UNMAE_PHONES = "phones";
    static final String BRANCH_USERS_UNMAE_SUBSCRIBED = "eventSubscribed";
    static final String BRANCH_USERS_UNMAE_HIDDEN = "eventHidden";
    static final String BRANCH_EVENTS = "event";
    static final String BRANCH_ALERTS = "alert";
    static final String BRANCH_ALERTS_DAILY = "daily";

    private static final int MAX_HOT_EVENTS_SHOW = 10;



    public interface OnDbError {
        default void onError(FirebaseDbException exception) {
            exception.printStackTrace();
            Log.e(TAG, exception.getMessage());
            Log.e(TAG, exception.getClass().getName());
        }
    }
//    public interface CallbackOnRequestEventsList extends DbReadyCallback<List<Event>> {
//        void onEventsListReady(List<Event> events);
//    }
//    public interface CallbackOnFinishedWorkingOnEvent extends OnDbError {
//        void onEventReady(Event event);
//    }
//
//    public interface CallbackOnDbFinished extends OnDbError {
//        void onActionFinished();
//    }

    public interface OnDbReadyCallback<E> extends OnDbError {
        void onDatabaseFinishedWorking(E valueFromFirebase);
    }
    private static class DoNothingOnDbReady<E> implements OnDbReadyCallback<E> {
        public void onDatabaseFinishedWorking(E valueFromFirebase) {}
    }

    @Getter private static FirebaseDbManager manager;
    private DatabaseReference db;

    public static void init(OnDbReadyCallback<User> callback) {
        manager = new FirebaseDbManager();
        manager.requestUserDownloadRefresh(callback);
    }

    private FirebaseDbManager() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    public void requestUserDownloadRefresh(@Nullable OnDbReadyCallback<User> callbackArg) {
        final OnDbReadyCallback<User> callback;
        if (callbackArg == null) {callback = new DoNothingOnDbReady<>();} else { callback = callbackArg; }
        String username = LocalRam.getManager().getUsername();
        if (username == null) {
            username = db.child(BRANCH_USERS).push().getKey();
            LocalStorageUsernamePhone.getManager().setUserName(username);
            LocalRam.getManager().setUsername(username);
        }

        db
                .child(BRANCH_USERS)
                .child(LocalRam.getManager().getUsername())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LocalRam.getManager().setUser(dataSnapshot.getValue(User.class));
                            callback.onDatabaseFinishedWorking(LocalRam.getManager().getUser());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(new FirebaseDbException(databaseError.getMessage()));
                    }
                });
    }


    public void uploadUserPhoneUpdate(String username, String phoneConstId, String phoneFirebaseNotificationToken) {
        if (phoneConstId == null || phoneFirebaseNotificationToken == null) return;

        db.updateChildren(requestUserPhoneUpload(username, phoneConstId, phoneFirebaseNotificationToken), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                requestUserDownloadRefresh(null);
            }
        });
    }


    public void uploadNewEvent(Event event, EventNotificationPolicy eventNotificationPolicy) throws FirebaseDbException.NotEventCreator {
        uploadNewEvent(event, eventNotificationPolicy, new DoNothingOnDbReady<>());
    }


    /**
     *  upload new event to firebase.
     *
     * @param eventToUpload
     * @param policy
     * @param callback
     */
    public void uploadNewEvent(final Event eventToUpload,
                               EventNotificationPolicy policy,
                               final OnDbReadyCallback<Event> callback)
            throws FirebaseDbException.NotEventCreator {

        handleNoInternet();
        if (eventToUpload == null) return;

        if (! eventToUpload.getCreator().equals(LocalRam.getManager().getUsername())) {
            throw new FirebaseDbException.NotEventCreator();
        }

        if (eventToUpload.isPublic)
            eventToUpload._query_publicSubscribers = Event.QUERY_PUBLIC_SUBSCRIBERS_IS_PUBLIC_NO_SUBSCRIBERS;
        else
            eventToUpload._query_publicSubscribers = Event.QUERY_PUBLIC_SUBSCRIBERS_IS_PRIVATE;

        uploadToDb(
                db,
                requestNewEventUpload(
                        db,
                        eventToUpload,
                        policy),
                () -> callback.onDatabaseFinishedWorking(eventToUpload)
        );

    }


    public void updateExistingEvent(Event event) throws FirebaseDbException.NotEventCreator {
        updateExistingEvent(event, new DoNothingOnDbReady<>());
    }


    public void updateExistingEvent(final Event toUpdload, final OnDbReadyCallback<Event> callback)
            throws FirebaseDbException.NotEventCreator
    {
        if (toUpdload == null) return;

        if (toUpdload.isPublic()) toUpdload._query_publicSubscribers = String.valueOf(toUpdload.subscribersAmount);
        else toUpdload._query_publicSubscribers = Event.QUERY_PUBLIC_SUBSCRIBERS_IS_PRIVATE;

        if (! toUpdload.getCreator().equals(LocalRam.getManager().getUsername())) {
            throw new FirebaseDbException.NotEventCreator();
        }

        handleNoInternet();


        uploadToDb(db, requestUpdateEventUpload(toUpdload), () -> {
                    if (callback != null) callback.onDatabaseFinishedWorking(toUpdload);
                }
        );
    }

    public void reqDownloadEvent(String eventId, OnDbReadyCallback<Event> callback) {
        handleNoInternet();
        if (eventId == null || callback == null) return;

        db.child(BRANCH_EVENTS).child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if (event == null) {
                    callback.onError(new FirebaseDbException.NoSuchItem());
                } else {
                    callback.onDatabaseFinishedWorking(event);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(new FirebaseDbException(databaseError.getMessage()));
            }
        });
    }

    public void reqDeleteEvent(String eventId) throws FirebaseDbException.NotEventCreator {
        reqDeleteEvent(eventId,  new DoNothingOnDbReady<>());
    }

    public void reqDeleteEvent(String eventId, final OnDbReadyCallback<Void> callback) throws FirebaseDbException.NotEventCreator {

        handleNoInternet();
        if (eventId == null) return;

        reqDownloadEvent(eventId, event -> {
            if (event.getCreator().equals(LocalRam.getManager().getUsername())) {
                uploadToDb(db, requestDeleteEventUpload(eventId, event.weeklyAlertDay()), () -> callback.onDatabaseFinishedWorking(null));
            } else {
                callback.onError(new FirebaseDbException.NotEventCreator());
            }
        });
    }



    /**
     * subscribes the user to a new event, and updates their notification policy -
     *  wrapper for reqSubscribe with a callback
     *
     * @param eventId
     * @param username
     * @param policy
     * @param callback
     */
    public void reqSubscribe(String eventId,
                             String weeklyAlertDay,
                             String username,
                             EventNotificationPolicy policy)
    {reqSubscribe(eventId, weeklyAlertDay, username, policy, new DoNothingOnDbReady<>());}

    /**
     * subscribes the user to a new event, and updates their notification policy -
     *
     * first, in a transaction, updates the event's subscribersAmount amount and puts the user as a subscriber
     *
     * than updates the policy and use the calee's callback
     *
     * @param eventId
     * @param username
     * @param policy
     * @param callback
     */
    public void reqSubscribe(String eventId,
                             String weeklyAlertDay,
                             String username,
                             EventNotificationPolicy policy,
                             final OnDbReadyCallback<Void> callback) {

        handleNoInternet();
        if (eventId == null || weeklyAlertDay == null || username == null || policy == null) return;

        db.child(BRANCH_EVENTS).child(eventId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Event event = mutableData.getValue(Event.class);
                if (event == null) {
                    callback.onError(new FirebaseDbException("no such event in the db! (key: " + eventId + ")"));
                    return Transaction.success(mutableData);
                }

                event.subscribersAmount++;
                if (event.isPublic()) {
                    event._query_publicSubscribers = String.valueOf(event.subscribersAmount);
                } else {
                    event._query_publicSubscribers = Event.QUERY_PUBLIC_SUBSCRIBERS_IS_PRIVATE;
                }

                mutableData.setValue(event);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    callback.onError(new FirebaseDbException(databaseError.toString()));
                } else {
                    Map<String, Object> updates = requestUpdatePolicyUpload(username, eventId, weeklyAlertDay, policy);
                    Map<String, Object> updatesSubscribe = requestUserSubscribeEvent(username, eventId, policy.toString());
                    updates.putAll(updatesSubscribe);
                    uploadToDb(db, updates, () -> callback.onDatabaseFinishedWorking(null));
                }
            }
        });
    }

    public String getNewEventId() {
        return  db.child(BRANCH_EVENTS).push().getKey();
    }

    public void reqUnsubscribe(String username, String eventId) {
        reqUnsubscribe(username, eventId, new DoNothingOnDbReady<>());
    }

    public void reqUnsubscribe(String username, String eventId, OnDbReadyCallback<Event> callbackArg) {
        final OnDbReadyCallback<Event> callback;
        if (callbackArg == null) {callback = new DoNothingOnDbReady<>();} else { callback = callbackArg; }


        handleNoInternet();
        if (eventId == null || username == null || callback == null) return;

        db.child(BRANCH_EVENTS).child(eventId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Event event = mutableData.getValue(Event.class);
                if (event==null) {
                    callback.onError(new FirebaseDbException("no such event in db!"));
                    return Transaction.success(mutableData);
                }

                event.subscribersAmount--;

                if (event.isPublic) event._query_publicSubscribers = String.valueOf(event.subscribersAmount);
                else event._query_publicSubscribers = Event.QUERY_PUBLIC_SUBSCRIBERS_IS_PRIVATE;

                mutableData.setValue(event);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    callback.onError(new FirebaseDbException(databaseError.getMessage()));
                }
                Map<String, Object> updates = requestUpdatePolicyUpload(
                        username,
                        eventId,
                        "0" ,
                        EventNotificationPolicy.DONT_NOTIFY);
                Map<String, Object> updatesSubscribe = requestUserUnsubscribeEvent(username, eventId);
                updates.putAll(updatesSubscribe);
                uploadToDb(db, updates, () -> {return;});
                Event completedEvent = dataSnapshot.getValue(Event.class);
                if (completedEvent != null) { callback.onDatabaseFinishedWorking(completedEvent); }

            }
        });
    }

    public void reqHideHotEventFromUesr(String username, String eventId) {
        reqHideHotEventFromUesr(username, eventId, new DoNothingOnDbReady<>());
    }


    public void reqHideHotEventFromUesr(String username, String eventId, OnDbReadyCallback<Void> callback) {
        Map<String, Object> updates = requestHideEventUpload(username, eventId);
        uploadToDb(db, updates, () -> {callback.onDatabaseFinishedWorking(null);});
    }
    /**
     * the user still wants to subscribe, but they want different notification policy.
     *
     * removes last policies if exists -
     *      delete /alert/daily/{eventId}/{username}
     *      delete /alert/{eventWeeklyDay}/{eventId}/{username}
     *
     * add based on the new policy:
     *      if DAILY:
     *          add /alert/daily/{eventId}/{username} : true
     *      if WEEKLY:
     *          add /alert/{eventWeeklyDay}/{eventId}/{username} : true
     *
     *
     * @param eventId
     * @param eventWeeklyAlertDay
     * @param username
     * @param newPolicy the notification policy
     * @param callback the callback to run when finished
     */
    public void reqUpdateNotificationPolicy(String eventId,
                                            String eventWeeklyAlertDay,
                                            String username,
                                            EventNotificationPolicy newPolicy,
                                            OnDbReadyCallback<Void> callback) {
        handleNoInternet();
        final OnDbReadyCallback<Void> callbackToUse;
        if (callback == null)
            callbackToUse = new DoNothingOnDbReady<>();
        else
            callbackToUse = callback;

        uploadToDb(db,
                requestUpdatePolicyUpload(username, eventId, eventWeeklyAlertDay, newPolicy),
                () -> callbackToUse.onDatabaseFinishedWorking(null));

    }

    /**
     * get all events with creator==username
     * @param username
     * @param callback
     */
    public void requestDownloadAllEventsUserCreated(String username, final OnDbReadyCallback<List<Event>> callback) {
        handleNoInternet();
        if (username == null || callback == null) return;

        db
                .child(BRANCH_EVENTS)
                .orderByChild("creator")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Event> retVal = new ArrayList<Event>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Event childEvent = child.getValue(Event.class);
                            if (childEvent != null) {
                                retVal.add(childEvent);
                            }
                        }
                        callback.onDatabaseFinishedWorking(retVal);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "error retrieving user events!");
                        Log.e(TAG, databaseError.getDetails());
                        Log.e(TAG, databaseError.getMessage());
                    }
                });
    }

    /**
     * may return events user is already subscribed to!
     * @param callback
     */
    public void requestDownloadHotEvents(OnDbReadyCallback<List<Event>> callback) {
        handleNoInternet();

        User user = LocalRam.getManager().getUser();

        db.child(BRANCH_EVENTS).orderByChild("_query_publicSubscribers").startAt(Event.QUERY_PUBLIC_SUBSCRIBERS_IS_PUBLIC_NO_SUBSCRIBERS).limitToFirst(MAX_HOT_EVENTS_SHOW).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Event> retVal = new ArrayList<Event>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Event event = child.getValue(Event.class);
                    if (event != null) if (!user.isHidden(event.uniqueId)) retVal.add(event);
                    else Log.e(TAG, "got null event on requestDownloadHotEvents() with event id: " + child.getKey());
                }
                callback.onDatabaseFinishedWorking(retVal);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                callback.onError(new FirebaseDbException(databaseError.getMessage()));
            }
        });
    }

    public void requestDownloadAllEventsUserIsSubscribedTo(String username, OnDbReadyCallback<List<Event>> callback) {
        handleNoInternet();

        User user = LocalRam.getManager().getUser();
        if (user == null) {
            requestUserDownloadRefresh(valueFromFirebase -> requestDownloadAllEventsUserIsSubscribedTo(username, callback));
            return;
        }

        if (!user.hasSubscribedEvents()) {
            // user has no subscribed events.
            callback.onDatabaseFinishedWorking(new ArrayList<>());
            return;
        }

        Set<String> eventsNoLongerExist = new HashSet<>();
        List<Event> retVal = new ArrayList<>();

        for (Map.Entry<String, String> eventIdToSubscriberPolicy: user.subscriptionsAsEntrySet()) {
            String eventId = eventIdToSubscriberPolicy.getKey();
            String ntfcPolicy = eventIdToSubscriberPolicy.getValue();

            db.child(BRANCH_EVENTS).child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event fromFirebase = dataSnapshot.getValue(Event.class);
                    if (fromFirebase == null) {
                        eventsNoLongerExist.add(dataSnapshot.getKey());
                    } else {
                        fromFirebase.set_local_subscriberNtfcPolicy(ntfcPolicy);

                        retVal.add(fromFirebase);
                    }

                    // if this was the last event to retrieve, continue!
                    if (retVal.size() + eventsNoLongerExist.size() == user.countSubscribedEvents()) {
                        if (eventsNoLongerExist.size() > 0) {
                            reqUserUnfollowDeletedEvents(eventsNoLongerExist, username);
                        }
                        callback.onDatabaseFinishedWorking(retVal);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(new FirebaseDbException(databaseError.getMessage()));
                }
            });
        }
    }

    /**
     * update the db that those events no longer exist so remove them from the user
     * @param eventsNoLongerExist
     */
    private void reqUserUnfollowDeletedEvents(Set<String> eventsNoLongerExist, String username) {

        uploadToDb(
                db,
                requestUserUnsubscribeDeletedEvents(username, eventsNoLongerExist),
                () -> requestUserDownloadRefresh(null)
        );
    }

    interface UpdateFinished extends FirebaseDbManager.OnDbError {void onUpdateFinished();}
    static void uploadToDb(DatabaseReference db, Map<String, Object> updatesRelativeToMain, UpdateFinished callback) {
        db.updateChildren(updatesRelativeToMain, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    callback.onError(new FirebaseDbException(databaseError.getMessage()));
                } else {
                    callback.onUpdateFinished();
                }
            }
        });
    }

    /**
     * handles situation with no internet.
     *
     * @return true iff user is in area without internet
     */
    private boolean handleNoInternet() {
//        if (!checkInternet(context)) {
//            Toast.makeText(context, R.string.ux_prompt_no_internet_slower_bg_data, Toast.LENGTH_SHORT).show();
//            return true;
//        }
        return false;
    }

}
