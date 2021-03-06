/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.toggles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.Phone;
import com.android.systemui.R;

public class LteToggle extends Toggle {

    private int mNetworkMode = -1;

    public LteToggle(Context c) {
        super(c);

        SettingsObserver obs = new SettingsObserver(new Handler());
        obs.observe();
        setLabel(R.string.toggle_lte);
        updateState();

    }

    @Override
    protected void onCheckChanged(boolean isChecked) {
        TelephonyManager tm = (TelephonyManager) mView.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        tm.toggleLTE(isChecked);
        updateState();
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.Secure
                    .getUriFor(Settings.Secure.PREFERRED_NETWORK_MODE), false,
                    this);
            updateState();
        }

        @Override
        public void onChange(boolean selfChange) {
            mNetworkMode = Settings.Secure.getInt(
                    mContext.getContentResolver(),
                    Settings.Secure.PREFERRED_NETWORK_MODE,
                    Phone.PREFERRED_NT_MODE);

            updateState();
        }
    }

    private static int getCurrentPreferredNetworkMode(Context context) {
        int network = -1;
        try {
            network = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.PREFERRED_NETWORK_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return network;
    }

    @Override
    protected boolean updateInternalToggleState() {
        mNetworkMode = getCurrentPreferredNetworkMode(mContext);
        if (mToggle != null)
            switch(mNetworkMode) {
                case Phone.NT_MODE_LTE_CDMA_EVDO:
                case Phone.NT_MODE_GLOBAL:
                    mToggle.setChecked(true);
                    break;
                default:
                    mToggle.setChecked(false);
                    break;
            }
        if (mToggle.isChecked()) {
            setIcon(R.drawable.toggle_lte);
        } else {
            setIcon(R.drawable.toggle_lte_off);
        }
        return mToggle.isChecked();
    }

    @Override
    protected boolean onLongPress() {
        Intent intent = new Intent(
                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return true;
    }
}
