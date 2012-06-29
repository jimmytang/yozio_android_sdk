package com.yozio.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.yozio.android.YozioDataStore.Events;

class YozioHelper {
  
  // Android device type is 3.
  static final String DEVICE_TYPE = "3";
  
  // For logging.
  private static final String LOGTAG = "YozioHelper";
  
  // Event data keys.
  private static final String D_EVENT_TYPE = "tp";
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
  private static final int FLUSH_BATCH_MAX = 50;
  
  private final YozioDataStore dataStore;
  private final YozioApiService apiService;
  private final SimpleDateFormat dateFormat;
  // Executor for AddEvent and Flush tasks.
  private final ThreadPoolExecutor addAndFlushExecutor;
  
  private Context context;
  private String appKey;
  private String secretKey;
  private String yozioUdid;
  
  YozioHelper(YozioDataStore dataStore, YozioApiService apiService) {
    this.dataStore = dataStore;
    this.apiService = apiService;
    // TODO(jt): do we need timezone in timestamp?
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    addAndFlushExecutor = new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }
  
  /**
   * Configures the helper.
   */
  void configure(Context context, String appKey, String secretKey) {
    this.context = context;
    this.appKey = appKey;
    this.secretKey = secretKey;
    OpenUDID.syncContext(context);
    this.yozioUdid = OpenUDID.getOpenUDIDInContext();
  }
  
  /**
   * Validates the configuration.
   * 
   * @return true iff everything is correctly configured.
   */
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
  
  /**
   * Makes a blocking request to retrieve the shortened URL.
   * 
   * @return the shortened URL if the request was successful,
   *         or the destinationUrl if unsuccessful.
   */
  String getUrl(String linkName, String destinationUrl) {
    String shortenedUrl = apiService.getUrl(appKey, yozioUdid, linkName, destinationUrl);
    return shortenedUrl != null ? shortenedUrl : destinationUrl;
  }
  
  /**
   * Makes a non-blocking request to store the event.
   */
  void collect(int eventType, String linkName) {
    JSONObject event = buildEvent(eventType, linkName);
    if (event == null) {
      return;
    }
    addAndFlushExecutor.submit(new AddEventTask(event));
  }
  
  /**
   * Forces a flush attempt to the Yozio server.
   */
  private void doFlush() {
    addAndFlushExecutor.submit(new FlushTask());
  }
  
  private JSONObject buildEvent(int eventType, String linkName) {
    try {
      JSONObject eventObject = new JSONObject();
      eventObject.put(D_EVENT_TYPE, eventType);
      eventObject.put(D_LINK_NAME, linkName);
      eventObject.put(D_TIMESTAMP, timestamp());
      return eventObject;
    } catch (JSONException e) {
      Log.e(LOGTAG, "buildEvent", e);
      return null;
    }
  }
  
  private String timestamp() {
    return dateFormat.format(Calendar.getInstance().getTime());
  }
  
  /**
   * Task to add an event to the data store.
   * Will try to flush if there are enough events stored.
   */
  private class AddEventTask implements Runnable {
    
    private final JSONObject event;
    
    AddEventTask(JSONObject event) {
      this.event = event;
    }
    
    public void run() {
      boolean eventAdded = dataStore.addEvent(event);
      // Flush if there are enough events.
      // Small optimization for the special case where FLUSH_BATCH_MIN == 1.
      boolean flushEligible =
          (eventAdded && FLUSH_BATCH_MIN == 1) ||
          (dataStore.getNumEvents() >= FLUSH_BATCH_MIN);
      // Don't need to flush if there is at least one other task waiting.
      // The last task will flush all the unflushed events at once.
      if (flushEligible && addAndFlushExecutor.getQueue().isEmpty()) {
        doFlush();
      }
    }
  }
  
  /**
   * Task to flush the data through the {@link YozioApiService}.
   */
  private class FlushTask implements Runnable {
    
    public void run() {
      final Events events = dataStore.getEvents(FLUSH_BATCH_MAX);
      if (events == null) {
        return;
      }
      JSONObject payload = buildPayload(events.getJsonArray());
      if (payload == null) {
        return;
      }
      if (apiService.batchEvents(payload)) {
        dataStore.removeEvents(events.getLastEventId());
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
  }
  
}
