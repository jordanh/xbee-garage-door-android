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

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "device", strict = false)
public class IDigiDevice {
    private static final long serialVersionUID = 7526472295622776108L;

    @Attribute
    public String id;

    @ElementList(inline = true, required = true)
    private ArrayList<RciReply> list;

    public ArrayList<RciReply> getList() {
        if (this.list == null)
            this.list = new ArrayList<RciReply>(0);

        return this.list;
    }
}
