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
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Public Yozio SDK.
 */
public final class Yozio {

  private static final String LOGTAG = "Yozio";

  // Event types. Visible for testing.
  static final int E_OPENED_APP = 5;
  static final int E_LOGIN = 6;
  static final int E_VIEWED_LINK = 11;
  static final int E_SHARED_LINK = 12;

  private static YozioHelper helper;

  /**
   * Callback for getYozioLinkAsync.
   *
   * The yozioLink argument will either be the Yozio link, or the default URL if
   * there was an error generating the Yozio link.
   */
  public interface GetYozioLinkCallback {
    void handleResponse(String yozioLink);
  }

  /**
   * Callback for initializeExperimentsAsync.
   */
  public interface InitializeExperimentsCallback {
    void onComplete();
  }

  /**
   * Configures the Yozio SDK. Must be called when your app is launched and
   * before any other method.
   *
   * @param context Application context.
   * @param appKey  Application specific key provided by Yozio.
   * @param secretKey  Application specific secret key provided by Yozio.
   */
  public static void configure(Context context, String appKey, String secretKey) {
    initializeIfNeeded(context, appKey);
    helper.configure(context, appKey, secretKey);
    if (!validate()) {
      return;
    }
    helper.collect(E_OPENED_APP, null, null);
  }

  /**
   * Makes a blocking HTTP request to download the experiment configurations.
   * Must be called prior to using any experiment related SDK calls
   * (i.e. stringForKey and intForKey).
   */
  public static void initializeExperiments() {
    if (!validate()) {
      return;
    }
    helper.initializeExperiments();
  }

  /**
   * Makes an asynchronous HTTP request to download the experiment configurations.
   * Must be called prior to using any experiment related SDK calls
   * (i.e. stringForKey and intForKey).
   *
   * @param callback  Called when experiments has been initialized.
   */
  public static void initializeExperimentsAsync(InitializeExperimentsCallback callback) {
    if (!validate()) {
      return;
    }
    helper.initializeExperimentsAsync(callback);
  }

  /**
   * Retrieve the string value for a given configuration key.
   *
   * @param key  Key of the value to retrieve.
   * @param defaultValue  Value to return if the key is not found.
   * @return String value, or defaultValue if the key is not found.
   */
  public static String stringForKey(String key, String defaultValue) {
    if (!validate()) {
      return defaultValue;
    }
    return helper.stringForKey(key, defaultValue);
  }

  /**
   * Retrieve the integer value for a given configuration key.
   *
   * @param key  Key of the value to retrieve.
   * @param defaultValue  Value to return if the key is not found.
   * @return Integer value, or defaultValue if the key is not found.
   */
  public static int intForKey(String key, int defaultValue) {
    if (!validate()) {
      return defaultValue;
    }
    return helper.intForKey(key, defaultValue);
  }

  /**
   * Notify Yozio that your user logged in.
   *
   * This will allow you to tie exported Yozio data with your own data.
   *
   * Warning: do not provide any personally identifiable information.
   *
   * @param userName  Name of the user that just logged in.
   */
  public static void userLoggedIn(String userName) {
    userLoggedIn(userName, null);
  }

  /**
   * Notify Yozio that your user logged in.
   *
   * This will allow you to tie exported Yozio data with your own data.
   *
   * Warning: do not provide any personally identifiable information.
   *
   * @param userName  Name of the user that just logged in.
   * @param properties  Arbitrary meta data to attach to this event.
   */
  public static void userLoggedIn(String userName, JSONObject properties) {
    if (!validate()) {
      return;
    }
    helper.setUserName(userName);
    helper.collect(E_LOGIN, null, null, properties);
  }

  /**
   * Makes a blocking HTTP request to generate a Yozio link.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @return A Yozio link, or the destinationUrl if there is an error generating
   *         the Yozio link.
   */
  public static String getYozioLink(String viralLoopName, String channel, String destinationUrl) {
    return getYozioLink(viralLoopName, channel, destinationUrl, null);
  }

  /**
   * Makes a blocking HTTP request to generate a Yozio link.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @return A Yozio link, or the destinationUrl if there is an error generating
   *         the Yozio link.
   */
  public static String getYozioLink(String viralLoopName, String channel, String destinationUrl,
      JSONObject properties) {
    if (!validate()) {
      return destinationUrl;
    }
    return helper.getYozioLink(viralLoopName, channel, destinationUrl, properties);
  }

  /**
   * Makes a blocking HTTP request to generate a Yozio link.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param iosDestinationUrl  URL that the generated Yozio link will redirect to
   *                           for iOS devices.
   * @param androidDestinationUrl URL that the generated Yozio link will redirect
   *                              to for Android devices.
   * @param nonMobileDestinationUrl  URL that the generated Yozio link will
   *                                 redirect to for all other devices.
   * @return A Yozio link, or the destinationUrl if there is an error generating
   *         the Yozio link.
   */
  public static String getYozioLink(String viralLoopName, String channel, String iosDestinationUrl,
      String androidDestinationUrl, String nonMobileDestinationUrl) {
    return getYozioLink(viralLoopName, channel, iosDestinationUrl, androidDestinationUrl,
        nonMobileDestinationUrl, null);
  }

  /**
   * Makes a blocking HTTP request to generate a Yozio link.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param iosDestinationUrl  URL that the generated Yozio link will redirect to
   *                           for iOS devices.
   * @param androidDestinationUrl URL that the generated Yozio link will redirect
   *                              to for Android devices.
   * @param nonMobileDestinationUrl  URL that the generated Yozio link will
   *                                 redirect to for all other devices.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @return A Yozio link, or the destinationUrl if there is an error generating
   *         the Yozio link.
   */
  public static String getYozioLink(String viralLoopName, String channel, String iosDestinationUrl,
      String androidDestinationUrl, String nonMobileDestinationUrl, JSONObject properties) {
    if (!validate()) {
      return nonMobileDestinationUrl;
    }
    return helper.getYozioLink(viralLoopName, channel, iosDestinationUrl, androidDestinationUrl,
        nonMobileDestinationUrl, properties);
  }

