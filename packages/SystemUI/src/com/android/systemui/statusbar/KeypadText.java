/*
 * Created by Nadlabak; Copyright (C) 2011 CyanogenMod Project
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

package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * This widget displays status of secondary keypad layout
 */
public class KeypadText extends TextView {
    private boolean mAttached;

    public KeypadText(Context context) {
        this(context, null);
    }

    public KeypadText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeypadText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateKeypadText();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("hw.keycharmap.change");
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateKeypadText();
        }
    };

    private void updateKeypadText() {
        boolean show = false;
        Configuration config = getResources().getConfiguration();
        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            String keypadPrefix = SystemProperties.get("ro.sys.keypad_prefix", "0");
            String keypadPrimary = SystemProperties.get("persist.sys.keypad_type", "0");
            String keypadSecondary = SystemProperties.get("persist.sys.keypad_type_sec", "none");
            String keypadCurrent = SystemProperties.get("hw.keyboards.0.devname", "0");
            if (!keypadSecondary.equals("none") &&
                    !keypadCurrent.equals(keypadPrefix + keypadPrimary)) {
                setText(keypadSecondary.substring(0,2).toUpperCase());
                show = true;
            }
        }
        if (show) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }
}
