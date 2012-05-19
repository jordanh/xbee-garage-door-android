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
package com.digi.garagedoor.idigi.xml_classes;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "DeviceCore", strict = false)
public class DeviceCore implements Serializable {
    private static final long serialVersionUID = 7526472295622776102L;

    /* Date this record was created */
    @Element
    public String devRecordStartDate;

    /* MAC address of the device */
    @Element(required = false)
    public String devMac;

    /* Modem ID of the device */
    @Element(required = false)
    public String devCellularModemId;

    /* Device ID of the device */
    @Element
    public String devConnectwareId;

    /* The automatically generated ID assigned to a customer */
    @Element
    public int cstId;

    /* The automatically generated ID assigned to a customer’s group */
    @Element
    public int grpId;

    /* The date the device was provisioned in iDigi */
    @Element
    public String devEffectiveStartDate;

    /* "false" if device is currently in the customer’s account */
    @Element
    public String devTerminated;

    /* ID of the vendor that manufactured the device */
    @Element(required = false)
    public String dvVendorId;

    /* Manufacturer’s device type such as ConnectPort X2 */
    @Element
    public String dpDeviceType;

    /* Firmware as an integer such as 34209795 */
    @Element(required = false)
    public int dpFirmwareLevel;

    /* Firmware level as a string such as 2.10.0.3 */
    @Element(required = false)
    public String dpFirmwareLevelDesc;

    /*
     * Restricts the device from connecting to iDigi 0 is OK, 1 is not allowed
     * to connect
     */
    @Element
    public String dpRestrictedStatus;

    /* IP address reported by the device such as 10.8.16.79 */
    @Element(required = false)
    public String dpLastKnownIp;

    /* IP address from which the device connected */
    @Element(required = false)
    public String dpGlobalIp;

    /* Connection Status - 1 for connected, 0 for disconnected */
    @Element
    public int dpConnectionStatus;

    /*
     * the date that the device last connected to iDigi such as
     * 2010-07-21T15:20:00Z
     */
    @Element(required = false)
    public String dpLastConnectTime;

    /* Contact setting from the device */
    @Element(required = false)
    public String dpContact;

    /* Description setting from the device */
    @Element(required = false)
    public String dpDescription;

    /* Location setting from the device */
    @Element(required = false)
    public String dpLocation;

    /* This is a user definable free format text field */
    @Element(required = false)
    public String dpUserMetaData;

    /* This is a comma delimited set of user defined tags */
    @Element(required = false)
    public String dpTags;

    /* PanId setting from the device */
    @Element(required = false)
    public String dpPanId;

    /* ZigBee extended address from the device */
    @Element(required = false)
    public String xpExtAddr;

    /* Map latitude setting from the device */
    @Element(required = false)
    public String dpMapLat;

    /* Map longitude setting from the device */
    @Element(required = false)
    public String dpMapLong;

    /* An ID of the current server the device is connected to */
    @Element(required = false)
    public String dpServerId;

    @Element(required = false)
    public int dpZigbeeCapabilities;

    @Element(required = false)
    public int dpCapabilities;

    @Element
    public DevId id;

    @Root(name = "id")
    public static class DevId implements Serializable {
        private static final long serialVersionUID = 7526472295622776106L;

        /* Automatically generated ID for the device */
        @Element
        public String devId;

        /* Will be 0 which indicates it is the most recent version */
        @Element
        public int devVersion;
    }
}
