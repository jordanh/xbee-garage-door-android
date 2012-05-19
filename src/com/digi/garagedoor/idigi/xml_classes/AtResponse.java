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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "at_response", strict = false)
public class AtResponse implements Serializable {
    private static final long serialVersionUID = 7526472295622776101L;

    @Attribute
    public String command;

    @Attribute
    public String operation;

    @Attribute
    public String result;

    @Attribute(required = false)
    public String type;

    @Attribute(required = false)
    public String value;

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("command: ");
        sb.append(command + "\n");
        sb.append("operation: ");
        sb.append(operation + "\n");
        sb.append("result: ");
        sb.append(result + "\n");
        sb.append("type: ");
        sb.append(type + "\n");
        sb.append("value: ");
        sb.append(value + "\n");
        return sb.toString();
    }
}
