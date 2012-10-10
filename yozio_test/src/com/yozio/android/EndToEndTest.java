package com.yozio.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.yozio.android.Yozio.GetYozioLinkCallback;
import com.yozio.android.Yozio.InitializeExperimentsCallback;

public class EndToEndTest extends InstrumentationTestCase {

  private static final String LOGTAG = "EndToEndTest";
  private static final String TEST_BASE_URL = "http://yoz.io";

  private static final String LOGIN_EMAIL = "test@yozio.com";
  private static final String LOGIN_PASSWORD = "oogabooga";

  private static final String APP_ID = "506ea7a3a6cc9f4e8c009ce6";
  private static final String LINK_ID = "506ea7faa6cc9f4e8c00a374";

  private static final String APP_KEY = "96d12600-f0fc-012f-2d87-12314000ac7c";
  private static final String SECRET_KEY = "96d13160-f0fc-012f-2d88-12314000ac7c";
  private static final String LOOP_NAME = "testloop";

  private static final String EXPERIMENT_ID = "50733c0fa6cc9f77e10030d3";
  private static final String CONTROL_VARIATION_ID = "50733c0fa6cc9f77e10030d2";
  private static final String VARIATION_A_ID = "50733c2fa6cc9f7c080027f6";
  private static final String VARIATION_B_ID = "50734f3aa6cc9f04b30083a9";


  private static final int E_VIRAL_CLICK = 31;

  private HttpClient httpClient;
  private Context context;
  private YozioDataStoreImpl dataStore;
  private String yozioUdid;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    context = getInstrumentation().getContext();
    context.deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    httpClient = Yozio.threadSafeHttpClient();
    YozioApiServiceImpl apiService = new YozioApiServiceImpl(httpClient);
    apiService.setBaseUrl(TEST_BASE_URL);
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(context);
    dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    YozioHelper helper = new YozioHelper(dataStore, apiService);
    Yozio.setHelper(helper);
    Yozio.configure(context, APP_KEY, SECRET_KEY);
    // Wait for the OPEN event triggered by configure to be sent.
    TestHelper.waitUntilEventSent(dataStore);
    // Login to the Yozio website.
    login();

