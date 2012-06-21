package com.yozio.android;

import org.json.JSONObject;

/**
 * Service that makes HTTP requests to the Yozio web api.
 */
interface YozioApiService {
  
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
   * @return the shortened URL or null if the request failed.
   */
  String getUrl(String appKey, String yozioUdid, String linkName, String destinationUrl);
  
  /**
   * Makes a non-blocking batch_events HTTP request to Yozio. Repeated calls
   * to this method are guaranteed to execute serially (i.e. the previous
   * async task must finish before the next one will start).
   * 
   * @param payload  the payload object.
   * @param callback  the callback to call when the request is complete.
   */
  void batchEvents(JSONObject payload, ThreadSafeCallback callback);
  
  /**
   * Callback for the non-blocking batch_events HTTP request.
   * 
   * NOTE: implementation must be thread safe.
   */
  interface ThreadSafeCallback {
    void onSuccess();
    void onFailure();
  }
}
