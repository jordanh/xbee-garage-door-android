/*
 * Copyright (c) 1996-2012 Digi International Inc.,
 * All rights not expressly granted are reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */
package com.digi.garagedoor;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity to display information regarding About page of iDigi
 */
public class AboutActivity extends Activity {
    TextView versionInfo;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        versionInfo = (TextView) findViewById(R.id.VersionInfo);
    }

    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
        String version = null;
        ;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version != null) {
            versionInfo.setText("Version " + version);
        }
    }

    /**
     * onPause
     */
    @Override
    protected void onPause() {
        super.onPause();
    }
}
