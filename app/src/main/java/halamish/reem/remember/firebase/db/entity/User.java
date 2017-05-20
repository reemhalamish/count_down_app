package halamish.reem.remember.firebase.db.entity;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Boolean> eventSubscribed; // firebase is using this variable!!

    public User() {
        phones = new HashMap<>();
    }
}
