/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class FakeHttpClient implements HttpClient {

  private HttpResponse httpResponse;
  private HttpUriRequest lastRequest;

  public void setHttpResonse(HttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  public HttpUriRequest getLastRequest() {
    return lastRequest;
  }

  public HttpResponse execute(HttpUriRequest request)
      throws IOException, ClientProtocolException {
    lastRequest = request;
    return httpResponse;
  }

  public HttpResponse execute(HttpUriRequest request, HttpContext context)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public HttpResponse execute(HttpHost target, HttpRequest request)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
          throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1, HttpContext arg2)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public <T> T execute(
      HttpHost arg0,
      HttpRequest arg1,
      ResponseHandler<? extends T> arg2,
      HttpContext arg3) throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  public ClientConnectionManager getConnectionManager() {
    throw new UnsupportedOperationException();
  }

  public HttpParams getParams() {
    throw new UnsupportedOperationException();
  }
}