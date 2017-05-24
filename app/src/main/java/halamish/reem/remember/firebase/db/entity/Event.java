package halamish.reem.remember.firebase.db.entity;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Re'em on 5/19/2017.
 *
 * represents event in firebase DB
 */

@NoArgsConstructor
@AllArgsConstructor
@IgnoreExtraProperties
@EqualsAndHashCode
public class Event implements Serializable {
    static final DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
    private static final DateFormat formatterOnlyCalendar = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    @Getter @Setter String date;
    @Getter @Setter String title;
    @Getter @Setter String body;
    @Getter @Setter String creator;   // the user that created this event. FirebaseDB is using this field!!!
    @Getter @Setter String creatorNtfcPolicy;   // the user that created this event. FirebaseDB is using this field!!!
    @Getter @Setter String _local_subscriberNtfcPolicy =  ""; // this field is for local use only.
    @Getter @Setter boolean isPublic;

    @Setter @Getter int weeklyAlertDay; // ranges [1,7] ,  FirebaseDB is using this field!!!

    @Getter @Setter String uniqueId;  // FirebaseDB is using this field!!!
    @Getter @Setter String picturePathHost = "";
    @Getter @Setter int subscribersAmount = 0;   // FirebaseDB is using this field!!! (for "getHotEvents()")

    public Event(PartiallyEventForGui eventForGui) {
        date = eventForGui.date;
        title = eventForGui.title;
        body = eventForGui.body;
        creator = eventForGui.creator;
        isPublic = eventForGui.isPublic;
        weeklyAlertDay = eventForGui.weeklyAlertDay;
        picturePathHost = eventForGui.picturePathHost;
        creatorNtfcPolicy = eventForGui.policy;
        uniqueId = eventForGui.eventId;
    }


    public Date asDate() {
        try {
            return Event.formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(0);
        }
    }
    public void toDate(Date dateIn) {
        this.date = Event.formatter.format(dateIn);
    }

    /**
     * this calendar needs the REPRESENTATION!
     * @param dateStringRepresentation
     * @return
     */
    public static Calendar toCalendar(String dateStringRepresentation) {
        try {
            Calendar retVal = Calendar.getInstance();
            retVal.setTime(Event.formatterOnlyCalendar.parse(dateStringRepresentation));
            return retVal;
        } catch (ParseException e) {
            e.printStackTrace();
            return Calendar.getInstance();
        }
    }

    public String weeklyAlertDay() {
        return String.valueOf(weeklyAlertDay);
    }

    public long get_local_CountDownDays() {
        Date date = new Date();
        Date theEventDate = asDate();
        long diff = theEventDate.getTime() - date.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public long get_local_CountDownHours() {
        Date date = new Date();
        Date theEventDate = asDate();
        long diff = theEventDate.getTime() - date.getTime();
        return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) return true;
//        if (obj.getClass() != Event.class) return false;
//
//        Event other = (Event) obj;
//        return other.uniqueId.equals(uniqueId);
//    }

}
