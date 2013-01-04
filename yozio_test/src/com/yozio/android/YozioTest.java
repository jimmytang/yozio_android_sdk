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

import com.yozio.android.Yozio.GetYozioLinkCallback;
import com.yozio.android.Yozio.InitializeExperimentsCallback;

public class YozioTest extends AndroidTestCase {

  private static final String APP_KEY = "139b0e10-ba56-012f-e1d9-2837371df2a8";
  private static final String TEST_SECRET_KEY = "test secret key";
  private static final String FB_CHANNEL = "facebook";
  private FakeYozioApiService apiService;
  private YozioDataStoreImpl dataStore;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getContext().deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    apiService = new FakeYozioApiService();
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(getContext());
    dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    YozioHelper helper = new YozioHelper(dataStore, apiService);
    Yozio.setHelper(helper);
  }

  public void testNullPointersWithoutConfigure() {
    // Nothing should throw an exception.

    Yozio.enteredViralLoop(null, null);
    Yozio.enteredViralLoop(null, null, null);

    assertNull(Yozio.getYozioLink(null, null, null));
    assertNull(Yozio.getYozioLink(null, null, null, null));
    assertNull(Yozio.getYozioLink(null, null, null, null, null));
    assertNull(Yozio.getYozioLink(null, null, null, null, null, null));

    GetYozioLinkCallback getCallback = new GetYozioLinkCallback() {
      public void handleResponse(String yozioLink) {
      }
    };
    Yozio.getYozioLinkAsync(null, null, null, getCallback);
    Yozio.getYozioLinkAsync(null, null, null, null, getCallback);
    Yozio.getYozioLinkAsync(null, null, null, null, null, getCallback);
    Yozio.getYozioLinkAsync(null, null, null, null, null, null, getCallback);

    InitializeExperimentsCallback initCallback = new InitializeExperimentsCallback() {
      public void onComplete() {
      }
    };
    Yozio.initializeExperiments();
    Yozio.initializeExperimentsAsync(initCallback);

    assertEquals(0, Yozio.intForKey(null, 0));
    assertNull(Yozio.stringForKey(null, null));

    Yozio.sharedYozioLink(null, null);
    Yozio.sharedYozioLink(null, null, null);

    Yozio.userLoggedIn(null);
    Yozio.userLoggedIn(null, null);
  }

  public void testEnteredViralLoopWithoutExternalProperties() {
    try {
      Yozio.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      TestHelper.waitUntilEventSent(dataStore);
      Yozio.enteredViralLoop("ooga", FB_CHANNEL);
      TestHelper.waitUntilEventSent(dataStore);
      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertFalse(event.has("external_properties"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testEnteredViralLoopWithExternalProperties() {
    try {
      Yozio.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      TestHelper.waitUntilEventSent(dataStore);
      JSONObject externalProperties = new JSONObject("{\"a\": \"b\"}");
      Yozio.enteredViralLoop("ooga", FB_CHANNEL, externalProperties);
      TestHelper.waitUntilEventSent(dataStore);
      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertEquals("b", event.getJSONObject("external_properties").get("a"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testSharedYozioLinkWithoutExternalProperties() {
    try {
      Yozio.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      TestHelper.waitUntilEventSent(dataStore);
      Yozio.sharedYozioLink("ooga", FB_CHANNEL);
      TestHelper.waitUntilEventSent(dataStore);
      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertFalse(event.has("external_properties"));
    } catch (JSONException e) {
      fail();
    }
  }

  public void testSharedYozioLinkWithExternalProperties() {
    JSONObject externalProperties;
    try {
      Yozio.configure(getContext(), APP_KEY, TEST_SECRET_KEY);
      TestHelper.waitUntilEventSent(dataStore);
      externalProperties = new JSONObject("{\"a\": \"b\"}");
      Yozio.sharedYozioLink("ooga", FB_CHANNEL, externalProperties);
      TestHelper.waitUntilEventSent(dataStore);
      JSONArray events = apiService.getPayload().getJSONArray("payload");
      JSONObject event = events.getJSONObject(0);
      assertEquals("b", event.getJSONObject("external_properties").get("a"));
    } catch (JSONException e) {
      fail();
    }
  }
}
