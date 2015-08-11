/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A @{code ImageView} which change the opacity of the icon if disabled.
 */
public class TwoStateImageView extends ImageView {
    private final float DISABLED_ALPHA = 0.4f;
    private boolean mFilterEnabled = true;

    public TwoStateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwoStateImageView(Context context) {
        this(context, null);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mFilterEnabled) {
            if (enabled) {
                setAlpha(1.0f);
            } else {
                setAlpha(DISABLED_ALPHA);
            }
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (mFilterEnabled) {
            if (pressed) {
                setAlpha(DISABLED_ALPHA);
            } else {
                setAlpha(1.0f);
            }
        }
    }

    public void enableFilter(boolean enabled) {
        mFilterEnabled = enabled;
    }
}
