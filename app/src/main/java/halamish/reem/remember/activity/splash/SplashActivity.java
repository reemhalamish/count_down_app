package halamish.reem.remember.activity.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import halamish.reem.remember.R;
import halamish.reem.remember.activity.main.MainActivity;
import halamish.reem.remember.firebase.FirebaseInitiationController;
import halamish.reem.remember.view.ViewUtil;

/**
 * Created by Re'em on 6/1/2017.
 *
 * the first activity to run
 */

public class SplashActivity extends AppCompatActivity {
    private static final int[] CIRCLE_COLOR_IDS = {R.color.accent, R.color.primary_dark, R.color.red500};
    //    private static final int[] CIRCLE_COLOR_IDS = {R.color.accent};//, R.color.primary_dark, R.color.red500};
    private static final int CIRCLES_AMNT_EACH_COLOR = 16;
    //    private static final int CIRCLES_AMNT_EACH_COLOR = 1;
    private static final int CIRCLE_SIZE_DP = 6;
    private static final int ANIM_ONE_ROUND_DUR_MS = 600;
    private static final int ANIM_STEP_BETWEEN_CIRCLES_DUR_MS = 80;
    private static final float PADDING_PRCTG_TOP = 0.35f;
    private static final float PADDING_PRCTG_BOTTOM = 0.875f;
    private static final float PADDING_PRCTG_RIGHT = 0.75f;
    private static final float PADDING_PRCTG_LEFT = 0.25f;
    private static final long ANIM_MIN_DUR_TOTAL_MS = 2000;
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final long DUR_BEFORE_START_MS = 50;
    private static int[] CIRCLE_COLORS;

    private List<ImageView> circles;
    private long startTime;
    private boolean isFinishedLoading = false;
    private View tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);        setContentView(R.layout.activity_splash);
        final Context context = this;

        circles = new ArrayList<>();
        RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.rl_splash_main);
        tvTitle = findViewById(R.id.tv_splash_app_name);

        CIRCLE_COLORS = new int[CIRCLE_COLOR_IDS.length];
        for (int i = 0; i < CIRCLE_COLOR_IDS.length; i++) {
            CIRCLE_COLORS[i] = ContextCompat.getColor(context, CIRCLE_COLOR_IDS[i]);
        }
        float circleSizePxl = ViewUtil.dpToPixel(context, CIRCLE_SIZE_DP);

        rlMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                startTime = System.currentTimeMillis();
                rlMain.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float height, width, top, left, right, bottom;
                top = rlMain.getTop();
                bottom = rlMain.getBottom();
                left = rlMain.getLeft();
                right = rlMain.getRight();
                height = bottom - top;
                width = right - left;

                float topP = top + (height * PADDING_PRCTG_TOP);
                float bottomP = top + (height * PADDING_PRCTG_BOTTOM);
                float leftP = left + (width * PADDING_PRCTG_LEFT);
                float rightP = left + (width * PADDING_PRCTG_RIGHT);
                float heightP = bottomP - topP;
                int viewsNumber = CIRCLES_AMNT_EACH_COLOR * CIRCLE_COLORS.length;

                for (int i = 0; i < viewsNumber; i++) {
                    ImageView circle = new ImageView(context);
                    circle.setImageResource(R.drawable.circle_half_dp);
                    circle.setColorFilter(CIRCLE_COLORS[i % CIRCLE_COLORS.length]);
                    float yPos = topP + i * heightP / viewsNumber;
                    float xPos = leftP;
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) circleSizePxl, (int) circleSizePxl);
                    params.topMargin = 0;
                    params.leftMargin = 0;
                    rlMain.addView(circle, params);
                    circle.setVisibility(View.INVISIBLE);
                    circles.add(circle);
                }


                Handler handler = new Handler(Looper.getMainLooper());
                for (int i = 0; i < viewsNumber; i++) {
                    int finalI = i;
                    handler.postDelayed(() -> {
                        circles.get(finalI).setVisibility(View.VISIBLE);
                        startMovingCircle(finalI, leftP, rightP, topP + finalI * heightP / viewsNumber);
                    }, DUR_BEFORE_START_MS + i * ANIM_STEP_BETWEEN_CIRCLES_DUR_MS);
                }

            }
        });
    }

    private void startMovingCircle(int index, float left, float right, float height) {
        Log.d(TAG, "starting circle " + index);
        long timeDelta = System.currentTimeMillis() - startTime;
        if (index == 0 && FirebaseInitiationController.isReady() && timeDelta > ANIM_MIN_DUR_TOTAL_MS) {
            isFinishedLoading = true;
        }

        if (isFinishedLoading) {
            float xPosTv = tvTitle.getLeft() + (tvTitle.getRight() - tvTitle.getLeft())/2;
            float yPosTv = tvTitle.getBottom();

            Animation animation = new TranslateAnimation(left, xPosTv, height, yPosTv);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(ANIM_ONE_ROUND_DUR_MS);
            circles.get(index).startAnimation(animation);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> circles.get(index).setVisibility(View.INVISIBLE), ANIM_ONE_ROUND_DUR_MS);
            if (index == circles.size() - 1) { // last one
                handler.postDelayed(this::goToNextActivity, ANIM_ONE_ROUND_DUR_MS + ANIM_STEP_BETWEEN_CIRCLES_DUR_MS);
            }
        }
        else {
//            Animation animation = new TranslateAnimation((int) left,Animation.RELATIVE_TO_PARENT, (int) (right), Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT);
            Animation animation = new TranslateAnimation(left, right, height, height);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(ANIM_ONE_ROUND_DUR_MS);
            circles.get(index).startAnimation(animation);
            new Handler(Looper.getMainLooper()).postDelayed(() -> moveCircleBack(index, left, right, height), ANIM_ONE_ROUND_DUR_MS);
        }
    }

    private void goToNextActivity() {
        // todo one day need to put here some info about the link to be opened in the next activity if someone used the "share" button
        Intent gotoNext = new Intent(this, MainActivity.class);
        startActivity(gotoNext);
        finish();
    }

    private void moveCircleBack(int index, float left, float right, float height) {
//        Animation animation = new TranslateAnimation((int) (right),Animation.ABSOLUTE, (int) left, Animation.ABSOLUTE, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT);
        Animation animation = new TranslateAnimation(right, left, height, height);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(ANIM_ONE_ROUND_DUR_MS);
        circles.get(index).startAnimation(animation);
        new Handler(Looper.getMainLooper()).postDelayed(() -> startMovingCircle(index, left, right, height), ANIM_ONE_ROUND_DUR_MS);
    }
}