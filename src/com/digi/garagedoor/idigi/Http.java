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
 * Http.java
 *
 * Interface for low-level calls across the wire.

 *
 * This was adapted from the original Edis communication in Android 1.2.
 */

package com.digi.garagedoor.idigi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class Http {

    private DefaultHttpClient hclient = null;

    /* DEBUG */
    private static Logger Log = Logger.getLogger("http");

    /**
     * Must be called before using any other method. (Set trustCerts to true for
     * testing)
     */

    public Http(boolean trustCerts) {
        hclient = getThreadSafeHttpClient(trustCerts);
    }

    // initialize connection object parameters, use a thread safe manager
    /**
     * getThreadSafeHttpClient
     */
    private static DefaultHttpClient getThreadSafeHttpClient(boolean trustAllCerts) {
        HttpParams params = new BasicHttpParams();

        // Increase max total connection to 200
        ConnManagerParams.setMaxTotalConnections(params, 200);
        // Increase default max connection per route to 20
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);

        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
        // Set timeout on requesting a new connection from the manager to avoid
        // hang
        ConnManagerParams.setTimeout(params, 1000);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        if (!trustAllCerts) {
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        } else {

            schemeRegistry.register(new Scheme("https", new Http.TrustingSSLSocketFactory(), 443));
        }

        // Timeouts for the actual connections
        // HttpConnectionParams.setConnectionTimeout(params,
        // connectionTimeoutMillis);
        // HttpConnectionParams.setSoTimeout(params, socketTimeoutMillis);

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        return new DefaultHttpClient(cm, params);
    }

    /**
     * setHttpCredentials
     * 
     * 
     * @param username
     * @param password
     */

    public void setHttpCredentials(String username, String password) {
        hclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY),
                new UsernamePasswordCredentials(username, password));

        return;
    }

    /**
     * 
     * remove credentials
     */
    public void clearHttpCredentials() {
        hclient.getCredentialsProvider().clear();

    }

    /**
     * doGet
     * 
     * 
     * New implementation of Https connection using HttpClient
     * 
     * @param url
     * 
     * @return the HttpResponse object
     */
    public Response<HttpResponse> doGet(String url) {
        HttpResponse response = null;

        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(url);

        } catch (Exception e) {
            return new Response<HttpResponse>(Response.FORMATTING_ERROR, e, "Error forming URL:"
                    + url);
        }

        /* DEBUG */
        Log.log(Level.FINE, "Attempting doGet connection to:" + url);

        try {

            response = hclient.execute(getRequest); // Do the connection and get
                                                    // response
        } catch (Exception e) {
            return new Response<HttpResponse>(Response.EXECUTION_ERROR, e);

        }

        return new Response<HttpResponse>(Response.SUCCESS, response);
    }

    /**
     * 
     * doDelete
     * 
     * Does a generic http delete on the given url
     * 
     * 
     * @param url
     *            : string, url to delete
     * @return int, responseCode of the connection
     */

    public Response<Integer> doDelete(String deleteURL) {

        HttpResponse response = null;

        HttpDelete deleteRequest = null;

        try {
            deleteRequest = new HttpDelete(deleteURL);

        } catch (Exception e) {
            return new Response<Integer>(Response.FORMATTING_ERROR, e, "Error forming URL:"
                    + deleteURL);
        }

        /* overwrite the default context */
        deleteRequest.setHeader("Content-Type", "text/xml");

        try {
            response = hclient.execute(deleteRequest);
        } catch (Exception e) {
            return new Response<Integer>(Response.EXECUTION_ERROR, e);

        }

        if (response != null) {
            /* TODO: check the return response code */

            return new Response<Integer>(Response.SUCCESS, response.getStatusLine().getStatusCode());
        } else {
            return new Response<Integer>(Response.NO_RESPONSE, new Exception());

        }
    }

    /**
     * 
     * simplePost
     * 
     * Method to do a simple post to the provded url
     * 
     * 
     * @param url
     * @return
     */
    public Response<Integer> simplePost(String url) {

        HttpResponse response = null;

        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(url);
        } catch (Exception e) {
            return new Response<Integer>(Response.FORMATTING_ERROR, e,

            "Error forming URL:" + url);
        }

        // Log.d(TAG, "Attempting post to:" + url);

        try {
            response = hclient.execute(postRequest);
        } catch (Exception e) {
            return new Response<Integer>(Response.EXECUTION_ERROR, e);

        }
        if (response != null) {
            // Log.d(TAG, "(HttpClient): " + "response_code == "
            // + response.getStatusLine().getStatusCode()

            // + "	Response Message: "
            // + response.getStatusLine().getReasonPhrase());
            return new Response<Integer>(Response.SUCCESS, response.getStatusLine().getStatusCode());

        } else {
            return new Response<Integer>(Response.NO_RESPONSE, new Exception());
        }
    }

    /**
     * createUserPost
     * 
     * 
     * @param url
     * @param xml
     * @return
     */

    public Response<Integer> doPost(String url, String xml) {

        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(url);
        } catch (Exception e) {
            return new Response<Integer>(Response.FORMATTING_ERROR, e, "Error forming URL:" + url);
        }

        /* overwrite the default context */
        postRequest.setHeader("Content-Type", "application/xml");
        try {
            postRequest.setEntity(new StringEntity(xml, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            return new Response<Integer>(Response.FORMATTING_ERROR, e1);
        }
        // Log.d(TAG, "Attempting createUserPost to:" + url);
        try {
            response = hclient.execute(postRequest);
        } catch (Exception e) {
            return new Response<Integer>(Response.EXECUTION_ERROR, e);
        }
        if (response != null) {
            if (response.getStatusLine().getStatusCode() == 400) {
                /* TODO: possibly call parseError...() from below */
                return new Response<Integer>(Response.BAD_REQUEST, new Exception(), response
                        .getEntity().toString());
            }

            return new Response<Integer>(Response.SUCCESS, response.getStatusLine().getStatusCode());
        } else {
            return new Response<Integer>(Response.NO_RESPONSE, new Exception());
        }
    }

    public Response<HttpResponse> doPostWithHttpResponse(String url, String xml) {

        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(url);
        } catch (Exception e) {
            return new Response<HttpResponse>(Response.FORMATTING_ERROR, e, "Error forming URL:"
                    + url);
        }

        /* overwrite the default context */
        postRequest.setHeader("Content-Type", "application/xml");
        try {
            postRequest.setEntity(new StringEntity(xml, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            return new Response<HttpResponse>(Response.FORMATTING_ERROR, e1);
        }
        // Log.d(TAG, "Attempting createUserPost to:" + url);
        try {
            response = hclient.execute(postRequest);
        } catch (Exception e) {
            return new Response<HttpResponse>(Response.EXECUTION_ERROR, e);
        }
        if (response != null) {
            if (response.getStatusLine().getStatusCode() == 400) {
                /* TODO: possibly call parseError...() from below */
                return new Response<HttpResponse>(Response.BAD_REQUEST, new Exception(), response
                        .getEntity().toString());
            }
            return new Response<HttpResponse>(Response.SUCCESS, response);
        } else {
            return new Response<HttpResponse>(Response.NO_RESPONSE, new Exception());
        }
    }

    /**
     * doPut
     * 
     * 
     * send xml to url as a PUT request
     * 
     * @param url
     * @param xml
     * 
     * @return
     */
    public Response<Integer> doPut(String url, String xml) {

        HttpResponse response = null;
        HttpPut putRequest = null;

        try {

            putRequest = new HttpPut(url);
        } catch (Exception e) {
            return new Response<Integer>(Response.FORMATTING_ERROR, e, "Error forming URL:" + url);

        }

        /* overwrite the default context */
        putRequest.setHeader("Content-Type", "text/xml");

        try {
            putRequest.setEntity(new StringEntity(xml, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            return new Response<Integer>(Response.FORMATTING_ERROR, e1);

        }
        // Log.d(TAG, "Attempting createUserput to:" + url);
        try {
            response = hclient.execute(putRequest);

        } catch (Exception e) {
            return new Response<Integer>(Response.EXECUTION_ERROR, e);
        }

        if (response != null) {
            if (response.getStatusLine().getStatusCode() == 400) {
                /*
                 * grab the error response and return it as an
                 * GarageDoorException
                 */
                try {

                    return new Response<Integer>(Response.IDIGI_ERROR_MESSAGE, new Exception(
                            Util.readInputStreamAsString(response.getEntity().getContent())));
                } catch (IllegalStateException e) {

                    return new Response<Integer>(Response.IDIGI_RESPONSE, e);
                } catch (IOException e) {
                    return new Response<Integer>(Response.IDIGI_RESPONSE, e);
                }

            }

            return new Response<Integer>(Response.SUCCESS, response.getStatusLine().getStatusCode());

        } else {
            return new Response<Integer>(Response.NO_RESPONSE, new Exception());
        }
    }

    /**
     * TrustingSSLSocketFactory
     * 
     * 
     * Trusting socket factory, workaround for GEC self-signed certs
     */
    private static class TrustingSSLSocketFactory implements SocketFactory, LayeredSocketFactory

    {
        private SSLContext sslcontext = null;

        public static SSLContext makeTrustingSSLSocketFactory() {

            try {
                SSLContext context = SSLContext.getInstance("TLS");
                TrustManager[] tm = new TrustManager[] { new TrustingX509TrustManager() };
                context.init(null, tm, new SecureRandom());

                return context;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private SSLContext getSSLContext() throws IOException {

            if (this.sslcontext == null) {
                this.sslcontext = makeTrustingSSLSocketFactory();
            }
            return this.sslcontext;

        }

        @Override
        public Socket createSocket() throws IOException {

            return getSSLContext().getSocketFactory().createSocket();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            // TODO Auto-generated method stub
            return getSSLContext().getSocketFactory().createSocket(socket,

            host, port, autoClose);
        }

        @Override
        public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress,
                int localPort, HttpParams params) throws IOException, UnknownHostException,
                ConnectTimeoutException {

            int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
            int soTimeout = HttpConnectionParams.getSoTimeout(params);

            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);

            SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

            if ((localAddress != null) || (localPort > 0)) {

                // we need to bind explicitly
                if (localPort < 0) {
                    localPort = 0; // indicates "any"
                }

                InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
                sslsock.bind(isa);
            }

            sslsock.connect(remoteAddress, connTimeout);
            sslsock.setSoTimeout(soTimeout);
            return sslsock;

        }

        @Override
        public boolean isSecure(Socket sock) throws IllegalArgumentException {

            // TODO Auto-generated method stub
            return true;
        }
    }

    /**
     * TrustingX509TrustManager
     * 
     */
    private static class TrustingX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;

        }
    }
}