  /**
   * Makes an asynchronous HTTP request to generate a Yozio link.
   *
   * Must be called from your application's main UI thread. The callback will
   * also be executed on the main UI thread.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param callback  Called when the HTTP request completes.
   *                  The argument passed into the callback will be the Yozio
   *                  link, or the destinationUrl if there is an error generating
   *                  the Yozio link.
   */
  public static void getYozioLinkAsync(String viralLoopName, String channel, String destinationUrl,
      GetYozioLinkCallback callback) {
    getYozioLinkAsync(viralLoopName, channel, destinationUrl, null, callback);
  }

  /**
   * Makes an asynchronous HTTP request to generate a Yozio link.
   *
   * Must be called from your application's main UI thread. The callback will
   * also be executed on the main UI thread.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @param callback  Called when the HTTP request completes.
   *                  The argument passed into the callback will be the Yozio
   *                  link, or the destinationUrl if there is an error generating
   *                  the Yozio link.
   */
  public static void getYozioLinkAsync(String viralLoopName, String channel, String destinationUrl,
      JSONObject properties, GetYozioLinkCallback callback) {
    if (!validate()) {
      callback.handleResponse(destinationUrl);
      return;
    }
    helper.getYozioLinkAsync(viralLoopName, channel, destinationUrl, properties, callback);
  }

  /**
   * Makes an asynchronous HTTP request to generate a Yozio link.
   *
   * Must be called from your application's main UI thread. The callback will
   * also be executed on the main UI thread.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param iosDestinationUrl  URL that the generated Yozio link will redirect to
   *                           for iOS devices.
   * @param androidDestinationUrl URL that the generated Yozio link will redirect
   *                              to for Android devices.
   * @param nonMobileDestinationUrl  URL that the generated Yozio link will
   *                                 redirect to for all other devices.
   * @param callback  Called when the HTTP request completes.
   *                  The argument passed into the callback will be the Yozio
   *                  link, or the destinationUrl if there is an error generating
   *                  the Yozio link.
   */
  public static void getYozioLinkAsync(String viralLoopName, String channel,
      String iosDestinationUrl, String androidDestinationUrl, String nonMobileDestinationUrl,
      GetYozioLinkCallback callback) {
    getYozioLinkAsync(viralLoopName, channel, iosDestinationUrl, androidDestinationUrl,
        nonMobileDestinationUrl, null, callback);
  }

  /**
   * Makes an asynchronous HTTP request to generate a Yozio link.
   *
   * Must be called from your application's main UI thread. The callback will
   * also be executed on the main UI thread.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param iosDestinationUrl  URL that the generated Yozio link will redirect to
   *                           for iOS devices.
   * @param androidDestinationUrl URL that the generated Yozio link will redirect
   *                              to for Android devices.
   * @param nonMobileDestinationUrl  URL that the generated Yozio link will
   *                                 redirect to for all other devices.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @param callback  Called when the HTTP request completes.
   *                  The argument passed into the callback will be the Yozio
   *                  link, or the destinationUrl if there is an error generating
   *                  the Yozio link.
   */
  public static void getYozioLinkAsync(String viralLoopName, String channel,
      String iosDestinationUrl, String androidDestinationUrl, String nonMobileDestinationUrl,
      JSONObject properties, GetYozioLinkCallback callback) {
    if (!validate()) {
      callback.handleResponse(nonMobileDestinationUrl);
      return;
    }
    helper.getYozioLinkAsync(viralLoopName, channel, iosDestinationUrl, androidDestinationUrl,
        nonMobileDestinationUrl, properties, callback);
  }

  /**
   * Notify Yozio that the user has entered the viral loop.
   *
   * This event should be triggered at whatever point you define the beginning
   * of the viral loop to be.
   *
   * For example, a user can enter a viral loop whenever the share button for
   * the viral loop is shown.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   */
  public static void enteredViralLoop(String viralLoopName, String channel) {
    enteredViralLoop(viralLoopName, channel, null);
  }

  /**
   * Notify Yozio that the user has entered the viral loop.
   *
   * This event should be triggered at whatever point you define the beginning
   * of the viral loop to be.
   *
   * For example, a user can enter a viral loop whenever the share button for
   * the viral loop is shown.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param properties  Arbitrary meta data to attach to the event.
   */
  public static void enteredViralLoop(String viralLoopName, String channel, JSONObject properties) {
    if (!validate()) {
      return;
    }
    helper.collect(E_VIEWED_LINK, viralLoopName, channel, properties);
  }

  /**
   * Notify Yozio that the user has shared a Yozio link.
   *
   * This event should be triggered whenever a user has successfully shared a
   * Yozio link generated by getYozioLink.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   */
  public static void sharedYozioLink(String viralLoopName, String channel) {
    sharedYozioLink(viralLoopName, channel, null);
  }

  /**
   * Notify Yozio that the user has shared a Yozio link.
   *
   * This event should be triggered whenever a user has successfully shared a
   * Yozio link generated by getYozioLink.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param channel  Social channel of the viral loop.
   * @param properties  Arbitrary meta data to attach to the event.
   */
  public static void sharedYozioLink(String viralLoopName, String channel, JSONObject properties) {
    if (!validate()) {
      return;
    }
    helper.collect(E_SHARED_LINK, viralLoopName, channel, properties);
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
