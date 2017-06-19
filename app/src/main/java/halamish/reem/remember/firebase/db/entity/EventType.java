package halamish.reem.remember.firebase.db.entity;

import lombok.Getter;

/**
 * Created by Re'em on 6/5/2017.
 */

public enum EventType {
    CREATOR(0), PRIVATE_SUBSCRIBER(1), HOT_EVENT(2);
    @Getter private final int index;

    EventType(int arg){index=arg;}
    public EventType fromInt(int arg) {
        switch (arg) {
            case 0:
                return CREATOR;
            case 1:
                return PRIVATE_SUBSCRIBER;
        }
        return HOT_EVENT;
    }
}
