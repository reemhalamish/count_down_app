package halamish.reem.remember;

import java.util.concurrent.atomic.AtomicInteger;

import halamish.reem.remember.firebase.db.entity.User;

/**
 * Created by Re'em on 5/19/2017.
 */

public class Util {

    public static AtomicInteger uniqueIntNumber = new AtomicInteger(1024);

    public static boolean allTrue(boolean[] array) {
        for (Boolean bool : array)
            if (!bool)
                return false;

        return true;
    }


}
