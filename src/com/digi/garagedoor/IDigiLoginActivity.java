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

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.digi.garagedoor.idigi.Response;
import com.digi.garagedoor.idigi.iDigiConnection;

public class IDigiLoginActivity extends Activity implements OnClickListener {
    SharedPreferences mPrefs;

    private AsyncTask<Void, Void, Boolean> loginTask = null;

    private String username;
    private String password;
    private boolean rememberLogin;
    private String device;
    private String xbee;

    private ArrayList<String> devices;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private CheckBox rememberLoginCheckBox;
    private Button loginButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        /*
         * Populate user preferences/settings.
         */
        mPrefs = getPreferences(MODE_PRIVATE);
        username = mPrefs.getString(App.PREFERENCES_USERNAME_KEY, "");
        password = mPrefs.getString(App.PREFERENCES_PASSWORD_KEY, "");
        rememberLogin = mPrefs.getBoolean(App.PREFERENCES_REMEMBER_KEY, false);
        device = mPrefs.getString(App.PREFERENCES_DEVICE_KEY, "");
        xbee = mPrefs.getString(App.PREFERENCES_XBEE_KEY, "");

        /*
         * Populate our various dialog/edit/text boxes.
         */
        usernameEditText = (EditText) findViewById(R.id.EditText_Username);
        usernameEditText.setText(username);
        passwordEditText = (EditText) findViewById(R.id.EditText_Password);
        passwordEditText.setText(password);
        rememberLoginCheckBox = (CheckBox) findViewById(R.id.CheckBox_RememberLogin);
        rememberLoginCheckBox.setChecked(rememberLogin);
        loginButton = (Button) findViewById(R.id.Button_Login);
        loginButton.setOnClickListener(this);

        /*
         * Allocate our iDigi Connection
         */
        App.idigi = new iDigiConnection(App.IDIGI_SERVER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
         * Before leaving, commit all our Settings/Preferences.
         */
        commitAllPreferences();
    }

    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        if (v.equals(loginButton)) {

            /*
             * Get username and password from the Edit boxes.
             */
            username = usernameEditText.getText().toString();
            password = passwordEditText.getText().toString();
            rememberLogin = rememberLoginCheckBox.isChecked();

            /*
             * And ensure they are reasonably valid. If not, create a quick
             * toast warning them, and then return.
             */
            if (username.equals("")) {
                Toast.makeText(this, R.string.invalid_username, Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.equals("")) {
                Toast.makeText(this, R.string.invalid_password, Toast.LENGTH_SHORT).show();
                return;
            }

            commitAllPreferences();

            /*
             * Start the Login task. But make sure another login task is not
             * running...
             */
            if (loginTask == null || loginTask.getStatus() == AsyncTask.Status.FINISHED
                    || loginTask.isCancelled()) {
                loginTask = new iDigiLogin(this, username, password).execute();
            }
        }
    }

    /**
     * Start the Device Selection Activity
     */
    private void commitAllPreferences() {
        /*
         * Go ahead and store the preferences/settings now.
         */
        SharedPreferences.Editor editor = mPrefs.edit();
        if (rememberLogin == true) {
            editor.putString(App.PREFERENCES_USERNAME_KEY, username);
            editor.putString(App.PREFERENCES_PASSWORD_KEY, password);
            editor.putString(App.PREFERENCES_DEVICE_KEY, device);
            editor.putString(App.PREFERENCES_XBEE_KEY, xbee);
        } else {
            editor.putString(App.PREFERENCES_USERNAME_KEY, "");
            editor.putString(App.PREFERENCES_PASSWORD_KEY, "");
            editor.putString(App.PREFERENCES_DEVICE_KEY, "");
            editor.putString(App.PREFERENCES_XBEE_KEY, "");
        }
        editor.putBoolean(App.PREFERENCES_REMEMBER_KEY, rememberLogin);
        editor.commit();
    }

    /**
     * Start the Device Selection Activity
     */
    private void startDeviceSelectionActivity(ArrayList<String> devices) {
        this.devices = devices;
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("devices", devices);
        Intent intent = new Intent(this, DeviceSelectionActivity.class);
        intent.putExtras(bundle);
        this.startActivityForResult(intent, App.GET_DEVICE);
    }

