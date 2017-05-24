package halamish.reem.remember.activity_create_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;
import halamish.reem.remember.firebase.db.entity.PartiallyEventForGui;
import halamish.reem.remember.view.ViewUtil;

/**
 * Created by Re'em on 5/20/2017.
 */

public class CreateEventActivity extends AppCompatActivity {
    public static final String NEW_CREATED_EVENT = "event_new_created";

    ImageView ivPicture;
    EditText edtTitle;
    EditText edtBody;
    TextView tvNtfc;
    TextView tvTime;
    TextView tvDate;
    ImageView ivNtfc;
    ImageView ivTime;
    ImageView ivDate;
    FloatingActionButton fab;
    ImageView ivPublic;
    TextView tvPublic;

    // todo add "isPublic" boolean!

    //    int purpleColor;
    int almostBlackColor;
    Drawable imgVectorPublic, imgVectorPrivate;
    WorkWithPicture.OnPictureCroppedAndReadyCallback mPictureCroppedAndReadyCallback;
    Bitmap mImageToUpload;

    //    @State
    String mDate = "2017/08/21"; // January is 0, December is 11, August is 7
    //    @State
    String mTime = "19:00";
    EventNotificationPolicy mPolicy = EventNotificationPolicy.NOTIFY_WEEKLY;
    boolean isPublic = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
//        purpleColor = ContextCompat.getColor(this, R.color.purple500);
        almostBlackColor = ContextCompat.getColor(this, R.color.secondary_text);
//        Icepick.restoreInstanceState(this, savedInstanceState);
        findViews();
        setDefaultValuesTextViews();

//        changeEditTextsColor();

        setListenerNtfc();
        setListenerTime();
        setListenerDate();
        setListenerPublic();

        setListenerFab();
        setPictureReadyCallback();


