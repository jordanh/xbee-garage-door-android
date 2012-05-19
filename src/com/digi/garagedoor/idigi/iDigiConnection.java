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

/**
 * iDigiConnection.java
 *
 * This is the web backend for the Smartlee android or web branches.
 *
 * This interface encapsulates a connection to the ( or an) iDigi server.
 */

package com.digi.garagedoor.idigi;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.digi.garagedoor.idigi.xml_classes.AtResponse;
import com.digi.garagedoor.idigi.xml_classes.DeviceCore;
import com.digi.garagedoor.idigi.xml_classes.DeviceCoreList;
import com.digi.garagedoor.idigi.xml_classes.DiaChannelDataFull;
import com.digi.garagedoor.idigi.xml_classes.DiaChannelDataFullList;
import com.digi.garagedoor.idigi.xml_classes.DoCommand;
import com.digi.garagedoor.idigi.xml_classes.IDigiDevice;
import com.digi.garagedoor.idigi.xml_classes.RciReply;
import com.digi.garagedoor.idigi.xml_classes.SciReply;
import com.digi.garagedoor.idigi.xml_classes.SendMessage;
import com.digi.garagedoor.idigi.xml_classes.XBeeCore;
import com.digi.garagedoor.idigi.xml_classes.XBeeCoreList;

/**
 * The Interface iDigi implementation for communication with a web service.
 * 
 * @param <X>
 */
public class iDigiConnection {

    private final String server;

    /* our trusty xml tool */
    private static Serializer serializer = new Persister();

    /* private internal Http connection */
    private final Http http;

    public iDigiConnection(String server) {
        this.server = server;
        http = new Http(true);
    }

    /**
     * Login
     * 
     * @param username
     *            the username
     * @param password
     *            the password
     * 
     * @return User object if login was successful
     * 
     */
    public Response<ArrayList<String>> login(String username, String password) {
        http.setHttpCredentials(username, password);
        /* authentication check */
        Response<ArrayList<String>> resp = this.getDeviceList();
        return resp;
    }

    /**
     * Forget credentials.
     * 
     * This function is idempotent.
     * 
     * After this call, nothing but login() will work.
     */

    public void logout() {
        http.clearHttpCredentials();
    }

    public Response<ArrayList<String>> getDeviceList() {
        Response<HttpResponse> resp = http.doGet("https://" + server + "/ws/DeviceCore");
        DeviceCoreList gwl;
        if (resp.wasSuccess()) {
            try {
                gwl = fromRequest(DeviceCoreList.class, resp.getMessage());
            } catch (GarageDoorException e) {
                return new Response<ArrayList<String>>(Response.IDIGI_RESPONSE, e);
            }

            ArrayList<String> devices = new ArrayList<String>();
            for (DeviceCore gateway : gwl.getList()) {
                devices.add(gateway.devConnectwareId);
            }
            return new Response<ArrayList<String>>(Response.SUCCESS, devices);

        } else {
            /* propagate the error */
            return new Response<ArrayList<String>>(resp);
        }
    }

    public Response<ArrayList<String>> getXBeeList(String device) {

        Response<HttpResponse> resp;

        /* Force a Node Discovery... We won't parse the results... */

        String str = "<sci_request version=\"1.0\"><send_message><targets><device id=\"" + device
                + "\"/></targets><rci_request version=\"1.1\">"
                + "<do_command target=\"zigbee\"><discover option=\"clear\"/></do_command>"
                + "</rci_request></send_message></sci_request>";
        resp = http.doPostWithHttpResponse("https://" + server + "/ws/sci", str);
        resp = http.doGet("https://" + server + "/ws/XbeeCore?condition=devConnectwareId='"
                + device + "'");
        XBeeCoreList xbl;
        if (resp.wasSuccess()) {
            try {
                xbl = fromRequest(XBeeCoreList.class, resp.getMessage());
            } catch (GarageDoorException e) {
                return new Response<ArrayList<String>>(Response.IDIGI_RESPONSE, e);
            }

            ArrayList<String> devices = new ArrayList<String>();
            for (XBeeCore xbee : xbl.getList()) {
                devices.add(xbee.xpExtAddr);
            }
            return new Response<ArrayList<String>>(Response.SUCCESS, devices);
        } else {
            /* propagate the error */
            return new Response<ArrayList<String>>(resp);
        }
    }

