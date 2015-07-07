package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
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
public class CartoonView extends RelativeLayout {

    private static final String TAG = "CartoonView";

    private static final int TEXT_PADDING_SIZE = 100;
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
                                            ((toRadius / fromRadius) * downScaleX) *
                                                    (
                                                            mPicture.getWidth() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                                                    + mPicture.getHeight() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                                    )
                                                    / mPicture.getWidth()
                                    )
                    );
                    mBorder.setScaleY((float)
                                    (
                                            ((toRadius / fromRadius) * downScaleY) *
                                                    (
                                                            mPicture.getWidth() * Math.abs(Math.sin(Math.toRadians(mPicture.getRotation())))
                                                                    + mPicture.getHeight() * Math.abs(Math.cos(Math.toRadians(mPicture.getRotation())))
                                                    )
                                                    / mPicture.getHeight()
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

                    mEditText.setMaxWidth((int) (mPicture.getWidth() * mPicture.getScaleX() - TEXT_PADDING_SIZE));
                    mEditText.setMaxHeight((int) (mPicture.getHeight() * mPicture.getScaleY() - TEXT_PADDING_SIZE));

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
    private int mImageSourceId;
    private int mBackgroundId;
    private boolean mEditable;

    public CartoonView(Context context) {
        this(context, null);
    }

    public CartoonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartoonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CartoonView);
        mImageSourceId = a.getResourceId(R.styleable.CartoonView_src, -1);
        mBackgroundId = a.getResourceId(R.styleable.CartoonView_border, -1);
        mEditable = a.getBoolean(R.styleable.CartoonView_editable, false);
        a.recycle();

        onCreateView(context, this);
    }

    protected View onCreateView(Context context, ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.cartoon_edit_text,
                parent,
                false);

        mPicture = (ImageView) layout.findViewById(R.id.image);
        mBorder = (ImageView) layout.findViewById(R.id.border);
        sButton = (ImageView) layout.findViewById(R.id.scale_btn);
        mEditText = (EditText) layout.findViewById(R.id.edit_text);

        if (mImageSourceId != -1) {
            mPicture.setImageResource(mImageSourceId);
        }
        if (mBackgroundId != -1) {
            mBorder.setBackgroundResource(mBackgroundId);
        }
        if (mEditable) {
            mEditText.setMaxWidth(mPicture.getWidth() - TEXT_PADDING_SIZE);
            mEditText.setMaxHeight(mPicture.getHeight() - TEXT_PADDING_SIZE);
        } else {
            mEditText.setVisibility(View.GONE);
        }
        sButton.setOnTouchListener(mTouchListener);

        addView(layout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        return layout;
    }

}
