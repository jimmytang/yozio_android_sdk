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
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    Yozio.initializeExperiments();
    Yozio.viewedLink(LINK_NAME);
    String deviceId = null;
    String connectionType = null;
    String eventExperimentDetails = null;
    try {
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      deviceId = (String) payload.get("device_id");
      connectionType = (String) payload.get("connection_type");
      eventExperimentDetails = payload.getString("event_experiment_details");
    } catch (Exception e) {
    }
    Assert.assertNotNull(deviceId);
    Assert.assertNotNull(connectionType);
    Assert.assertEquals("{\"experiment1\":\"variation1\"}", eventExperimentDetails);
  }

  public void testLogin() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    Yozio.userLoggedIn("spaceman");
    String userName = null;
    try {
      Thread.sleep(2000);
      JSONObject payload = apiService.getPayload();
      userName = (String) payload.get("external_user_id");
    } catch (Exception e) {
    }
    Assert.assertEquals("spaceman", userName);
  }

  public void testIntForKeyForExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    Yozio.initializeExperiments();
    Assert.assertEquals(123, Yozio.intForKey("key", 111));
  }

  public void testIntForKeyForNonExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    Yozio.initializeExperiments();
    Assert.assertEquals(111, Yozio.intForKey("ooga", 111));
  }

  public void testStringForKeyForExistingKey() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
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
