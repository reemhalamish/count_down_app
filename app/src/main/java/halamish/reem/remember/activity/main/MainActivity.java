package halamish.reem.remember.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collection;
import java.util.HashSet;

import halamish.reem.remember.LocalRam;
import halamish.reem.remember.R;
import halamish.reem.remember.Util;
import halamish.reem.remember.activity.create_event.CreateEditEventActivity;
import halamish.reem.remember.activity.main.recycler_view.EventAdapter;
import halamish.reem.remember.activity.main.recycler_view.RowMovingTouchListener;
import halamish.reem.remember.activity.view_event.ViewEventActivity;
import halamish.reem.remember.firebase.FirebaseInitiationController;
import halamish.reem.remember.firebase.db.FirebaseDbManager;
import halamish.reem.remember.firebase.db.entity.Event;

import static halamish.reem.remember.Util.allTrue;
import static halamish.reem.remember.activity.intent.IntentConsts.IO_EVENT;

@SuppressWarnings("JavaDoc")
public class MainActivity extends AppCompatActivity
        implements FirebaseInitiationController.OnFirebaseReadyToStartCallback,
        EventAdapter.EventAdapterCallbacks {

    private static final int REQUEST_CREATE_NEW_EVENT = Util.uniqueIntNumber.incrementAndGet();
    private static final int REQUEST_EVENT_OUT = Util.uniqueIntNumber.incrementAndGet();

    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int IDX_MY_EVENTS = 0;
    private static final int IDX_SUBSCRIBED_EVENTS = 1;
    private static final int IDX_HOT_EVENTS = 2;
    ProgressBar mProgressBar;
    Collection<Event> mAllEvents = null;
    FloatingActionButton mFab;
    View mMainView;
    RecyclerView mRv;
    TextView mTvNothing;
    private EventAdapter mAdapter;

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
            mAdapter = new EventAdapter(mAllEvents, this, this, false);
            mRv.setAdapter(mAdapter);
            mRv.setLayoutManager(new LinearLayoutManager(this));
            new ItemTouchHelper(new RowMovingTouchListener(mAdapter)).attachToRecyclerView(mRv);
        }
    }

    @Override
    protected void onDestroy() {
        mAdapter.onActivityDestroyed();
        super.onDestroy();
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
        if (resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (requestCode == REQUEST_CREATE_NEW_EVENT) {
            Event event = (Event) data.getSerializableExtra(IO_EVENT);
            if (event == null) return;
            mAllEvents.add(event);
            mAdapter.addEvent(event);

        } else if (requestCode == REQUEST_EVENT_OUT) {
            Event event = (Event) data.getSerializableExtra(IO_EVENT);
            if (event == null) return;
            mAdapter.update(event);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void gotoViewActivity(Event event) {
        Intent gotoView = new Intent(this, ViewEventActivity.class);
        gotoView.putExtra(IO_EVENT, event);
        startActivityForResult(gotoView, REQUEST_EVENT_OUT);
    }

    @Override
    public void gotoEditActivity(Event event) {
        Intent gotoEdit = new Intent(this, CreateEditEventActivity.class);
        gotoEdit.putExtra(IO_EVENT, event);
        gotoEdit.putExtra(CreateEditEventActivity.INPUT_IS_NEW, false);
        startActivityForResult(gotoEdit, REQUEST_EVENT_OUT);
    }

    @Override
    public void eventRemoved(Event event) {
        mAllEvents.remove(event);
    }
}
