/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import org.json.JSONObject;

public class FakeYozioApiService implements YozioApiService {

  private JSONObject payload;
  private JSONObject experimentConfigs = new JSONObject();
  private JSONObject experimentVariationSids = new JSONObject();

  public String getUrl(String appKey, String yozioUdid,
      String linkName, String destinationUrl, JSONObject experimentVariationSids) {
    return null;
  }

  public JSONObject getPayload() {
    return payload;
  }

  public boolean batchEvents(JSONObject payload) {
    this.payload = payload;
    return true;
  }

  public void setExperimentConfigs(JSONObject configs) {
    this.experimentConfigs = configs;
  }

  public void setExperimentVariationSids(JSONObject experimentVariationSids) {
    this.experimentVariationSids = experimentVariationSids;
  }

  public ExperimentInfo getExperimentInfo(String appKey, String yozioUdid) {
    return new ExperimentInfo() {
      public JSONObject getConfigs() {
        return experimentConfigs;
      }

      public JSONObject getExperimentVariationSids() {
        return experimentVariationSids;
      }
    };
  }
}