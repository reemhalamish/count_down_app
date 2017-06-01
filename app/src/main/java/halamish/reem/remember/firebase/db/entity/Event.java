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

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
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
public class Event implements Serializable {
    static final DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
    private static final DateFormat formatterOnlyCalendar = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private static final DateFormat formatterOnlyTime = new SimpleDateFormat("HH:mm", Locale.US);
    public static final String QUERY_PUBLIC_SUBSCRIBERS_IS_PRIVATE = null;
    public static final String QUERY_PUBLIC_SUBSCRIBERS_IS_PUBLIC_NO_SUBSCRIBERS = "0";

    @Getter @Setter public String date;
    @Getter @Setter public String time;
    @Getter @Setter public String title;
    @Getter @Setter public String body;
    @Getter @Setter public String creator;   // the user that created this event. FirebaseDB is using this field!!!
    @Getter @Setter public String creatorNtfcPolicy;   // the user that created this event. FirebaseDB is using this field!!!
    @Getter @Setter public String _local_subscriberNtfcPolicy =  EventNotificationPolicy.DONT_NOTIFY.toString(); // this field is for local use only.
    @Getter @Setter public boolean isPublic;
    @Getter @Setter public String _query_publicSubscribers = QUERY_PUBLIC_SUBSCRIBERS_IS_PRIVATE;

    @Setter @Getter public int weeklyAlertDay; // ranges [1,7] ,  FirebaseDB is using this field!!!

    @Getter @Setter public String uniqueId;  // FirebaseDB is using this field!!!
    @Getter @Setter public int subscribersAmount = 0;   // FirebaseDB is using this field!!! (for "getHotEvents()")

    public Date asDate() {
        try {
            return Event.formatter.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(0);
        }
    }
    public void fromDate(Date dateIn) {
        String dateAndTime = Event.formatter.format(dateIn);
        date = dateAndTime.substring(0, 10);
        time = dateAndTime.substring(11);
    }

    /**
     * this calendar needs the REPRESENTATION!
     * @param dateStringRepresentation
     * @return
     */
    public static Calendar toCalendar(String dateStringRepresentation) {
        Calendar retVal = Calendar.getInstance();
        if (dateStringRepresentation==null || dateStringRepresentation.equals("")) return retVal;
        try {
            retVal.setTime(Event.formatterOnlyCalendar.parse(dateStringRepresentation));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retVal;
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

    public static Event createNewHalfFilled() {
        Event retval = new Event();
        retval.creator = LocalRam.getManager().getUsername();
        retval.creatorNtfcPolicy = EventNotificationPolicy.DONT_NOTIFY.toString();
        retval.uniqueId = FirebaseDbManager.getManager().getNewEventId();

        Calendar calendar = Calendar.getInstance();
//        retval.setDate(getDateAsString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
//        retval.setTime(getTimeAsString(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)));

        return retval;

    }

    public static Event createMock() {
        Event retval = new Event();
        retval.title = "Some title";
        retval.body = "Bodyyyyyyyyyyyyyyyy";
        retval.time = "10:15";
        retval.date = "2017/08/21";
        return retval;

    }

    public Calendar asCalendar() {
        return toCalendar(date);
    }

    public int get_local_Hours() {
        try {
            return Integer.parseInt(time.substring(0, 2));
        } catch (Exception exception) {
            exception.printStackTrace();
            return 10;
        }
    }

    public int get_local_Minutes() {
        try {
            return Integer.parseInt(time.substring(3));
        } catch (Exception exception) {
            exception.printStackTrace();
            return 10;
        }
    }

    /**
     *
     * @param year
     * @param month ranges [0, 11]
     * @param day
     * @return
     */
    public static String getDateAsString(int year, int month, int day) {
        month++; // now ranges [1, 12]
        String sYear = String.valueOf(year);
        String sMonth = String.valueOf(month);
        if (month < 10) sMonth = "0" + sMonth;
        String sDay = String.valueOf(day);
        if (day < 10) sDay = "0" + sDay;
        return sYear + "/" + sMonth + "/" + sDay;
    }

    public static String getTimeAsString(int hours, int minutes) {
        String sHour = String.valueOf(hours);
        if (hours < 10)
            sHour = "0" + sHour;
        String sMin = String.valueOf(minutes);
        if (minutes < 10)
            sMin = "0" + sMin;
        return sHour + ":" + sMin;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Event && ((Event) obj).uniqueId.equals(uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
