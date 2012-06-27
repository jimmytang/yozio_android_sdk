package com.yozio.android.testing;

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
  
  @Override
  public HttpResponse execute(HttpUriRequest request)
      throws IOException, ClientProtocolException {
    lastRequest = request;
    return httpResponse;
  }

  @Override
  public HttpResponse execute(HttpUriRequest request, HttpContext context)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
          throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1, HttpContext arg2)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2)
      throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T execute(
      HttpHost arg0,
      HttpRequest arg1,
      ResponseHandler<? extends T> arg2,
      HttpContext arg3) throws IOException, ClientProtocolException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClientConnectionManager getConnectionManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public HttpParams getParams() {
    throw new UnsupportedOperationException();
  }
}