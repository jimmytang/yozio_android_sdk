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
import org.json.JSONObject;

/**
 * Thread safe data store of events collected by Yozio.
 */
interface YozioDataStore {

  /**
   * Add an event to the data store.
   *
   * @return true iff the event was added successfully.
   */
  boolean addEvent(JSONObject event);

  /**
   * Get the number of events in the data store.
   *
   * @return the number of events in the data store, or -1 if an error occurs.
   */
  int getNumEvents();

  /**
   * Retrieve events from the data store starting with the oldest event
   * in the data store.
   *
   * @param limit  the maximum number of events to return.
   * @return an {@link Events} object, or null if an error occurs.
   */
  Events getEvents(int limit);

  /**
   * Remove events from the data store starting with the oldest event in the
   * data store and ending with the event with lastEventId (inclusive).
   *
   * @param lastEventId  the id of the last event to remove.
   * @return true iff the events were removed successfully.
   */
  boolean removeEvents(String lastEventId);

  /**
   * Return value for getEvents.
   */
  class Events {

    private final JSONArray jsonArray;
    private final String lastEventId;

    Events(JSONArray eventsArr, String lastEventId) {
      this.jsonArray = eventsArr;
      this.lastEventId = lastEventId;
    }

    JSONArray getJsonArray() {
      return jsonArray;
    }

    String getLastEventId() {
      return lastEventId;
    }
  }
}
