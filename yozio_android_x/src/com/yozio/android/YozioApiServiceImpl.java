package com.yozio.android;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

class YozioApiServiceImpl implements YozioApiService {

  // TODO(jt): use real BASE_URL
//  public static final String BASE_URL = "http://yoz.io";
  private static final String BASE_URL = "http://192.168.1.128:3000";
  private static final String GET_URL_BASE_URL = BASE_URL + "/api/v1/get_url";
  private static final String BATCH_EVENTS_BASE_URL = BASE_URL + "/api/v1/batch_events";
  
  private static final String LOGTAG = "YozioApiServiceImpl";
  
  // Request param names
  private static final String GET_URL_P_APP_KEY = "app_key";
  private static final String GET_URL_P_YOZIO_UDID = "yozio_udid";
  private static final String GET_URL_P_DEVICE_TYPE = "device_type";
  private static final String GET_URL_P_LINK_NAME = "link_name";
  private static final String GET_URL_P_DEST_URL = "dest_url";
  private static final String BATCH_EVENTS_P_DATA = "data";
  
  // Response param names
  private static final String GET_URL_R_URL = "url";
  private static final String BATCH_EVENTS_R_STATUS = "status";
  
  private final HttpClient httpClient;
  private final ThreadPoolExecutor executor;
  
  /**
   * Implementation of {@link YozioApiService} that talks to a Yozio backend.
   * 
   * @param httpClient  a thread safe HttpClient.
   * @param executor  a ThreadPoolExecutor with a pool size of 1.
   */
  public YozioApiServiceImpl(HttpClient httpClient, ThreadPoolExecutor executor) {
    // In order to guarantee that repeated calls to sdk are executed serially,
    // we must make sure the executor can only have 1 thread.
    executor.setCorePoolSize(1);
    executor.setMaximumPoolSize(1);
    this.httpClient = httpClient;
    this.executor = executor;
  }
  
  public String getUrl(String appKey, String yozioUdid, String linkName, String destinationUrl) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(GET_URL_P_APP_KEY, appKey));
    params.add(new BasicNameValuePair(GET_URL_P_YOZIO_UDID, yozioUdid));
    params.add(new BasicNameValuePair(GET_URL_P_DEVICE_TYPE, YozioPrivate.DEVICE_TYPE));
    params.add(new BasicNameValuePair(GET_URL_P_LINK_NAME, linkName));
    params.add(new BasicNameValuePair(GET_URL_P_DEST_URL, destinationUrl));
    String uri = HttpUtils.urlWithParams(GET_URL_BASE_URL, params);
    String response = HttpUtils.doGetRequest(httpClient, uri);
    return getJsonValue(response, GET_URL_R_URL);
  }

  public void batchEvents(JSONObject payload, ThreadSafeCallback callback) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(BATCH_EVENTS_P_DATA, payload.toString()));
    String uri = HttpUtils.urlWithParams(BATCH_EVENTS_BASE_URL, params);
    executor.submit(new HttpGetTask(httpClient, uri, callback));
  }
  
  /**
   * Returns the value mapped by the key.
   * 
   * @param jsonString  the serialized JSON string.
   * @param key  the key to get the value for.
   * @return the String value, or null there is no mapping for the key.
   */
  private static String getJsonValue(String jsonString, String key) {
    if (jsonString != null) {
      try {
        return new JSONObject(jsonString).getString(key);
      } catch (JSONException e) {
        Log.e(LOGTAG, "getJsonValue", e);
      }
    }
    return null;
  }
  
  /**
   * Task to fire off an sdk HTTP GET request.
   */
  private static class HttpGetTask implements Runnable {
    
    private final HttpClient httpClient;
    private final String uri;
    private final ThreadSafeCallback callback;
    
    HttpGetTask(HttpClient httpClient, String uri, ThreadSafeCallback callback) {
      this.httpClient = httpClient;
      this.uri = uri;
      this.callback = callback;
    }
    
    public void run() {
      String response = HttpUtils.doGetRequest(httpClient, uri);
      String status = getJsonValue(response, BATCH_EVENTS_R_STATUS);
      if (status != null && status.equalsIgnoreCase("ok")) {
        callback.onSuccess();
      } else {
        callback.onFailure();
      }
    }
  }
}
