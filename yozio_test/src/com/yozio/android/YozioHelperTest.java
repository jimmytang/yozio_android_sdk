/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

public class YozioHelperTest extends AndroidTestCase {

  private static final String APP_KEY = "APP KEY";
  private static final String LINK_NAME = "babboon";
  private FakeYozioApiService apiService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getContext().deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    apiService = new FakeYozioApiService();
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(getContext());
    YozioDataStore dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    YozioHelper helper = new YozioHelper(dataStore, apiService);
    Yozio.setHelper(helper);
  }

  public void testParams() {
    try {
      Yozio.configure(getContext(), APP_KEY, "test secret key");
      apiService.setExperimentVariationSids(new JSONObject().put("experiment1", "variation1"));

      Yozio.initializeExperiments();
      Yozio.viewedLink(LINK_NAME);
      // TODO(dounanshi): restructure to not need sleep
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      String deviceId = payload.getString("device_id");
      String connectionType = payload.getString("connection_type");
      String experimentVariationIds = payload.getString("experiment_variation_ids");
      Assert.assertNotNull(deviceId);
      Assert.assertNotNull(connectionType);
      Assert.assertEquals("{\"experiment1\":\"variation1\"}", experimentVariationIds);
    } catch (Exception e) {
      fail();
    }
  }

  public void testLogin() {
    try {
      Yozio.configure(getContext(), APP_KEY, "test secret key");
      Yozio.userLoggedIn("spaceman");
      // TODO(dounanshi): restructure to not need sleep
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      String userName = payload.getString("external_user_id");
      Assert.assertEquals("spaceman", userName);
    } catch (Exception e) {
      fail();
    }
  }

  public void testIntForKeyForExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    Yozio.initializeExperiments();
    Assert.assertEquals(123, Yozio.intForKey("key", 111));
  }

  public void testIntForKeyForNonExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    Yozio.initializeExperiments();
    Assert.assertEquals(111, Yozio.intForKey("ooga", 111));
  }

  public void testIntForKeyForNonCoercibleType() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "NON_CONVERTIBLE_STRING");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    Yozio.initializeExperiments();
    Assert.assertEquals(111, Yozio.intForKey("key", 111));
  }

  public void testStringForKeyForExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");

    JSONObject configs = null;
    try {
      configs = new JSONObject().put("key", "123");
      apiService.setExperimentConfigs(configs);
    } catch (JSONException e) {
      fail();
    }

    Yozio.initializeExperiments();
    Assert.assertEquals("123", Yozio.stringForKey("key", "booga"));
  }

  public void testStringForKeyForNonExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    Yozio.initializeExperiments();
    Assert.assertEquals("booga", Yozio.stringForKey("ooga", "booga"));
  }

  /**
   * YozioHelper test cases - Only 1 flush event should happen - Flush fail,
   * next flush should try to batch the data - Data being added while flushing
   * (less than FLUSH_BATCH_MAX), the flush should not remove new data - Data
   * store exceptional return values (null, -1, etc)
   */
}
