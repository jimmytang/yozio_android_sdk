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

import android.util.Log;

import com.yozio.android.testing.FakeHttpClient;

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
   * GetUrl tests
   ****************************************************************************/
  
  public void testGetUrlSuccess() {
    String expectedShortUrl = "www.foobar.com";
    fakeHttpClient.setHttpResonse(createJsonHttpResponse("url", expectedShortUrl));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertEquals(expectedShortUrl, shortUrl);
  }
  
  public void testGetUrlNonJsonResponse() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse("not {a : json} string"));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(shortUrl);
  }
  
  public void testGetUrlResponseMissingUrlKey() {
    fakeHttpClient.setHttpResonse(createJsonHttpResponse("wrong json key", "www.foobar.com"));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(shortUrl);
  }
  
  public void testGetUrlNullHttpEntity() {
    fakeHttpClient.setHttpResonse(createHttpResponse(null));
    String shortUrl = apiService.getUrl(APP_KEY, UDID, LINK_NAME, DEST_URL);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertNull(shortUrl);
  }
  
  
  /****************************************************************************
   * BatchEvents tests
   ****************************************************************************/
  
  public void testBatchEventsSuccess() {
    fakeHttpClient.setHttpResonse(createJsonHttpResponse("status", "ok"));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertTrue(success);
  }
  
  public void testBatchEventsNonJsonResponse() {
    fakeHttpClient.setHttpResonse(createStringHttpResponse("not {a : json} string"));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertFalse(success);
  }
  
  public void testBatchEventsResponseMissingStatusKey() {
    fakeHttpClient.setHttpResonse(createJsonHttpResponse("wrong json key", "ok"));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertFalse(success);
  }
  
  public void testBatchEventsNullHttpEntity() {
    fakeHttpClient.setHttpResonse(createHttpResponse(null));
    boolean success = apiService.batchEvents(PAYLOAD);
    assertNotNull(fakeHttpClient.getLastRequest());
    assertFalse(success);
  }
  
  
  /****************************************************************************
   * Helper methods
   ****************************************************************************/
  
  private HttpResponse createJsonHttpResponse(String key, String value) {
    try {
      JSONObject responseObj = new JSONObject();
      responseObj.put(key, value);
      return createStringHttpResponse(responseObj.toString());
    } catch (JSONException e) {
      Log.e(LOGTAG, "createJsonHttpResponse", e);
      fail("Faiure creating a JSON HttpResponse response");
    }
    return null;
  }
  
  private HttpResponse createStringHttpResponse(String responseStr) {
    try {
      return createHttpResponse(new StringEntity(responseStr));
    } catch (UnsupportedEncodingException e) {
      Log.e(LOGTAG, "createHttpResponse", e);
      fail("Faiure creating a StringEntity");
    }
    return null;
  }
  
  private HttpResponse createHttpResponse(HttpEntity httpEntity) {
    // Status code and reason do not matter.
    StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, 0, "");
    BasicHttpResponse httpResponse = new BasicHttpResponse(statusLine);
    httpResponse.setEntity(httpEntity);
    return httpResponse;
  }
}