package com.ben.sample.forkviewdemo.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class PaperViewGroup extends RelativeLayout {

    private final static String TAG = "drag";

    private Context mContext;
    private View mDragView;

    private SecretPaperAdapter mAdapter;

    private OnRefreshListener mOnRefreshListener;

    private int mPosition = 0;
    private float mTranslationX = 0;
    private float mTranslationY = 0;

    private float touchDownX = 0;
    private float touchDownY = 0;

    private final static int MAX_PAGE_SIZE = 7; // 一共 加载 MAX_PAGE_SIZE + 1 页

    public PaperViewGroup(Context context) {
        this(context, null);
    }

    public PaperViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaperViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public static interface OnRefreshListener {
        public void onRefresh(boolean lastPage);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Log.d(TAG, "onLayout left = " + l + ", right = " + r + ", top = " + t
        // + ", bottom = " + b);
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (i == MAX_PAGE_SIZE - 2) {
                    // Log.d(TAG,
                    // "mTranslationY + mTranslationX = "
                    // + (Math.abs(mTranslationX) + Math
                    // .abs(mTranslationY)));
                    float moved = Math.abs(mTranslationX)
                            + Math.abs(mTranslationY);
                    float scale = 0.8F + moved / 2000;
                    child.setScaleX(scale < 0.9F ? scale : 0.9F); // 0.8->0.9
                    child.setScaleY(scale < 0.9F ? scale : 0.9F);
                    float trY = 50 - (scale - 0.8F) * 250;
                    child.setTranslationY(trY > 25 ? TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, trY, getResources()
                                    .getDisplayMetrics()) : TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25,
                                    getResources().getDisplayMetrics())); // 50->25
                } else if (i == MAX_PAGE_SIZE - 1) {
                    float moved = Math.abs(mTranslationX)
                            + Math.abs(mTranslationY);
                    float scale = 0.9F + moved / 2000;
                    child.setScaleX(scale < 1 ? scale : 1); // 0.9 -> 1.0
                    child.setScaleY(scale < 1 ? scale : 1);
                    float trY = 25 - (scale - 0.9F) * 250;
                    child.setTranslationY(trY > 0 ? TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, trY, getResources()
                                    .getDisplayMetrics()) : 0); // 25->0
                } else if (i == MAX_PAGE_SIZE) {
                    // Log.d(TAG, "mtouchDownX = " + touchDownX
                    // + ", mtouchDownY = " + touchDownY);
                    child.setTranslationX(mTranslationX);
                    child.setTranslationY(mTranslationY);
                    float rotation;
                    if (touchDownY < (b - t) / 2) {
                        rotation = mTranslationX / 20;
                    } else {
                        rotation = -mTranslationX / 20;
                    }
                    if (rotation > 0) {
                        child.setRotation(rotation < 20 ? rotation : 20);
                    } else {
                        child.setRotation(rotation > -20 ? rotation : -20);
                    }
                } else {
                    child.setScaleX(0.8F);
                    child.setScaleY(0.8F);
                    child.setTranslationY(TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 50, getResources()
                                    .getDisplayMetrics()));
                }
            }
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void refreshView(boolean freshAll, boolean pre) {
        Log.d(TAG, "refreshView mPosition = " + mPosition);

        RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mLayoutParams.alignWithParent = true;

        if (mPosition == 0 || freshAll) {
            removeAllViews();
            for (int i = MAX_PAGE_SIZE; i > 0; i--) {
                addView(mAdapter.getView(mPosition + i, null, this),
                        mLayoutParams);
            }
            addView(mAdapter.getView(mPosition, null, this), mLayoutParams);
        } else {
            removeViewAt(MAX_PAGE_SIZE);
            addView(mAdapter.getView(mPosition + MAX_PAGE_SIZE, null, this), 0,
                    mLayoutParams);
        }
        mAdapter.getButtonView(mPosition);

        mDragView = getChildAt(MAX_PAGE_SIZE);
        if (pre) {
            mDragView.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.zoom_in_center_paper));
        }
        mDragView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Log.d(TAG, "down ev.getRawX() = " + event.getRawX());
                    // Log.d(TAG, "down ev.getRawY() = " + event.getRawY());
                    touchDownX = event.getRawX();
                    touchDownY = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Log.d(TAG, "move ev.getRawX() = " + event.getRawX());
                    // Log.d(TAG, "move ev.getRawY() = " + event.getRawY());
                    if (touchDownX < 0.001f && touchDownX < 0.001f) {
                        move(0, 0);
                    } else {
                        move((event.getRawX() - touchDownX),
                                (event.getRawY() - touchDownY));
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    // Log.d(TAG, "up ev.getRawX() = " + event.getRawX());
                    // Log.d(TAG, "up ev.getRawY() = " + event.getRawY());
                    if (touchDownX < 0.001f && touchDownX < 0.001f) {
                        move(0, 0);
                    } else {
                        if (Math.abs(event.getRawX() - touchDownX) <= ViewConfiguration
                                .get(mContext).getScaledTouchSlop()
                                && Math.abs(event.getRawY() - touchDownY) <= ViewConfiguration
                                        .get(mContext).getScaledTouchSlop()) {
                            resetXY();
                            v.performClick();
                        } else {
                            remove(touchDownX, touchDownY, event.getRawX(),
                                    event.getRawY());
                        }
                    }
                    return true;
                    // break;

                case MotionEvent.ACTION_CANCEL:
                    break;
                }

                return false;
            }
        });
    }

    public void move(float x, float y) {
        // Log.d(TAG, "move x = " + x + ", y = " + y);
        mTranslationX = x;
        mTranslationY = y;
        requestLayout();
    }

    void remove(final float downX, final float downY, final float upX,
            final float upY) {
        // Log.d(TAG, "remove downX = " + downX + ", downY = " + downY
        // + ", upX = " + upX + ", upY = " + upY);

        final float min = (PlatformUtils.getScreenWidth(getContext())) / 5;
        ValueAnimator translateLeft = ValueAnimator.ofInt(0,
                (int) (PlatformUtils.getScreenWidth(getContext())));
        translateLeft.setInterpolator(new AccelerateInterpolator());
        translateLeft
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        if ((Math.abs(upX - downX) < min)
                                && (Math.abs(upY - downY) < min)) {
                            // 返回原来的位置
                            if (upX > downX) {
                                if (upY > downY) {
                                    move((upX - downX - val) > 0 ? (upX - downX - val)
                                            : 0, (upY - downY - val) > 0 ? (upY
                                            - downY - val) : 0);
                                } else {
                                    move((upX - downX - val) > 0 ? (upX - downX - val)
                                            : 0, (upY - downY + val) < 0 ? (upY
                                            - downY + val) : 0);
                                }
                            } else {
                                if (upY > downY) {
                                    move((upX - downX + val) < 0 ? (upX - downX + val)
                                            : 0, (upY - downY - val) > 0 ? (upY
                                            - downY - val) : 0);
                                } else {
                                    move((upX - downX + val) < 0 ? (upX - downX + val)
                                            : 0, (upY - downY + val) < 0 ? (upY
                                            - downY + val) : 0);
                                }
                            }
                        } else if (upX > downX) {
                            move((upX - downX + val), upY - downY);
                        } else {
                            move((upX - downX - val), upY - downY);
                        }
                    }
                });
        translateLeft.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Log.d(TAG, "remove end");
                if ((Math.abs(upX - downX) < min)
                        && (Math.abs(upY - downY) < min)) {
                    resetXY();
                } else {
                    resetXY();
                    nextPage();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        if ((Math.abs(upX - downX) < min) && (Math.abs(upY - downY) < min)) {
            translateLeft.setDuration(500);
        } else {
            translateLeft.setDuration(200);
        }
        translateLeft.start();
    }

    public void setAdapter(SecretPaperAdapter adapter) {
        mAdapter = adapter;
    }

    public void nextPage() {
        Log.d(TAG, "nextPage adpate.size =  " + mAdapter.size()
                + ", mPistion = " + mPosition);
        if (mAdapter.size() == mPosition + 1) {
            mOnRefreshListener.onRefresh(true);
        }
        if (mAdapter.size() - mPosition < MAX_PAGE_SIZE + 5) {
            mOnRefreshListener.onRefresh(false);
        }
        mPosition++;
        refreshView(false, false);
    }

    public void resetXY() {
        mTranslationX = 0;
        mTranslationY = 0;
    }

    public void prePage() {
        if (mPosition > 0) {
            mPosition--;
            refreshView(true, true);
        }
    }

    public Object getCurrentItem() {
        if (mPosition >= 0 && mPosition < mAdapter.size()) {
            return mAdapter.getItem(mPosition);
        } else {
            return null;
        }
    }

    public void clear() {
        mAdapter.clear();
        mPosition = 0;
    }

    public boolean performClick() {
        View view = getChildAt(MAX_PAGE_SIZE);
        if (view != null) {
            return getChildAt(MAX_PAGE_SIZE).performClick();
        } else {
            return false;
        }
    }
}
