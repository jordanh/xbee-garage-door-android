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
 * Util.java
 *
 * Various string and data manipulations.
 */
package com.digi.garagedoor.idigi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class Util {
    /*
     * string util
     */
    public static <X> String join(String prefix, Collection<X> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<X> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(prefix + iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /*
     * string util
     */
    public static String readInputStreamAsString(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    public static byte[] readInputStreamAsByteArray(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toByteArray();
    }

    /**
     * 
     * @param cal
     * @return String as ISO 8061
     */
    public static String getServerStartTimeInUTC(Date cal) {
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return serverFormat.format(cal);
    }

    /**
     * Return str with split inserted at every interval.
     * 
     * Assumes that len(str) % interval == 0
     * 
     * @param str
     * @param split
     * @param interval
     * @return
     */
    public static String insertSplitter(String str, String split, int interval) {
        int index = interval;
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, interval));
        while (index < str.length()) {
            sb.append(split);
            sb.append(str.substring(index, index + interval));
            index += interval;
        }

        return sb.toString();
    }
}
