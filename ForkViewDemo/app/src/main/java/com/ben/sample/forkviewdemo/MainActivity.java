package com.ben.sample.forkviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.widget.CartoonView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity {

    private RelativeLayout mCanveLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCanveLayout = (RelativeLayout) findViewById(R.id.canve_layout);
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
                mCanveLayout.addView(newCartoonView(this, R.drawable.fav_search_location, false));
                break;
            case R.id.add_edit:
                mCanveLayout.addView(newCartoonView(this, R.drawable.speech_vector, true));
                break;
            case R.id.add_squre:
                mCanveLayout.addView(newCartoonView(this, R.drawable.app_attach_file_icon_music_large, false));
                break;
            case R.id.save:
                //mCanveLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                //mCanveLayout.layout(0, 0, mCanveLayout.getMeasuredWidth(), mCanveLayout.getMeasuredHeight());
                mCanveLayout.buildDrawingCache();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File file = new File(path, "demo1.png");
                        try {
                            path.mkdirs();
                            OutputStream out = new FileOutputStream(file);
                            mCanveLayout.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 90, out);
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
