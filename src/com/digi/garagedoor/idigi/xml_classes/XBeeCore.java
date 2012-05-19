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

@Root(name = "XbeeCore", strict = false)
public class XBeeCore implements Serializable {
    private static final long serialVersionUID = 7526472295622776112L;

    /* ZigBee 64bit extended address of node */
    @Element
    public String xpExtAddr;

    /* Device ID of the gateway discovering this attributes node */
    @Element
    public String devConnectwareId;

    /* iDigi ID of customer owning the gateway discovering the node */
    @Element
    public int cstId;

    /* iDigi ID of the customer group owning the gateway */
    @Element
    public int grpId;

    /* ZigBee 16bit network address of the node */
    @Element
    public int xpNetAddr;

    /* ZigBee node (device) type (0=coordinator, 1=router, 2=endnode) */
    @Element
    public int xpNodeType;

    /*
     * If node is an endnode this will be the network addr of the router it
     * connects to. If node is a router this will be 0xFFFE.
     */
    @Element
    public String xpParentAddr;

    /* ZigBee device profile id of the node */
    @Element
    public String xpProfileId;

    /* ZigBee manufacturing id of the node */
    @Element
    public String xpMfgId;

    /*
     * Device type of the node. Low order 16 bits indicates the product type.
     * High order 16 bits indicates the module type. For convenience text
     * descriptions are returned in xmtModuleTypeDesc and xptProductTypeDesc
     * fields.
     */
    @Element
    public int xpDeviceType;

    /* ZigBee node identifier */
    @Element(required = false)
    public String xpNodeId;

    /* Index within the list of nodes discovered on the HAN */
    @Element
    public int xpDiscoveryIndex;

    /* 1 for connected, 0 for disconnected */
    @Element(required = false)
    public int xpStatus;

    /* Text description of the module type defined in xpDeviceType */
    @Element
    public String xmtModuleTypeDesc;

    /* Text description of the product type defined in xpDeviceType */
    @Element
    public String xptProductTypeDesc;

    /* Time the node was last discovered */
    @Element
    public String xpUpdateTime;

    /* This is a user definable free format text field */
    @Element(required = false)
    public String xpUserMetaData;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExtAddr: " + xpExtAddr + "\n");
        return sb.toString();
    }
}
