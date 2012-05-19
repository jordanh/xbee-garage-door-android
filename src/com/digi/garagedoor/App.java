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
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.digi.garagedoor.idigi.iDigiConnection;

//Extends Application to save application's context when its process
//is created

public class App extends Application {

    private static Context appContext;
    public static boolean isDeveloperBuild = false;
    public static boolean debugLogsEnabled = false;

    public static final String IDIGI_SERVER = "developer.idigi.com";

    public static final String PREFERENCES_USERNAME_KEY = "username";
    public static final String PREFERENCES_PASSWORD_KEY = "password";
    public static final String PREFERENCES_REMEMBER_KEY = "remember";
    public static final String PREFERENCES_DEVICE_KEY = "device";
    public static final String PREFERENCES_XBEE_KEY = "xbee";

    public static final int GET_DEVICE = 1;
    public static final int GET_XBEE = 2;
    public static final int SHOW_GARAGE_DOOR = 3;
    public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
    public static final int RESULT_LOGOUT = Activity.RESULT_FIRST_USER + 1;

    public static final int MINIMUM_FLING_VELOCITY = 800;

    public static iDigiConnection idigi;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        try {
            if ((getPackageManager().getApplicationInfo(getPackageName(), 0).flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                isDeveloperBuild = true;
                debugLogsEnabled = true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @return
     */
    public static Context getContext() {
        return appContext;
    }

}
