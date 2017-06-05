package halamish.reem.remember.activity.create_event;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.firebase.db.FirebaseDbException;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;
import halamish.reem.remember.view.ViewUtil;

/**
 * Created by Re'em on 5/20/2017.
 *
 * activity to update (create new \ edit) an event
 */

public class CreateEditEventActivity extends AppCompatActivity {
    public static final String OUTPUT_EVENT = "output_event_updated@CreateEditEvent";
    public static final String OUTPUT_IS_NEW = "output_is_new@CreateEditEvent";
    public static final String INPUT_IS_NEW = "input_is_new@CreateEditEvent";
    public static final String INPUT_EVENT = "input_event_in@CreateEditEvent";
    private static final String BUNDLE_EVENT_STARTING_TO_WORK = "BUNDLE_EVENT_STARTING_TO_WORK@CreateEditEvent";
    private static final String BUNDLE_EVENT_IS_NEW =  "EVENT_IS_NEW@CreateEditEvent";
    private static final String TAG = CreateEditEventActivity.class.getSimpleName();

    ImageView ivPictureAdd;
    View ivClearPicture;
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

    //    int purpleColor;
    int textSecondaryColor;
    int accentColor;
    Drawable imgVectorPublic, imgVectorPrivate;
    WorkWithPicture pictureWorker;

    boolean isNewEvent;