        // to change editText color:
//
    }

    private void setPictureReadyCallback() {
        mPictureCroppedAndReadyCallback = image -> {
            ivPicture.setImageBitmap(image);
            mImageToUpload = image;
        };
    }


    private void setDefaultValuesTextViews() {
        tvTime.setText(mTime);
        tvDate.setText(mDate);
        tvNtfc.setText(R.string.dont_notify);
    }

    private void setListenerFab() {
        fab.setOnClickListener(view -> {
            String title = edtTitle.getText().toString();
            if (title.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, R.string.please_provide_title, Toast.LENGTH_SHORT).show();
                return;
            }
            String body = edtBody.getText().toString();
            String dateToSave = mDate;
            String timeToSave = mTime;
            String dateAndTime = dateToSave + " " + timeToSave;
            PartiallyEventForGui newbie =
                    new PartiallyEventForGui(
                            dateAndTime,
                            title,
                            body,
                            Util.username,
                            mPolicy.toString(),
                            isPublic,
                            true
                    );

            Intent backIntent = new Intent();
            backIntent.putExtra(NEW_CREATED_EVENT, newbie);
            setResult(RESULT_OK, backIntent);


            CreateEventActivity.this.supportFinishAfterTransition();
        });

    }

    private void changeEditTextsColor(int color) {
        for (EditText editText : Arrays.asList(edtTitle, edtBody)) {
            editText.getBackground()
                    .mutate()
                    .setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void setListenerPublic() {
        imgVectorPrivate = ViewUtil.getVectorAsset(this, R.drawable.ic_fingerprint_black_24dp);
        imgVectorPublic = ViewUtil.getVectorAsset(this, R.drawable.ic_public_black_24dp);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence[] arrayString = {getString(R.string.public_), getString(R.string.private_)};
                boolean[] arrayAnswer = {true, false};
                Drawable[] arrayPicture = {imgVectorPublic, imgVectorPrivate};
                new AlertDialog.Builder(CreateEventActivity.this, R.style.activityCreate_DialogTheme)
                        .setTitle(R.string.wanna_public_event)
                        .setSingleChoiceItems(arrayString, 0, (dialog, which) -> {
                            isPublic = arrayAnswer[which];
                            tvPublic.setText(arrayString[which]);
                            tvPublic.setTextColor(almostBlackColor);
                            ivPublic.setImageDrawable(arrayPicture[which]);
                            ivPublic.setColorFilter(almostBlackColor);
                            dialog.dismiss();
                        })
                        .create()
                        .show();

            }
        };

        ivPublic.setOnClickListener(listener);
        tvPublic.setOnClickListener(listener);
    }

    private void setListenerNtfc() {
        View.OnClickListener ntfcPolicyListener = view -> {
            CharSequence[] arrayString = {getString(R.string.notify_daily), getString(R.string.notify_weekly), getString(R.string.dont_notify)};
            EventNotificationPolicy[] arrayNotification = {EventNotificationPolicy.NOTIFY_DAILY, EventNotificationPolicy.NOTIFY_WEEKLY, EventNotificationPolicy.DONT_NOTIFY};
            new AlertDialog.Builder(CreateEventActivity.this, R.style.activityCreate_DialogTheme)
                    .setTitle(R.string.select_notification_timing)
                    .setSingleChoiceItems(arrayString, -1, (dialog, which) -> {
                        mPolicy = arrayNotification[which];
                        tvNtfc.setText(arrayString[which]);
                        tvNtfc.setTextColor(almostBlackColor);
                        ivNtfc.setColorFilter(almostBlackColor);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        };

        ivNtfc.setOnClickListener(ntfcPolicyListener);
        tvNtfc.setOnClickListener(ntfcPolicyListener);
    }

    private void setListenerDate() {
        View.OnClickListener dateListener = view -> {
            Calendar calendar = Event.toCalendar(mDate);
            int year, month, day;
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(this,
                    R.style.activityCreate_DialogTheme,
                    (datePicker, year2, month2, day2) -> {
                        mDate = getDateAsString(year2, month2, day2);
                        tvDate.setText(mDate);
                        tvDate.setTextColor(almostBlackColor);
                        ivDate.setColorFilter(almostBlackColor);
                    },
                    year,
                    month,
                    day);
            dialog.show();

        };

        ivDate.setOnClickListener(dateListener);
        tvDate.setOnClickListener(dateListener);
    }

    private void setListenerTime() {
        View.OnClickListener timeListener = view -> {

            new TimePickerDialog(
                    this,
                    R.style.activityCreate_DialogTheme,
                    (timePicker, hour, minute) -> {
                        String sHour = String.valueOf(hour);
                        if (hour < 10)
                            sHour = "0" + sHour;
                        String sMin = String.valueOf(minute);
                        if (minute < 10)
                            sMin = "0" + sMin;

                        mTime = sHour + ":" + sMin;
                        tvTime.setText(mTime);

                        tvTime.setTextColor(almostBlackColor);
                        ivTime.setColorFilter(almostBlackColor);
                    },
                    getHourFromTimeTextView(),
                    getMinuteFromTimeTextView(),
                    false)
                    .show();
        };

        ivTime.setOnClickListener(timeListener);
        tvTime.setOnClickListener(timeListener);
    }

    private void findViews() {
        ivPicture = (ImageView) findViewById(R.id.iv_create_add_picture);
        edtTitle = (EditText) findViewById(R.id.edt_create_title);
        edtBody = (EditText) findViewById(R.id.edt_create_body);
        tvDate = (TextView) findViewById(R.id.tv_create_date);
        tvTime = (TextView) findViewById(R.id.tv_create_time);
        tvNtfc = (TextView) findViewById(R.id.tv_create_ntfc);
        ivDate = (ImageView) findViewById(R.id.iv_create_icon_date);
        ivTime = (ImageView) findViewById(R.id.iv_create_icon_time);
        ivNtfc = (ImageView) findViewById(R.id.iv_create_icon_ntfc);
        ivPublic = (ImageView) findViewById(R.id.iv_create_icon_public);
        tvPublic = (TextView) findViewById(R.id.tv_create_public);
        fab = (FloatingActionButton) findViewById(R.id.fab_create);
    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
////        Icepick.saveInstanceState(this, outState);
//    }

    private int getMinuteFromTimeTextView() {
        return Integer.parseInt(tvTime.getText().toString().substring(3));
    }
    private int getHourFromTimeTextView() {
        return Integer.parseInt(tvTime.getText().toString().substring(0,2));
    }

    public static String getRepresentation(String actualDate) {
        int month = Integer.parseInt(actualDate.substring(5,7));
        month++;
        String monthRepresnt = String.valueOf(month);
        if (month < 10) monthRepresnt = "0" + monthRepresnt;

        return actualDate.substring(0, 5) + monthRepresnt + actualDate.substring(7);
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
}
