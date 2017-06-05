package halamish.reem.remember.activity.view_event;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.activity.create_event.CreateEditEventActivity;
import halamish.reem.remember.firebase.db.FirebaseDbException;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.firebase.db.entity.EventNotificationPolicy;

/**
 * Created by Re'em on 5/25/2017.
 *
 * when being called from the main activity, a couple of options can happen:
 *
 *      @ the user presses the "subscribe!" button  ---> call main activity with
 *          OUTPUT_EVENT = event
 *          OUTPUT_IS_EVENT_SUBSCRIBED = true
 *          OUTPUT_ACTION = OUTPUT_IS_EVENT_SUBSCRIBED
 *
 *      @ the user presses the "unsubscribe :(" button  ---> call main activity with
 *          OUTPUT_EVENT = event
 *          OUTPUT_ACTION_SUBSCRIBED = false
 *          OUTPUT_ACTION = OUTPUT_IS_EVENT_SUBSCRIBED
 *
 *
 *      @ the user presses the "edit" button  ---> call for result CreateEditEventActivity
 *                                                  and when it returns call main activity with
 *          OUTPUT_EVENT = the new updated event
 *          OUTPUT_ACTION = OUTPUT_ACTION_EDITED
 *
 *
 */

public class ViewEventActivity extends AppCompatActivity implements LocalRam.OnNewImageInserted {
    public static final String INPUT_EVENT = "input_event@ViewEventActivity";
    public static final String OUTPUT_EVENT = "output_event@ViewEventActivity";
    public static final String OUTPUT_ACTION = "OUTPUT_ACTION@ViewEventActivity";
    public static final String OUTPUT_ACTION_EDITED = "OUTPUT_ACTION_EDITED@ViewEventActivity";
    public static final String OUTPUT_ACTION_SUBSCRIBED = "OUTPUT_ACTION_SUBSCRIBED@ViewEventActivity";
    public static final String OUTPUT_IS_EVENT_SUBSCRIBED = "OUTPUT_IS_EVENT_SUBSCRIBED@ViewEventActivity";



    private static final String BUNDLE_EVENT = "bundle_event_was_edited@ViewEventActivity";
    private static final int REQ_EDIT_EVENT = Util.uniqueIntNumber.incrementAndGet();
    private static final float ALPHA_FULL = 1.0f;
    private static final float ALPHA_NOTHING = 0.0f;
    private static final int DELAY_SUBSCRIBE_FAB_ANIM_MS = 450;

    CoordinatorLayout clMain;
    ImageView ivPicture;
    ProgressBar pbLoadingPicture;
    TextView tvBody;
    ImageView ivBody;
    View vSeperatorAfterBody;
    TextView tvNtfc;
    ImageView ivNtfc;
    TextView tvTime;
    TextView tvDate;
    TextView tvPublic;
    ImageView ivPublic;
    FloatingActionButton fabShare;
    FloatingActionButton fabSubscribe;
    FloatingActionButton fabEdit;

    Bitmap mImage;

    Event mEvent;
    boolean userWasSubscribed;

    Intent mDataBack;

