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

  // Use this class instead of HttpResponse so our code can ensure that
  // the HttpResponse is always cleaned up after a request.
  private static class Response {
    private final int status;
    private final String responseString;

    private Response(int status, String responseString) {
      this.status = status;
      this.responseString = responseString;
    }
  }

  private static final String DEFAULT_BASE_URL = "http://yoz.io";
  private static final String GET_URL_ROUTE = "/api/viral/v1/get_url";
  private static final String GET_CONFIGURATIONS_ROUTE = "/api/yozio/v1/get_configurations";
  private static final String BATCH_EVENTS_ROUTE = "/api/sdk/v1/batch_events";

  private static final String LOGTAG = "YozioApiServiceImpl";

  // Request param names
  private static final String GET_CONFIGURATION_P_APP_KEY = "app_key";
  private static final String GET_CONFIGURATION_P_DEVICE_TYPE = "device_type";
  private static final String GET_CONFIGURATION_P_YOZIO_UDID = "yozio_udid";
  private static final String GET_URL_P_APP_KEY = "app_key";
  private static final String GET_URL_P_DEST_URL = "dest_url";
  private static final String GET_URL_P_DEVICE_TYPE = "device_type";
  private static final String GET_URL_P_EXTERNAL_PROPERTIES = "external_properties";
  private static final String GET_URL_P_LINK_NAME = "link_name";
  private static final String GET_URL_P_YOZIO_PROPERTIES = "yozio_properties";
  private static final String GET_URL_P_YOZIO_UDID = "yozio_udid";
  private static final String BATCH_EVENTS_P_DATA = "data";

  // Response param names
  private static final String GET_URL_R_URL = "url";
  private static final String GET_CONFIGURATIONS_R_EXPERIMENT_CONFIGS = "experiment_configs";
  private static final String GET_CONFIGURATIONS_R_EXPERIMENT_VARIATION_SIDS = "experiment_variation_sids";

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

  public String getUrl(String appKey, String yozioUdid,
      String linkName, String destinationUrl, JSONObject yozioProperties,
      JSONObject externalProperties) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(GET_URL_P_APP_KEY, appKey));
    params.add(new BasicNameValuePair(GET_URL_P_YOZIO_UDID, yozioUdid));
    params.add(new BasicNameValuePair(GET_URL_P_DEVICE_TYPE, YozioHelper.DEVICE_TYPE));
    params.add(new BasicNameValuePair(GET_URL_P_LINK_NAME, linkName));
    params.add(new BasicNameValuePair(GET_URL_P_DEST_URL, destinationUrl));
    addParam(params, GET_URL_P_YOZIO_PROPERTIES, yozioProperties);
    addParam(params, GET_URL_P_EXTERNAL_PROPERTIES, externalProperties);
    Response response = doPostRequest(baseUrl + GET_URL_ROUTE, params);
    return getJsonValue(response, GET_URL_R_URL);
  }

  public ExperimentInfo getExperimentInfo(String appKey, String yozioUdid) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(GET_CONFIGURATION_P_APP_KEY, appKey));
    params.add(new BasicNameValuePair(GET_CONFIGURATION_P_YOZIO_UDID, yozioUdid));
    params.add(new BasicNameValuePair(GET_CONFIGURATION_P_DEVICE_TYPE, YozioHelper.DEVICE_TYPE));
    Response response = doPostRequest(baseUrl + GET_CONFIGURATIONS_ROUTE, params);

    final JSONObject experimentConfigs = getJsonObjectValue(response,
        GET_CONFIGURATIONS_R_EXPERIMENT_CONFIGS);
    final JSONObject experimentVariationSids = getJsonObjectValue(response,
        GET_CONFIGURATIONS_R_EXPERIMENT_VARIATION_SIDS);

    return new ExperimentInfo() {
      public JSONObject getConfigs() {
        return experimentConfigs;
      }

      public JSONObject getExperimentVariationSids() {
        return experimentVariationSids;
      }
    };
  }

  public boolean batchEvents(JSONObject payload) {
    List<NameValuePair> params = new LinkedList<NameValuePair>();
    params.add(new BasicNameValuePair(BATCH_EVENTS_P_DATA, payload.toString()));
    Response response = doPostRequest(baseUrl + BATCH_EVENTS_ROUTE, params);
    // Events that result in 400 will always fail, so pretend like the server handled it correctly.
    // Otherwise, these invalid events will be never be taken off the flush queue.
    return response != null && (response.status == 200 || response.status == 400);
  }

  /**
   * Performs a blocking HTTP POST request to the specified uri.
   *
   * @param httpClient  the client to execute the HTTP request.
   * @param baseUrl  the base url to append the parameters to.
   * @param params  the GET parameters.
   * @return  the {@link Response}, or null if the request failed.
   */
  //TODO: Cache baseUrl and NameValuePair list. Add option.
  Response doPostRequest(String baseUrl, List<NameValuePair> params) {
    try {
      HttpPost httpPost = new HttpPost(baseUrl);
      httpPost.setHeader(YozioHelper.H_SDK_VERSION, YozioHelper.YOZIO_SDK_VERSION);
      httpPost.setEntity(new UrlEncodedFormEntity(params));
      HttpResponse httpResponse = httpClient.execute(httpPost);
      HttpEntity httpEntity = httpResponse.getEntity();
      if (httpEntity != null) {
        String responseString = EntityUtils.toString(httpEntity);
        // Release the content.
        httpEntity.consumeContent();
        return new Response(httpResponse.getStatusLine().getStatusCode(), responseString);
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
   * @param params  a list of name value pairs to add to
   * @param key    the param key
   * @param value  the param value
   *
   * Adds a BasicNameValuePair to params for given key and value
   * when value is not null and not empty
   */
  //TODO: Sort this before toString() if toString() is not deterministic;
  private void addParam(List<NameValuePair> params, String key, JSONObject value) {
    if (value != null && value.length() > 0) {
      params.add(new BasicNameValuePair(key, value.toString()));
    }
  }

  /**
   * Returns the value mapped by the key.
   *
   * @param response  the Yozio http response object.
   * @param key  the key to get the value for.
   * @return the String value, or null there is no mapping for the key.
   */
  private static String getJsonValue(Response response, String key) {
    if (response != null && response.responseString != null) {
      try {
        JSONObject json = new JSONObject(response.responseString);
        if (json.has(key)) {
          return json.getString(key);
        }
      } catch (JSONException e) {
      }
    }
    return null;
  }

  /**
   * Returns the JSONObject mapped by the key
   *
   * @param response  the Yozio http response object.
   * @param key  the key to get the value for.
   * @return the JSONObject, or empty JSONObject if there is no mapping for the key.
   */
  private static JSONObject getJsonObjectValue(Response response, String key) {
    if (response != null && response.responseString != null) {
      try {
        JSONObject json = new JSONObject(response.responseString);
        if (json.has(key)) {
          return json.getJSONObject(key);
        }
      } catch (JSONException e) {
      }
    }
    return new JSONObject();
  }
}
