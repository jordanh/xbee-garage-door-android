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

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "result", strict = false)
public class XBeeCoreList {
    private static final long serialVersionUID = 7526472295622776113L;

    /* Total number of resources that match the condition */
    @Element
    public int resultTotalRows;

    /* The record number of the first result */
    @Element
    public int requestedStartRow;

    /* The number of results returned */
    @Element
    public int resultSize;

    /* The number of results returned */
    @Element
    public int requestedSize;

    /* The remaining number of resources */
    @Element
    public int remainingSize;

    @ElementList(inline = true, required = false)
    private ArrayList<XBeeCore> list;

    public ArrayList<XBeeCore> getList() {
        if (this.list == null)
            this.list = new ArrayList<XBeeCore>(0);

        return this.list;
    }
}
