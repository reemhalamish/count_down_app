package halamish.reem.remember.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collection;
import java.util.HashSet;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.activity.create_event.CreateEditEventActivity;
import halamish.reem.remember.firebase.FirebaseInitiationController;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;
import halamish.reem.remember.view.event_recycler.EventAdapter;

import static halamish.reem.remember.Util.allTrue;

@SuppressWarnings("JavaDoc")
public class MainActivity extends AppCompatActivity implements FirebaseInitiationController.OnFirebaseReadyToStartCallback {

    private static final int IDX_NOT_IN_LIST = -1;
    private static final int REQUEST_CREATE_NEW_EVENT = Util.uniqueIntNumber.incrementAndGet();
    private static final int REQUEST_VIEW_EVENT = Util.uniqueIntNumber.incrementAndGet();
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int DELAY_UI_CHANGE_EVENTS_MS = 500;
    private static final int DELAY_NOTHING_MS = 0;
    private static final int IDX_MY_EVENTS = 0;
    private static final int IDX_SUBSCRIBED_EVENTS = 1;
    private static final int IDX_HOT_EVENTS = 2;
    ProgressBar mProgressBar;
    Collection<Event> mAllEvents = null;
    FloatingActionButton mFab;
    View mMainView;
    RecyclerView mRv;
    TextView mTvNothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        mProgressBar.setIndeterminate(true);


        FirebaseInitiationController.getManager().whenReady(this);

    }

    private void findViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.pb_main);
        mFab = (FloatingActionButton) findViewById(R.id.fab_main);
        mMainView = findViewById(R.id.activity_main);
        mTvNothing = (TextView) findViewById(R.id.tv_main_nothing);
        mRv = (RecyclerView) findViewById(R.id.rv_main);
    }

    private void invisProgressBar() {mProgressBar.setVisibility(View.GONE);}

    /**
     * now we can start asking for updates
     */
    @Override
    public void onFirebaseReady() {
        String username = LocalRam.getManager().getUsername();
        FirebaseDbManager manager = FirebaseDbManager.getManager();

        // fab - create new event
        mFab.setVisibility(View.VISIBLE);
        mFab.setOnClickListener(view -> {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this, mFab, "fab");

            Intent createNewEventIntent = new Intent(MainActivity.this, CreateEditEventActivity.class);
            createNewEventIntent.putExtra(CreateEditEventActivity.INPUT_IS_NEW, true);

            startActivityForResult(createNewEventIntent, REQUEST_CREATE_NEW_EVENT, options.toBundle());
        });


        // events
        mAllEvents = new HashSet<>();
        boolean[] allEventsReady = {false, false, false};


        manager.requestDownloadAllEventsUserCreated(username, eventsUserCreated -> {
            mAllEvents.addAll(eventsUserCreated);
            allEventsReady[IDX_MY_EVENTS] = true;
            if (allTrue(allEventsReady)) updateUiGotEventsFromFirebase();
        });


        manager.requestDownloadAllEventsUserIsSubscribedTo(username, eventsUserSubscribed -> {
            mAllEvents.addAll(eventsUserSubscribed);
            allEventsReady[IDX_SUBSCRIBED_EVENTS] = true;
            if (allTrue(allEventsReady)) updateUiGotEventsFromFirebase();
        });



        manager.requestDownloadHotEvents(hotEvents -> {
            mAllEvents.addAll(hotEvents);
            allEventsReady[IDX_HOT_EVENTS] = true;
            if (allTrue(allEventsReady)) updateUiGotEventsFromFirebase();
        });
    }

    private void updateUiGotEventsFromFirebase() {
        invisProgressBar();
        if (mAllEvents.size() == 0)
            mTvNothing.setVisibility(View.VISIBLE);
        else {
            mRv.setVisibility(View.VISIBLE);
            halamish.reem.remember.activity.main.EventAdapter adapter = new halamish.reem.remember.activity.main.EventAdapter(mAllEvents, this, false);
            mRv.setAdapter(adapter);
            mRv.setLayoutManager(new LinearLayoutManager(this));
        }
    }

}
