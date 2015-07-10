package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by benben on 15-7-9.
 */
public class NewCartoonView extends RelativeLayout {

    private static final String TAG = "CartoonView";

    private RelativeLayout mParentView;
    private ImageView mSourceImage;
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

        mSourceImage = new ImageView(context);
        mSourceImage.setFocusableInTouchMode(true);
        if (mImageSourceId != -1) {
            mSourceImage.setImageResource(mImageSourceId);
        }

        addView(mSourceImage, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    public void addCartton(Context context, int img_id, int width, int height, boolean editable) {
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
        imageView.requestFocus();
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
                        v.requestFocus();
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

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });

        centerView.setFocusable(true);
        centerView.setFocusableInTouchMode(true);
        centerView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.bringToFront();
                    bordView.setVisibility(View.VISIBLE);
                    bordView.bringToFront();
                } else {
                    bordView.setVisibility(View.GONE);
                }
            }
        });
    }

    public void saveCartoonView() {
        mSourceImage.requestFocus();
        mParentView.buildDrawingCache();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File file = new File(path, "demo1.png");
                try {
                    path.mkdirs();
                    OutputStream out = new FileOutputStream(file);
                    mParentView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