    public Response<ArrayList<Dio>> getDiaDioChannels(String device, String xbee,
            ArrayList<Dio> channels) {

        Response<HttpResponse> resp;
        String safeUrl;

        String url = "https://" + server + "/ws/DiaChannelDataFull?condition=";

        String xbeeStr = "XBee_";

        // Strip everything alpha numerics
        String xbeeTmp = xbee.replaceAll("[^a-zA-Z0-9]", "");

        // Get the data after 8 characters until the end, and append it to our
        // string.
        xbeeStr += xbeeTmp.substring(8);

        try {
            safeUrl = URLEncoder.encode("devConnectwareId='" + device + "'"
                    + " and ddInstanceName='" + xbeeStr + "'", "UTF-8");
        } catch (Exception e) {
            safeUrl = "";
        }
        url += safeUrl;

        resp = http.doGet(url);

        DiaChannelDataFullList dl;
        if (resp.wasSuccess()) {
            try {
                dl = fromRequest(DiaChannelDataFullList.class, resp.getMessage());
            } catch (GarageDoorException e) {
                return new Response<ArrayList<Dio>>(Response.IDIGI_RESPONSE, e);
            }

            ArrayList<Dio> chans = new ArrayList<Dio>();
            for (DiaChannelDataFull channel : dl.getList()) {
                // diaId = channel.getList().get(0);

                // Check to see if the user wanted this channel returned...
                for (Dio wantedChan : channels) {
                    // User wanted the channel, add it into our return array
                    if (wantedChan.getName().equals(channel.id.dcChannelName)) {
                        if (channel.dcdStringValue.equals("1")) {
                            Dio dio = new Dio(wantedChan.getName(), true);
                            chans.add(dio);
                        } else {
                            Dio dio = new Dio(wantedChan.getName(), false);
                            chans.add(dio);
                        }
                    }
                }
            }

            return new Response<ArrayList<Dio>>(Response.SUCCESS, chans);
        } else {
            /* propagate the error */
            return new Response<ArrayList<Dio>>(resp);
        }
    }

    public Response<ArrayList<AtResponse>> initializeXBeeState(String device, String xbee) {
        /*
         * Verify that the xbee string has the exclamation point at the end of
         * it. If not, add it.
         */
        if (!(xbee.endsWith("!"))) {
            xbee = xbee + "!";
        }
        Response<HttpResponse> resp = http.doPostWithHttpResponse("https://" + server + "/ws/sci",
                "<sci_request version=\"1.0\"><send_message><targets><device id=\"" + device
                        + "\"/></targets><rci_request version=\"1.1\">"
                        + "<do_command target=\"xig\">" + "<at hw_address=\"" + xbee
                        + "\" command=\"D0\" value=\"1\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"D1\" value=\"4\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"D2\" value=\"4\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"D3\" value=\"3\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"D4\" value=\"3\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"IR\" value=\"0\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"IC\" value=\"0x000C\" />" + "<at hw_address=\"" + xbee
                        + "\" command=\"WR\" apply=\"True\" />"
                        + "</do_command></rci_request></send_message></sci_request>");

        SciReply sciReply;
        if (resp.wasSuccess()) {
            try {
                sciReply = fromRequest(SciReply.class, resp.getMessage());
            } catch (GarageDoorException e) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, e);
            }

