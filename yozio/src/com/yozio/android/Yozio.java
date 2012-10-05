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

  // Event types.
  private static final int E_OPENED_APP = 5;
  private static final int E_LOGIN = 6;
  private static final int E_VIEWED_LINK = 11;
  private static final int E_SHARED_LINK = 12;

  private static YozioHelper helper;

  /**
   * Callback for getUrlAsync.
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
    helper.collect(E_OPENED_APP, "");
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
   * This allows you to tie your data with Yozio's by user name.
   *
   * @param userName  Name of the user that just logged in.
   */
  public static void userLoggedIn(String userName) {
    userLoggedIn(userName, null);
  }

  /**
   * Notify Yozio that your user logged in.
   * This allows you to tie your data with Yozio's by user name.
   *
   * @param userName  Name of the user that just logged in.
   * @param properties  Arbitrary meta data to attach to this event.
   */
  public static void userLoggedIn(String userName, JSONObject properties) {
    if (!validate()) {
      return;
    }
    helper.setUserName(userName);
    helper.collect(E_LOGIN, "", properties);
  }

  /**
   * Makes a blocking HTTP request to generate a Yozio link.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @return A Yozio link, or the destinationUrl if there is an error generating
   *         the Yozio link.
   */
  public static String getYozioLink(String viralLoopName, String destinationUrl) {
    return getYozioLink(viralLoopName, destinationUrl, null);
  }

  /**
   * Makes a blocking HTTP request to generate a Yozio link.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @return A Yozio link, or the destinationUrl if there is an error generating
   *         the Yozio link.
   */
  public static String getYozioLink(String linkName, String destinationUrl, JSONObject properties) {
    if (!validate()) {
      return destinationUrl;
    }
    return helper.getYozioLink(linkName, destinationUrl, properties);
  }

  /**
   * Makes an asynchronous HTTP request to generate a Yozio link.
   *
   * Must be called from your application's main UI thread. The callback will
   * also be executed on the main UI thread.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param callback  Called when the HTTP request completes.
   *                  The argument passed into the callback will be the Yozio
   *                  link, or the destinationUrl if there is an error generating
   *                  the Yozio link.
   */
  public static void getYozioLinkAsync(String linkName, String destinationUrl,
      GetYozioLinkCallback callback) {
    getYozioLinkAsync(linkName, destinationUrl, null, callback);
  }

  /**
   * Makes an asynchronous HTTP request to generate a Yozio link.
   *
   * Must be called from your application's main UI thread. The callback will
   * also be executed on the main UI thread.
   *
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @param callback  Called when the HTTP request completes.
   *                  The argument passed into the callback will be the Yozio
   *                  link, or the destinationUrl if there is an error generating
   *                  the Yozio link.
   */
  public static void getYozioLinkAsync(String linkName, String destinationUrl,
      JSONObject properties, GetYozioLinkCallback callback) {
    if (!validate()) {
      callback.handleResponse(destinationUrl);
    }
    helper.getYozioLinkAsync(linkName, destinationUrl, properties, callback);
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
    viewedLink(linkName, null);
  }

  /**
   * Notify Yozio that a user has viewed a link.
   *
   * @param linkName  Name of the viral tracking link. Must match one of the
   *                  viral tracking link names created on the Yozio dashboard.
   * @param properties  Arbitrary meta data to attach to the event.
   */
  public static void viewedLink(String linkName, JSONObject properties) {
    if (!validate()) {
      return;
    }
    helper.collect(E_VIEWED_LINK, linkName, properties);
  }

  /**
   * Notify Yozio that a user has shared a link.
   *
   * @param linkName  Name of the viral tracking link. Must match one of the
   *                  viral tracking link names created on the Yozio dashboard.
   * @param properties  Additional meta properties to tag your event.
   */
  public static void sharedLink(String linkName) {
    sharedLink(linkName, null);
  }

  /**
   * Notify Yozio that a user has shared a link.
   *
   * @param linkName  Name of the viral tracking link. Must match one of the
   *                  viral tracking link names created on the Yozio dashboard.
   * @param properties  Arbitrary meta data to attach to the event.
   */
  public static void sharedLink(String linkName, JSONObject properties) {
    if (!validate()) {
      return;
    }
    helper.collect(E_SHARED_LINK, linkName, properties);
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
