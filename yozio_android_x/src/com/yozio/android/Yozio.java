package com.yozio.android;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.util.Log;

/**
 * Entry point to the Yozio SDK.
 */
public final class Yozio {
  
  private static final String LOGTAG = "Yozio";
  private static final String USER_AGENT = "Yozio Android SDK";
  
  // Event types.
  private static final int E_VIEWED_LINK = 11;
  private static final int E_SHARED_LINK = 12;
  private static final int E_OPENED_APP = 13;
  
  private static YozioPrivate instance;
  
  /**
   * Configures the Yozio SDK. Must be called when the app is initialized.
   *
   * @param context the application context.
   * @param appKey  the application specific key provided by Yozio.
   * @param secretKey  the application specific shared secret key provided
   *                   by Yozio.
   */
  public static void configure(Context context, String appKey, String secretKey) {
    getInstance().configure(context, appKey, secretKey);
    if (!validateInstance()) {
      return;
    }
    instance.collect(E_OPENED_APP, "");
  }
  
  /**
   * Makes a blocking HTTP request to Yozio to retrieve a shortened URL
   * specific to the device for the given linkName.
   *
   * @param linkName  the name of the tracking link to retrieve the device
   *                  specific shortened URL for. This MUST match one of the
   *                  link names created on the Yozio web UI.
   * @param destinationUrl  a custom destination URL that the returned
   *                        shortened URL should redirect to.
   * @param fallbackUrl  the URL to return if the HTTP request fails.
   */
  public static String getUrl(String linkName, String destinationUrl, String fallbackUrl) {
    if (!validateInstance()) {
      return fallbackUrl;
    }
    return instance.getUrl(linkName, destinationUrl, fallbackUrl);
  }
  
  /**
   * Alert Yozio that a user has viewed a link.
   *
   * @param linkName  the name of the tracking link viewed by the user.
   *                  This MUST match one of the link names created on the
   *                  Yozio web UI.
   */
  public static void viewedLink(String linkName) {
    if (!validateInstance()) {
      return;
    }
    instance.collect(E_VIEWED_LINK, linkName);
  }

  /**
   * Alert Yozio that a user has shared a link.
   *
   * @param linkName  the name of the tracking link shared by the user.
   *                  This MUST match one of the link names created on the
   *                  Yozio web UI.
   */
  public static void sharedLink(String linkName) {
    if (!validateInstance()) {
      return;
    }
    instance.collect(E_SHARED_LINK, linkName);
  }
  
  /**
   * Should only be called by the configure method. All other methods should
   * access the instance through the static variable.
   */
  private static YozioPrivate getInstance() {
    if (instance == null) {
      HttpClient httpClient = threadSafeHttpClient();
      ThreadPoolExecutor executor = new ThreadPoolExecutor(
          1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
      YozioApiService apiService = new YozioApiServiceImpl(httpClient, executor); 
      instance = new YozioPrivate(new YozioDataStoreImpl(), apiService);
    }
    return instance;
  }
  
  private static HttpClient threadSafeHttpClient() {
    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    HttpProtocolParams.setUserAgent(params, USER_AGENT);
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, registry);
    return new DefaultHttpClient(cm, params);
  }
  
  private static boolean validateInstance() {
    if (instance == null) {
      Log.e(LOGTAG, "Yozio.configure() not called!");
      return false;
    }
    return instance.validate();
  }
  
  // For testing
  static void setInstance(YozioPrivate instance) {
    Yozio.instance = instance;
  }
}