            /*
             * Cascade down through the classes until we get to the AtResponse
             * class.
             */
            ArrayList<SendMessage> messages = sciReply.getList();
            if (messages.isEmpty() || messages.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<IDigiDevice> idigiDevices = messages.get(0).getList();
            if (idigiDevices.isEmpty() || idigiDevices.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<RciReply> rciReplies = idigiDevices.get(0).getList();
            if (rciReplies.isEmpty() || rciReplies.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<DoCommand> doCommands = rciReplies.get(0).getList();
            if (doCommands.isEmpty() || doCommands.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }

            return new Response<ArrayList<AtResponse>>(Response.SUCCESS, doCommands.get(0)
                    .getList());

        } else {
            /* propagate the error */
            return new Response<ArrayList<AtResponse>>(resp);
        }
    }

    public Response<ArrayList<AtResponse>> getXigCurrentState(String device, String xbee) {
        Response<HttpResponse> resp = http
                .doPostWithHttpResponse(
                        "https://" + server + "/ws/sci",
                        "<sci_request version=\"1.0\"><send_message><targets><device id=\""
                                + device
                                + "\"/></targets><rci_request version=\"1.1\"><do_command target=\"xig\"><at hw_address=\""
                                + xbee
                                + "\" command=\"IS\" /></do_command></rci_request></send_message></sci_request>");

        SciReply sciReply;
        if (resp.wasSuccess()) {
            try {
                sciReply = fromRequest(SciReply.class, resp.getMessage());
            } catch (GarageDoorException e) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, e);
            }

            /*
             * Cascade down through the classes until we get to the AtResponse
             * class.
             */
            ArrayList<SendMessage> messages = sciReply.getList();
            if (messages.isEmpty() || messages.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<IDigiDevice> idigiDevices = messages.get(0).getList();
            if (idigiDevices.isEmpty() || idigiDevices.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<RciReply> rciReplies = idigiDevices.get(0).getList();
            if (rciReplies.isEmpty() || rciReplies.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<DoCommand> doCommands = rciReplies.get(0).getList();
            if (doCommands.isEmpty() || doCommands.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }

            return new Response<ArrayList<AtResponse>>(Response.SUCCESS, doCommands.get(0)
                    .getList());

        } else {
            /* propagate the error */
            return new Response<ArrayList<AtResponse>>(resp);
        }
    }

    public Response<ArrayList<AtResponse>> XigSetStateDio(String device, String xbee,
            ArrayList<Dio> channels) {

        /*
         * Verify that the xbee string has the exclamation point at the end of
         * it. If not, add it.
         */
        if (!(xbee.endsWith("!"))) {
            xbee = xbee + "!";
        }

        String command = "<sci_request version=\"1.0\"><send_message><targets><device id=\""
                + device + "\"/></targets><rci_request version=\"1.1\"><do_command target=\"xig\">";

        for (Dio channel : channels) {
            String tmp = "";
            String value;
            if (channel.getValue() == true) {
                value = "5";
            } else {
                value = "4";
            }

            if (channel.getName().equals("DIO1")) {
                tmp = "<at hw_address=\"" + xbee + "\" command=\"D1\" value=\"" + value
                        + "\" apply=\"True\" />";
            } else if (channel.getName().equals("DIO2")) {
                tmp = "<at hw_address=\"" + xbee + "\" command=\"D2\" value=\"" + value
                        + "\" apply=\"True\" />";
            } else if (channel.getName().equals("DIO3")) {
                tmp = "<at hw_address=\"" + xbee + "\" command=\"D3\" value=\"" + value
                        + "\" apply=\"True\" />";
            } else if (channel.getName().equals("DIO4")) {
                tmp = "<at hw_address=\"" + xbee + "\" command=\"D4\" value=\"" + value
                        + "\" apply=\"True\" />";
            }
            command += tmp;
        }

        command += "</do_command></rci_request></send_message></sci_request>";

        Response<HttpResponse> resp = http.doPostWithHttpResponse("https://" + server + "/ws/sci",
                command);

        SciReply sciReply;
        if (resp.wasSuccess()) {
            try {
                sciReply = fromRequest(SciReply.class, resp.getMessage());
            } catch (GarageDoorException e) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, e);
            }

            /*
             * Cascade down through the classes until we get to the AtResponse
             * class.
             */
            ArrayList<SendMessage> messages = sciReply.getList();
            if (messages.isEmpty() || messages.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<IDigiDevice> idigiDevices = messages.get(0).getList();
            if (idigiDevices.isEmpty() || idigiDevices.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<RciReply> rciReplies = idigiDevices.get(0).getList();
            if (rciReplies.isEmpty() || rciReplies.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }
            ArrayList<DoCommand> doCommands = rciReplies.get(0).getList();
            if (doCommands.isEmpty() || doCommands.size() != 1) {
                return new Response<ArrayList<AtResponse>>(Response.IDIGI_RESPONSE, 1);
            }

            return new Response<ArrayList<AtResponse>>(Response.SUCCESS, doCommands.get(0)
                    .getList());

        } else {
            /* propagate the error */
            return new Response<ArrayList<AtResponse>>(resp);
        }
    }

    private static String respToString(HttpResponse resp) {
        String msg = null;

        try {
            msg = Util.readInputStreamAsString(resp.getEntity().getContent());
        } catch (IllegalStateException e) {
            throw new GarageDoorException(e, GarageDoorException.UNKNOWN);
        } catch (IOException e) {
            throw new GarageDoorException(e, GarageDoorException.UNKNOWN);
        }

        return msg;
    }

    /**
     * annotations must be defined
     * 
     * @param <X>
     *            the type of object to return ( which is passed via classType
     *            parameter)
     */
    private static <X> X fromRequest(Class<X> classType, HttpResponse resp)
            throws GarageDoorException {
        return fromXML(classType, respToString(resp));
    }

    private static <X> X fromXML(Class<X> classType, String str) throws GarageDoorException {
        X ret = null;
        try {
            ret = serializer.read(classType, str);
        } catch (Exception e1) {
            GarageDoorException err = null;
            try {
                /* try to parse the error message */
                com.digi.garagedoor.idigi.Error error = serializer.read(
                        com.digi.garagedoor.idigi.Error.class, str);
                err = new GarageDoorException(error.error_message, error.error_no);
            } catch (Exception e2) {
                throw new GarageDoorException(e2, GarageDoorException.UNKNOWN);
            }
            if (err != null) {
                throw err;
            }
        }

        return ret;
    }
}
