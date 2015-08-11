/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ben.sample.forkviewdemo.widget.camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
final class CameraConfigurationManager {

    private static final String TAG = "CustomCamera";

    // This is bigger than the size of a small screen, which is still supported.
    // The routine
    // below will still select the default (presumably 320x240) size for these.
    // This prevents
    // accidental selection of very low resolution on some devices.
    private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
    private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
    private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final int MIN_FPS = 5;

    public static final String KEY_FLASH_MODE = "preferences_flash_mode";

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;
    private Point pictureResolution;

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Camera camera) {
        if (false) {
            Camera.Parameters parameters = camera.getParameters();
            WindowManager manager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point theScreenResolution = new Point();
            display.getSize(theScreenResolution);
            screenResolution = theScreenResolution;
            Log.i(TAG, "Screen resolution: " + screenResolution);
            cameraResolution = findBestPreviewSizeValue(parameters,
                    screenResolution);
            Log.i(TAG, "Camera resolution: " + cameraResolution);
        } else {
            // portrait
            Camera.Parameters parameters = camera.getParameters();
            WindowManager manager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            if (width < height) {
                int temp = width;
                width = height;
                height = temp;
            }
            screenResolution = new Point(height, width);
            cameraResolution = findBestPreviewSizeValue(parameters, new Point(
                    width, height));
            pictureResolution = getBestPictureSize(parameters, width, height);
        }
    }

    void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG,
                    "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG,
                    "In camera config safe mode -- most settings will not be honored");
        }

        // initializeTorch(parameters, prefs, safeMode);

        setBestPreviewFPS(parameters);

        String focusMode = null;
        focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                Camera.Parameters.FOCUS_MODE_AUTO);
        if (focusMode == null) {
            focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_MACRO,
                    Camera.Parameters.FOCUS_MODE_EDOF);
        }
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String flashMode = null;
        String keyFlash = prefs.getString(KEY_FLASH_MODE, "");
        if (!"".equalsIgnoreCase(keyFlash)) {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    keyFlash);
        } else {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_AUTO);
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
        }

        // String focusMode = null;
        // if (prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true)) {
        // if (safeMode
        // || prefs.getBoolean(
        // PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS,
        // true)) {
        // focusMode = findSettableValue(
        // parameters.getSupportedFocusModes(),
        // Camera.Parameters.FOCUS_MODE_AUTO);
        // } else {
        // focusMode = findSettableValue(
        // parameters.getSupportedFocusModes(),
        // Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
        // Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
        // Camera.Parameters.FOCUS_MODE_AUTO);
        // }
        // }
        // // Maybe selected auto-focus but not available, so fall through
        // here:
        // if (!safeMode && focusMode == null) {
        // focusMode =
        // findSettableValue(parameters.getSupportedFocusModes(),
        // Camera.Parameters.FOCUS_MODE_MACRO,
        // Camera.Parameters.FOCUS_MODE_EDOF);
        // }
        // if (focusMode != null) {
        // parameters.setFocusMode(focusMode);
        // }
        //
        // if (!safeMode) {
        // if (prefs.getBoolean(PreferencesActivity.KEY_INVERT_SCAN, false))
        // {
        // String colorMode = findSettableValue(
        // parameters.getSupportedColorEffects(),
        // Camera.Parameters.EFFECT_NEGATIVE);
        // if (colorMode != null) {
        // parameters.setColorEffect(colorMode);
        // }
        // }
        //
        // if (!prefs.getBoolean(
        // PreferencesActivity.KEY_DISABLE_BARCODE_SCENE_MODE, true)) {
        // String sceneMode = findSettableValue(
        // parameters.getSupportedSceneModes(),
        // Camera.Parameters.SCENE_MODE_BARCODE);
        // if (sceneMode != null) {
        // parameters.setSceneMode(sceneMode);
        // }
        // }
        //
        // if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_METERING,
        // true)) {
        // if (parameters.isVideoStabilizationSupported()) {
        // Log.i(TAG, "Enabling video stabilization...");
        // parameters.setVideoStabilization(true);
        // } else {
        // Log.i(TAG,
        // "This device does not support video stabilization");
        // }
        //
        // MeteringInterface.setFocusArea(parameters);
        // MeteringInterface.setMetering(parameters);
        // }
        //
        // }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        parameters.setPictureSize(pictureResolution.x, pictureResolution.y);
        camera.setParameters(parameters);

        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null
                && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size "
                    + cameraResolution.x + 'x' + cameraResolution.y
                    + ", but after setting it, preview size is "
                    + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }
        Camera.Size picSize = afterParameters.getPictureSize();
        if (picSize != null) {
            Log.v(TAG, "picture size is " + picSize.width + "x"
                    + picSize.height);
            pictureResolution.x = picSize.width;
            pictureResolution.y = picSize.height;
        }
        camera.setDisplayOrientation(90);
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    Point getPictureResolution() {
        return pictureResolution;
    }

    void setFlashState(Camera camera, int mode) {

        Camera.Parameters parameters = camera.getParameters();
        String flashMode;

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();

        switch (mode) {
        case 0:
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_AUTO);
            break;
        case 1:
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_ON);
            break;
        case 2:
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_OFF);
            break;
        default:
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_AUTO);
            break;
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
            camera.setParameters(parameters);
            edit.putString(KEY_FLASH_MODE, flashMode);
            edit.commit();
        }
    }

    void setFocusParameters(Camera camera, FocusManager focusManager,
            String focusmode) {

        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getMaxNumFocusAreas() > 0) {
            parameters.setFocusAreas(focusManager.getFocusAreas());
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            // Use the same area for focus and metering.
            parameters.setMeteringAreas(focusManager.getMeteringAreas());
        }

        String focusMode = null;

        focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                focusmode);
        if (focusMode == null) {
            focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                    Camera.Parameters.FOCUS_MODE_AUTO);
        }
        if (focusMode == null) {
            focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_MACRO,
                    Camera.Parameters.FOCUS_MODE_EDOF);
        }
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }
        camera.setParameters(parameters);
        focusManager.overrideFocusMode(parameters.getFocusMode());
    }

    // boolean getTorchState(Camera camera) {
    // if (camera != null) {
    // Camera.Parameters parameters = camera.getParameters();
    // if (parameters != null) {
    // String flashMode = camera.getParameters().getFlashMode();
    // return flashMode != null &&
    // (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
    // Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
    // }
    // }
    // return false;
    // }

    // void setTorch(Camera camera, boolean newSetting) {
    // Camera.Parameters parameters = camera.getParameters();
    // doSetTorch(parameters, newSetting, false);
    // camera.setParameters(parameters);
    // }
    //
    // private void initializeTorch(Camera.Parameters parameters,
    // SharedPreferences prefs, boolean safeMode) {
    // boolean currentSetting = false;//FrontLightMode.readPref(prefs) ==
    // FrontLightMode.ON;
    // doSetTorch(parameters, currentSetting, safeMode);
    // }
    //
    // private void doSetTorch(Camera.Parameters parameters, boolean newSetting,
    // boolean safeMode) {
    // String flashMode;
    // if (newSetting) {
    // flashMode = findSettableValue(parameters.getSupportedFlashModes(),
    // Camera.Parameters.FLASH_MODE_TORCH,
    // Camera.Parameters.FLASH_MODE_ON);
    // } else {
    // flashMode = findSettableValue(parameters.getSupportedFlashModes(),
    // Camera.Parameters.FLASH_MODE_OFF);
    // }
    // if (flashMode != null) {
    // parameters.setFlashMode(flashMode);
    // }

    // SharedPreferences prefs =
    // PreferenceManager.getDefaultSharedPreferences(context);
    // if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_EXPOSURE, true)) {
    // if (!safeMode) {
    // int minExposure = parameters.getMinExposureCompensation();
    // int maxExposure = parameters.getMaxExposureCompensation();
    // if (minExposure != 0 || maxExposure != 0) {
    // float step = parameters.getExposureCompensationStep();
    // int desiredCompensation;
    // if (newSetting) {
    // // Light on; set low exposure compensation
    // desiredCompensation = Math.max((int) (MIN_EXPOSURE_COMPENSATION / step),
    // minExposure);
    // } else {
    // // Light off; set high compensation
    // desiredCompensation = Math.min((int) (MAX_EXPOSURE_COMPENSATION / step),
    // maxExposure);
    // }
    // Log.i(TAG, "Setting exposure compensation to " + desiredCompensation +
    // " / " + (step * desiredCompensation));
    // parameters.setExposureCompensation(desiredCompensation);
    // } else {
    // Log.i(TAG, "Camera does not support exposure compensation");
    // }
    // }
    // }
    // }

    private static void setBestPreviewFPS(Camera.Parameters parameters) {
        // Required for Glass compatibility; also improves battery/CPU
        // performance a tad
        List<int[]> supportedPreviewFpsRanges = parameters
                .getSupportedPreviewFpsRange();
        Log.i(TAG, "Supported FPS ranges: "
                + toString(supportedPreviewFpsRanges));
        if (supportedPreviewFpsRanges != null
                && !supportedPreviewFpsRanges.isEmpty()) {
            int[] minimumSuitableFpsRange = null;
            for (int[] fpsRange : supportedPreviewFpsRanges) {
                int fpsMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
                if (fpsMax >= MIN_FPS * 1000
                        && (minimumSuitableFpsRange == null || fpsMax > minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])) {
                    minimumSuitableFpsRange = fpsRange;
                }
            }
            if (minimumSuitableFpsRange == null) {
                Log.i(TAG, "No suitable FPS range?");
            } else {
                int[] currentFpsRange = new int[2];
                parameters.getPreviewFpsRange(currentFpsRange);
                if (!Arrays.equals(currentFpsRange, minimumSuitableFpsRange)) {
                    Log.i(TAG,
                            "Setting FPS range to "
                                    + Arrays.toString(minimumSuitableFpsRange));
                    parameters
                            .setPreviewFpsRange(
                                    minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                                    minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                }
            }
        }
    }

    // Actually prints the arrays properly:
    private static String toString(Collection<int[]> arrays) {
        if (arrays == null || arrays.isEmpty()) {
            return "[]";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        Iterator<int[]> it = arrays.iterator();
        while (it.hasNext()) {
            buffer.append(Arrays.toString(it.next()));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * 
     * @param sizes
     * @param width
     * @param height
     * @return
     */
    private Point getBestPictureSize(Camera.Parameters parameters, int w, int h) {
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Log.d(TAG, "getBestPictureSize w=" + w + " h=" + h);
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            Log.d(TAG, "support pic size width=" + size.width + " height="
                    + size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        Log.d(TAG, "get pic size width=" + optimalSize.width + " height="
                + optimalSize.height);
        return new Point(optimalSize.width, optimalSize.height);
    }

    private Point findBestPreviewSizeValue(Camera.Parameters parameters,
            Point screenResolution) {

        List<Camera.Size> rawSupportedSizes = parameters
                .getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG,
                    "Device returned no supported preview sizes; using default");
            Camera.Size defaultSize = parameters.getPreviewSize();
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
                rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        if (Log.isLoggable(TAG, Log.INFO)) {
            StringBuilder previewSizesString = new StringBuilder();
            for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
                previewSizesString.append(supportedPreviewSize.width)
                        .append('x').append(supportedPreviewSize.height)
                        .append(' ');
            }
            Log.i(TAG, "Supported preview sizes: " + previewSizesString);
        }

        double screenAspectRatio = (double) screenResolution.x
                / (double) screenResolution.y;

        // Remove sizes that are unsuitable
        Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewSize = it.next();
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight
                    : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth
                    : realHeight;
            double aspectRatio = (double) maybeFlippedWidth
                    / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            if (maybeFlippedWidth == screenResolution.x
                    && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(realWidth, realHeight);
                Log.i(TAG, "Found preview size exactly matching screen size: "
                        + exactPoint);
                return exactPoint;
            }
        }

        // If no exact match, use largest preview size. This was not a great
        // idea on older devices because
        // of the additional computation needed. We're likely to get here on
        // newer Android 4+ devices, where
        // the CPU is much more powerful.
        if (!supportedPreviewSizes.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewSizes.get(0);
            Point largestSize = new Point(largestPreview.width,
                    largestPreview.height);
            Log.i(TAG, "Using largest suitable preview size: " + largestSize);
            return largestSize;
        }

        // If there is nothing at all suitable, return current preview size
        Camera.Size defaultPreview = parameters.getPreviewSize();
        Point defaultSize = new Point(defaultPreview.width,
                defaultPreview.height);
        Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);
        return defaultSize;
    }

    public static String findSettableValue(Collection<String> supportedValues,
            String... desiredValues) {
        Log.i(TAG, "Supported values: " + supportedValues);
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        Log.i(TAG, "Settable value: " + result);
        return result;
    }

}
