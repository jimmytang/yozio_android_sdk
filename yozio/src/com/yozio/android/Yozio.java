/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

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
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Public Yozio SDK.
 */
public final class Yozio {

  private static final String LOGTAG = "Yozio";
  private static final String USER_AGENT = "Yozio Android SDK";

  // Event types.
  private static final int E_OPENED_APP = 5;
  private static final int E_LOGIN = 6;
  private static final int E_VIEWED_LINK = 11;
  private static final int E_SHARED_LINK = 12;

  private static YozioHelper helper;

  /**
   * Callback for getUrlAsync.
   *
   * The url argument will either be the Yozio short url, or the destination url if the request
   * failed.
   */
  public interface GetUrlCallback {
    void handleResponse(String shortUrl);
  }

  /**
   * Configures the Yozio SDK. Must be called when the app is initialized.
   *
   * @param context the application context.
   * @param appKey  the application specific key provided by Yozio.
   * @param secretKey  the application specific shared secret key provided
   *                   by Yozio.
   */
  public synchronized static void configure(Context context, String appKey, String secretKey) {
    initializeIfNeeded(context, appKey);
    helper.configure(context, appKey, secretKey);
    if (!validate()) {
      return;
    }
    helper.collect(E_OPENED_APP, "");
  }


  /**
   * Use only for Yozio Experiments
   *
   * Initializes the Yozio SDK for experiments. Must be called when the app is initialized.
   * Makes a blocking HTTP request to download the experiment configurations.
   */
  public synchronized static void initializeExperiments() {
    if (!validate()) {
      return;
    }
    helper.initializeExperiments();
  }

  /**
   * Use only for Yozio Experiments
   *
   * Retrieve the String value for a given configuration key.
   *
   * @param key  The key of the value to retrieve. Must match a configuration key created online.
   * @param defaultValue  The value to return if the key is not found.
   * @return The configuration String value, or defaultValue if the key is not found.
   */
  public synchronized static String stringForKey(String key, String defaultValue) {
    if (!validate()) {
      return defaultValue;
    }
    return helper.stringForKey(key, defaultValue);
  }

  /**
   * Use only for Yozio Experiments
   *
   * Retrieve the int value for a given configuration key.
   *
   * @param key  The key of the value to retrieve. Must match a configuration key created online.
   * @param defaultValue  The value to return if the key is not found.
   * @return The configuration int value, or defaultValue if the key is not found.
   */
  public synchronized static int intForKey(String key, int defaultValue) {
    if (!validate()) {
      return defaultValue;
    }
    return helper.intForKey(key, defaultValue);
  }

  /**
   * Configures Yozio with your user's user name. This is used to provide a better
   * display to your data. (Optional)
   *
   * @param userName  the application's user name
   */
  public synchronized static void userLoggedIn(String userName) {
    if (!validate()) {
      return;
    }
    helper.setUserName(userName);
    helper.collect(E_LOGIN, "");
  }

  /**
   * Use only for Yozio Viral.
   *
   * Makes a blocking HTTP request to Yozio to retrieve a shortened URL
   * specific to the device for the given linkName.
   *
   * @param linkName  the name of the tracking link to retrieve the device
   *                  specific shortened URL for. This MUST match one of the
   *                  link names created on the Yozio web UI.
   * @param destinationUrl  a custom destination URL that the returned
   *                        shortened URL should redirect to.
   * @return the Yozio short url that redirects to the destinationUrl.
   */
  public static String getUrl(String linkName, String destinationUrl) {
    if (!validate()) {
      return destinationUrl;
    }
    return helper.getUrl(linkName, destinationUrl);
  }

  /**
   * Use only for Yozio Viral.
   *
   * Makes a non-blocking HTTP request to Yozio to retrieve a shortened URL
   * specific to the device for the given linkName.
   *
   * @param linkName  the name of the tracking link to retrieve the device
   *                  specific shortened URL for. This MUST match one of the
   *                  link names created on the Yozio web UI.
   * @param destinationUrl  a custom destination URL that the returned
   *                        shortened URL should redirect to.
   * @param callback  the {@link GetUrlCallback} to handle the Yozio short url.
   */
  public static void getUrlAsync(
  		String linkName,
  		String destinationUrl,
  		GetUrlCallback callback) {
    if (!validate()) {
    	callback.handleResponse(destinationUrl);
    }
    helper.getUrlAsync(linkName, destinationUrl, callback);
  }

  /**
   * Use only for Yozio Viral.
   *
   * Alert Yozio that a user has viewed a link.
   *
   * @param linkName  the name of the tracking link viewed by the user.
   *                  This MUST match one of the link names created on the
   *                  Yozio web UI.
   */
  public static void viewedLink(String linkName) {
    if (!validate()) {
      return;
    }
    helper.collect(E_VIEWED_LINK, linkName);
  }

  /**
   * Use only for Yozio Viral.
   *
   * Alert Yozio that a user has shared a link.
   *
   * @param linkName  the name of the tracking link shared by the user.
   *                  This MUST match one of the link names created on the
   *                  Yozio web UI.
   */
  public static void sharedLink(String linkName) {
    if (!validate()) {
      return;
    }
    helper.collect(E_SHARED_LINK, linkName);
  }

  private static void initializeIfNeeded(Context context, String appKey) {
    if (helper != null) {
      return;
    }
    HttpClient httpClient = threadSafeHttpClient();
    YozioApiService apiService = new YozioApiServiceImpl(httpClient);
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(context);
    YozioDataStore dataStore = new YozioDataStoreImpl(dbHelper, appKey);
    helper = new YozioHelper(dataStore, apiService);
  }

  // Visible for testing.
  static HttpClient threadSafeHttpClient() {
    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    HttpProtocolParams.setUserAgent(params, USER_AGENT);
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, registry);
    return new DefaultHttpClient(cm, params);
  }

  private static boolean validate() {
    if (helper == null) {
      Log.e(LOGTAG, "Yozio.configure() not called!");
      return false;
    }
    return helper.validate();
  }

  // For testing
  static void setHelper(YozioHelper helper) {
    Yozio.helper = helper;
  }
}
