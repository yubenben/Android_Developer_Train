package com.ben.sample.forkviewdemo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by benben on 15-7-13.
 */
public class CustomImageView extends View {

    private Bitmap mBitmap;
    private Bitmap newBitmap;
    private Matrix mMatrix;
    private float mRotation;

    public CustomImageView(Context context) {
        this(context, null);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public CustomImageView(Context context, int reference) {
        this(context);

        mBitmap = BitmapFactory.decodeResource(getResources(), reference);
        mMatrix = new Matrix();
        flushBitmap();
    }

    public static Bitmap createTransparentBitmapFromBitmap(Bitmap bitmap,
                                                           int replaceThisColor) {
        if (bitmap != null) {
            int picw = bitmap.getWidth();
            int pich = bitmap.getHeight();
            int[] pix = new int[picw * pich];
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

            int top, left, bottom, right;

            for (int y = 0; y < pich; y++) {
                // from left to right
                for (int x = 0; x < picw; x++) {
                    int index = y * picw + x;

                    if (pix[index] == replaceThisColor) {
                        pix[index] = Color.TRANSPARENT;
                    } else if (pix[index] == Color.TRANSPARENT) {

                    } else {
                        left = picw;
                        break;
                    }
                }

                // from right to left
                for (int x = picw - 1; x >= 0; x--) {
                    int index = y * picw + x;

                    if (pix[index] == replaceThisColor) {
                        pix[index] = Color.TRANSPARENT;
                    } else {
                        right = picw;
                        break;
                    }
                }
            }

            Bitmap bm = Bitmap.createBitmap(pix, picw, pich,
                    Bitmap.Config.ARGB_4444);

            return bm;
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(newBitmap.getWidth(), newBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(0xff00ff00);
        paint.setStrokeWidth(10);
        canvas.drawLine(0, 0, newBitmap.getWidth(), 0, paint);
        canvas.drawLine(0, 0, 0, newBitmap.getHeight(), paint);
        canvas.drawLine(0, newBitmap.getHeight(), newBitmap.getWidth(), newBitmap.getHeight(), paint);
        canvas.drawLine(newBitmap.getWidth(), 0, newBitmap.getWidth(), newBitmap.getHeight(), paint);
        canvas.drawBitmap(newBitmap, 0, 0, paint);
    }

    public float getRotation() {
        return mRotation;
    }

    public void setRotation(float rotation) {
        mMatrix.setRotate(rotation, getWidth() / 2, getHeight() / 2);
        flushBitmap();
        requestLayout();
        invalidate();
        mRotation = rotation;
    }

    private void flushBitmap() {
        newBitmap = createTransparentBitmapFromBitmap(Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mMatrix, true), 0xff000000);
    }

}
