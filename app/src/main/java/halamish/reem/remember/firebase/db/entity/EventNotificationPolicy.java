package halamish.reem.remember.firebase.db.entity;

/**
 * Created by Re'em on 5/19/2017.
 */

public enum EventNotificationPolicy {
    NOTIFY_DAILY("daily"), NOTIFY_WEEKLY("weekly"), DONT_NOTIFY("dont");

    private final String asString;
    private EventNotificationPolicy(String str){asString = str;}

    @Override
    public String toString() {
        return asString;
    }

    public static EventNotificationPolicy fromString(String string) {
        switch (string) {
            case "daily":
                return NOTIFY_DAILY;
            case "weekly":
                return NOTIFY_WEEKLY;
            default:
                return DONT_NOTIFY;
        }
    }
}
