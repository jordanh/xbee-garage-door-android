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
 * XbeeNodeList.java
 *
 * Wrapper for Xbee requests with multiple returns.

 *
 * (This could almost be done with generics, but we
 * need type inspection for the XML serializer... sigh.)
 */

package com.digi.garagedoor.idigi.xml_classes;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "DiaChannelDataFull", strict = false)
public class DiaChannelDataFull {
    private static final long serialVersionUID = 7526472295622776104L;

    /* iDigi ID of customer owning the gateway */
    @Element
    public String cstId;

    /* ZigBee 64bit extended address of node */
    @Element
    public String xpExtAddr;

    /* The type of data element returned (e.g. float, integer, string) */
    @Element
    public int dcDataType;

    /*
     * The reported value’s unit of measure (e.g meters, Fahrenheit, Volts).
     * This is the value element in the Dia channel sample.
     */
    @Element(required = false)
    public String dcUnits;

    /*
     * The time when the driver reports that the value was acquired. This is the
     * timestamp as recorded by the device in the Dia channel sample.
     */
    @Element
    public String dcdUpdateTime;

    /* String form of the value */
    @Element
    public String dcdStringValue;

    /* Integer value (only available if value format is integer, else null) */
    @Element(required = false)
    public int dcdIntegerValue;

    /* Float value (only available if value format is float, else null) */
    @Element(required = false)
    public float dcdFloatValue;

    /* Date value (only available if value is a date, else null) */
    @Element(required = false)
    public String dcdDateValue;

    /* Boolean value (only available if value is boolean, else null) */
    @Element(required = false)
    public boolean dcdBooleanValue;

    /* Unique numeric ID assigned by iDigi for historical records only */
    @Element(required = false)
    public int dcdhId;

    @Element
    public DiaId id;

    @Root(name = "id")
    public static class DiaId implements Serializable {
        private static final long serialVersionUID = 7526472295622776106L;

        /* Device ID of the gateway */
        @Element
        public String devConnectwareId;

        /* Dia device’s instance name */
        @Element
        public String ddInstanceName;

        /* Dia channel name */
        @Element
        public String dcChannelName;
    }
}
