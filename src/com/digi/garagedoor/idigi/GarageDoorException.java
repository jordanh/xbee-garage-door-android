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
 * GarageDoorException.java
 * 
 * Thrown when iDigi returns an error message instead of the
 * expected response.
 */
package com.digi.garagedoor.idigi;

public class GarageDoorException extends RuntimeException {
    private static final long serialVersionUID = 7526472295622776201L;
    /*
     * UNKNOWN is used for an unparseable problem.
     */
    public static final String UNKNOWN = "unknown";
    private final String errno;

    public GarageDoorException(String message, String errno) {
        super(message);
        this.errno = errno;
    }

    public GarageDoorException(Exception e1, String errno) {
        super(e1);
        this.errno = errno;
    }

    public String getErrno() {
        return this.errno;
    }
}
