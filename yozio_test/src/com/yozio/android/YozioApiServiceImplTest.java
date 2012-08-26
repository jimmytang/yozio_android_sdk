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
import java.util.ArrayList;

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

import android.util.Log;


public class YozioApiServiceImplTest extends TestCase {

  private static final String LOGTAG = "YozioApiServiceImplTest";

  // GetUrl arguments
  private static final String APP_KEY = "test app key";
  private static final String UDID = "test yozio udid";
  private static final String LINK_NAME = "test link name";
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
	    JSONObject experimentConfig = new JSONObject().put("key", "value");
	  	JSONObject experimentVariation = new JSONObject().put("experiment1", "variation1");
	  	String response = new JSONObject().
	  			put("experiment_configs", experimentConfig).
	  			put("experiment_variation_ids", experimentVariation).
	  			toString();

	  	ArrayList<JSONObject> result = new ArrayList<JSONObject>();
	  	result.add(experimentConfig);
	  	result.add(experimentVariation);

	  	fakeHttpClient.setHttpResonse(createStringHttpResponse(200, response));
	    ArrayList<JSONObject> apiResult = apiService.getExperimentDetails(APP_KEY, UDID);

	    assertNotNull(fakeHttpClient.getLastRequest());
	    assertEquals(result.toString(), apiResult.toString());
    } catch (JSONException e) {
    }
  }

  public void testGetExperimentConfigsNullHttpEntity() {
  	fakeHttpClient.setHttpResonse(createHttpResponse(200, null));
    ArrayList<JSONObject> apiResult = apiService.getExperimentDetails(APP_KEY, UDID);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertEquals("[{}, {}]", apiResult.toString());
  }

  public void testGetExperimentConfigsNonJsonResponse() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(200, "not {a : json} string"));
    ArrayList<JSONObject> apiResult = apiService.getExperimentDetails(APP_KEY, UDID);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertEquals("[{}, {}]", apiResult.toString());
  }


  /****************************************************************************
   * GetUrl tests
   ****************************************************************************/

  public void testGetUrlSuccess() {
    String expectedShortUrl = "www.foobar.com";
    fakeHttpClient.setHttpResonse(createJsonHttpResponse(200, "url", expectedShortUrl));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertEquals(expectedShortUrl, shortUrl);
  }

  public void testGetUrlNonJsonResponse() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse(200, "not {a : json} string"));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(shortUrl);
  }

  public void testGetUrlResponseMissingUrlKey() {
    fakeHttpClient.setHttpResonse(createJsonHttpResponse(200, "wrong json key", "www.foobar.com"));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(shortUrl);
  }

  public void testGetUrlNullHttpEntity() {
    fakeHttpClient.setHttpResonse(createHttpResponse(200, null));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(shortUrl);
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
