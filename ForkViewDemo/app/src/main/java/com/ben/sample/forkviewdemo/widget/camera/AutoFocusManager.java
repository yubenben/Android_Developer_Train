/*
 * Copyright (C) 2012 ZXing authors
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

package com.crazy.gezi.ui.custom.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;

final class AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = "CustomCamera";

    private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(3);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
        FOCUS_MODES_CALLING_AF
                .add(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private boolean stopped;
    private boolean focusing;
    private final boolean useAutoFocus;
    private final Camera camera;
    private AsyncTask<?, ?, ?> outstandingTask;

    AutoFocusManager(Context context, Camera camera) {
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        Log.i(TAG, "Current focus mode '" + currentFocusMode
                + "'; use auto focus? " + useAutoFocus);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        // autoFocusAgainLater();
    }

    private void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                Log.w(TAG, "Could not request auto focus", ree);
            }
        }
    }

    synchronized void start() {
        if (useAutoFocus) {
            cancelOutstandingTask();
            if (!stopped && !focusing) {
                try {
                    camera.autoFocus(this);
                    focusing = true;
                } catch (RuntimeException re) {
                    // Have heard RuntimeException reported in Android 4.0.x+;
                    // continue?
                    Log.w(TAG, "Unexpected exception while focusing", re);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }

    private void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }

    synchronized void stop() {
        stopped = true;
        if (useAutoFocus) {
            cancelOutstandingTask();
            // Doesn't hurt to call this even if not focusing
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+;
                // continue?
                Log.w(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
    }

    private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
            } catch (InterruptedException e) {
                // continue
            }
            start();
            return null;
        }
    }

    private static String toString(Iterable<Camera.Area> areas) {
        if (areas == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Camera.Area area : areas) {
            result.append(area.rect).append(':').append(area.weight)
                    .append(' ');
        }
        return result.toString();
    }

    protected void focusOnTouch(MotionEvent event, int width, int height) {
        if (camera != null) {

            Log.d(TAG,
                    "event.getX=" + event.getRawX() + " evnet.getY="
                            + event.getRawY() + " width=" + width + " height="
                            + height);
            camera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(event.getRawX(), event.getRawY(),
                    width, height, 1f);
            Rect meteringRect = calculateTapArea(event.getRawX(),
                    event.getRawX(), width, height, 1.5f);

            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getMaxNumFocusAreas() > 0) {
                parameters.setFocusAreas(Collections
                        .singletonList(new Camera.Area(focusRect, 1000)));
                Log.i(TAG,
                        "touch focus areas: "
                                + toString(parameters.getFocusAreas()));
            }

            if (parameters.getMaxNumMeteringAreas() > 0 && false) {
                parameters.setMeteringAreas(Collections
                        .singletonList(new Camera.Area(meteringRect, 1000)));
                Log.i(TAG,
                        "touch metering areas: "
                                + toString(parameters.getMeteringAreas()));
            }
            camera.setParameters(parameters);
            start();
        }
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to
     * 1000:1000.
     */
    private Rect calculateTapArea(float x, float y, int width, int height,
            float coefficient) {
        float focusAreaSize = 50F;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        // Point resolution = configManager.getCameraResolution();
        // Log.v(TAG, "resolution x=" + resolution.x + " y=" + resolution.y);
        Log.v(TAG, "width=" + width + " height=" + height);
        Log.v(TAG, "getRaw x=" + x + " y=" + y);
        // int centerX = (int) (x * 2000 / resolution.x - 1000);
        // int centerY = (int) (y * 2000 / resolution.y - 1000);
        int centerX = (int) (x * 2000 / width - 1000);
        int centerY = (int) (y * 2000 / height - 1000);
        Log.d(TAG, "center x=" + centerX + " y=" + centerY);

        int left = clamp(centerX - areaSize / 2, -1000, 1000 - areaSize);
        int top = clamp(centerY - areaSize / 2, -1000, 1000 - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top),
                Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

}
