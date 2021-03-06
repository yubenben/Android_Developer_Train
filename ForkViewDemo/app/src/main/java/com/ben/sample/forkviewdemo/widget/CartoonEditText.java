package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.R;

/**
 * Created by benben on 15-7-6.
 */
public class CartoonEditText extends RelativeLayout {

    private static final String TAG = "CartoonView";

    private Context mContext;

    private ImageView sButton;
    private ImageView mPicture;
    private ImageView mBorder;
    private EditText mEditText;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

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

                    mBorder.setScaleX((float)
                                    (
                                            (
                                                    mPicture.getWidth() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                                            + mPicture.getHeight() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                            )
                                                    / mPicture.getWidth() * ((toRadius / fromRadius) * downScaleX)
                                    )
                    );
                    mBorder.setScaleY((float)
                                    (
                                            (
                                                    mPicture.getWidth() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                                            + mPicture.getHeight() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                            )
                                                    / mPicture.getHeight() * ((toRadius / fromRadius) * downScaleY)
                                    )
                    );

                    sButton.setTranslationX((float)
                                    (
                                            (
                                                    (
                                                            mPicture.getWidth() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                                                    + mPicture.getHeight() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                                    )
                                                            / mPicture.getWidth() * ((toRadius / fromRadius) * downScaleX)
                                            )
                                                    - 1
                                    )
                                    * (mPicture.getWidth() / 2)
                    );
                    sButton.setTranslationY((float)
                                    (
                                            (
                                                    (
                                                            mPicture.getWidth() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                                                    + mPicture.getHeight() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                                    )
                                                            / mPicture.getHeight() * ((toRadius / fromRadius) * downScaleY)
                                            )
                                                    - 1
                                    )
                                    * (mPicture.getHeight() / 2)
                    );

                    mEditText.setMaxWidth((int) (mPicture.getWidth() * mPicture.getScaleX() - 20));
                    mEditText.setMaxHeight((int) (mPicture.getHeight() * mPicture.getScaleY() - 20));

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

    public CartoonEditText(Context context) {
        this(context, null);
    }

    public CartoonEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartoonEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        onCreateView(this);
    }

    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.cartoon_edit_text,
                parent,
                false);

        mPicture = (ImageView) layout.findViewById(R.id.image);
        mBorder = (ImageView) layout.findViewById(R.id.border);
        sButton = (ImageView) layout.findViewById(R.id.scale_btn);
        mEditText = (EditText) layout.findViewById(R.id.edit_text);
        mEditText.setMaxWidth(mPicture.getWidth() - 20);
        mEditText.setMaxHeight(mPicture.getHeight() - 20);
        sButton.setOnTouchListener(mTouchListener);

        addView(layout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        return layout;
    }

}
