/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

import com.yozio.android.Yozio.InitializeExperimentsCallback;

public class YozioHelperTest extends AndroidTestCase {

  private static final String APP_KEY = "APP KEY";
  private static final String TEST_SECRET_KEY = "test secret key";
  private FakeYozioApiService apiService;
  private YozioHelper helper;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getContext().deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    apiService = new FakeYozioApiService();
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(getContext());
    YozioDataStore dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    helper = new YozioHelper(dataStore, apiService);
  }

  public void testInitializeExperimentsAsync() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    try {
      helper.initializeExperimentsAsync(new InitializeExperimentsCallback() {
        public void onComplete() {}
      });
      Thread.sleep(2000);
      assertEquals(123, helper.intForKey("key", 111));
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testIntForKeyBeforeInitializeExperimentsAsyncCompletes() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperimentsAsync(new InitializeExperimentsCallback() {
      public void onComplete() {}
    });
    assertEquals(111, helper.intForKey("key", 111));
  }

  public void testIntForKeyForExistingKey() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperiments();
    assertEquals(123, helper.intForKey("key", 111));
  }

  public void testIntForKeyForNonExistingKey() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    helper.initializeExperiments();
    int defaultValue = 111;
    assertEquals(defaultValue, helper.intForKey("ooga", defaultValue));
  }

  public void testIntForKeyForNonCoercibleType() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "NON_CONVERTIBLE_STRING");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperiments();
    assertEquals(111, helper.intForKey("key", 111));
  }

  public void testStringForKeyForExistingKey() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    helper.initializeExperiments();
    assertEquals("123", helper.stringForKey("key", "booga"));
  }

  public void testStringForKeyForNonExistingKey() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    helper.initializeExperiments();
    assertEquals("booga", helper.stringForKey("ooga", "booga"));
  }

  public void testGetUrlWithExperimentVariationSids() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    JSONObject experimentVariationSids = null;
    try {
      experimentVariationSids = new JSONObject().put("123", "456");
      apiService.setExperimentVariationSids(experimentVariationSids);
    } catch (JSONException e) {
      fail();
    }
    helper.initializeExperiments();
    helper.getUrl("link name", "www.ooga.booga");
    try {
      assertEquals(
          experimentVariationSids.toString(),
          apiService.getYozioProperties().get("experiment_variation_sids").toString());
    } catch (JSONException e) {
      fail();
    }
  }

  public void testGetUrlWithoutExperimentVariationSids() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    helper.initializeExperiments();
    helper.getUrl("link name", "www.ooga.booga");
    assertFalse(apiService.getYozioProperties().has("experiment_variation_sids"));
  }

  public void testGetUrlWithExternalProperties() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    helper.initializeExperiments();
    try {
      JSONObject externalProperties = new JSONObject("{\"a\": \"b\"}");
      helper.getUrl("link name", "www.ooga.booga", externalProperties);
      assertEquals("b", apiService.getExternalProperties().get("a"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testGetUrlWithoutCallingInitializeExperiments() {
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    helper.getUrl("link name", "www.ooga.booga");
    assertEquals("{}", apiService.getYozioProperties().toString());
  }

  public void testCollect() {
    try {
      helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      helper.collect(11, "Link Name");
      // TODO(dounanshi): restructure to not need sleep
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();

      assertFalse(payload.has("experiment_variation_sids"));
      assertFalse(payload.has("external_user_id"));

      // TODO(kevinliu): add assertions for the rest of the payload params
      assertNotNull(payload.getString("device_id"));
      assertNotNull(payload.getString("connection_type"));
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testCollectWithExternalProperties() {
    try {
      helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      JSONObject externalProperties = new JSONObject("{\"ooga\": \"booga\"}");

      helper.collect(11, "Link Name", externalProperties);
      // TODO(dounanshi): restructure to not need sleep
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      JSONArray events = payload.getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertEquals(
          event.getJSONObject("external_properties").toString(),
          externalProperties.toString());

    } catch (JSONException e) {
      e.printStackTrace();
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testCollectWithUserName() {
    try {
      helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      helper.setUserName("spaceman");
      helper.collect(123, "Link Name");

      // TODO(dounanshi): restructure to not need sleep
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      assertEquals("spaceman", payload.getString("external_user_id"));
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testCollectWithInitializeExperiments() {
    try {
      helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      apiService.setExperimentVariationSids(new JSONObject().put("experiment1", "variation1"));
      helper.initializeExperiments();
      helper.collect(123, "Link Name");
      // TODO(dounanshi): restructure to not need sleep
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      String experimentVariationSids = payload.getString("experiment_variation_sids");
      assertEquals("{\"experiment1\":\"variation1\"}", experimentVariationSids);
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
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
