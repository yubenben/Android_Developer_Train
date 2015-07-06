package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.R;

/**
 * Created by benben on 15-7-3.
 */
public class BorderLayout extends RelativeLayout{
    private static final String TAG = "CartoonView";

    private Context mContext;
    private ImageView mCenterView;
    private ImageView scaleButton, delButton, rightButton, bottomButton;

    public BorderLayout(Context context) {
        this(context, null);
    }

    public BorderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BorderLayout);
        int alignView = a.getResourceId(R.styleable.BorderLayout_center_view, -1);
        ViewGroup mViewGroup = (ViewGroup)getParent();
        mCenterView = (ImageView)findViewById(alignView);
        a.recycle();

        onCreateView(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "mCenterView.getMeasuredWidth() " + mCenterView.getMeasuredWidth());
        Log.d(TAG, "mCenterView.getMeasuredHeight() " + mCenterView.getMeasuredHeight());
    }

    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.border_layout,
                parent,
                false);

        scaleButton = (ImageView) layout.findViewById(R.id.scale_btn);
        scaleButton.setOnTouchListener(mTouchListener);

        addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        return layout;
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {

        float centerX, centerY;
        float downScaleX, downScaleY, downRotation;
        double fromRadius, toRadius;
        double fromAngle, toAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    centerX = getX() + getWidth() / 2;
                    centerY = getY() + getHeight() / 2;
                    fromRadius = Math.sqrt(Math.pow((event.getRawX() - centerX), 2)
                            + Math.pow((event.getRawY() - centerY), 2));
                    fromAngle = Math.toDegrees(Math.atan((event.getRawY() - centerY)
                            / (event.getRawX() - centerX)));
                    downScaleX = mCenterView.getScaleX();
                    downScaleY = mCenterView.getScaleY();
                    downRotation = mCenterView.getRotation();

                    //                Log.d(TAG, "down event.getRawX():" + event.getRawX());
                    //                Log.d(TAG, "down event.getRawY():" + event.getRawY());
                    //                Log.d(TAG, "down getX():" + getX());
                    //                Log.d(TAG, "down getY():" + getY());
                    //                Log.d(TAG, "down getWidth():" + getWidth());
                    //                Log.d(TAG, "down getHeight():" + getHeight());
                    //                Log.d(TAG, "down centerX:" + centerX);
                    //                Log.d(TAG, "down centerY:" + centerY);
                    //                Log.d(TAG, "scale fromRadius=" + fromRadius);
                    //                Log.d(TAG, "scale fromAngle=" + fromAngle);
                    //                Log.d(TAG, " ");
                    break;
                case MotionEvent.ACTION_MOVE:
                    toRadius = Math.sqrt(Math.pow((event.getRawX() - centerX), 2)
                            + Math.pow((event.getRawY() - centerY), 2));
                    toAngle = Math.toDegrees(Math.atan((event.getRawY() - centerY)
                            / (event.getRawX() - centerX)));
                    if (event.getRawX() < centerX) {
                        toAngle += 180;
                    }
                    mCenterView.setRotation((float) (toAngle - fromAngle)
                            + downRotation);
                    mCenterView.setScaleX((float) (toRadius / fromRadius) * downScaleX);
                    mCenterView.setScaleY((float) (toRadius / fromRadius) * downScaleY);

                    //                Log.d(TAG, "move event.getRawX():" + event.getRawX());
                    //                Log.d(TAG, "move event.getRawY():" + event.getRawY());
                    //                Log.d(TAG, "scale toRadius=" + toRadius);
                    //                Log.d(TAG, "scale toAngle=" + toAngle);
                    //                Log.d(TAG, "scale " + (toRadius / fromRadius));
                    //                Log.d(TAG, " ");

                    break;
                case MotionEvent.ACTION_UP:

                    break;
                case MotionEvent.ACTION_CANCEL:

                    break;

                default:
                    break;
            }
            return true;
        }
    };
}
