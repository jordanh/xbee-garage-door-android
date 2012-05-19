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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.digi.garagedoor.idigi.Response;

public class DeviceSelectionActivity extends ListActivity implements OnClickListener {
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> devices;
    private ArrayList<String> devicesShort;

    private Button refreshButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devices);

        /*
         * Retrieve any extra data passed to us through the extra Bundle.
         */
        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("devices") == true) {
                devices = getIntent().getExtras().getStringArrayList("devices");
            } else {
                devices = new ArrayList<String>();
            }
        }

        /*
         * Create a "shortened" list of Device names, where the first set of 18
         * unneeded characters ("00000000-00000000-") are snipped off.
         */
        devicesShort = new ArrayList<String>();
        devicesShort.clear();
        for (String device : devices) {
            String str = device.substring(18);
            devicesShort.add(str);
        }

        /*
         * Populate the list with "shortened" version of the Device names.
         */
        listAdapter = new ArrayAdapter<String>(this, R.drawable.listview_item, R.id.list_content,
                devicesShort);

        setListAdapter(listAdapter);

        /*
         * Set up our refresh button, and attach a listener to it.
         */
        refreshButton = (Button) findViewById(R.id.Button_Device_Refresh);
        refreshButton.setOnClickListener(this);
    }

    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        if (v.equals(refreshButton)) {
            /*
             * On user pressing the refresh button, start up a thread to ask
             * iDigi what Gateway/Devices are associated with our account.
             */
            new iDigiGetDevices(this).execute();
        }
    }

    /**
     * onListItemClick
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        /*
         * Since our device list and short device list are indexed the same, we
         * can take the position and grab the long string from the regular
         * device list.
         */
        String item = devices.get(position);

        /*
         * Pack up device that was selected into a bundle, and then return it
         * back to the caller.
         */
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("device", item);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * onCreateOptionsMenu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.devices_menu, menu);
        return true;
    }

    /**
     * onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.devices_menu_refresh:
            new iDigiGetDevices(this).execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * iDigiGetDevices
     * 
     */
    private class iDigiGetDevices extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;
        private final Context context;
        private ArrayList<String> devicesTmp;

        public iDigiGetDevices(Context context) {
            this.context = context;
            progressDialog = new ProgressDialog(this.context);
            devicesTmp = new ArrayList<String>();
            devicesTmp.clear();
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(context.getString(R.string.refreshing));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Response<ArrayList<String>> resp = App.idigi.getDeviceList();
            if (resp.wasSuccess()) {
                devicesTmp = resp.getMessage();
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
                devices = devicesTmp;
            } else {
                // Pop up a banner/warning that the GetDeviceList failed.
            }
        }
    }

}
