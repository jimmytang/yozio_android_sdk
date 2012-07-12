/*
 * Copyright (C) 2012 Yozio Inc.
 * 
 * This file is part of the Yozio SDK.
 * 
 * By using the Yozio SDK in your software, you agree to the terms of the
 * Yozio SDK License Agreement which can be found at www.yozio.com/sdk_license.
 */

package com.yozio.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.util.Log;

import com.yozio.android.YozioDataStore.Events;
import com.yozio.android.YozioDataStoreImpl.DatabaseHelper;

public class YozioDataStoreImplTest extends AndroidTestCase {
  
  private static final String LOGTAG = "YozioDataStoreImplTest";
  private static final String APP_KEY = "test app key";
  private static final String EVENT_KEY = "event key";
  
  private DatabaseHelper databaseHelper;
  private YozioDataStoreImpl dataStore;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getContext().deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    databaseHelper = new DatabaseHelper(getContext());
    dataStore = new YozioDataStoreImpl(databaseHelper, APP_KEY);
  }
  
  public void testAddEvent() {
    JSONObject event1 = buildTestEvent("event 1");
    JSONObject event2 = buildTestEvent("event 2");
    JSONObject event3 = buildTestEvent("event 3");
    
    dataStore.addEvent(event1);
    assertEquals(1, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event1);
    
    dataStore.addEvent(event2);
    assertEquals(2, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event1, event2);
    
    dataStore.addEvent(event3);
    assertEquals(3, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event1, event2, event3);
  }
  
  public void testGetEventWithLimit() {
    JSONObject event1 = buildTestEvent("event 1");
    JSONObject event2 = buildTestEvent("event 2");
    JSONObject event3 = buildTestEvent("event 3");
    dataStore.addEvent(event1);
    dataStore.addEvent(event2);
    dataStore.addEvent(event3);
    assertNull(dataStore.getEvents(0));
    assertEventsEqual(dataStore.getEvents(1).getJsonArray(), event1);
    assertEventsEqual(dataStore.getEvents(2).getJsonArray(), event1, event2);
    assertEventsEqual(dataStore.getEvents(3).getJsonArray(), event1, event2, event3);
    assertEventsEqual(dataStore.getEvents(4).getJsonArray(), event1, event2, event3);
  }
  
  public void testGetEventsWithNoEvents() {
    assertEquals(0, dataStore.getNumEvents());
    assertNull(dataStore.getEvents(10));
  }
  
  public void testRemoveEvents() {
    JSONObject event1 = buildTestEvent("event 1");
    JSONObject event2 = buildTestEvent("event 2");
    JSONObject event3 = buildTestEvent("event 3");
    dataStore.addEvent(event1);
    dataStore.addEvent(event2);
    dataStore.addEvent(event3);
    assertEquals(3, dataStore.getNumEvents());
    Events events = dataStore.getEvents(10);
    dataStore.removeEvents(events.getLastEventId());
    assertEquals(0, dataStore.getNumEvents());
  }
  
  public void testRemoveEventsWithLimit() {
    JSONObject event1 = buildTestEvent("event 1");
    JSONObject event2 = buildTestEvent("event 2");
    JSONObject event3 = buildTestEvent("event 3");
    dataStore.addEvent(event1);
    dataStore.addEvent(event2);
    dataStore.addEvent(event3);
    assertEquals(3, dataStore.getNumEvents());
    Events events = dataStore.getEvents(2);
    dataStore.removeEvents(events.getLastEventId());
    assertEquals(1, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event3);
  }
  
  public void testRemoveEventsWithNoEvents() {
    assertEquals(0, dataStore.getNumEvents());
    dataStore.removeEvents("10");
    assertEquals(0, dataStore.getNumEvents());
  }
  
  public void testAddAndRemoveEvents() {
    JSONObject event1 = buildTestEvent("event 1");
    JSONObject event2 = buildTestEvent("event 2");
    JSONObject event3 = buildTestEvent("event 3");
    JSONObject event4 = buildTestEvent("event 4");
    JSONObject event5 = buildTestEvent("event 5");
    dataStore.addEvent(event1);
    dataStore.addEvent(event2);
    dataStore.addEvent(event3);
    assertEquals(3, dataStore.getNumEvents());
    
    Events events = dataStore.getEvents(1);
    dataStore.removeEvents(events.getLastEventId());
    assertEquals(2, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event2, event3);
    
    dataStore.addEvent(event4);
    events = dataStore.getEvents(1);
    dataStore.removeEvents(events.getLastEventId());
    assertEquals(2, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event3, event4);
    
    dataStore.addEvent(event5);
    events = dataStore.getEvents(2);
    dataStore.removeEvents(events.getLastEventId());
    assertEquals(1, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event5);
  }
  
  public void testUpgradeDatabase() {
    JSONObject event1 = buildTestEvent("event 1");
    JSONObject event2 = buildTestEvent("event 2");
    
    dataStore.addEvent(event1);
    assertEquals(1, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event1);

    // Make sure onUpgrade clears the database.
    databaseHelper.onUpgrade(
        databaseHelper.getWritableDatabase(),
        YozioDataStoreImpl.DATABASE_VERSION,
        YozioDataStoreImpl.DATABASE_VERSION + 1);
    assertEquals(0, dataStore.getNumEvents());
    
    // Make sure events can still be added/removed after upgrading.
    dataStore.addEvent(event2);
    assertEquals(1, dataStore.getNumEvents());
    assertEventsEqual(dataStore.getEvents(10).getJsonArray(), event2);
    Events events = dataStore.getEvents(10);
    dataStore.removeEvents(events.getLastEventId());
    assertEquals(0, dataStore.getNumEvents());
  }
  
  // Builds a JSONObject to represent an event.
  // Does not have the same structure as an actual event.
  private JSONObject buildTestEvent(String name) {
    try {
      JSONObject eventObject = new JSONObject();
      eventObject.put(EVENT_KEY, name);
      return eventObject;
    } catch (JSONException e) {
      Log.e(LOGTAG, "buildTestEvent", e);
      fail("Faiure building test event");
      return null;
    }
  }
  
  // Asserts that the JSONArray is equivalent to eventsArr.
  private void assertEventsEqual(JSONArray eventsJsonArr, JSONObject... expectedEventsArr) {
    assertEquals(eventsJsonArr.length(), expectedEventsArr.length);
    for (int i = 0; i < expectedEventsArr.length; i++) {
      try {
        JSONObject event = (JSONObject) eventsJsonArr.get(i);
        JSONObject expectedEvent = expectedEventsArr[i];
        assertEquals(event.toString(), expectedEvent.toString());
      } catch (JSONException e) {
        Log.e(LOGTAG, "assertEventsEqual", e);
        fail("Faiure comparing JSON objects");
      }
    }
  }
}
