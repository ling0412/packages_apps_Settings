/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.settings.gestures;

import static com.android.settings.gestures.PanicGesturePreferenceController.OFF;
import static com.android.settings.gestures.PanicGesturePreferenceController.ON;

import static com.google.common.truth.Truth.assertThat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;

import com.android.settings.R;
import com.android.settings.testutils.shadow.SettingsShadowResources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = SettingsShadowResources.class)
public class PanicGesturePreferenceControllerTest {

    private static final String TEST_PKG_NAME = "test_pkg";
    private static final String TEST_CLASS_NAME = "name";
    private static final Intent SETTING_INTENT = new Intent(
            PanicGesturePreferenceController.ACTION_PANIC_SETTINGS).setPackage(TEST_PKG_NAME);

    private Context mContext;
    private ContentResolver mContentResolver;
    private ShadowPackageManager mPackageManager;
    private PanicGesturePreferenceController mController;
    private static final String PREF_KEY = "gesture_panic_button";

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mContentResolver = mContext.getContentResolver();
        mPackageManager = Shadows.shadowOf(mContext.getPackageManager());
        mController = new PanicGesturePreferenceController(mContext, PREF_KEY);
    }

    @After
    public void tearDown() {
        SettingsShadowResources.reset();
    }

    @Test
    public void constructor_hasCustomPackageConfig_shouldSetIntent() {
        final ResolveInfo info = new ResolveInfo();
        info.activityInfo = new ActivityInfo();
        info.activityInfo.packageName = TEST_PKG_NAME;
        info.activityInfo.name = TEST_CLASS_NAME;

        mPackageManager.addResolveInfoForIntent(SETTING_INTENT, info);

        SettingsShadowResources.overrideResource(
                R.bool.config_show_panic_gesture_settings,
                Boolean.TRUE);

        SettingsShadowResources.overrideResource(
                R.string.panic_gesture_settings_package,
                TEST_PKG_NAME);

        mController = new PanicGesturePreferenceController(mContext, PREF_KEY);

        assertThat(mController.mIntent).isNotNull();
    }

    @Test
    public void isAvailable_configIsTrue_shouldReturnTrue() {
        SettingsShadowResources.overrideResource(
                R.bool.config_show_panic_gesture_settings,
                Boolean.TRUE);

        assertThat(mController.isAvailable()).isTrue();
    }

    @Test
    public void isAvailable_configIsTrue_shouldReturnFalse() {
        SettingsShadowResources.overrideResource(
                R.bool.config_show_panic_gesture_settings,
                Boolean.FALSE);

        assertThat(mController.isAvailable()).isFalse();
    }

    @Test
    public void isChecked_configIsNotSet_shouldReturnTrue() {
        // Set the setting to be enabled.
        Settings.Secure.putInt(mContentResolver, Settings.Secure.PANIC_GESTURE_ENABLED, ON);
        mController = new PanicGesturePreferenceController(mContext, PREF_KEY);

        assertThat(mController.isChecked()).isTrue();
    }

    @Test
    public void isChecked_configIsSet_shouldReturnFalse() {
        // Set the setting to be disabled.
        Settings.Secure.putInt(mContentResolver, Settings.Secure.PANIC_GESTURE_ENABLED, OFF);
        mController = new PanicGesturePreferenceController(mContext, PREF_KEY);

        assertThat(mController.isChecked()).isFalse();
    }

    @Test
    public void isSliceableCorrectKey_returnsTrue() {
        final PanicGesturePreferenceController controller =
                new PanicGesturePreferenceController(mContext, PREF_KEY);
        assertThat(controller.isSliceable()).isTrue();
    }

    @Test
    public void isSliceableIncorrectKey_returnsFalse() {
        final DoubleTapPowerPreferenceController controller =
                new DoubleTapPowerPreferenceController(mContext, "bad_key");
        assertThat(controller.isSliceable()).isFalse();
    }

}
