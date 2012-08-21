/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class FakeYozioApiService implements YozioApiService {

  private JSONObject payload;
  private ArrayList<JSONObject> configs = new ArrayList<JSONObject>();

  public String getUrl(String appKey, String yozioUdid, String linkName, String destinationUrl) {
    return null;
  }

  public JSONObject getPayload() {
    return payload;
  }

  public boolean batchEvents(JSONObject payload) {
    this.payload = payload;
    return true;
  }

  public ArrayList<JSONObject> getExperimentConfigs(String appKey, String yozioUdid) {
    try {
      this.configs.add(new JSONObject().put("key", "123"));
      this.configs.add(new JSONObject().put("experiment1", "variation1"));
      return this.configs;
    } catch (JSONException e) {
    }
    return null;
  }
}