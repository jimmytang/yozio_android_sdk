package com.yozio.android;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

class YozioApiServiceImpl implements YozioApiService {

  // TODO(jt): make the urls settable for tests
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
  
  /**
   * Implementation of {@link YozioApiService} that talks to a Yozio server.
   * 
   * @param httpClient  a thread safe HttpClient.
   */
  public YozioApiServiceImpl(HttpClient httpClient) {
    this.httpClient = httpClient;
  }
  
  public String getUrl(String appKey, String yozioUdid, String linkName, String destinationUrl) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(GET_URL_P_APP_KEY, appKey));
    params.add(new BasicNameValuePair(GET_URL_P_YOZIO_UDID, yozioUdid));
    params.add(new BasicNameValuePair(GET_URL_P_DEVICE_TYPE, YozioHelper.DEVICE_TYPE));
    params.add(new BasicNameValuePair(GET_URL_P_LINK_NAME, linkName));
    params.add(new BasicNameValuePair(GET_URL_P_DEST_URL, destinationUrl));
    String response = doGetRequest(GET_URL_BASE_URL, params);
    return getJsonValue(response, GET_URL_R_URL);
  }

  public boolean batchEvents(JSONObject payload) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(BATCH_EVENTS_P_DATA, payload.toString()));
    String response = doGetRequest(BATCH_EVENTS_BASE_URL, params);
    String status = getJsonValue(response, BATCH_EVENTS_R_STATUS);
    return status != null && status.equalsIgnoreCase("ok");
  }
  
  /**
   * Performs a blocking HTTP GET request to the specified uri.
   * 
   * @param httpClient  the client to execute the HTTP request.
   * @param baseUrl  the base url to append the parameters to.
   * @param params  the GET parameters.
   * @return  the String response, or null if the request failed.
   */
  String doGetRequest(String baseUrl, List<NameValuePair> params) {
    try {
      HttpGet httpGet = new HttpGet(urlWithParams(baseUrl, params));
      HttpResponse httpResponse = httpClient.execute(httpGet);
      HttpEntity httpEntity = httpResponse.getEntity();
      if (httpEntity != null) {
        String responseString = EntityUtils.toString(httpEntity);
        // Release the content.
        httpEntity.consumeContent();
        return responseString;
      }
    } catch (ClientProtocolException e) {
      Log.e(LOGTAG, "doGetRequest", e);
    } catch (IOException e) {
      Log.e(LOGTAG, "doGetRequest", e);
    }
    return null;
  }
  
  /**
   * Constructs a URL with the GET parameters appended.
   * 
   * @param baseUrl  the base url to append the parameters to.
   * @param params  the parameters to append to the baseUrl.
   * @return the URL with the appended parameters.
   */
  String urlWithParams(String baseUrl, List<NameValuePair> params) {
    String paramString = URLEncodedUtils.format(params, "utf-8");
    return baseUrl + "?" + paramString;
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
        JSONObject json = new JSONObject(jsonString);
        if (json.has(key)) {
          return json.getString(key);
        }
      } catch (JSONException e) {
        Log.e(LOGTAG, "getJsonValue", e);
      }
    }
    return null;
  }
}
