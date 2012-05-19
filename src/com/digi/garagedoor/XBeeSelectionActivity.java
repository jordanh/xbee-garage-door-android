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

public class XBeeSelectionActivity extends ListActivity implements OnClickListener {
    private ArrayAdapter<String> listAdapter;
    private String device;
    private ArrayList<String> xbees;

    private Button refreshButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xbees);

        /*
         * Retrieve any extra data passed to us through the extra Bundle.
         */
        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("device") == true) {
                device = getIntent().getExtras().getString("device");
            } else {
                device = new String();
            }
        }

        xbees = new ArrayList<String>();
        notifyListViewOfChanges();

        /*
         * Set up our refresh button, and attach a listener to it.
         */
        refreshButton = (Button) findViewById(R.id.Button_XBee_Refresh);
        refreshButton.setOnClickListener(this);

        new iDigiGetXBees(this, false).execute();
    }

    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        if (v.equals(refreshButton)) {
            /*
             * On user pressing the refresh button, start up a thread to ask
             * iDigi what XBee devices are associated with the currently
             * selected device.
             */
            new iDigiGetXBees(this, true).execute();
        }
    }

    /**
     * onCreateOptionsMenu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.xbees_menu, menu);
        return true;
    }

    /**
     * onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.xbees_menu_refresh:
            new iDigiGetXBees(this, true).execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * onListItemClick
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        /*
         * Get the item from our XBee listed, based on the position returned.
         */
        String item = xbees.get(position);

        /*
         * Pack up device that was selected into a bundle, and then return it
         * back to the caller.
         */
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("xbee", item);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * notifyListViewOfChanges
     * 
     */
    private void notifyListViewOfChanges() {
        listAdapter = new ArrayAdapter<String>(this, R.drawable.listview_item, R.id.list_content,
                xbees);
        setListAdapter(listAdapter);
    }

    /**
     * iDigiGetDevices
     * 
     */
    private class iDigiGetXBees extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;
        private final Context context;
        private final boolean refresh;
        private ArrayList<String> xbeesTmp;

        public iDigiGetXBees(Context context, boolean refresh) {
            this.context = context;
            this.refresh = refresh;
            progressDialog = new ProgressDialog(this.context);
            xbeesTmp = new ArrayList<String>();
            xbeesTmp.clear();
        }

        @Override
        protected void onPreExecute() {
            if (refresh == true) {
                progressDialog.setMessage(context.getString(R.string.refreshing));
            } else {
                progressDialog.setMessage(context.getString(R.string.xbees));
            }

            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Response<ArrayList<String>> resp = App.idigi.getXBeeList(device);
            if (resp.wasSuccess()) {
                xbeesTmp = resp.getMessage();
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
                xbees = xbeesTmp;
                notifyListViewOfChanges();
            } else {
                // Pop up a banner/warning that the getXBeeList failed.
            }
        }
    }
}
