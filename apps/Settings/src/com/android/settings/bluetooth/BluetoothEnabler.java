/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.WirelessSettings;
import com.mediatek.xlog.Xlog;
import com.mediatek.settings.PDebug;

/**
 * BluetoothEnabler is a helper to manage the Bluetooth on/off checkbox
 * preference. It turns on/off Bluetooth and ensures the summary of the
 * preference reflects the current state.
 */
public final class BluetoothEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "BluetoothEnabler";
    private final Context mContext;
    private Switch mSwitch;
    private boolean mValidListener;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;
    ///M: indicate whether need to enable/disable BT or just update the preference
    private boolean mUpdateStatusOnly = false;
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Broadcast receiver is always running on the UI thread here,
            // so we don't need consider thread synchronization.
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            Xlog.d(TAG, "BluetoothAdapter state changed to" + state);
            handleStateChanged(state);
        }
    };

    public BluetoothEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mValidListener = false;

        LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context);
        if (manager == null) {
            // Bluetooth is not supported
            mLocalAdapter = null;
            mSwitch.setEnabled(false);
        } else {
            mLocalAdapter = manager.getBluetoothAdapter();
        }
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public void resume() {
        PDebug.Start("BluetoothEnabler.resume");
        if (mLocalAdapter == null) {
            mSwitch.setEnabled(false);
            return;
        }

        // Bluetooth state is not sticky, so set it manually
        handleStateChanged(mLocalAdapter.getBluetoothState());

        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);
        mValidListener = true;
        PDebug.End("BluetoothEnabler.resume");
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }

        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnCheckedChangeListener(null);
        mValidListener = false;
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(mValidListener ? this : null);

        int bluetoothState = BluetoothAdapter.STATE_OFF;
        if (mLocalAdapter != null) bluetoothState = mLocalAdapter.getBluetoothState();
        boolean isOn = bluetoothState == BluetoothAdapter.STATE_ON;
        boolean isOff = bluetoothState == BluetoothAdapter.STATE_OFF;
        setChecked(isOn);
        mSwitch.setEnabled(isOn || isOff);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Show toast message if Bluetooth is not allowed in airplane mode
        Xlog.d(TAG, "onCheckChanged to " + isChecked);       
        if (isChecked &&
                !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_BLUETOOTH)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off
            buttonView.setChecked(false);
        }

          Xlog.d(TAG, "mUpdateStatusOnly is " + mUpdateStatusOnly);
        /// M: if receive bt status changed broadcast, do not need enable/disable bt.
        if (mLocalAdapter != null && !mUpdateStatusOnly) {
            mLocalAdapter.setBluetoothEnabled(isChecked);
        }
        mSwitch.setEnabled(false);
    }

    void handleStateChanged(int state) {
        PDebug.Start("BluetoothEnabler.handleStateChanged");
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                /// M: receive bt status changed broadcast, set mUpdateStatusOnly true @{
                mUpdateStatusOnly = true;
                Xlog.d(TAG, "Begin update status: set mUpdateStatusOnly to true");
                /// @}
                setChecked(true);
                mSwitch.setEnabled(true);               
                /// M: after set the switch checked status, set mUpdateStatusOnly false @{
                mUpdateStatusOnly = false;
                Xlog.d(TAG, "End update status: set mUpdateStatusOnly to false");
                /// @}
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
                  /// M: receive bt status changed broadcast, set mUpdateStatusOnly true @{
                  mUpdateStatusOnly = true;
                  Xlog.d(TAG, "Begin update status: set mUpdateStatusOnly to true");
                  /// @}
                setChecked(false);
                mSwitch.setEnabled(true);               
                /// M: after set the switch checked status, set mUpdateStatusOnly false @{
                mUpdateStatusOnly = false;
                Xlog.d(TAG, "End update status: set mUpdateStatusOnly to false");
                /// @}
                break;
            default:
                setChecked(false);
                mSwitch.setEnabled(true);
        }
        PDebug.End("BluetoothEnabler.handleStateChanged");
    }

    private void setChecked(boolean isChecked) {
        if (isChecked != mSwitch.isChecked()) {
            // set listener to null, so onCheckedChanged won't be called
            // if the checked status on Switch isn't changed by user click
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(null);
            }
            mSwitch.setChecked(isChecked);
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(this);
            }
        }
    }
}
