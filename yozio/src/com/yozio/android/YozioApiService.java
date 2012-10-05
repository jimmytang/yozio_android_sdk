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

  public class ExperimentInfo {

    private final JSONObject configs;
    private final JSONObject experimentVariationSids;

    public ExperimentInfo(JSONObject configs, JSONObject experimentVariationSids) {
      this.configs = configs;
      this.experimentVariationSids = experimentVariationSids;
    }

    /**
     * @return key value pairs for the experiment.
     */
    public JSONObject getConfigs() {
      return configs;
    }

    /**
     * @return map of experimentSid to variationSid.
     */
    public JSONObject getExperimentVariationSids() {
      return experimentVariationSids;
    }
  }

  /**
   * Makes a blocking HTTP request to Yozio to generate the Yozio link specific
   * to the device for the given linkName.
   *
   * @param appKey  the application specific key provided by Yozio.
   * @param yozioUdid  a unique device identifier.
   * @param viralLoopName  Name of the viral loop. Must match the name of one of
   *                       the viral loops created on the Yozio dashboard.
   * @param destinationUrl  URL that the generated Yozio link will redirect to.
   * @param yozioProperties a JSONObject of internal Yozio properties.
   * @param properties  Arbitrary meta data to attach to the generated Yozio link.
   * @return A Yozio link, or null if there is an error generating the Yozio link.
   */
  String getYozioLink(String appKey, String yozioUdid,
      String viralLoopName, String destinationUrl, JSONObject yozioProperties,
      JSONObject externalProperties);

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