    int colorRed, colorGold, colorGray300;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mEvent = (Event) getIntent().getSerializableExtra(INPUT_EVENT);
            if (mEvent == null) {finish(); return; }
        } else {
            mEvent = (Event) savedInstanceState.getSerializable(BUNDLE_EVENT);
        }
        userWasSubscribed = LocalRam.getManager().getUser().eventSubscribed.containsKey(mEvent.getUniqueId());

        setContentView(R.layout.activity_view_event);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViews();
        setInitValues();
        handleFabSubscribe();
        handleFabShare();
        handleFabEdit();
        handlePicture();
        handleNotification();


    }

    private void handleNotification() {
        View.OnClickListener listenerChangeNotification = view -> {
            if (!userWasSubscribed) return;

            CharSequence[] arrayString = {getString(R.string.notify_daily), getString(R.string.notify_weekly), getString(R.string.dont_notify)};
            EventNotificationPolicy[] arrayNotification = {EventNotificationPolicy.NOTIFY_DAILY, EventNotificationPolicy.NOTIFY_WEEKLY, EventNotificationPolicy.DONT_NOTIFY};
            int curChoiceIndex = -1;
            EventNotificationPolicy policy1 = EventNotificationPolicy.fromString(LocalRam.getManager().getUser().eventSubscribed.get(mEvent.getUniqueId()));
            if (policy1.equals(EventNotificationPolicy.NOTIFY_DAILY)) curChoiceIndex = 0;
            if (policy1.equals(EventNotificationPolicy.NOTIFY_WEEKLY)) curChoiceIndex = 1;
            if (policy1.equals(EventNotificationPolicy.DONT_NOTIFY)) curChoiceIndex = 2;

            new AlertDialog.Builder(ViewEventActivity.this, R.style.activityCreate_DialogTheme)
                    .setTitle(R.string.select_notification_timing)
                    .setSingleChoiceItems(arrayString, curChoiceIndex, (dialog, which) -> {
                        EventNotificationPolicy policyChosen = arrayNotification[which];
                        mEvent.set_local_subscriberNtfcPolicy(policyChosen.toString());
                        LocalRam.getManager().getUser().eventSubscribed.put(mEvent.getUniqueId(), policyChosen.toString());
                        FirebaseDbManager.getManager().reqUpdateNotificationPolicy(
                                mEvent.getUniqueId(),
                                mEvent.weeklyAlertDay(),
                                LocalRam.getManager().getUsername(),
                                policyChosen,
                                null);
                        tvNtfc.setText(arrayString[which]);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        };
        tvNtfc.setOnClickListener(listenerChangeNotification);
        ivNtfc.setOnClickListener(listenerChangeNotification);
    }

    private void handleFabSubscribe() {
        if (LocalRam.getManager().getUsername().equals(mEvent.getCreator())) {
            fabSubscribe.setVisibility(View.GONE);
        } else {
            Animation switchFab = AnimationUtils.loadAnimation(this, R.anim.rotation_shrink_than_overgrow);
            switchFab.setFillAfter(false);
            switchFab.setRepeatCount(1);
            switchFab.setInterpolator(new LinearInterpolator());

            fabSubscribe.setOnClickListener(view -> {
                final boolean userIsNowSubscribed = !userWasSubscribed;

//                fabSubscribe.startAnimation(switchFab);

//                new Handler().postDelayed(() -> {
                    int inSnackbar;
                    int fabFgTintColor;
                    int fabIconId;
                    if (userIsNowSubscribed) {
                        fabFgTintColor = colorGold;
                        fabIconId = R.drawable.ic_star_black_24dp;
                        inSnackbar = R.string.subscribed_success;
                    } else {
                        fabFgTintColor = colorGray300;
                        fabIconId = R.drawable.ic_star_border_black_24dp;
                        inSnackbar = R.string.unsubscribed_success;
                    }
//                    fabSubscribe.setBackgroundTintList(ColorStateList.valueOf(fabFgTintColor));
                    fabSubscribe.setColorFilter(fabFgTintColor);
                    fabSubscribe.setImageResource(fabIconId);
                    Snackbar.make(clMain, inSnackbar, BaseTransientBottomBar.LENGTH_SHORT).show();
//                }, DELAY_SUBSCRIBE_FAB_ANIM_MS);



                mDataBack = new Intent();
                mDataBack.putExtra(OUTPUT_EVENT, mEvent);
                mDataBack.putExtra(OUTPUT_ACTION, OUTPUT_ACTION_SUBSCRIBED);
                mDataBack.putExtra(OUTPUT_IS_EVENT_SUBSCRIBED, userIsNowSubscribed);
                setResult(RESULT_OK, mDataBack);

                if (userIsNowSubscribed) {
                    FirebaseDbManager.getManager().reqSubscribe(
                            mEvent.getUniqueId(),
                            mEvent.weeklyAlertDay(),
                            LocalRam.getManager().getUsername(),
                            EventNotificationPolicy.fromString(mEvent.get_local_subscriberNtfcPolicy()),
                            null);
                } else {
                    FirebaseDbManager.getManager().reqUnsubscribe(
                            LocalRam.getManager().getUsername(),
                            mEvent.getUniqueId(),
                            null
                    );
                }
//                if (userWasSubscribed) {
//
//
//                } else { // the user wasn't subscribed before!
//
//                    // todo here some nice animation of a star flying up, and the fab disappears
//                    // and after a couple of seconds, ask the user what will be the notification for this event
//                    // save the answer in mEvent.set_local_subscriberNtfcPolicy()
//                    // and then call finish()
//
//                }

                userWasSubscribed = !userWasSubscribed; // for future references
            });
        }
    }

//                    new AlertDialog.Builder(ViewEventActivity.this)
//                            .setMessage(R.string.sure_wanna_unsubscribe)
//                            .setCancelable(true)
//                            .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
//
//                                // return to main activity to tell her the user wants to un-subscribe
//                                mDataBack = new Intent();
//                                mDataBack.putExtra(OUTPUT_EVENT, mEvent);
//                                mDataBack.putExtra(OUTPUT_ACTION, OUTPUT_ACTION_SUBSCRIBED);
//                                mDataBack.putExtra(OUTPUT_IS_EVENT_SUBSCRIBED, false);
//                                setResult(RESULT_OK, mDataBack);
////                                supportFinishAfterTransition();
//
//                                dialogInterface.dismiss();
//                            })
//                            .show();


    private void handleFabShare() {
        // TODO: 5/25/2017 create an option to make a link so that the app can read the link and relate to that!
    }



    private void handleFabEdit() {
        if (LocalRam.getManager().getUsername().equals(mEvent.getCreator())) {
            fabEdit.setOnClickListener(view -> {
                Intent goToEdit = new Intent(ViewEventActivity.this, CreateEditEventActivity.class);
                goToEdit.putExtra(CreateEditEventActivity.INPUT_EVENT, mEvent);
                goToEdit.putExtra(CreateEditEventActivity.INPUT_IS_NEW, false);
                // todo this boolean is not used anywhere, as we call it from different places. so we can figure out if event was to be added or modified. should I delete this?

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.
                                makeSceneTransitionAnimation(
                                        ViewEventActivity.this, fabEdit, "fab");

                startActivityForResult(goToEdit, REQ_EDIT_EVENT, options.toBundle());
            });
        } else {
            fabEdit.setVisibility(View.GONE);
        }
    }

    private void handlePicture() {
        LocalRam.getManager().requestPicture(mEvent.getUniqueId(), this);
    }

    private void setInitValues() {
        colorGold = ContextCompat.getColor(this, R.color.yellow800);
        colorRed = ContextCompat.getColor(this, R.color.red500);
        colorGray300 = ContextCompat.getColor(this, R.color.gray300);

        setTitle(mEvent.getTitle());

        String body = mEvent.getBody();
        if (body!=null && !body.equals("")) {
            tvBody.setText(body);
        } else {
            tvBody.setVisibility(View.GONE);
            ivBody.setVisibility(View.GONE);
            vSeperatorAfterBody.setVisibility(View.GONE);
        }

        tvNtfc.setText(EventNotificationPolicy.fromString(mEvent.getCreatorNtfcPolicy()).asStringResource());

        tvTime.setText(mEvent.getTime());
        tvDate.setText(mEvent.getDate());
        if (mEvent.isPublic()) {
            tvPublic.setText(R.string.public_);
            ivPublic.setImageResource(R.drawable.ic_public_black_24dp);
        } else {
            tvPublic.setText(R.string.private_);
            ivPublic.setImageResource(R.drawable.ic_fingerprint_black_24dp);
        }

        // fab subscribe
        if (userWasSubscribed) {
//            fabSubscribe.setBackgroundTintList(ColorStateList.valueOf(colorRed));
            fabSubscribe.setColorFilter(colorGold);
            fabSubscribe.setImageResource(R.drawable.ic_star_black_24dp);
        } else {
            fabSubscribe.setColorFilter(colorGray300);
            fabSubscribe.setImageResource(R.drawable.ic_star_border_black_24dp);
        }


        // notification area
        if (userWasSubscribed) {
            EventNotificationPolicy policy = EventNotificationPolicy.fromString(LocalRam.getManager().getUser().eventSubscribed.get(mEvent.getUniqueId()));
            tvNtfc.setText(policy.asStringResource());
        }
    }

    private void findViews() {
        clMain = (CoordinatorLayout) findViewById(R.id.cl_view_main);
        ivPicture = (ImageView) findViewById(R.id.iv_view_picture);
        pbLoadingPicture = (ProgressBar) findViewById(R.id.pb_view_loading_picture);
        ivBody = (ImageView) findViewById(R.id.iv_view_icon_body);
        tvBody = (TextView) findViewById(R.id.tv_view_body);
        vSeperatorAfterBody = findViewById(R.id.v_view_space_1);
        tvDate = (TextView) findViewById(R.id.tv_view_date);
        tvTime = (TextView) findViewById(R.id.tv_view_time);
        tvNtfc = (TextView) findViewById(R.id.tv_view_ntfc);
        ivNtfc = (ImageView) findViewById(R.id.iv_view_icon_ntfc);
        tvPublic = (TextView) findViewById(R.id.tv_view_public);
        ivPublic = (ImageView) findViewById(R.id.iv_view_icon_public);
        fabEdit = (FloatingActionButton) findViewById(R.id.fab_view_edit);
        fabShare = (FloatingActionButton) findViewById(R.id.fab_view_share);
        fabSubscribe = (FloatingActionButton) findViewById(R.id.fab_view_subscribe);
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQ_EDIT_EVENT) {
            Event updatedEvent = (Event) data.getSerializableExtra(CreateEditEventActivity.OUTPUT_EVENT);
            Intent backIntent = new Intent();
            backIntent.putExtra(OUTPUT_ACTION, OUTPUT_ACTION_EDITED);
            backIntent.putExtra(OUTPUT_EVENT, updatedEvent);
            setResult(RESULT_OK, backIntent);
//            supportFinishAfterTransition();

            // update in firebase
            try {
                FirebaseDbManager.getManager().updateExistingEvent(updatedEvent, null);
                mEvent = updatedEvent;
                setInitValues();
            } catch (FirebaseDbException.NotEventCreator notEventCreator) {
                notEventCreator.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_EVENT, mEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalRam.getManager().removeCallback(this);
    }

    @Override
    public boolean imageReady(String eventId, Bitmap image) {
        boolean thisIsTheImage = eventId.equals(mEvent.getUniqueId());
        if (thisIsTheImage) {
            mImage = image;
            ivPicture.setImageBitmap(image);
            ivPicture.setVisibility(View.VISIBLE);
            // todo with some nice animation!
//            pbLoadingPicture.startAnimation(new AlphaAnimation(ALPHA_FULL, ALPHA_NOTHING));
            pbLoadingPicture.setVisibility(View.GONE);
        }
        return thisIsTheImage;
    }
}
