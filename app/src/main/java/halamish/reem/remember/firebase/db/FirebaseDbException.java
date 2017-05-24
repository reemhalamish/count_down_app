package halamish.reem.remember.firebase.db;

import com.google.firebase.database.DatabaseError;

/**
 * Created by Re'em on 5/19/2017.
 */

public class FirebaseDbException extends Exception {
    public static class NotEventCreator extends FirebaseDbException {NotEventCreator() {super("You can't change an event you did not create!");}}
    public static class NoSuchItem extends FirebaseDbException {NoSuchItem() {super("You can't change an event you did not create!");}}



    FirebaseDbException(String string) {
        super(string);
    }

}
