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
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.digi.garagedoor.idigi.Dio;
import com.digi.garagedoor.idigi.Response;
import com.digi.garagedoor.idigi.xml_classes.AtResponse;

public class GarageDoorActivity extends Activity implements OnClickListener {
    private String device = "";
    private String xbee = "";

    private ImageView doorImage;
    private AnimationDrawable doorAnimation = null;
    private GestureDetector flingDetector;
    private Button doorButton;
    private boolean garageDoorIsOpen = false;
    private boolean garageDoorUIIsOpen = false;

    private final Handler mHandler = new Handler();
    private final Runnable mRefreshLoop = new RefreshLoop();

    private static final boolean moveDoorUp = true;
    private static final boolean moveDoorDown = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.door);

        /*
         * Retrieve any extra data passed to us through the extra Bundle.
         */
        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("device") == true) {
                device = getIntent().getExtras().getString("device");
            } else {
                device = new String();
            }
            if (getIntent().getExtras().containsKey("xbee") == true) {
                xbee = getIntent().getExtras().getString("xbee");
            } else {
                xbee = new String();
            }
        }

        /*
         * Verify that we have received both the device and xbee values from our
         * caller.
         */
        if (device.isEmpty() || xbee.isEmpty()) {
            this.finish();
        }

        doorButton = (Button) findViewById(R.id.button_Door);
        doorButton.setOnClickListener(this);

        doorImage = (ImageView) findViewById(R.id.imageView_Door);
        doorImage.setBackgroundResource(R.drawable.door01);
        doorAnimation = null;

        flingDetector = new GestureDetector(new FlingDetector());

        /*
         * Push settings that need to be set on the XBee adapter.
         */
        initializeGarageDoor();

    }

    @Override
    public void onResume() {
        super.onResume();
        /*
         * Start our slow poller, which will continually check iDigi to find out
         * whether the door state is open or closed.
         */
        startIDigiSlowPoll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRefreshLoop);
        Log.d("Door", "Refresh service - on destroy");
    }

    /**
     * initializeGarageDoor
     */
    private void initializeGarageDoor() {
        /*
         * Force specific settings to be run against the XBee that is
         * controlling our garage door.
         */
        new initializeGarageDoorTask().execute();
    }

    /**
     * initializeGarageDoor
     */
    private void startIDigiSlowPoll() {
        new getGarageDoorDio().execute();
    }

    private void scheduleNextRefresh() {
        if (mHandler != null) {
            mHandler.postDelayed(mRefreshLoop, 30000);
        }
    }

    private class RefreshLoop implements Runnable {
        @Override
        public void run() {
            new getGarageDoorDio().execute();
        }
    }

    /**
     * drawGarageDoor()
     */
    private void drawGarageDoor(boolean direction) {
        Log.d("Door", "drawGarageDoor direction: " + direction);
        Log.d("Door", "drawGarageDoor UIisOpen: " + garageDoorUIIsOpen);

        if (garageDoorUIIsOpen == true) {
            if (direction == moveDoorDown) {
                if (doorAnimation != null) {
                    doorAnimation.stop();
                }
                doorImage.setBackgroundResource(R.anim.door_animation_open_to_closed);
                doorAnimation = (AnimationDrawable) doorImage.getBackground();
                doorAnimation.start();
                garageDoorUIIsOpen = false;
            }
        } else {
            if (direction == moveDoorUp) {
                if (doorAnimation != null) {
                    doorAnimation.stop();
                }
                doorImage.setBackgroundResource(R.anim.door_animation_closed_to_open);
                doorAnimation = (AnimationDrawable) doorImage.getBackground();
                doorAnimation.start();
                garageDoorUIIsOpen = true;
            }
        }
    }

    /**
     * moveGarageDoor
     */
    private void moveGarageDoor(boolean direction) {
        Log.d("Door", "Door isOpen: " + garageDoorIsOpen);
        if (garageDoorIsOpen == true) {
            drawGarageDoor(moveDoorDown);
            if (direction == moveDoorDown) {
                new toggleGarageDoor().execute();
                garageDoorIsOpen = false;
            }
        } else {
            drawGarageDoor(moveDoorUp);
            if (direction == moveDoorUp) {
                new toggleGarageDoor().execute();
                garageDoorIsOpen = true;
            }
        }
    }

    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        if (v.equals(doorButton)) {
            Log.d("Door", "Button PRESS!");
            moveGarageDoor(!garageDoorIsOpen);
        }
    }

    /**
     * onCreateOptionsMenu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.door_menu, menu);
        return true;
    }

    /**
     * onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.door_menu_logout:
            App.idigi.logout();
            intent = new Intent();
            setResult(App.RESULT_LOGOUT, intent);
            this.finish();
            return true;
        case R.id.door_menu_about:
            intent = new Intent(this, AboutActivity.class);
            this.startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return flingDetector.onTouchEvent(ev);
    }

    /**
     *
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (flingDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Class to detect vertical flings. We will open/close the garage door based
     * on the motion of the fling.
     */
    class FlingDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // TOP - BOTTOM
            // FLING DOWN
            if (velocityY > App.MINIMUM_FLING_VELOCITY && velocityX < App.MINIMUM_FLING_VELOCITY
                    && velocityX > -App.MINIMUM_FLING_VELOCITY) {
                moveGarageDoor(moveDoorDown);
                return true;
            }
            // BOTTOM - TOP
            // FLING UP
            if (velocityY < -App.MINIMUM_FLING_VELOCITY && velocityX < App.MINIMUM_FLING_VELOCITY
                    && velocityX > -App.MINIMUM_FLING_VELOCITY) {
                moveGarageDoor(moveDoorUp);
                return true;
            }
            return false;
        }
    }

    /**
     * toggleGarageDoor
     * 
     */
    private class toggleGarageDoor extends AsyncTask<Void, Void, Boolean> {

        public toggleGarageDoor() {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            /*
             * Allocate an array of DIO channels we are interested in setting.
             * In this case, the door is controlled by the first (DIO1) pin. To
             * correctly toggle to door, we have to issue a false, true, false
             * to DIO1.
             */
            ArrayList<Dio> setChannels = new ArrayList<Dio>();
            setChannels.clear();
            Dio setDio = new Dio("DIO1", false);
            setChannels.add(setDio);
            setDio = new Dio("DIO1", true);
            setChannels.add(setDio);
            setDio = new Dio("DIO1", false);
            setChannels.add(setDio);

            Response<ArrayList<AtResponse>> resp = App.idigi.XigSetStateDio(device, xbee,
                    setChannels);
            if (resp.wasSuccess()) {
                ArrayList<AtResponse> atResponses = resp.getMessage();
                for (AtResponse atResponse : atResponses) {
                    Log.d("Door", "AT Response: " + atResponse);
                }
            } else {
                Log.d("Door", "Bad XBee response");
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {

        }
    }

    /**
     * initializeGarageDoor
     * 
     */
    private class initializeGarageDoorTask extends AsyncTask<Void, Void, Boolean> {

        public initializeGarageDoorTask() {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            Response<ArrayList<AtResponse>> resp = App.idigi.initializeXBeeState(device, xbee);
            if (resp.wasSuccess()) {
                ArrayList<AtResponse> atResponses = resp.getMessage();
                for (AtResponse atResponse : atResponses) {
                    Log.d("Door", "AT Response: " + atResponse);
                }
            } else {
                Log.d("Door", "Bad XBee response");
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {

        }
    }

    /**
     * initializeGarageDoor
     * 
     */
    private class getGarageDoorDio extends AsyncTask<Void, Void, Boolean> {
        ArrayList<Dio> returned_channels;

        public getGarageDoorDio() {
            returned_channels = new ArrayList<Dio>();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            /*
             * Allocate an array of DIO channels we are interested in. In this
             * case, the current state of the door is on DIO3.
             */
            ArrayList<Dio> channels = new ArrayList<Dio>();
            Dio dio = new Dio("DIO3");
            channels.add(dio);

            // Get the channels from iDigi's Dia storage.
            Response<ArrayList<Dio>> resp = App.idigi.getDiaDioChannels(device, xbee, channels);
            if (resp.wasSuccess()) {
                returned_channels = resp.getMessage();
            } else {
                returned_channels.clear();
                Log.d("Door", "Bad DiaChannels");
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            for (Dio tmp : returned_channels) {
                if (tmp.getName().equals("DIO3")) {
                    /* If DIO3 comes back as true, the door is open */
                    if (tmp.getValue() == true) {
                        garageDoorIsOpen = true;
                        drawGarageDoor(moveDoorUp);
                    } else {
                        garageDoorIsOpen = false;
                        drawGarageDoor(moveDoorDown);
                    }
                }
            }

            scheduleNextRefresh();
        }
    }

}
