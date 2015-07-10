package com.ben.sample.forkviewdemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.ben.sample.forkviewdemo.widget.NewCartoonView;


public class MainActivity extends ActionBarActivity {

    private RelativeLayout mCanvasLayout;
    private NewCartoonView mNewCartoonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCanvasLayout = (RelativeLayout) findViewById(R.id.canve_layout);
        mNewCartoonView = (NewCartoonView) findViewById(R.id.cartoon);
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
                mNewCartoonView.addCartton(this, R.drawable.fav_search_location, 700, 700, false);
                break;
            case R.id.add_edit:
                mNewCartoonView.addCartton(this, R.drawable.speech_vector, 700, 700, true);
                break;
            case R.id.add_squre:
                mNewCartoonView.addCartton(this, R.drawable.app_attach_file_icon_music_large, 700, 700, false);
                break;
            case R.id.save:
                mNewCartoonView.saveCartoonView();
//                int count = mCanvasLayout.getChildCount();
//                for (int i = 0; i < count; i++) {
//                    View view = mCanvasLayout.getChildAt(i);
//                    if (view instanceof CartoonView) {
//                        ((CartoonView) view).setBorderVisible(View.GONE);
//                    }
//                }
//                mCanvasLayout.buildDrawingCache();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//                        File file = new File(path, "demo1.png");
//                        try {
//                            path.mkdirs();
//                            OutputStream out = new FileOutputStream(file);
//                            mCanvasLayout.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 90, out);
//                            out.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
