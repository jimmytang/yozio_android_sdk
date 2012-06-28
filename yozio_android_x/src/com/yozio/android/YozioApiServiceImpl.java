package com.yozio.android;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

class YozioApiServiceImpl implements YozioApiService {

  private static final String DEFAULT_BASE_URL = "http://yoz.io";
  private static final String GET_URL_ROUTE = "/api/viral/v1/get_url";
  private static final String BATCH_EVENTS_ROUTE = "/api/viral/v1/batch_events";
  
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
  private String baseUrl;
  
  /**
   * Implementation of {@link YozioApiService} that talks to a Yozio server.
   * 
   * @param httpClient  a thread safe HttpClient.
   */
  YozioApiServiceImpl(HttpClient httpClient) {
    this.httpClient = httpClient;
    baseUrl = DEFAULT_BASE_URL;
  }
  
  public String getUrl(String appKey, String yozioUdid, String linkName, String destinationUrl) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(GET_URL_P_APP_KEY, appKey));
    params.add(new BasicNameValuePair(GET_URL_P_YOZIO_UDID, yozioUdid));
    params.add(new BasicNameValuePair(GET_URL_P_DEVICE_TYPE, YozioHelper.DEVICE_TYPE));
    params.add(new BasicNameValuePair(GET_URL_P_LINK_NAME, linkName));
    params.add(new BasicNameValuePair(GET_URL_P_DEST_URL, destinationUrl));
    String response = doPostRequest(baseUrl + GET_URL_ROUTE, params);
    return getJsonValue(response, GET_URL_R_URL);
  }

  public boolean batchEvents(JSONObject payload) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(BATCH_EVENTS_P_DATA, payload.toString()));
    String response = doPostRequest(baseUrl + BATCH_EVENTS_ROUTE, params);
    String status = getJsonValue(response, BATCH_EVENTS_R_STATUS);
    return status != null && status.equalsIgnoreCase("ok");
  }
  
  /**
   * Performs a blocking HTTP POST request to the specified uri.
   * 
   * @param httpClient  the client to execute the HTTP request.
   * @param baseUrl  the base url to append the parameters to.
   * @param params  the GET parameters.
   * @return  the String response, or null if the request failed.
   */
  String doPostRequest(String baseUrl, List<NameValuePair> params) {
    try {
      HttpPost httpPost = new HttpPost(baseUrl);
      httpPost.setEntity(new UrlEncodedFormEntity(params));
      HttpResponse httpResponse = httpClient.execute(httpPost);
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
  
  // For testing.
  void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
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
        Log.w(LOGTAG, "getJsonValue", e);
      }
    }
    return null;
  }
}
