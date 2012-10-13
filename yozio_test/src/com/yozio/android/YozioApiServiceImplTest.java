/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import com.yozio.android.YozioApiService.ExperimentInfo;

public class YozioApiServiceImplTest extends TestCase {

  private static final String LOGTAG = "YozioApiServiceImplTest";

  // GetYozioLink arguments
  private static final String APP_KEY = "test app key";
  private static final String UDID = "test yozio udid";
  private static final String LOOP_NAME = "test loop name";
  private static final String DEST_URL = "test.com";

  // BatchEvents arguments
  private static final JSONObject PAYLOAD = new JSONObject();

  private FakeHttpClient fakeHttpClient;
  private YozioApiServiceImpl apiService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    fakeHttpClient = new FakeHttpClient();
    apiService = new YozioApiServiceImpl(fakeHttpClient);
  }

  /****************************************************************************
   * GetExperimentConfigs tests
   ****************************************************************************/

  public void testGetExperimentConfigsSuccess() {
    try {
      JSONObject experimentConfigs = new JSONObject().put("key", "value");
      JSONObject experimentVariationSids = new JSONObject().put("experiment1", "variation1");
      String response = new JSONObject().put("experiment_configs", experimentConfigs)
          .put("experiment_variation_sids", experimentVariationSids).toString();

      fakeHttpClient.setHttpResonse(createStringHttpResponse(200, response));
      ExperimentInfo apiResult = apiService.getExperimentInfo(APP_KEY, UDID);

      assertNotNull(fakeHttpClient.getLastRequest());
      assertEquals(experimentConfigs.toString(), apiResult.getConfigs().toString());
      assertEquals(experimentVariationSids.toString(), apiResult.getExperimentVariationSids()
          .toString());
    } catch (JSONException e) {
    }
  }

  public void testGetExperimentConfigsNullHttpEntity() {
    fakeHttpClient.setHttpResonse(createHttpResponse(200, null));
    ExperimentInfo apiResult = apiService.getExperimentInfo(APP_KEY, UDID);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(apiResult.getConfigs());
    assertNull(apiResult.getExperimentVariationSids());
  }

  public void testGetExperimentConfigsNonJsonResponse() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(200, "not {a : json} string"));
    ExperimentInfo apiResult = apiService.getExperimentInfo(APP_KEY, UDID);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(apiResult.getConfigs());
    assertNull(apiResult.getExperimentVariationSids());
  }

  /****************************************************************************
   * GetYozioLink tests
   ****************************************************************************/
  public void testGetYozioLinkSuccess() {
    String expectedYozioLink = "www.foobar.com";
    fakeHttpClient.setHttpResonse(createJsonHttpResponse(200, "url", expectedYozioLink));

    try {
      JSONObject externalProperties = new JSONObject("{\"a\": \"b\", \"c\": \"d\"}");
      JSONObject experimentVariationSids = new JSONObject("{\"ooga\" : \"booga\"}");
      JSONObject yozioProperties = new JSONObject("{\"e\": \"f\"}");
      yozioProperties.put("experiment_variation_sids", experimentVariationSids);

      String yozioLink = apiService.getYozioLink(
          APP_KEY, UDID, LOOP_NAME, DEST_URL, yozioProperties, externalProperties);

      Uri requestUri = fakeHttpClient.getLastRequestUri();
      JSONObject returnedExternalProperties =
          new JSONObject(requestUri.getQueryParameter("external_properties"));
      JSONObject returnedYozioProperties =
          new JSONObject(requestUri.getQueryParameter("yozio_properties"));

      assertEquals(externalProperties.toString(), returnedExternalProperties.toString());
      assertEquals(yozioProperties.toString(), returnedYozioProperties.toString());
      assertEquals(expectedYozioLink, yozioLink);
    } catch (JSONException e) {
      fail();
    }
  }

  public void testGetYozioLinkWithMultipleDestUrls() {
    String expectedYozioLink = "www.foobar.com";
    String expectedIosDestUrl = "www.ios.com";
    String expectedAndroidDestUrl = "www.android.com";
    String expectedNonMobileDestUrl = "www.nonmobile.com";

    fakeHttpClient.setHttpResonse(createJsonHttpResponse(200, "url", expectedYozioLink));

    String yozioLink = apiService.getYozioLink(
        APP_KEY, UDID, LOOP_NAME, expectedIosDestUrl, expectedAndroidDestUrl,
        expectedNonMobileDestUrl, null, null);

    Uri requestUri = fakeHttpClient.getLastRequestUri();
    String iosDestUrl = requestUri.getQueryParameter("ios_dest_url");
    String androidDestUrl = requestUri.getQueryParameter("android_dest_url");
    String nonMobileDestUrl = requestUri.getQueryParameter("non_mobile_dest_url");

    assertEquals(expectedIosDestUrl, iosDestUrl);
    assertEquals(expectedAndroidDestUrl, androidDestUrl);
    assertEquals(expectedNonMobileDestUrl, nonMobileDestUrl);
    assertEquals(expectedYozioLink, yozioLink);
  }

  public void testGetYozioLinkNonJsonResponse() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(200, "not {a : json} string"));
    String yozioLink = apiService.getYozioLink(APP_KEY, UDID, LOOP_NAME, DEST_URL, null, null);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(yozioLink);
  }

  public void testGetYozioLinkResponseMissingUrlKey() {
    fakeHttpClient.setHttpResonse(createJsonHttpResponse(200, "wrong json key", "www.foobar.com"));
    String yozioLink = apiService.getYozioLink(APP_KEY, UDID, LOOP_NAME, DEST_URL, null, null);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(yozioLink);
  }

  public void testGetYozioLinkNullHttpEntity() {
    fakeHttpClient.setHttpResonse(createHttpResponse(200, null));
    String yozioLink = apiService.getYozioLink(APP_KEY, UDID, LOOP_NAME, DEST_URL, null, null);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(yozioLink);
  }

  /****************************************************************************
   * BatchEvents tests
   ****************************************************************************/

  public void testBatchEventsSuccess() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(200, ""));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertTrue(success);
  }

  public void testBatchEventsInvalidUserInput() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(400, ""));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertTrue(success);
  }

  public void testBatchEventsServerError() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(500, ""));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertFalse(success);
  }

  public void testBatchEventsNullHttpEntity() {
    // Should fail even if status is 200.
    fakeHttpClient.setHttpResonse(createHttpResponse(200, null));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertFalse(success);
  }

  /****************************************************************************
   * Helper methods
   ****************************************************************************/
  private HttpResponse createJsonHttpResponse(int status, String key, String value) {
    try {
      JSONObject responseObj = new JSONObject();
      responseObj.put(key, value);
      return createStringHttpResponse(status, responseObj.toString());
    } catch (JSONException e) {
      Log.e(LOGTAG, "createJsonHttpResponse", e);
      fail("Faiure creating a JSON HttpResponse response");
    }
    return null;
  }

  private HttpResponse createStringHttpResponse(int status, String responseStr) {
    try {
      return createHttpResponse(status, new StringEntity(responseStr));
    } catch (UnsupportedEncodingException e) {
      Log.e(LOGTAG, "createHttpResponse", e);
      fail("Faiure creating a StringEntity");
    }
    return null;
  }

  private HttpResponse createHttpResponse(int status, HttpEntity httpEntity) {
    StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, status, "");
    BasicHttpResponse httpResponse = new BasicHttpResponse(statusLine);
    httpResponse.setEntity(httpEntity);
    return httpResponse;
  }
}
