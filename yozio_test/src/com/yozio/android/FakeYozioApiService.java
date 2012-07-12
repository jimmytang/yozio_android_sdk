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

	public String getUrl(String appKey, String yozioUdid, String linkName,
			String destinationUrl) {
		return null;
	}

	public JSONObject getPayload() {
		return payload;
	}

	public boolean batchEvents(JSONObject payload) {
		this.payload = payload;
		return true;
	}
}