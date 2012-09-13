/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

public class YozioTest extends AndroidTestCase {

  private static final String APP_KEY = "139b0e10-ba56-012f-e1d9-2837371df2a8";
  private static final String LINK_NAME = "testlink";
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


  public void testNotConfigured() {
    // TODO
  }
}
