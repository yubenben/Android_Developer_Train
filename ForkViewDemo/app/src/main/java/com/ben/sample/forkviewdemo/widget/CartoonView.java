package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
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
public class CartoonView extends RelativeLayout {

    private static final String TAG = "CartoonView";

    private Context mContext;

    private ImageView sButton;
    private ImageView mPicture;

    public CartoonView(Context context) {
        this(context, null);
    }

    public CartoonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartoonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        onCreateView(this);
    }

    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.cartoon_view_layout,
                parent,
                false);

        sButton = (ImageView) layout.findViewById(R.id.scale_btn);
        sButton.setOnTouchListener(mTouchListener);

        mPicture = (ImageView) layout.findViewById(R.id.image);

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
                    downScaleX = getScaleX();
                    downScaleY = getScaleY();
                    downRotation = mPicture.getRotation();

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
                    mPicture.setRotation((float) (toAngle - fromAngle)
                            + downRotation);
                    setScaleX((float) (toRadius / fromRadius) * downScaleX);
                    setScaleY((float) (toRadius / fromRadius) * downScaleY);

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
