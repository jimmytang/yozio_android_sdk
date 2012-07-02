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
  private static final int E_VIEWED_LINK = 11;
  private static final int E_SHARED_LINK = 12;
  
  private static YozioHelper helper;
  
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
   * Makes a blocking HTTP request to Yozio to retrieve a shortened URL
   * specific to the device for the given linkName.
   *
   * @param linkName  the name of the tracking link to retrieve the device
   *                  specific shortened URL for. This MUST match one of the
   *                  link names created on the Yozio web UI.
   * @param destinationUrl  a custom destination URL that the returned
   *                        shortened URL should redirect to.
   */
  public static String getUrl(String linkName, String destinationUrl) {
    if (!validate()) {
      return destinationUrl;
    }
    return helper.getUrl(linkName, destinationUrl);
  }
  
  /**
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
  
  private static HttpClient threadSafeHttpClient() {
    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    // TODO(dounan): consider putting a more useful user agent based on device info
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