    this.yozioUdid = OpenUDID.getOpenUDIDInContext();
  }

  /**
   * Tests the the generated Yozio link is valid and makes sure that the click
   * count goes up in the UI when the Yozio link is clicked.
   */
  public void testGetYozioLink() {
    final String yozioLink = Yozio.getYozioLink(LOOP_NAME, "www.google.com");
    assertTrue(yozioLink.startsWith(TEST_BASE_URL + "/r/"));
    Runnable visitLink = new Runnable() {
      public void run() {
        String response = doGetRequest(yozioLink);
        assertTrue(response.contains("<title>Google</title>"));
      }
    };
    assertLoopEventCountChange(E_VIRAL_CLICK, 1, visitLink);
  }

  /**
   * Tests the the generated Yozio link is valid and makes sure that the click
   * count goes up in the UI when the Yozio link is clicked.
   */
  public void testGetYozioLinkAsync() throws Throwable {
    final CountDownLatch signal = new CountDownLatch(1);
    runTestOnUiThread(new Runnable() {
      public void run() {
        // Actual test
        Yozio.getYozioLinkAsync(LOOP_NAME, "www.yahoo.com", new GetYozioLinkCallback() {
          public void handleResponse(final String yozioLink) {
            assertTrue(yozioLink.startsWith(TEST_BASE_URL + "/r/"));
            Runnable visitLink = new Runnable() {
              public void run() {
                String response = doGetRequest(yozioLink);
                assertTrue(response.contains("<title>Yahoo!</title>"));
              }
            };
            assertLoopEventCountChange(E_VIRAL_CLICK, 1, visitLink);
            signal.countDown();
          }
        });
      }
    });
    signal.await(20, TimeUnit.SECONDS);
  }

  /**
   * Tests the the generated Yozio link is valid for multiple destination urls
   * and makes sure that the click count goes up in the UI when the Yozio link is clicked.
   */
  public void testGetYozioLinkForMultipleDestUrls() {
    final String yozioLink = Yozio.getYozioLink(LOOP_NAME,
        "www.google.com", "www.bing.com", "www.yahoo.com");
    assertTrue(yozioLink.startsWith(TEST_BASE_URL + "/r/"));
    Runnable visitLink = new Runnable() {
      public void run() {
        String response = doGetRequest(yozioLink, "iphone");
        assertTrue(response.contains("<title>Google</title>"));

        response = doGetRequest(yozioLink, "android");
        assertTrue(response.contains("<title>Bing</title>"));

        response = doGetRequest(yozioLink, "other");
        assertTrue(response.contains("<title>Yahoo!</title>"));
      }
    };
    assertLoopEventCountChange(E_VIRAL_CLICK, 3, visitLink);
  }

  /**
   * Tests the the generated Yozio link is valid for multiple destination urls
   * and makes sure that the click count goes up in the UI when the Yozio link is clicked.
   */
  public void testGetYozioLinkAsyncForMultipleDestinationUrls() throws Throwable {
    final CountDownLatch signal = new CountDownLatch(1);
    runTestOnUiThread(new Runnable() {
      public void run() {
        // Actual test
        Yozio.getYozioLinkAsync(LOOP_NAME, "www.google.com", "www.bing.com",
            "www.yahoo.com", new GetYozioLinkCallback() {
          public void handleResponse(final String yozioLink) {
            assertTrue(yozioLink.startsWith(TEST_BASE_URL + "/r/"));
            Runnable visitLink = new Runnable() {
              public void run() {
                String response = doGetRequest(yozioLink, "iphone");
                assertTrue(response.contains("<title>Google</title>"));

                response = doGetRequest(yozioLink, "android");
                assertTrue(response.contains("<title>Bing</title>"));

                response = doGetRequest(yozioLink, "other");
                assertTrue(response.contains("<title>Yahoo!</title>"));
              }
            };
            assertLoopEventCountChange(E_VIRAL_CLICK, 3, visitLink);
            signal.countDown();
          }
        });
      }
    });
    signal.await(20, TimeUnit.SECONDS);
  }


  /**
   * Test the Entrances value in the UI goes up.
   */
  public void testEnteredViralLoop() {
    assertLoopEventCountChange(Yozio.E_VIEWED_LINK, 1, new Runnable() {
      public void run() {
        Yozio.enteredViralLoop(LOOP_NAME);
        TestHelper.waitUntilEventSent(dataStore);
      }
    });
  }

  /**
   * Test the Shares value in the UI goes up.
   */
  public void testSharedYozioLink() {
    assertLoopEventCountChange(Yozio.E_SHARED_LINK, 1, new Runnable() {
      public void run() {
        Yozio.sharedYozioLink(LOOP_NAME);
        TestHelper.waitUntilEventSent(dataStore);
      }
    });
  }

  /**
   * Test sending user invalid enteredViralLoop event to make sure it gets
   * removed from the queue.
   */
  public void testDiscardInvalidEvents() {
    assertEquals(0, dataStore.getNumEvents());
    Yozio.enteredViralLoop("nonexistentname");
    assertEquals(1, dataStore.getNumEvents());
    TestHelper.waitUntilEventSent(dataStore);
    assertEquals(0, dataStore.getNumEvents());
  }

  /**
   * Test intForKey with initializeExperiments
   */
  public void testIntForKeyWithInitializeExperiments() {
    forceVariation(CONTROL_VARIATION_ID);
    Yozio.initializeExperiments();
    assertEquals(789, Yozio.intForKey("height", 789));

    forceVariation(VARIATION_A_ID);
    Yozio.initializeExperiments();
    assertEquals(123, Yozio.intForKey("height", 789));

    forceVariation(VARIATION_B_ID);
    Yozio.initializeExperiments();
    assertEquals(456, Yozio.intForKey("height", 789));
  }

  /**
   * Test stringForKey with initializeExperiments
   */
  public void testStringForKeyWithInitializeExperiments() {
    forceVariation(CONTROL_VARIATION_ID);
    Yozio.initializeExperiments();
    assertEquals("control", Yozio.stringForKey("ooga", "control"));

    forceVariation(VARIATION_A_ID);
    Yozio.initializeExperiments();
    assertEquals("booga", Yozio.stringForKey("ooga", "control"));

    forceVariation(VARIATION_B_ID);
    Yozio.initializeExperiments();
    assertEquals("foobar", Yozio.stringForKey("ooga", "control"));
  }

  /**
   * Test initializeExperimentsAsync
   */
  public void testInitializeExperimentsAsync() throws Throwable {
    forceVariation(VARIATION_A_ID);

    final CountDownLatch signal = new CountDownLatch(1);
    runTestOnUiThread(new Runnable() {
      public void run() {
        // Actual test
        Yozio.initializeExperimentsAsync(new InitializeExperimentsCallback() {
          public void onComplete() {
            assertEquals(123, Yozio.intForKey("height", 789));
            assertEquals("booga", Yozio.stringForKey("ooga", "control"));
            signal.countDown();
          }
        });
      }
    });
    signal.await(20, TimeUnit.SECONDS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // Helper methods
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Forces the a variation for the device running this test
   */
  void forceVariation(String variationId) {
    String queryString = "?device_id=" + this.yozioUdid + "&experiment_id=" + EXPERIMENT_ID +
        "&variation_id=" + variationId;
    doJsonGetRequest(TEST_BASE_URL + "/demo/force_variation/" + queryString);
  }

  /**
   * Asserts that the runnable changes the event type count by diff.
   */
  void assertLoopEventCountChange(int eventType, int diff, Runnable runnable) {
    JSONObject preAppStats = getAppStats();
    runnable.run();
    JSONObject postAppStats = getAppStats();
    assertEquals(
        getLoopEventCount(preAppStats, eventType) + diff,
        getLoopEventCount(postAppStats, eventType));
  }

  /**
   * Gets the appStats from the Yozio website.
   */
  JSONObject getAppStats() {
    String response = doJsonGetRequest(TEST_BASE_URL + "/viral_apps/" + APP_ID);
    try {
      return new JSONObject(response);
    } catch (JSONException e) {
    }
    fail();
    return null;
  }

  /**
   * Parses appStats and returns the event count for the given event type.
   */
  int getLoopEventCount(JSONObject appStats, int eventType) {
    try {
      return appStats
          .getJSONObject("event_counts_per_link")
          .getJSONObject(LINK_ID)
          .getInt(String.valueOf(eventType));
    } catch (JSONException e) {
    }
    fail();
    return -1;
  }

  String doJsonGetRequest(String url) {
    HttpGet httpGet = new HttpGet(url);
    httpGet.setHeader("Accept","application/json");
    return doGetRequest(httpGet, null);
  }

  String doGetRequest(String url) {
    return doGetRequest(new HttpGet(url), null);
  }

  String doGetRequest(String url, String userAgent) {
    HttpGet httpGet = new HttpGet(url);
    httpGet.setHeader("User-Agent", userAgent);
    return doGetRequest(httpGet, userAgent);
  }

  String doGetRequest(HttpGet httpGet, String userAgent) {
    try {
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
   * Logs in to the Yozio website and persists the session in the shared httpClient.
   */
  void login() {
    try {
      HttpPost httpPost = new HttpPost(TEST_BASE_URL + "/login");
      httpPost.setHeader(YozioHelper.H_SDK_VERSION, YozioHelper.YOZIO_SDK_VERSION);
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("email", LOGIN_EMAIL));
      params.add(new BasicNameValuePair("password", LOGIN_PASSWORD));
      httpPost.setEntity(new UrlEncodedFormEntity(params));
      httpClient.execute(httpPost);
    } catch (ClientProtocolException e) {
      Log.e(LOGTAG, "doGetRequest", e);
    } catch (IOException e) {
      Log.e(LOGTAG, "doGetRequest", e);
    }
  }
}
