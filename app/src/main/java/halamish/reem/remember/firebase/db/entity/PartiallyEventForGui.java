package halamish.reem.remember.firebase.db.entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Re'em on 5/20/2017.
 */

public class PartiallyEventForGui {
    @Getter @Setter String date;
    @Getter @Setter String title;
    @Getter @Setter String body;
    @Getter @Setter String creator;   // the user that created this event. FirebaseDB is using this field!!!
    @Getter @Setter boolean isPublic;

    @Setter int weeklyAlertDay; // ranges [1,7] ,  FirebaseDB is using this field!!!

    public PartiallyEventForGui(String date,
                 String title,
                 String body,
                 String creator,
                 boolean isPublic,
                 boolean notShabbat) {
        this.date = date;
        this.title = title;
        this.body = body;
        this.creator = creator;
        this.isPublic = isPublic;
        randomWeeklyAlertDay();
        if (notShabbat) {
            while (weeklyAlertDay == 7) randomWeeklyAlertDay();
        }
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

    public String weeklyAlertDay() {
        return String.valueOf(weeklyAlertDay);
    }


    private void randomWeeklyAlertDay() {
        Random random = new Random();
        weeklyAlertDay = random.nextInt(7) + 1;
    }

}