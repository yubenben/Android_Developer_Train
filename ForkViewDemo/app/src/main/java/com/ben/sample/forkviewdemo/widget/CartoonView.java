package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
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
    private RelativeLayout mParentView;
    private ImageView mTransButton;
    private ImageView mPicture;
    private ImageView mBorder;
    private EditText mEditText;
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

        mParentView = this;
        onCreateView(context, this);
    }

    protected View onCreateView(Context context, ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.cartoon_view,
                parent,
                false);

        mPicture = (ImageView) layout.findViewById(R.id.image);
        mBorder = (ImageView) layout.findViewById(R.id.border);
        mTransButton = (ImageView) layout.findViewById(R.id.scale_btn);
        mEditText = (EditText) layout.findViewById(R.id.edit_text);

        setPictureResource(mImageSourceId);
        setBorderResource(mBackgroundId);
        setEditable(mEditable);
        mTransButton.setOnTouchListener(new View.OnTouchListener() {

            float centerX, centerY;
            float downScaleX, downScaleY, downRotation;
            double fromRadius, toRadius;
            double fromAngle, toAngle;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        final int[] location = new int[2];
                        getLocationOnScreen(location);
                        centerX = location[0] + getWidth() / 2;
                        centerY = location[1] + getHeight() / 2;
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

                        mTransButton.setTranslationX((float)
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
                        mTransButton.setTranslationY((float)
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
                        Log.d(TAG, "mPicture.getWidth()=" + mPicture.getWidth() + "  mPicture.getHeight()=" + mPicture.getHeight());

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
        });
        layout.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setBorderVisible(View.VISIBLE);
                } else {
                    setBorderVisible(View.GONE);
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            float downX, downY, translationX, translationY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        translationX = v.getTranslationX();
                        translationY = v.getTranslationY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        v.setTranslationX(translationX + event.getRawX() - downX);
                        v.setTranslationY(translationY + event.getRawY() - downY);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });

        addView(layout, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        return layout;
    }

    public void setPictureResource(int reference) {
        if (reference != -1) {
            mPicture.setImageResource(reference);
        }
    }

    public void setBorderResource(int reference) {
        if (reference != -1) {
            mPicture.setImageResource(reference);
        }
    }

    public void setBorderVisible(int visible) {
        mBorder.setVisibility(visible);
        mTransButton.setVisibility(visible);
    }

    public void setEditable(boolean editable) {
        if (editable) {
            mEditText.setVisibility(View.VISIBLE);
            mEditText.setMaxWidth(mPicture.getWidth() - TEXT_PADDING_SIZE);
            mEditText.setMaxHeight(mPicture.getHeight() - TEXT_PADDING_SIZE);
        } else {
            mEditText.setVisibility(View.GONE);
        }
    }
}