    Event event;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            event = (Event) savedInstanceState.get(BUNDLE_EVENT_STARTING_TO_WORK);
            isNewEvent = savedInstanceState.getBoolean(BUNDLE_EVENT_IS_NEW);
        } else {
            isNewEvent = getIntent().getBooleanExtra(BUNDLE_EVENT_IS_NEW, false);
            if (isNewEvent) {
                event = (Event) getIntent().getSerializableExtra(INPUT_EVENT);
            } else {
                event = Event.createNewHalfFilled();
            }
        }
        setContentView(R.layout.activity_create_event);

        textSecondaryColor = ContextCompat.getColor(this, R.color.secondary_text);
        accentColor = ContextCompat.getColor(this, R.color.accent);

        isNewEvent = getIntent().getBooleanExtra(INPUT_IS_NEW, true);


        findViews();
        setDefaultValues();
        setListenerNtfc();
        setListenerTime();
        setListenerDate();
        setListenerPublic();

        setListenerFab();

        setPictureListenerAndStartValues();

    }

    private void setPictureListenerAndStartValues() {
        ivClearPicture.setOnClickListener(view -> {
            int padding24Dp = (int) ViewUtil.dpToPixel(CreateEditEventActivity.this, 24);
            ivPictureAdd.setPadding(padding24Dp, padding24Dp, padding24Dp, padding24Dp);
            ivPictureAdd.setColorFilter(accentColor);
            ivPictureAdd.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ivPictureAdd.setImageResource(R.drawable.ic_add_a_photo_white_48dp);

            ivClearPicture.setVisibility(View.GONE);
        });

        WorkWithPicture.OnPictureCroppedAndReadyCallback callback = image -> {
            ivPictureAdd.setColorFilter(null);
            ivPictureAdd.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivPictureAdd.setImageBitmap(image);
            ivPictureAdd.setPadding(0,0,0,0);
            ivClearPicture.setVisibility(View.VISIBLE);
        };
        pictureWorker = new WorkWithPicture(callback);
        LocalRam.getManager().requestPicture(event.getUniqueId(), (eventId, image) -> {
            if (eventId.equals(event.getUniqueId())) {callback.onPictureReady(image);return true;}
            return false;
        });

        ivPictureAdd.setOnClickListener(view -> pictureWorker.getPictures(this));
        ivPictureAdd.setColorFilter(accentColor);
    }


    private void setDefaultValues() {
        if (isNewEvent) setTitle(R.string.create_new_event);
        else setTitle(R.string.edit_event);
        tvTime.setText(event.getTime());
        tvDate.setText(event.getDate());
        tvNtfc.setText(EventNotificationPolicy.fromString(event.getCreatorNtfcPolicy()).asStringResource());
        if (event.isPublic()) {
            tvPublic.setText(R.string.public_);
            ivPublic.setImageResource(R.drawable.ic_public_black_24dp);
        } else {
            tvPublic.setText(R.string.private_);
            ivPublic.setImageResource(R.drawable.ic_fingerprint_black_24dp);
        }
        edtTitle.setText(event.getTitle());
        edtBody.setText(event.getBody());
    }

    /**
     * the upload of the event will happen only if the user hasn't dismissed the snackbar
     * the upload of the pictures will happen anyway, and if the user dismissed the snackbar they will cancel
     */
    private void setListenerFab() {
        fab.setOnClickListener(view -> {
            // firstly - remove the keyboard
            View focusedView = getCurrentFocus();
            if (focusedView != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            String title = edtTitle.getText().toString();
            if (title.isEmpty()) {
                Toast.makeText(CreateEditEventActivity.this, R.string.please_provide_title, Toast.LENGTH_SHORT).show();
                edtTitle.setHintTextColor(Color.RED);
                return;
            } else {
                edtTitle.setHintTextColor(textSecondaryColor);
            }

            String date = event.getDate();
            if (date == null || date.equals("")) {
                Toast.makeText(this, "Please Choose date!", Toast.LENGTH_SHORT).show();
                ivDate.setColorFilter(Color.RED);
                tvDate.setHintTextColor(Color.RED);
                return;
            } else {
                ivDate.setColorFilter(textSecondaryColor);
                tvDate.setHintTextColor(textSecondaryColor);
            }

            String time = event.getTime();
            if (time == null || time.equals("")) {
                Toast.makeText(this, "Please Choose time!", Toast.LENGTH_SHORT).show();
                ivTime.setColorFilter(Color.RED);
                tvTime.setHintTextColor(Color.RED);
                return;
            } else {
                ivTime.setColorFilter(textSecondaryColor);
                tvTime.setHintTextColor(textSecondaryColor);
            }


            event.setTitle(title);

            String body = edtBody.getText().toString();
            event.setBody(body);



            try {
                if (isNewEvent)
                    FirebaseDbManager.getManager().uploadNewEvent(event, event.creatorPolicy(), null);
                else
                    FirebaseDbManager.getManager().updateExistingEvent(event, null);
            }
            catch (FirebaseDbException.NotEventCreator ignored) {}


            pictureWorker.uploadPicturesQuietlyInBg(event.getUniqueId());

            Intent backIntent = new Intent();
            backIntent.putExtra(OUTPUT_EVENT, event);
            backIntent.putExtra(OUTPUT_IS_NEW, isNewEvent);
            setResult(RESULT_OK, backIntent);


            CreateEditEventActivity.this.supportFinishAfterTransition();
        });

    }
//
//    private void changeEditTextsColor(int color) {
//        for (EditText editText : Arrays.asList(edtTitle, edtBody)) {
//            editText.getBackground()
//                    .mutate()
//                    .setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
//        }
//    }

    private void setListenerPublic() {
        imgVectorPrivate = ViewUtil.getVectorAsset(this, R.drawable.ic_fingerprint_black_24dp);
        imgVectorPublic = ViewUtil.getVectorAsset(this, R.drawable.ic_public_black_24dp);

        View.OnClickListener listener = view -> {
            CharSequence[] arrayString = {getString(R.string.public_), getString(R.string.private_)};
            boolean[] arrayAnswer = {true, false};
            int curChoiceIndex = event.isPublic() ? 0 : 1;

            Drawable[] arrayPicture = {imgVectorPublic, imgVectorPrivate};
            new AlertDialog.Builder(CreateEditEventActivity.this, R.style.activityCreate_DialogTheme)
                    .setTitle(R.string.wanna_public_event)
                    .setSingleChoiceItems(arrayString, curChoiceIndex, (dialog, which) -> {
                        event.setPublic(arrayAnswer[which]);
                        tvPublic.setText(arrayString[which]);
                        tvPublic.setTextColor(textSecondaryColor);
                        ivPublic.setImageDrawable(arrayPicture[which]);
                        ivPublic.setColorFilter(textSecondaryColor);
                        dialog.dismiss();
                    })
                    .create()
                    .show();

        };

        ivPublic.setOnClickListener(listener);
        tvPublic.setOnClickListener(listener);
    }

    private void setListenerNtfc() {
        View.OnClickListener ntfcPolicyListener = view -> {
            CharSequence[] arrayString = {getString(R.string.notify_daily), getString(R.string.notify_weekly), getString(R.string.dont_notify)};
            EventNotificationPolicy[] arrayNotification = {EventNotificationPolicy.NOTIFY_DAILY, EventNotificationPolicy.NOTIFY_WEEKLY, EventNotificationPolicy.DONT_NOTIFY};
            int curChoiceIndex = -1;
            EventNotificationPolicy ntfc = EventNotificationPolicy.fromString(event.getCreatorNtfcPolicy());
            if (ntfc.equals(EventNotificationPolicy.NOTIFY_DAILY)) curChoiceIndex = 0;
            if (ntfc.equals(EventNotificationPolicy.NOTIFY_WEEKLY)) curChoiceIndex = 1;
            if (ntfc.equals(EventNotificationPolicy.DONT_NOTIFY)) curChoiceIndex = 2;

            new AlertDialog.Builder(CreateEditEventActivity.this, R.style.activityCreate_DialogTheme)
                    .setTitle(R.string.select_notification_timing)
                    .setSingleChoiceItems(arrayString, curChoiceIndex, (dialog, which) -> {
                        event.setCreatorNtfcPolicy(arrayNotification[which].toString());
                        tvNtfc.setText(arrayString[which]);
                        tvNtfc.setTextColor(textSecondaryColor);
                        ivNtfc.setColorFilter(textSecondaryColor);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        };

        ivNtfc.setOnClickListener(ntfcPolicyListener);
        tvNtfc.setOnClickListener(ntfcPolicyListener);
    }

    @SuppressLint("WrongConstant")
    private void setListenerDate() {
        View.OnClickListener dateListener = view -> {
            Calendar calendar = event.asCalendar();
            int year, month, day;
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(this,
                    R.style.activityCreate_DialogTheme,
                    (datePicker, year2, month2, day2) -> {
                        event.setDate(Event.getDateAsString(year2, month2, day2));
                        tvDate.setText(event.getDate());
                        tvDate.setTextColor(textSecondaryColor);
                        ivDate.setColorFilter(textSecondaryColor);
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
        View.OnClickListener timeListener = view -> new TimePickerDialog(
                this,
                R.style.activityCreate_DialogTheme,
                (timePicker, hour, minute) -> {
                    event.setTime(Event.getTimeAsString(hour, minute));
                    tvTime.setText(event.getTime());

                    tvTime.setTextColor(textSecondaryColor);
                    ivTime.setColorFilter(textSecondaryColor);
                },
                event.localGetHours(),
                event.localGetMinutes(),
                false)
                .show();

        ivTime.setOnClickListener(timeListener);
        tvTime.setOnClickListener(timeListener);
    }

    private void findViews() {
        ivPictureAdd = (ImageView) findViewById(R.id.iv_create_add_picture);
        ivClearPicture = findViewById(R.id.iv_create_clear_picture);
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

    /**
     * Dispatch incoming result to the pictureWorker.
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        pictureWorker.onResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_EVENT_STARTING_TO_WORK, event);
        outState.putBoolean(BUNDLE_EVENT_IS_NEW, isNewEvent);
    }
}
