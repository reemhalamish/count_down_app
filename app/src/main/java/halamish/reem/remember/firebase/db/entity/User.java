package halamish.reem.remember.firebase.db.entity;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Created by Re'em on 5/19/2017.
 *
 * represents a user in firebase DB
 */

@AllArgsConstructor
@IgnoreExtraProperties
public class User {
    public Map<String, String> phones;
    public Map<String, String> eventSubscribed; // firebase is using this variable!!
    public Map<String, Boolean> eventHidden; // firebase is using this variable!!

    public User() {
        phones = new HashMap<>();
        eventSubscribed = new HashMap<>();
        eventHidden = new HashMap<>();
    }


    public boolean hasSubscribedEvents() {
        return !eventSubscribed.isEmpty();
    }

    public Set<Map.Entry<String, String>> subscriptionsAsEntrySet() {
        return eventSubscribed.entrySet();
    }


    public boolean isSubscribed(String eventUniqueId) {
        return eventSubscribed.containsKey(eventUniqueId);
    }

    public String getSubscriptionPolicy(String eventId){
        return eventSubscribed.get(eventId);
    }
    public void removeSubscription(String eventId) {
        eventSubscribed.remove(eventId);
    }
    public void addSubscription(String eventId, String eventPolicyString) {
        eventSubscribed.put(eventId, eventPolicyString);
    }

    public boolean isHidden(String eventUniqueId) {
        return eventHidden.containsKey(eventUniqueId);
    }

    public void addHidden(String eventId) {
        eventHidden.put(eventId, true);
    }

    public void removeHidden(String eventId) {
        eventHidden.remove(eventId);
    }

    public int countSubscribedEvents() {
        return eventSubscribed.size();
    }
}
