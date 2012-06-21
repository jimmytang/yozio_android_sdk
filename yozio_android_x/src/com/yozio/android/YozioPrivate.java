package com.yozio.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.yozio.android.YozioApiService.ThreadSafeCallback;

class YozioPrivate {
  
  public static final String DEVICE_TYPE = "3";
  
  private static final String LOGTAG = "YozioPrivate";
  
  // Event data keys.
  private static final String D_TYPE = "tp";
  private static final String D_LINK_NAME = "ln";
  private static final String D_TIMESTAMP = "ts";
  
  // Payload keys.
  private static final String P_APP_KEY = "ak";
  private static final String P_UDID = "ud";
  private static final String P_DEVICE_TYPE = "dt";
  private static final String P_PAYLOAD = "pl";
  
  // Minimum number of events before flushing.
  private static final int FLUSH_BATCH_MIN = 1;
  // Maximum number of events that can be batched.
  private static final int FLUSH_BATCH_MAX = 10;
  
  private final YozioDataStore dataStore;
  private final YozioApiService apiService;
  private final SimpleDateFormat dateFormat;
  
  private Context context;
  private String appKey;
  private String secretKey;
  private String yozioUdid;
  
  // Keeps track of the number of times collect is called.
  private int collectCount;
  
  YozioPrivate(YozioDataStore dataStore, YozioApiService apiService) {
    this.dataStore = dataStore;
    this.apiService = apiService;
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.collectCount = 0;
  }
  
  void configure(Context context, String appKey, String secretKey) {
    this.context = context;
    this.appKey = appKey;
    this.secretKey = secretKey;
    OpenUDID.syncContext(context);
    this.yozioUdid = OpenUDID.getOpenUDIDInContext();
  }
  
  String getUrl(String linkName, String destinationUrl, String fallbackUrl) {
    String shortenedUrl = apiService.getUrl(appKey, yozioUdid, linkName, destinationUrl);
    if (shortenedUrl == null) {
      return fallbackUrl;
    }
    return shortenedUrl;
  }
  
  void collect(int eventType, String linkName) {
    collectCount++;
    JSONObject eventObject = buildEvent(eventType, linkName);
    if (eventObject == null) {
      return;
    }
    dataStore.addEvent(eventObject);
    if (collectCount % FLUSH_BATCH_MIN == 0) {
      tryFlush();
    }
  }
  
  boolean validate() {
    if (context == null) {
      Log.e(LOGTAG, "context is null!");
      return false;
    }
    if (appKey == null) {
      Log.e(LOGTAG, "appKey is null!");
      return false;
    }
    if (secretKey == null) {
      Log.e(LOGTAG, "secretKey is null!");
      return false;
    }
    return true;
  }
  
  private void tryFlush() {
    if (dataStore.getNumEvents() >= FLUSH_BATCH_MIN) {
      doFlush();
    }
  }
  
  private void doFlush() {
    final JSONArray events = dataStore.getEvents(FLUSH_BATCH_MAX);
    if (events == null) {
      return;
    }
    JSONObject payload = buildPayload(events);
    if (payload == null) {
      return;
    }
    apiService.batchEvents(payload, new ThreadSafeCallback() {
      public void onSuccess() {
        dataStore.removeEvents(events.length());
      }
      public void onFailure() {
        // Don't do anything if the request fails.
      }
    });
  }
  
  private JSONObject buildEvent(int eventType, String linkName) {
    try {
      JSONObject eventObject = new JSONObject();
      eventObject.put(D_TYPE, eventType);
      eventObject.put(D_LINK_NAME, linkName);
      eventObject.put(D_TIMESTAMP, timestamp());
      return eventObject;
    } catch (JSONException e) {
      Log.e(LOGTAG, "buildEvent", e);
      return null;
    }
  }
  
  private JSONObject buildPayload(JSONArray events) {
    try {
      JSONObject payloadObject = new JSONObject();
      payloadObject.put(P_APP_KEY, appKey);
      payloadObject.put(P_DEVICE_TYPE, DEVICE_TYPE);
      payloadObject.put(P_UDID, yozioUdid);
      payloadObject.put(P_PAYLOAD, events);
      return payloadObject;
    } catch (JSONException e) {
      Log.e(LOGTAG, "buildPayload", e);
      return null;
    }
  }
  
  private String timestamp() {
    return dateFormat.format(Calendar.getInstance().getTime());
  }
}
