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
 * Response.java
 *
 * Container for success/failure integer plus the message or error.

 * (This exists because Java doesn't support multiple return values.)
 */

package com.digi.garagedoor.idigi;

/**
 * The Response class.
 * 
 * 
 * WARNING: Creators of Response objects must use the following convention: 1)
 * if the return value >= 0, the msg MUST BE of type X 2) if the return value <
 * 0, the msg MUST BE an Exception
 */

public class Response<X> {

    /******************* hooray codes **************************/

    public static int SUCCESS = 0;
    public static int NO_OP = 1; /* nothing to do... win by default! */

    /*********** error codes for http communication ***********/

    public static final int FORMATTING_ERROR = -1;
    public static final int EXECUTION_ERROR = -2;
    public static final int NO_RESPONSE = -3;

    public static final int BAD_REQUEST = -4; /* corresponds to HTTP 400 */
    public static final int IDIGI_RESPONSE = -5;
    public static final int IDIGI_ERROR_MESSAGE = -6; /*
                                                       * exception text is xml
                                                       * message
                                                       */

    /* warnings */
    public static final int EMPTY_REQUEST = -7; /*
                                                 * nothing needs to be sent over
                                                 * the wire
                                                 */

    /** The return value. */
    final private int returnValue;
    /** The result (on success). */
    final private X value;
    final private Exception exc;
    final private String failureMsg;

    /**
     * Instantiates a new response.
     * 
     * @param retVal
     *            the ret val
     * @param msg
     *            the msgn
     */
    @SuppressWarnings("unchecked")
    public Response(int retVal, Object msg) {
        this.returnValue = retVal;
        if (retVal < 0) {
            this.exc = (Exception) msg;
            this.value = null;
            this.failureMsg = null;
        } else {
            this.exc = null;
            this.value = (X) msg;
            this.failureMsg = null;
        }
    }

    /**
     * Convenience constructor for propagating an error response up a stack
     * (while handling different generic types)
     * 
     * @param resp
     */
    public Response(Response<? extends Object> resp) {
        if (resp.wasSuccess()) {
            throw new RuntimeException("cascading Response successes is not allowed!");
        }
        this.exc = resp.exc;
        this.value = null;
        this.failureMsg = resp.failureMsg;
        this.returnValue = resp.returnValue;
    }

    /**
     * Instantiates a new (failure) response with added description.
     * 
     * @param retVal
     *            the ret val
     * @param msg
     *            the msg
     * @param failureMsg
     *            the failure msg
     */
    public Response(int retVal, Object msg, String failureMsg) {
        this.returnValue = retVal;
        if (retVal < 0) {
            this.exc = (Exception) msg;
            this.value = null;
            this.failureMsg = failureMsg;
        } else {
            throw new RuntimeException(
                    "Response(..., String failureMsg) constructor being abused! It is only for extra logging on failures.");
        }
    }

    public int getReturnValue() {
        return returnValue;
    }

    /**
     * shortcut for success checking
     * 
     * @return
     */
    public boolean wasSuccess() {
        return !(this.returnValue < 0);
    }

    public Exception getException() {
        if (returnValue < 0) {
            return this.exc;
        } else {
            throw new RuntimeException(
                    "Response class has been abused! (getException() called on a successful Response)");
        }
    }

    public X getMessage() {
        if (returnValue < 0) {
            throw new RuntimeException(
                    "Response class has been abused! (getMessage() called on a failed Response)");
        } else {
            return this.value;
        }
    }

    public String getFailureMessage() {
        if (returnValue < 0) {
            return this.failureMsg + "(" + returnValue + ") : " + this.exc;
        } else {
            throw new RuntimeException(
                    "Response class has been abused! (getFailureMessage() called on a successful Response)");
        }
    }
}
