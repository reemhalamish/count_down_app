package halamish.reem.remember.firebase.db;

import com.google.firebase.database.DatabaseReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.Util;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;

import static halamish.reem.remember.firebase.db.FirebaseDbManager.BRANCH_ALERTS;
import static halamish.reem.remember.firebase.db.FirebaseDbManager.BRANCH_ALERTS_DAILY;
import static halamish.reem.remember.firebase.db.FirebaseDbManager.BRANCH_EVENTS;
import static halamish.reem.remember.firebase.db.FirebaseDbManager.BRANCH_USERS;
import static halamish.reem.remember.firebase.db.FirebaseDbManager.BRANCH_USERS_UNMAE_EVENTS;
import static halamish.reem.remember.firebase.db.FirebaseDbManager.BRANCH_USERS_UNMAE_PHONES;
import static halamish.reem.remember.firebase.Helper.toFirebaseBranch;

/**
 * Created by Re'em on 5/20/2017.
 *
 * helper class that generates the updates to firebase
 */

@SuppressWarnings("JavaDoc")
class UpdatesGenerator {

    static Map<String, Object> requestUserPhoneUpload(String username, String phoneConstId, String phoneFirebaseNotificationToken) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(toFirebaseBranch(BRANCH_USERS, username, BRANCH_USERS_UNMAE_PHONES, phoneConstId), phoneFirebaseNotificationToken);
        return updates;
    }

    /**
     *
     *   uploads:
     *      @ the event to  /event
     *      @ if policy is DAILY - upload to /alert/daily
     *      @ if policy is WEEKLY - upload to /alert/{relevant_day}
     *
     *
     * @param db
     * @param eventToUpload
     * @param policy
     * @return
     */
    static Map<String, Object> requestNewEventUpload(DatabaseReference db, Event eventToUpload, EventNotificationPolicy policy) {
        Map<String, Object> uploads = new HashMap<>();
        uploads.put(toFirebaseBranch(BRANCH_EVENTS, eventToUpload.getUniqueId()), eventToUpload);
        switch (policy) {
            case NOTIFY_DAILY:
                uploads.put(toFirebaseBranch(BRANCH_ALERTS, BRANCH_ALERTS_DAILY, eventToUpload.getUniqueId(), LocalRam.getManager().getUsername()), true);
                break;
            case NOTIFY_WEEKLY:
                uploads.put(toFirebaseBranch(BRANCH_ALERTS, eventToUpload.weeklyAlertDay(), eventToUpload.getUniqueId(), LocalRam.getManager().getUsername()), true);
                break;
            case DONT_NOTIFY: break;
        }
        return uploads;
    }
    static Map<String, Object> requestUpdateEventUpload(Event eventToUpload) {
        Map<String, Object> uploads = new HashMap<>();
        uploads.put(toFirebaseBranch(BRANCH_EVENTS, eventToUpload.getUniqueId()), eventToUpload);
        return uploads;
    }

    /**
     * delete the event from /event
     * delete pointer from /alert/daily
     * delete pointer from /alert/{relevant_day}
     *
     * // todo ONEDAY how to delete from all the subscribers?
     * currnetly just if subscriber gets a null event they ignore it and delete for next times
     *
     * @param eventId
     * @param dayOfWeekAlert
     * @return
     */
    static Map<String, Object> requestDeleteEventUpload(String eventId, String dayOfWeekAlert) {
        Map<String, Object> uploads = new HashMap<>();
        uploads.put(toFirebaseBranch(BRANCH_EVENTS, eventId), null);
        uploads.put(toFirebaseBranch(BRANCH_ALERTS, BRANCH_ALERTS_DAILY, eventId), null);
        uploads.put(toFirebaseBranch(BRANCH_ALERTS, dayOfWeekAlert, eventId), null);
        return uploads;
    }



    static Map<String, Object> requestUpdatePolicyUpload(String username,
                                                         String eventId,
                                                         String eventWeeklyAlertDay,
                                                         EventNotificationPolicy newPolicy){
        Map<String, Object> updates = new HashMap<>();

        // first we put the "un-reqSubscribe" requests,
        // so that if the new policy will collide with those request, the policy will stay in the updates map
        updates.put(toFirebaseBranch(BRANCH_ALERTS, BRANCH_ALERTS_DAILY, eventId, username), null);
        updates.put(toFirebaseBranch(BRANCH_ALERTS, eventWeeklyAlertDay, eventId, username), null);

        // put new alerts for the user
        switch (newPolicy) {
            case NOTIFY_DAILY:
                updates.put(toFirebaseBranch(BRANCH_ALERTS, BRANCH_ALERTS_DAILY, eventId, username), true);
                break;
            case NOTIFY_WEEKLY:
                updates.put(toFirebaseBranch(BRANCH_ALERTS, eventWeeklyAlertDay, eventId, username), true);
                break;
            case DONT_NOTIFY:
                break;
        }
        return updates;
    }

    /**
     * recieves updates ONLY FOR THE USER BRANCH ( /user/{uid}/eventSubscribed/{eventId} : true )
     * AND NOT FOR EVENT BRANCH ( /event/{eventId}/subscribers/{userId} : true )
     * as the event needs also needs to increment it's counter, so the event is out of this scope
     * @param username
     * @param eventId
     * @return
     */
    static Map<String, Object> requestUserSubscribeEvent(String username, String eventId, String newPolicy) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(toFirebaseBranch(BRANCH_USERS, username, BRANCH_USERS_UNMAE_EVENTS, eventId), newPolicy);
        return updates;
    }

    /**
     * recieves updates ONLY FOR THE USER BRANCH ( /user/{uid}/eventSubscribed/{eventId} : null )
     * AND NOT FOR EVENT BRANCH ( /event/{eventId}/subscribers/{userId} : null )
     * as the event needs also needs to increment it's counter, so the event is out of this scope
     * @param username
     * @param eventId
     * @return
     */
    static Map<String, Object> requestUserUnsubscribeEvent(String username, String eventId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(toFirebaseBranch(BRANCH_USERS, username, BRANCH_USERS_UNMAE_EVENTS, eventId), null);
        return updates;
    }

    static Map<String, Object> requestUserUnsubscribeDeletedEvents(String username, Collection<String> eventsNoLongerExist) {
        Map<String, Object> updates = new HashMap<>();
        for (String eventId : eventsNoLongerExist) {
            updates.put(toFirebaseBranch(BRANCH_USERS, username, BRANCH_USERS_UNMAE_EVENTS, eventId), null);
        }
        return updates;
    }


}
