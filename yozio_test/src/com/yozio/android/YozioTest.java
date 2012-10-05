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

public class YozioTest extends AndroidTestCase {

  private static final String APP_KEY = "139b0e10-ba56-012f-e1d9-2837371df2a8";
  private static final String TEST_SECRET_KEY = "test secret key";
  private FakeYozioApiService apiService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getContext().deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    apiService = new FakeYozioApiService();
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(getContext());
    YozioDataStore dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    YozioHelper helper = new YozioHelper(dataStore, apiService);
    helper.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
    Yozio.setHelper(helper);
  }

  public void testEnteredViralLoopWithoutExternalProperties() {
    Yozio.enteredViralLoop("ooga");
    try {
      Thread.sleep(2000);

      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertFalse(event.has("external_properties"));
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testEnteredViralLoopWithExternalProperties() {
    JSONObject externalProperties;
    try {
      externalProperties = new JSONObject("{\"a\": \"b\"}");
      Yozio.enteredViralLoop("ooga", externalProperties);
      Thread.sleep(2000);

      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertEquals("b", event.getJSONObject("external_properties").get("a"));
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testSharedYozioLinkWithoutExternalProperties() {
    Yozio.sharedYozioLink("ooga");
    try {
      Thread.sleep(2000);

      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertFalse(event.has("external_properties"));
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }

  public void testSharedYozioLinkWithExternalProperties() {
    JSONObject externalProperties;
    try {
      externalProperties = new JSONObject("{\"a\": \"b\"}");
      Yozio.sharedYozioLink("ooga", externalProperties);
      Thread.sleep(2000);

      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertEquals("b", event.getJSONObject("external_properties").get("a"));
    } catch (JSONException e) {
      fail();
    } catch (InterruptedException e) {
      fail();
    }
  }
}
