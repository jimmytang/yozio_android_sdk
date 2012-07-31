/*
 * Copyright (C) 2012 Yozio Inc.
 * 
 * This file is part of the Yozio SDK.
 * 
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import org.apache.http.client.HttpClient;

import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.util.Log;

import com.yozio.android.Yozio.GetUrlCallback;

public class YozioTest extends AndroidTestCase {
	
	private static final String TEST_BASE_URL = "http://10.0.2.2:3000";
	private static final String APP_KEY = "139b0e10-ba56-012f-e1d9-2837371df2a8";
	private static final String LINK_NAME = "testlink";
	
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getContext().deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    HttpClient httpClient = Yozio.threadSafeHttpClient();
    YozioApiServiceImpl apiService = new YozioApiServiceImpl(httpClient);
    apiService.setBaseUrl(TEST_BASE_URL);
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(getContext());
    YozioDataStore dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    YozioHelper helper = new YozioHelper(dataStore, apiService);
    Yozio.setHelper(helper);
  }
  
  public void testEndToEnd() {
    Yozio.configure(getContext(), APP_KEY, "test secret key");
    String url = Yozio.getUrl(LINK_NAME, "www.google.com");
    Yozio.getUrlAsync(LINK_NAME, "www.yahoo.com", new GetUrlCallback() {
			public void handleResponse(String url) {
				Log.i("YozioTest", "getUrlAsync(): " + url);
			}
		});
    Yozio.userLoggedIn("spaceman");
    Log.i("YozioTest", "getUrl(): " + url);
    Yozio.viewedLink(LINK_NAME);
    Yozio.sharedLink(LINK_NAME);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Log.e("YozioTest", "testEndToEnd", e);
    }
  }
  
  public void testNotConfigured() {
    // TODO
  }
}