    /**
     * Start the Xbee Selection Activity
     */
    private void startXBeeSelectionActivity(String device) {
        Bundle bundle = new Bundle();
        bundle.putString("device", device);
        Intent intent = new Intent(this, XBeeSelectionActivity.class);
        intent.putExtras(bundle);
        this.startActivityForResult(intent, App.GET_XBEE);
    }

    /**
     * Start the Garage Door Activity
     */
    private void startGarageDoorActivity(String device, String xbee) {
        Bundle bundle = new Bundle();
        bundle.putString("device", device);
        bundle.putString("xbee", xbee);
        Intent intent = new Intent(this, GarageDoorActivity.class);
        intent.putExtras(bundle);
        this.startActivityForResult(intent, App.SHOW_GARAGE_DOOR);
    }

    /**
     * Capture the return of the called Activity, and act upon it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == App.GET_DEVICE) {
            switch (resultCode) {
            case RESULT_OK:
                /*
                 * Retrieve the device that the Activity picked for us.
                 */
                if (intent != null && intent.getExtras() != null) {
                    if (intent.getExtras().containsKey("device") == true) {
                        device = intent.getExtras().getString("device");
                    } else {
                        device = new String();
                    }
                }

                /*
                 * If rememberLogin is set, store the device information.
                 */
                if (rememberLogin == true) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(App.PREFERENCES_DEVICE_KEY, device);
                    editor.commit();
                }
                break;
            case App.RESULT_ERROR:
                device = new String();
                break;
            case RESULT_CANCELED:
                device = new String();
                break;
            default:
                device = new String();
                break;
            }

            /*
             * If we now have the device value, call the XBee page/picker.
             */
            if (!device.isEmpty()) {
                startXBeeSelectionActivity(device);
            }
        } else if (requestCode == App.GET_XBEE) {
            switch (resultCode) {
            case RESULT_OK:
                /*
                 * Retrieve the xbee that the Activity picked for us.
                 */
                if (intent != null && intent.getExtras() != null) {
                    if (intent.getExtras().containsKey("xbee") == true) {
                        xbee = intent.getExtras().getString("xbee");
                    } else {
                        xbee = new String();
                    }
                }
                /*
                 * If rememberLogin is set, store the xbee information.
                 */
                if (rememberLogin == true) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(App.PREFERENCES_XBEE_KEY, xbee);
                    editor.commit();
                }
                break;
            case App.RESULT_ERROR:
                xbee = new String();
                break;
            case RESULT_CANCELED:
                xbee = new String();
                startDeviceSelectionActivity(devices);
                break;
            default:
                xbee = new String();
                break;
            }

            /*
             * If we now have the xbee value, we can now call our main page.
             */
            if (!xbee.isEmpty()) {
                startGarageDoorActivity(device, xbee);
            }
        } else if (requestCode == App.SHOW_GARAGE_DOOR) {
            switch (resultCode) {
            case App.RESULT_LOGOUT:
                if (rememberLogin != true) {
                    username = "";
                    usernameEditText.setText(username);
                    password = "";
                    passwordEditText.setText(password);
                    device = "";
                    xbee = "";
                }
                break;
            case RESULT_OK:
            case App.RESULT_ERROR:
            case RESULT_CANCELED:
            default:
                break;
            }
        }
    }

    /**
     * iDigiLogin
     * 
     */
    private class iDigiLogin extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final String username;
        private final String password;
        ProgressDialog progressDialog;
        private ArrayList<String> devices;

        public iDigiLogin(Context context, String username, String password) {
            this.context = context;
            this.username = username;
            this.password = password;
            progressDialog = new ProgressDialog(this.context);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(context.getString(R.string.logging_in));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Response<ArrayList<String>> resp = App.idigi.login(username, password);
            if (resp.wasSuccess()) {
                devices = resp.getMessage();
            } else {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                // attempt to stop crashes on screen rotate
            }

            if (success == true) {
                /*
                 * Determine which Activity we should call next...
                 */
                if (device.isEmpty()) {
                    startDeviceSelectionActivity(devices);
                } else if (xbee.isEmpty()) {
                    startXBeeSelectionActivity(device);
                } else {
                    startGarageDoorActivity(device, xbee);
                }
            } else {
                Toast.makeText(context, R.string.invalid_username_password, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

}
