/*
 * Copyright (C) 2012 Yozio Inc.
 *
 * This file is part of the Yozio SDK.
 *
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.yozio.android.Yozio.GetYozioLinkCallback;
import com.yozio.android.Yozio.InitializeExperimentsCallback;
import com.yozio.android.YozioApiService.ExperimentInfo;
import com.yozio.android.YozioDataStore.Events;

class YozioHelper {

  // SDK version
  protected static final String YOZIO_SDK_VERSION = "ANDROID-v2.3";

  // Android device type is 3.
  static final String DEVICE_TYPE = "3";

  // For logging.
  private static final String LOGTAG = "YozioHelper";

  // Event data keys.
  private static final String D_EVENT_TYPE = "event_type";
  private static final String D_LINK_NAME = "link_name";
  private static final String D_CHANNEL = "channel";
  private static final String D_TIMESTAMP = "timestamp";
  private static final String D_EVENT_IDENTIFIER = "event_identifier";
  private static final String D_EXTERNAL_PROPERTIES = "external_properties";

  // Header keys.
  protected static final String H_SDK_VERSION = "yozio-sdk-version";

  // Payload keys.
  private static final String P_APP_KEY = "app_key";
  private static final String P_APP_VERSION = "app_version";
  private static final String P_COUNTRY_CODE = "country_code";
  private static final String P_DEVICE_TYPE = "device_type";
  private static final String P_HARDWARE = "hardware";
  private static final String P_LANGUAGE_CODE = "language_code";
  private static final String P_MAC_ADDRESS = "mac_address";
  private static final String P_OPEN_UDID = "open_udid";
  private static final String P_OS_VERSION = "os_version";
  private static final String P_PAYLOAD = "payload";
  private static final String P_USER_NAME = "external_user_id";
  private static final String P_YOZIO_UDID = "yozio_udid";

  private static final String P_ANDROID_ID = "android_id";
  private static final String P_DEVICE_ID = "device_id";
  private static final String P_DEVICE_MANUFACTURER = "device_manufacturer";
  private static final String P_SERIAL_ID = "serial_id";
  private static final String P_DEVICE_SCREEN_DENSITY = "device_screen_density";
  private static final String P_DEVICE_SCREEN_LAYOUT_SIZE = "device_screen_layout_size";
  private static final String P_CARRIER_NAME = "carrier_name";
  private static final String P_CARRIER_COUNTRY_CODE = "device_model";
  private static final String P_MOBILE_COUNTRY_CODE = "mobile_country_code";
  private static final String P_MOBILE_NETWORK_CODE = "mobile_network_code";
  private static final String P_CONNECTION_TYPE = "connection_type";

  private static final String P_EXPERIMENT_VARIATION_SIDS = "experiment_variation_sids";

  // Minimum number of events before flushing.
  private static final int FLUSH_BATCH_MIN = 1;
  // Maximum number of events that can be batched.
  private static final int FLUSH_BATCH_MAX = 50;

  private final YozioDataStore dataStore;
  private final YozioApiService apiService;
  private final SimpleDateFormat dateFormat;
  // Executor for AddEvent and Flush tasks.
  private final ThreadPoolExecutor executor;

  private JSONObject experimentConfigs;
  private JSONObject experimentVariationSids;

  private Context context;
  private String appKey;
  private String secretKey;
  private String userName;

  private String appVersion;
  private String countryCode;
  private String hardware;
  private String languageCode;
  private String macAddress;
  private String openUdid;
  private String osVersion;
  private String yozioUdid;

  private String androidId;
  private String deviceId;
  private String deviceManufacturer;
  private String serialId;
  private String deviceScreenDensity;
  private String deviceScreenLayoutSize;
  private String carrierName;
  private String carrierCountryCode;
  private String mobileCountryCode;
  private String mobileNetworkCode;
  private String connectionType;

  YozioHelper(YozioDataStore dataStore, YozioApiService apiService) {
    this.dataStore = dataStore;
    this.apiService = apiService;
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    // This executor MUST have pool size of 1 (1 task allowed to run at once).
    // Otherwise, we might send the same event multiple times.
    executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
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
    setDeviceParams();
  }

  /**
   * Fetches experiment configurations Makes a blocking request
   */
  void initializeExperiments() {
    printYozioUdid();
    ExperimentInfo experimentInfo = apiService.getExperimentInfo(appKey, yozioUdid);
    this.experimentConfigs = experimentInfo.getConfigs();
    this.experimentVariationSids = experimentInfo.getExperimentVariationSids();
  }

  /**
   * Makes a non-blocking request to retrieve the experiment configurations.
   */
  void initializeExperimentsAsync(InitializeExperimentsCallback callback) {
    printYozioUdid();
    new InitializeExperimentsTask(callback).execute();
  }

  /**
   * Returns an experiment configuration String for the given key
   */
  String stringForKey(String key, String defaultValue) {
    try {
      return this.experimentConfigs.getString(key);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Returns an experiment configuration int for the given key
   */
  int intForKey(String key, int defaultValue) {
    try {
      return this.experimentConfigs.getInt(key);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Sets the user name.
   */
  void setUserName(String userName) {
    this.userName = userName;
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
   * Makes a blocking request to retrieve the Yozio link.
   *
   * @param externalProperties  meta-data Customer wants to attach to url
   * @return the Yozio link if the request was successful, or the destinationUrl if unsuccessful.
   */
  String getYozioLink(String viralLoopName, String channel, String destinationUrl,
      JSONObject externalProperties) {
    String yozioLink = apiService.getYozioLink(appKey, yozioUdid, viralLoopName, destinationUrl,
        getYozioProperties(channel), externalProperties);
    return yozioLink != null ? yozioLink : destinationUrl;
  }

  /**
   * Makes a blocking request to retrieve the Yozio link.
   *
   * @param externalProperties  meta-data Customer wants to attach to url
   * @return the Yozio link if the request was successful, or the destinationUrl if unsuccessful.
   */
  String getYozioLink(String viralLoopName, String channel, String iosDestinationUrl,
      String androidDestinationUrl, String nonMobileDestinationUrl, JSONObject externalProperties) {
    String yozioLink = apiService.getYozioLink(appKey, yozioUdid, viralLoopName, iosDestinationUrl,
        androidDestinationUrl, nonMobileDestinationUrl, getYozioProperties(channel),
        externalProperties);
    return yozioLink != null ? yozioLink : nonMobileDestinationUrl;
  }

  /**
   * Makes a non-blocking request to retrieve the Yozio link.
   */
  void getYozioLinkAsync(String viralLoopName, String channel, String destinationUrl,
      JSONObject externalProperties, GetYozioLinkCallback callback) {
    JSONObject yozioProperties = getYozioProperties(channel);
    new GetYozioLinkTask(viralLoopName, destinationUrl, yozioProperties, externalProperties,
        callback).execute();
  }

  /**
   * Makes a non-blocking request to retrieve the Yozio link.
   */
  void getYozioLinkAsync(String viralLoopName, String channel, String iosDestinationUrl,
      String androidDestinationUrl, String nonMobileDestinationUrl, JSONObject externalProperties,
      GetYozioLinkCallback callback) {
    JSONObject yozioProperties = getYozioProperties(channel);
    new GetYozioLinkTask(viralLoopName, iosDestinationUrl, androidDestinationUrl,
        nonMobileDestinationUrl, yozioProperties, externalProperties, callback).execute();
  }

  /**
   * Makes a non-blocking request to store the event.
   */
  void collect(int eventType, String viralLoopName, String channel) {
    collect(eventType, viralLoopName, channel, null);
  }

  /**
   * Makes a non-blocking request to store the event.
   */
  void collect(int eventType, String viralLoopName, String channel, JSONObject externalProperties) {
    JSONObject event = buildEvent(eventType, viralLoopName, channel, externalProperties);
    if (event == null) {
      return;
    }
    executor.submit(new AddEventTask(event));
  }

  // For testing
  void setYozioUdid(String yozioUdid) {
    this.yozioUdid = yozioUdid;
  }

  // For testing
  String getYozioUdid() {
    return yozioUdid;
  }

  /**
   * Forces a flush attempt to the Yozio server.
   */
  private void doFlush() {
    executor.submit(new FlushTask());
  }

  private JSONObject getYozioProperties(String channel) {
    JSONObject yozioProperties = new JSONObject();
    try {
      // null values are discarded by JSONObject
      yozioProperties.put("experiment_variation_sids", experimentVariationSids);
      yozioProperties.put(D_CHANNEL, channel);
    } catch (JSONException e) {
    }
    return yozioProperties;
  }

  private JSONObject buildEvent(int eventType, String viralLoopName, String channel,
      JSONObject externalProperties) {
    try {
      JSONObject eventObject = new JSONObject();
      eventObject.put(D_EVENT_TYPE, eventType);
      eventObject.put(D_LINK_NAME, viralLoopName);
      eventObject.put(D_CHANNEL, channel);
      eventObject.put(D_TIMESTAMP, timestamp());
      eventObject.put(D_EVENT_IDENTIFIER, UUID.randomUUID());
      // null values are discarded by JSONObject
      eventObject.put(D_EXTERNAL_PROPERTIES, externalProperties);
      return eventObject;
    } catch (JSONException e) {
      return null;
    }
  }

  private String timestamp() {
    return dateFormat.format(Calendar.getInstance().getTime());
  }

  private void setDeviceParams() {
    androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    countryCode = Locale.getDefault().getCountry();
    hardware = android.os.Build.MODEL;
    languageCode = Locale.getDefault().getLanguage();
    openUdid = OpenUDID.getOpenUDIDInContext();
    osVersion = android.os.Build.VERSION.RELEASE;
    deviceManufacturer = android.os.Build.MANUFACTURER;
    setAppVersion();
    setConnectionType();
    setCarrierMobileAndDeviceInfo();
    setMacAddress();
    setScreenInfo();
  }

  private void setAppVersion() {
    try {
      PackageManager manager = context.getPackageManager();
      PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
      appVersion = packageInfo.versionName;
    } catch (Exception e) {
    }
  }

  private void printYozioUdid() {
    // Like clutch.io, we print the Yozio device id to LogCat so developers can force experiment
    // variations in the UI.
    Log.i("Yozio",
        "Yozio Device Identifier (To force an experiment variation): \"" + yozioUdid + "\"");
  }

  private boolean isValidDeviceId(String deviceId) {
    // ----------------------------------------
    // Is the device ID null or empty?
    // ----------------------------------------
    if (deviceId == null) {
      return false;
    }
    // ----------------------------------------
    // Is this an emulator device ID?
    // ----------------------------------------
    else if (deviceId.length() == 0 || deviceId.equals("000000000000000") || deviceId.equals("0")
            || deviceId.equals("unknown")) {
      return false;
    } else {
      return true;
    }
  }

  private void setCarrierMobileAndDeviceInfo() {
    SharedPreferences settings = context.getSharedPreferences("yozioPreferences", 0);

    try {
      TelephonyManager telephonyManager = (TelephonyManager) context
              .getSystemService(Context.TELEPHONY_SERVICE);
      carrierName = telephonyManager.getNetworkOperatorName();
      carrierCountryCode = telephonyManager.getNetworkCountryIso();

      if (telephonyManager.getNetworkOperator() != null
              && (telephonyManager.getNetworkOperator().length() == 5 || telephonyManager
                      .getNetworkOperator().length() == 6)) {
        mobileCountryCode = telephonyManager.getNetworkOperator().substring(0, 3);
        mobileNetworkCode = telephonyManager.getNetworkOperator().substring(3);
      }

      deviceId = telephonyManager.getDeviceId();

      if (!isValidDeviceId(deviceId)) {
        // Fetch the emulator device ID from the preferences
        deviceId = settings.getString("emulatorDeviceId", null);
      }

      if (!isValidDeviceId(deviceId)) {
        StringBuffer buff = new StringBuffer();
        buff.append("emulator");

        String chars = "1234567890abcdefghijklmnopqrstuvw";
        int ccLength = chars.length() - 1;

        for (int i = 0; i < 32; i++) {
          int index = (int) (Math.random() * ccLength);
          buff.append(chars.charAt(index));
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("emulatorDeviceId", deviceId);
        editor.commit();

        deviceId = buff.toString();
      }

      deviceId = deviceId.toLowerCase();
    } catch (Exception e) {
      deviceId = null;
    }
  }

  /**
   * Gets the connection type used by this device ("mobile" or "wifi").
   *
   * @return Connection type the device is using.
   */
  private void setConnectionType() {
    try {
      // Get connection type
      ConnectivityManager connectivityManager = (ConnectivityManager) context
              .getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
        switch (connectivityManager.getActiveNetworkInfo().getType()) {
        case ConnectivityManager.TYPE_WIFI:
        case 0x6: // ConnectivityManager.TYPE_WIMAX
          connectionType = "wifi";
          break;
        default:
          connectionType = "mobile";
          break;
        }
      }
    } catch (Exception e) {
    }
  }

  // Get screen density and layout
  private void setScreenInfo() {
    try {
      // This is a backwards compatibility fix for Android 1.5 which has no
      // display metric API.
      // If this is 1.6 or higher, then load the class, otherwise the class
      // never loads and
      // no crash occurs.
      if (Integer.parseInt(android.os.Build.VERSION.SDK) > 3) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Configuration configuration = context.getResources().getConfiguration();

        deviceScreenDensity = "" + displayMetrics.densityDpi;
        deviceScreenLayoutSize = ""
                + (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
      }
    } catch (Exception e) {
    }
  }

  private void setMacAddress() {
    try {
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

      if (wifiManager != null) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo != null) {
          macAddress = wifiInfo.getMacAddress();
        }
      }
    } catch (Exception e) {
    }
  }

  private class InitializeExperimentsTask extends AsyncTask<Void, Void, ExperimentInfo> {

    private final InitializeExperimentsCallback callback;

    InitializeExperimentsTask(InitializeExperimentsCallback callback) {
      this.callback = callback;
    }

    @Override
    protected ExperimentInfo doInBackground(Void... params) {
      return apiService.getExperimentInfo(appKey, yozioUdid);
    }

    @Override
    protected void onPostExecute(ExperimentInfo experimentInfo) {
      experimentConfigs = experimentInfo.getConfigs();
      experimentVariationSids = experimentInfo.getExperimentVariationSids();
      callback.onComplete();
    }
  }

  private class GetYozioLinkTask extends AsyncTask<Void, Void, String> {

    private final String viralLoopName;
    private final String destinationUrl;
    private final String iosDestinationUrl;
    private final String androidDestinationUrl;
    private final String nonMobileDestinationUrl;
    private final JSONObject externalProperties;
    private final JSONObject yozioProperties;
    private final GetYozioLinkCallback callback;

    GetYozioLinkTask(String viralLoopName, String destinationUrl, JSONObject yozioProperties,
        JSONObject externalProperties, GetYozioLinkCallback callback) {
      this.viralLoopName = viralLoopName;
      this.iosDestinationUrl = null;
      this.androidDestinationUrl = null;
      this.nonMobileDestinationUrl = null;
      this.destinationUrl = destinationUrl;
      this.externalProperties = externalProperties;
      this.yozioProperties = yozioProperties;
      this.callback = callback;
    }

    GetYozioLinkTask(String viralLoopName, String iosDestinationUrl, String androidDestinationUrl,
        String nonMobileDestinationUrl, JSONObject yozioProperties, JSONObject externalProperties,
        GetYozioLinkCallback callback) {
      this.viralLoopName = viralLoopName;
      this.iosDestinationUrl = iosDestinationUrl;
      this.androidDestinationUrl = androidDestinationUrl;
      this.nonMobileDestinationUrl = nonMobileDestinationUrl;
      this.destinationUrl = null;
      this.externalProperties = externalProperties;
      this.yozioProperties = yozioProperties;
      this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... arg0) {
      if (destinationUrl != null) {
        return apiService.getYozioLink(
            appKey, yozioUdid, viralLoopName, destinationUrl, yozioProperties, externalProperties);
      } else {
        return apiService.getYozioLink(
            appKey, yozioUdid, viralLoopName, iosDestinationUrl, androidDestinationUrl,
            nonMobileDestinationUrl, yozioProperties, externalProperties);
      }
    }

    @Override
    protected void onPostExecute(String yozioLink) {
      callback.handleResponse(yozioLink != null ? yozioLink : destinationUrl);
    }
  }

  /**
   * Task to add an event to the data store. Will try to flush if there are
   * enough events stored.
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
      boolean flushEligible = (eventAdded && FLUSH_BATCH_MIN == 1)
              || (dataStore.getNumEvents() >= FLUSH_BATCH_MIN);
      if (flushEligible) {
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
        payloadObject.put(P_USER_NAME, userName);
        payloadObject.put(P_DEVICE_TYPE, DEVICE_TYPE);
        payloadObject.put(P_YOZIO_UDID, yozioUdid);
        payloadObject.put(P_PAYLOAD, events);

        payloadObject.put(P_APP_VERSION, appVersion);
        payloadObject.put(P_COUNTRY_CODE, countryCode);
        payloadObject.put(P_HARDWARE, hardware);
        payloadObject.put(P_LANGUAGE_CODE, languageCode);
        payloadObject.put(P_MAC_ADDRESS, macAddress);
        payloadObject.put(P_OPEN_UDID, openUdid);
        payloadObject.put(P_OS_VERSION, osVersion);

        payloadObject.put(P_ANDROID_ID, androidId);
        payloadObject.put(P_DEVICE_ID, deviceId);
        payloadObject.put(P_DEVICE_MANUFACTURER, deviceManufacturer);
        payloadObject.put(P_SERIAL_ID, serialId);
        payloadObject.put(P_DEVICE_SCREEN_DENSITY, deviceScreenDensity);
        payloadObject.put(P_DEVICE_SCREEN_LAYOUT_SIZE, deviceScreenLayoutSize);
        payloadObject.put(P_CARRIER_NAME, carrierName);
        payloadObject.put(P_CARRIER_COUNTRY_CODE, carrierCountryCode);
        payloadObject.put(P_MOBILE_COUNTRY_CODE, mobileCountryCode);
        payloadObject.put(P_MOBILE_NETWORK_CODE, mobileNetworkCode);
        payloadObject.put(P_CONNECTION_TYPE, connectionType);

        // null values are discarded by JSONObject
        payloadObject.put(P_EXPERIMENT_VARIATION_SIDS, experimentVariationSids);

        return payloadObject;
      } catch (JSONException e) {
        return null;
      }
    }
  }
}
