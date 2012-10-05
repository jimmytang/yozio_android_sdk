/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.InstrumentationTestCase;

import com.yozio.android.Yozio.InitializeExperimentsCallback;

public class YozioHelperTest extends InstrumentationTestCase {

  private static final String APP_KEY = "APP KEY";
  private static final String TEST_SECRET_KEY = "test secret key";
  private Context context;
  private FakeYozioApiService fakeApiService;
  private YozioDataStoreImpl dataStore;
  private YozioHelper helper;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    context = getInstrumentation().getContext();
    context.deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    fakeApiService = new FakeYozioApiService();
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(context);
    dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    helper = new YozioHelper(dataStore, fakeApiService);
    helper.configure(context, APP_KEY, TEST_SECRET_KEY);
  }

  public void testInitializeExperimentsAsync() throws Throwable {
    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      fakeApiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    final CountDownLatch signal = new CountDownLatch(1);
    runTestOnUiThread(new Runnable() {
      public void run() {
        helper.initializeExperimentsAsync(new InitializeExperimentsCallback() {
          public void onComplete() {
            assertEquals(123, helper.intForKey("key", 111));
            signal.countDown();
          }
        });
      }
    });
    signal.await(10, TimeUnit.SECONDS);
  }

  public void testIntForKeyBeforeInitializeExperimentsAsyncCompletes() {
    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      fakeApiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperimentsAsync(new InitializeExperimentsCallback() {
      public void onComplete() {}
    });
    assertEquals(111, helper.intForKey("key", 111));
  }

  public void testIntForKeyForExistingKey() {
    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      fakeApiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperiments();
    assertEquals(123, helper.intForKey("key", 111));
  }

  public void testIntForKeyForNonExistingKey() {
    helper.initializeExperiments();
    int defaultValue = 111;
    assertEquals(defaultValue, helper.intForKey("ooga", defaultValue));
  }

  public void testIntForKeyForNonCoercibleType() {
    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "NON_CONVERTIBLE_STRING");
      fakeApiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperiments();
    assertEquals(111, helper.intForKey("key", 111));
  }

  public void testStringForKeyForExistingKey() {
    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      fakeApiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperiments();
    assertEquals("123", helper.stringForKey("key", "booga"));
  }

  public void testStringForKeyForNonExistingKey() {
    helper.initializeExperiments();
    assertEquals("booga", helper.stringForKey("ooga", "booga"));
  }

  public void testGetYozioLinkWithExperimentVariationSids() {
    JSONObject experimentVariationSids = null;
    try {
      experimentVariationSids = new JSONObject().put("123", "456");
      fakeApiService.setExperimentVariationSids(experimentVariationSids);
    } catch (JSONException e) {
      fail();
    }
    helper.initializeExperiments();
    helper.getYozioLink("loop name", "www.ooga.booga");
    try {
      assertEquals(
          experimentVariationSids.toString(),
          fakeApiService.getYozioProperties().get("experiment_variation_sids").toString());
    } catch (JSONException e) {
      fail();
    }
  }

  public void testGetYozioLinkWithoutExperimentVariationSids() {
    helper.initializeExperiments();
    helper.getYozioLink("loop name", "www.ooga.booga");
    assertFalse(fakeApiService.getYozioProperties().has("experiment_variation_sids"));
  }

  public void testGetYozioLinkWithExternalProperties() {
    helper.configure(context, APP_KEY, TEST_SECRET_KEY);
    helper.initializeExperiments();
    try {
      JSONObject externalProperties = new JSONObject("{\"a\": \"b\"}");
      helper.getYozioLink("loop name", "www.ooga.booga", externalProperties);
      assertEquals("b", fakeApiService.getExternalProperties().get("a"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testGetYozioLinkWithoutCallingInitializeExperiments() {
    helper.getYozioLink("loop name", "www.ooga.booga");
    assertEquals("{}", fakeApiService.getYozioProperties().toString());
  }

  public void testCollect() {
    try {
      helper.collect(11, "loop name");
      TestHelper.waitUntilEventSent(dataStore);
      JSONObject payload = fakeApiService.getPayload();

      assertFalse(payload.has("experiment_variation_sids"));
      assertFalse(payload.has("external_user_id"));

      // TODO(kevinliu): add assertions for the rest of the payload params
      assertNotNull(payload.getString("device_id"));
      assertNotNull(payload.getString("connection_type"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testCollectWithExternalProperties() {
    try {
      JSONObject externalProperties = new JSONObject("{\"ooga\": \"booga\"}");
      helper.collect(11, "loop name", externalProperties);
      TestHelper.waitUntilEventSent(dataStore);

      JSONObject payload = fakeApiService.getPayload();
      JSONArray events = payload.getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertEquals(
          event.getJSONObject("external_properties").toString(),
          externalProperties.toString());

    } catch (JSONException e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testCollectWithUserName() {
    try {
      helper.setUserName("spaceman");
      helper.collect(123, "loop name");
      TestHelper.waitUntilEventSent(dataStore);
      JSONObject payload = fakeApiService.getPayload();
      assertEquals("spaceman", payload.getString("external_user_id"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testCollectWithInitializeExperiments() {
    try {
      fakeApiService.setExperimentVariationSids(new JSONObject().put("experiment1", "variation1"));
      helper.initializeExperiments();
      helper.collect(123, "loop name");
      TestHelper.waitUntilEventSent(dataStore);
      JSONObject payload = fakeApiService.getPayload();
      String experimentVariationSids = payload.getString("experiment_variation_sids");
      assertEquals("{\"experiment1\":\"variation1\"}", experimentVariationSids);
    } catch (JSONException e) {
      fail();
    }
  }

  /**
   * YozioHelper test cases - Only 1 flush event should happen - Flush fail,
   * next flush should try to batch the data - Data being added while flushing
   * (less than FLUSH_BATCH_MAX), the flush should not remove new data - Data
   * store exceptional return values (null, -1, etc)
   */
}
