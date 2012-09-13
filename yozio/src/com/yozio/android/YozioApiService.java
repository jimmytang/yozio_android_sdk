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

/**
 * Thread safe service that makes HTTP requests to the Yozio web api.
 */
interface YozioApiService {

  interface ExperimentInfo {
    /**
     * @return key value pairs for the experiment.
     */
    JSONObject getConfigs();

    /**
     * @return map of experimentSid to variationSid.
     */
    JSONObject getExperimentVariationSids();
  }

  /**
   * Makes a blocking HTTP request to Yozio to retrieve a shortened URL specific
   * to the device for the given linkName.
   *
   * @param appKey  the application specific key provided by Yozio.
   * @param yozioUdid  a unique device identifier.
   * @param linkName  the name of the tracking link to retrieve the device
   *                  specific shortened URL for. This MUST match one of the
   *                  link names created on the Yozio web UI.
   * @param destinationUrl  a custom destination URL that the returned shortened
   *                        URL should redirect to.
   * @param experimentVariationSids  a JSONObject of experiment to variation sid mappings
   * @return the shortened URL or null if the request failed.
   */
  String getUrl(String appKey, String yozioUdid,
      String linkName, String destinationUrl, JSONObject experimentVariationSids);

  /**
   * Makes a blocking HTTP request to download the experiment configurations
   *
   * @param appKey  the application specific key provided by Yozio.
   * @param yozioUdid  a unique device identifier.
   *
   * @return an {@link ExperimentInfo}.
   */
  ExperimentInfo getExperimentInfo(String appKey, String yozioUdid);


  /**
   * Makes a blocking batch_events HTTP request to Yozio.
   *
   * @param payload  the payload object.
   * @return true iff the request succeeded.
   */
   boolean batchEvents(JSONObject payload);
}
