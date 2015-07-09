package com.ben.sample.forkviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.widget.CartoonView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity {

    private RelativeLayout mCanvasLayout;
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCanvasLayout = (RelativeLayout) findViewById(R.id.canve_layout);
        mImageView = (ImageView) findViewById(R.id.photo_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_circle:
                mCanvasLayout.addView(newCartoonView(this, R.drawable.fav_search_location, false));
                break;
            case R.id.add_edit:
                mCanvasLayout.addView(newCartoonView(this, R.drawable.speech_vector, true));
                break;
            case R.id.add_squre:
                mCanvasLayout.addView(newCartoonView(this, R.drawable.app_attach_file_icon_music_large, false));
                break;
            case R.id.save:
//                mCanvasLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//                mCanvasLayout.layout(0, 0, mCanvasLayout.getMeasuredWidth(), mCanvasLayout.getMeasuredHeight());
                int count = mCanvasLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = mCanvasLayout.getChildAt(i);
                    if (view instanceof CartoonView) {
                        ((CartoonView) view).setBorderVisible(View.GONE);
                    }
                }
                mCanvasLayout.buildDrawingCache();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File file = new File(path, "demo1.png");
                        try {
                            path.mkdirs();
                            OutputStream out = new FileOutputStream(file);
                            mCanvasLayout.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private CartoonView newCartoonView(Context context, int img_id, boolean editable) {
        CartoonView cartoon = new CartoonView(context);
        cartoon.setPictureResource(img_id);
        cartoon.setEditable(editable);
        return cartoon;
    }
}
