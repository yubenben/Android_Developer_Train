package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.R;

/**
 * Created by benben on 15-7-9.
 */
public class NewCartoonView extends RelativeLayout {

    private static final String TAG = "CartoonView";

    private RelativeLayout mParentView;
    private int mImageSourceId;

    public NewCartoonView(Context context) {
        this(context, null);
    }

    public NewCartoonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewCartoonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CartoonView);
        mImageSourceId = a.getResourceId(R.styleable.CartoonView_src, -1);
        a.recycle();

        mParentView = this;

        ImageView imageView = new ImageView(context);
        if (mImageSourceId != -1) {
            imageView.setImageResource(mImageSourceId);
        }

        addView(imageView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    public void addCartton(Context context, int img_id, int width, int height) {
        LayoutParams imageParams = new LayoutParams(width, height);
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(img_id);
        addView(imageView, imageParams);

        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View border = layoutInflater.inflate(R.layout.border_layout,
                null,
                false);
        ImageView scaleBtn = (ImageView) border.findViewById(R.id.scale_btn);
        LayoutParams borderParams = new LayoutParams(width, height);
        borderParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(border, borderParams);

        addDragAnimation(imageView, border, scaleBtn);
    }

    private void addDragAnimation(final View centerView, final View bordView, final View dragBtn) {

        dragBtn.setOnTouchListener(new View.OnTouchListener() {

            float centerX, centerY;
            float downScaleX, downScaleY, downRotation;
            double fromRadius, toRadius;
            double fromAngle, toAngle;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        final int[] location = new int[2];
                        bordView.getLocationOnScreen(location);
                        centerX = location[0] + bordView.getWidth() * bordView.getScaleX() / 2;
                        centerY = location[1] + bordView.getHeight() * bordView.getScaleY() / 2;
                        fromRadius = Math.sqrt(Math.pow((event.getRawX() - centerX), 2)
                                + Math.pow((event.getRawY() - centerY), 2));
                        fromAngle = Math.toDegrees(Math.atan((event.getRawY() - centerY)
                                / (event.getRawX() - centerX)));
                        downScaleX = centerView.getScaleX();
                        downScaleY = centerView.getScaleY();
                        downRotation = centerView.getRotation();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        toRadius = Math.sqrt(Math.pow((event.getRawX() - centerX), 2)
                                + Math.pow((event.getRawY() - centerY), 2));
                        toAngle = Math.toDegrees(Math.atan((event.getRawY() - centerY)
                                / (event.getRawX() - centerX)));
                        if (event.getRawX() < centerX) {
                            toAngle += 180;
                        }
                        centerView.setRotation((float) (toAngle - fromAngle)
                                + downRotation);
                        centerView.setScaleX((float) ((toRadius / fromRadius) * downScaleX));
                        centerView.setScaleY((float) (toRadius / fromRadius) * downScaleY);

                        ViewGroup.LayoutParams params = bordView.getLayoutParams();
                        params.width =
                                (int) (
                                        ((toRadius / fromRadius) * downScaleX) *
                                                (
                                                        centerView.getWidth() * Math.abs(Math.cos(Math.toRadians(centerView.getRotation())))
                                                                + centerView.getHeight() * Math.abs(Math.sin(Math.toRadians(centerView.getRotation())))
                                                )
                                );
                        params.height =
                                (int) (
                                        ((toRadius / fromRadius) * downScaleY) *
                                                (
                                                        centerView.getWidth() * Math.abs(Math.sin(Math.toRadians(centerView.getRotation())))
                                                                + centerView.getHeight() * Math.abs(Math.cos(Math.toRadians(centerView.getRotation())))
                                                )
                                );

                        bordView.setLayoutParams(params);

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

        centerView.setOnTouchListener(new OnTouchListener() {
            float downX, downY, translationX, translationY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        translationX = centerView.getTranslationX();
                        translationY = centerView.getTranslationY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        centerView.setTranslationX(translationX + event.getRawX() - downX);
                        centerView.setTranslationY(translationY + event.getRawY() - downY);
                        bordView.setTranslationX(translationX + event.getRawX() - downX);
                        bordView.setTranslationY(translationY + event.getRawY() - downY);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.requestFocus();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });

        centerView.setFocusable(true);
        centerView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bordView.setVisibility(View.VISIBLE);
                } else {
                    bordView.setVisibility(View.GONE);
                }
            }
        });

        centerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                centerView.requestFocus();
            }
        });
    }
}
