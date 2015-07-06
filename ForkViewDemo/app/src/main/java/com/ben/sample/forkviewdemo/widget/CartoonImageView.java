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
public class CartoonImageView extends RelativeLayout {

    private static final String TAG = "CartoonView";

    private Context mContext;

    private ImageView sButton;
    private ImageView mPicture;
    private ImageView mBorder;
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
                    downScaleX = mPicture.getScaleX();
                    downScaleY = mPicture.getScaleY();
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
                    mPicture.setScaleX((float) ((toRadius / fromRadius) * downScaleX));
                    mPicture.setScaleY((float) (toRadius / fromRadius) * downScaleY);
//                    Log.d(TAG, "mPicture.getRotation() " + mPicture.getRotation());
//                    Log.d(TAG, "mPicture roating " + (float) (toAngle - fromAngle)
//                            + downRotation);
//                    Log.d(TAG, "Math.cos(Math.toRadians(mPicture.getRotation())) " + Math.cos(Math.toRadians(mPicture.getRotation())));
//                    Log.d(TAG, "Math.sin(Math.toRadians(mPicture.getRotation())) " + Math.sin(Math.toRadians(mPicture.getRotation())));
//                    int d = Log.d(TAG, "scale " + (mPicture.getWidth() * Math.cos(Math.toRadians(mPicture.getRotation())) + mPicture.getHeight() * Math.sin(Math.toRadians(mPicture.getRotation()))));
                    mBorder.setScaleX((float) (
                                    (mPicture.getWidth() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                            + mPicture.getHeight() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                    )
                                            / mPicture.getWidth()
                                            * ((toRadius / fromRadius) * downScaleX)
                            )
                    );
                    mBorder.setScaleY((float) (
                                    (mPicture.getWidth() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                            + mPicture.getHeight() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                    )
                                            / mPicture.getHeight() * ((toRadius / fromRadius) * downScaleY)
                            )
                    );

                    sButton.setTranslationX((float) (
                                    (
                                            (mPicture.getWidth() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                                    + mPicture.getHeight() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                            )
                                                    / mPicture.getWidth() * ((toRadius / fromRadius) * downScaleX)
                                    )
                                            - 1
                            )
                                    * (mPicture.getWidth() / 2)
                    );
                    sButton.setTranslationY((float) (
                                    (
                                            (mPicture.getWidth() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                                    + mPicture.getHeight() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                            )
                                                    / mPicture.getHeight() * ((toRadius / fromRadius) * downScaleY)
                                    )
                                            - 1
                            )
                                    * (mPicture.getHeight() / 2)
                    );

                    //                Log.d(TAG, "move event.getRawY():" + event.getRawY());
                    //                Log.d(TAG, "scale toRadius=" + toRadius);
                    //                Log.d(TAG, "scale toAngle=" + toAngle);
                    //                Log.d(TAG, "scale " + (toRadius / fromRadius));
                    //                    Log.d(TAG, "");

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

    public CartoonImageView(Context context) {
        this(context, null);
    }

    public CartoonImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartoonImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        onCreateView(this);
    }

    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.cartoon_image_view,
                parent,
                false);

        mPicture = (ImageView) layout.findViewById(R.id.image);
        mBorder = (ImageView) layout.findViewById(R.id.border);
        sButton = (ImageView) layout.findViewById(R.id.scale_btn);
        sButton.setOnTouchListener(mTouchListener);

        addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        return layout;
    }

}
