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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "send_message", strict = false)
public class SendMessage {
    private static final long serialVersionUID = 7526472295622776111L;

    @ElementList(inline = true, required = true)
    private ArrayList<IDigiDevice> list;

    public ArrayList<IDigiDevice> getList() {
        if (this.list == null)
            this.list = new ArrayList<IDigiDevice>(0);

        return this.list;
    }
}
