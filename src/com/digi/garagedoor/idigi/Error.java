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
 * Error.java
 * 
 * Java container for iDigi error response.
 */
package com.digi.garagedoor.idigi;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@SuppressWarnings("serial")
@Root
public class Error implements Serializable {
    @Element
    public String error_no;

    @Element
    public String error_message;
}
