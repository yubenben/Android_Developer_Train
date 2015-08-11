package com.ben.sample.forkviewdemo.widget.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.crazy.gezi.R;
import com.crazy.gezi.ui.widget.GridLinesView;
import com.crazy.gezi.utils.CacheManager;
import com.crazy.gezi.utils.PlatformUtils;
import com.soundcloud.android.crop.Crop;

public class CustomCameraActivity extends Activity implements
        SurfaceHolder.Callback, FocusManager.Listener {
    private final static String TAG = "CustomCamera";

    private CameraManager cameraManager;

    // View to display the camera output.
    private SurfaceView mSurfaceView;
    private boolean hasSurface;

    // Reference to the containing view.
    private ImageView mCancleButton;
    private GridLinesView mGridLinesView;
    private ImageView mGridLinesButton;
    private ImageView mSwitchCamera;
    private ImageView mSwitchFlash;
    private ImageView mGalleryButton;
    private ShutterButton mCaptureButton;
    private ImageView mRefreshButton;
    private RotateLayout mRotateLayout;
    private RotateImageView mThumbnailView;

    private Thumbnail mThumbnail;
    private ContentResolver mContentResolver;
    private View mTopGuidance = null;
    private boolean mGuidanceHasShown = false;

    private int mFlashMode = 0; // 0 自动 1 打开 2 关
    private boolean hastakedPicture = false;

    private File mPictureFile = null;

    private int currentCameraId = OpenCameraInterface.CAMERA_FACE_BACK;

    /**
     * OnCreateView fragment override
     * 
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_custom_camera_capture);

        mContentResolver = getContentResolver();
        hasSurface = false;
        initView();
    }

    private void initView() {

        mSurfaceView = (SurfaceView) findViewById(R.id.preview_view);
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                cameraManager.getFocusManger().onTouch(
                        CustomCameraActivity.this, event);
                return true;
            }
        });

        mRotateLayout = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);

        mGridLinesView = (GridLinesView) findViewById(R.id.grid_lines_view);
        mGridLinesButton = (ImageView) findViewById(R.id.grid_lines_button);
        mGridLinesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mGridLinesView.getVisibility() == View.VISIBLE) {
                    mGridLinesView.setVisibility(View.INVISIBLE);
                } else {
                    mGridLinesView.setVisibility(View.VISIBLE);
                }
            }
        });

        mSwitchCamera = (ImageView) findViewById(R.id.switch_camera_button);
        mSwitchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    changeCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mSwitchFlash = (ImageView) findViewById(R.id.flash_button);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String keyFlash = prefs.getString(
                CameraConfigurationManager.KEY_FLASH_MODE, "");
        if (keyFlash.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
            mSwitchFlash.setImageResource(R.drawable.camera_flash_auto);
        } else if (keyFlash.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
            mSwitchFlash.setImageResource(R.drawable.camera_flash_on);
        } else if (keyFlash.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
            mSwitchFlash.setImageResource(R.drawable.camera_flash_off);
        }
        mSwitchFlash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mFlashMode++;
                if (mFlashMode > 2)
                    mFlashMode = 0;
                switch (mFlashMode) {
                case 0:
                    mSwitchFlash.setImageResource(R.drawable.camera_flash_auto);
                    break;
                case 1:
                    mSwitchFlash.setImageResource(R.drawable.camera_flash_on);
                    break;
                case 2:
                    mSwitchFlash.setImageResource(R.drawable.camera_flash_off);
                    break;
                default:
                    break;
                }
                cameraManager.setFlash(mFlashMode);
            }
        });

        mGalleryButton = (ImageView) findViewById(R.id.fragment_camera_gallery_button);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Crop.pickImage(CustomCameraActivity.this);
                StatService.onEvent(CustomCameraActivity.this, "107002",
                        "publish.activity", 1);
            }
        });

        mThumbnailView = (RotateImageView) findViewById(R.id.thumbnail);
        mThumbnailView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Crop.pickImage(CustomCameraActivity.this);
                StatService.onEvent(CustomCameraActivity.this, "107002",
                        "publish.activity", 1);
            }
        });
        initThumbnailButton();

        // Trap the capture button.
        mCaptureButton = (ShutterButton) findViewById(R.id.fragment_camera_shutter_button);
        mCaptureButton
                .setOnShutterButtonListener(new ShutterButton.OnShutterButtonListener() {

                    @Override
                    public void onShutterButtonFocus(boolean pressed) {
                        if (hastakedPicture) {
                            return;
                        }
                        if (pressed) {
                            cameraManager.getFocusManger().onShutterDown();
                        } else {
                            cameraManager.getFocusManger().onShutterUp();
                        }
                    }

                    @Override
                    public void onShutterButtonClick() {
                        if (hastakedPicture) {
                            Intent intent = new Intent();
                            // intent.setData(Uri.fromFile(mPictureFile));
                            intent.putExtra("photo",
                                    mPictureFile.getAbsolutePath());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            cameraManager.getFocusManger().doSnap();
                        }
                    }
                });

        mRefreshButton = (ImageView) findViewById(R.id.fragment_camera_refresh_button);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (hastakedPicture) {
                    hastakedPicture = false;
                    mCaptureButton
                            .setBackgroundResource(R.drawable.camera_shutter_button_background);
                    mCaptureButton.setClickable(true);
                    resetCamera();
                }
            }
        });

        mCancleButton = (ImageView) findViewById(R.id.action_bar_cancel);
        mCancleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mGuidanceHasShown = PlatformUtils.getPreferenceValue(
                "camera.guidance.shown", false);

        if (!mGuidanceHasShown) {
            mGuidanceHasShown = true;
            showTopIndicator(R.string.guidance_camera);
        }
    }

    private void showTopIndicator(int indicatorResId) {

        if (mTopGuidance == null) {
            ViewStub mTopStub = (ViewStub) findViewById(R.id.guidance_stub_camera);
            mTopGuidance = mTopStub.inflate();

            final View root = mTopGuidance.findViewById(R.id.guidance_root);
            root.setVisibility(View.INVISIBLE);
            ImageView indicator = (ImageView) mTopGuidance
                    .findViewById(R.id.indicator);
            View container = mTopGuidance.findViewById(R.id.container);
            container.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mTopGuidance.setVisibility(View.INVISIBLE);
                }
            });

            RelativeLayout.LayoutParams containerLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            containerLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            containerLayoutParams.addRule(RelativeLayout.ABOVE,
                    indicator.getId());
            container.setLayoutParams(containerLayoutParams);

            TextView guidance = (TextView) mTopGuidance
                    .findViewById(R.id.content);
            View anchorView = root.findViewById(R.id.bottom1);

            guidanceLayout(anchorView, indicator, root);

            indicator.setImageResource(R.drawable.ic_guidance_indicator_down);
            guidance.setText(indicatorResId);
            PlatformUtils.setPreferenceValue("camera.guidance.shown", true);
        }
    }

    private void guidanceLayout(final View anchorView, final View targetView,
            final View rootView) {
        anchorView.setVisibility(View.VISIBLE);
        OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                int anchorWidth = anchorView.getWidth();
                int anchorHeight = anchorView.getHeight();
                int left = anchorView.getLeft();
                int right = anchorView.getRight();

                // Log.d("layout", "anchor width=" + anchorWidth);
                // Log.d("layout", "anchor height=" + anchorHeight);
                // Log.d("layout", "anchor left=" + left);
                // Log.d("layout", "anchor right=" + right);

                int targetWidth = targetView.getWidth();
                int targetHeight = targetView.getHeight();
                int targetLeft = targetView.getLeft();
                int targetRight = targetView.getRight();

                // Log.d("layout", "targetWidth width=" + targetWidth);
                // Log.d("layout", "targetHeight height=" + targetHeight);
                // Log.d("layout", "targetLeft left=" + targetLeft);
                // Log.d("layout", "targetRight right=" + targetRight);

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) targetView
                        .getLayoutParams();
                layoutParams.setMargins(left + anchorWidth / 2 - targetWidth
                        / 2, layoutParams.topMargin, layoutParams.rightMargin,
                        layoutParams.bottomMargin);
                targetView.setLayoutParams(layoutParams);

                anchorView.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                rootView.setVisibility(View.VISIBLE);
            }
        };

        anchorView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private void initThumbnailButton() {
        // Load the thumbnail from the disk.
        mThumbnail = Thumbnail.loadFrom(new File(getFilesDir(),
                Thumbnail.LAST_THUMB_FILENAME));
        updateThumbnailButton();
    }

    private void updateThumbnailButton() {
        // Update last image if URI is invalid and the storage is ready.
        if ((mThumbnail == null || !Util.isUriValid(mThumbnail.getUri(),
                mContentResolver)) /* && mPicturesRemaining >= 0 */) {
            mThumbnail = Thumbnail.getLastThumbnail(mContentResolver);
        }
        if (mThumbnail != null) {
            mThumbnailView.setBitmap(mThumbnail.getBitmap());
        } else {
            mThumbnailView.setImageResource(R.drawable.camera_glyph_gallery);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        cameraManager.stopPreview();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Ignore failed requests
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
        case Crop.REQUEST_PICK:
            Uri uri = data.getData();
            doCropPhoto(uri);
            break;

        case Crop.REQUEST_CROP:
            Intent intent = new Intent();
            // intent.setData(Uri.fromFile(mPictureFile));
            intent.putExtra("photo", mPictureFile.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
            break;
        default:
            Log.w(TAG, "onActivityResult : invalid request code");

        }
    }

    // Menu键抢拍功能
    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
    // cameraManager.takePicture(null, null, mPicture, currentCameraId);
    // }
    // return super.onKeyDown(keyCode, event);
    // }

    private int ICON_SIZE = 480;

    protected void doCropPhoto(Uri inputUri) {
        Log.d(TAG, "裁剪图片：" + inputUri);
        try {
            mPictureFile = CacheManager.getInstance().mkCacheFile("1234");
            // mPictureFile = getOutputMediaFile();
            Uri outputUri = Uri.fromFile(mPictureFile);
            new Crop(inputUri).output(outputUri).asSquare()
                    .withMaxSize(ICON_SIZE, ICON_SIZE).start(this);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Cannot crop image", e);
            Toast.makeText(this, "Cannot crop image", Toast.LENGTH_LONG).show();
        }
    }

    private void resetCamera() {
        cameraManager.stopPreview();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.removeCallback(this);
            mSurfaceView.destroyDrawingCache();
        }

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    private void changeCamera() throws IOException {
        hastakedPicture = false;
        mCaptureButton
                .setBackgroundResource(R.drawable.camera_shutter_button_background);

        if (currentCameraId == OpenCameraInterface.CAMERA_FACE_FRONT) {
            currentCameraId = (OpenCameraInterface.CAMERA_FACE_BACK);
            mSwitchFlash.setVisibility(View.VISIBLE);
        } else if (currentCameraId == OpenCameraInterface.CAMERA_FACE_BACK) {
            currentCameraId = (OpenCameraInterface.CAMERA_FACE_FRONT);
            mSwitchFlash.setVisibility(View.INVISIBLE);
        }

        resetCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG,
                    "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG,
                    "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder, currentCameraId);
            cameraManager.startPreview();
            cameraManager.getFocusManger().initialize(mRotateLayout,
                    mSurfaceView, this,
                    currentCameraId == OpenCameraInterface.CAMERA_FACE_FRONT,
                    90);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        // builder.setPositiveButton(R.string.ok, new
        // FinishListener(this));
        // builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    public void stopFaceDetection() {

    }

    @Override
    public void startFaceDetection() {

    }

    @Override
    public void setFocusParameters() {
        cameraManager
                .setCameraParameters(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    @Override
    public boolean capture() {
        hastakedPicture = true;
        mCaptureButton.setBackgroundResource(R.drawable.circlegray);
        mCaptureButton.setClickable(false);

        cameraManager.takePicture(mShutterCallback, null, mPicture);
        return true;
    }

    @Override
    public void cancelAutoFocus() {
        cameraManager.cacelAutoFocus();
    }

    @Override
    public void autoFocus() {
        cameraManager.autoFocus();
    }

    private final ShutterCallback mShutterCallback = new ShutterCallback();

    private final class ShutterCallback implements
            android.hardware.Camera.ShutterCallback {
        public void onShutter() {
            cameraManager.getFocusManger().onShutter();
        }
    }

    /**
     * Picture Callback for handling a picture capture and saving it out to a
     * file.
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // try {
            // FileOutputStream fos2 = new FileOutputStream(
            // getOutputMediaFile());
            // fos2.write(data);
            // fos2.close();
            // } catch (Exception e) {
            //
            // }

            // mPictureFile = getOutputMediaFile();
            mPictureFile = CacheManager.getInstance().mkCacheFile("photo");
            if (mPictureFile == null) {
                Toast.makeText(CustomCameraActivity.this,
                        "Image retrieval failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
                Bitmap srcBmp, rotBmp, dstBmp;
                srcBmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                float degrees = 0f;
                switch (display.getRotation()) {
                case Surface.ROTATION_0:
                    degrees = 90f;
                    break;
                case Surface.ROTATION_90:
                    degrees = 0f;
                    break;
                case Surface.ROTATION_180:
                    degrees = 0f;
                    break;
                case Surface.ROTATION_270:
                    degrees = 180f;
                    break;
                }
                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postRotate(degrees);
                if (android.os.Build.VERSION.SDK_INT > 13
                        && currentCameraId == OpenCameraInterface.CAMERA_FACE_FRONT) {
                    float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
                    matrix = new Matrix();
                    Matrix matrixMirrorY = new Matrix();
                    matrixMirrorY.setValues(mirrorY);
                    matrix.postConcat(matrixMirrorY);
                    matrix.preRotate(270);
                }

                rotBmp = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(),
                        srcBmp.getHeight(), matrix, true);

                if (!srcBmp.isRecycled()) {
                    srcBmp.recycle();
                }
                matrix.reset();

                int x, y, w, h, wh;
                Point cameraResolution = cameraManager
                        .getCameraConfigurationManager().getCameraResolution();
                h = rotBmp.getHeight();
                w = rotBmp.getHeight() * cameraResolution.y
                        / cameraResolution.x;
                wh = w > h ? h : w;// 裁切后所取的正方形区域边长
                // Log.d(TAG, "rotBmp getHeight() = " + rotBmp.getHeight()
                // + ", getWidth() = " + rotBmp.getWidth());
                // Log.d(TAG, "camera cameraResolution x = " +
                // cameraResolution.x
                // + ", y = " + cameraResolution.y);
                // Log.d(TAG, "rotBmp w = " + w + ", wh = " + wh);

                x = (rotBmp.getWidth() - w) / 2;
                float barHeight = getResources().getDimension(
                        R.dimen.action_bar_height);
                y = (int) ((barHeight * rotBmp.getHeight()) / PlatformUtils
                        .getScreenHeight(CustomCameraActivity.this));

                float scale_wh = (480F / wh);
                if (scale_wh < 1F) {
                    matrix.postScale(scale_wh, scale_wh);
                }

                Log.v(TAG, "dstBmp x=" + x + " y=" + y + " wh=" + wh);
                dstBmp = Bitmap
                        .createBitmap(rotBmp, x, y, wh, wh, matrix, true);

                if (!rotBmp.isRecycled()) {
                    rotBmp.recycle();
                }
                FileOutputStream fos = new FileOutputStream(mPictureFile);
                dstBmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                if (!dstBmp.isRecycled()) {
                    dstBmp.recycle();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mCaptureButton.setClickable(true);
            }
        }
    };

    /**
     * Used to return the camera File output.
     * 
     * @return
     */
    // private File getOutputMediaFile() {
    //
    // File mediaStorageDir = new File(
    // Environment
    // .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
    // "ZXSCameraGuideApp");
    //
    // if (!mediaStorageDir.exists()) {
    // if (!mediaStorageDir.mkdirs()) {
    // Log.d("Camera Guide", "Required media storage does not exist");
    // return null;
    // }
    // }
    //
    // // Create a media file name
    // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
    // .format(new Date());
    // File mediaFile;
    // mediaFile = new File(mediaStorageDir.getPath() + File.separator
    // + "IMG_" + timeStamp + (Math.random() * 1000) + ".jpg");
    //
    // // DialogHelper.showDialog("Success!", "Your picture has been saved!",
    // // NativeCameraActivity.this);
    //
    // return mediaFile;
    // }
}
