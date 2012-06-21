package com.yozio.android;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Persistent data store of events collected by Yozio.
 * 
 * NOTE: implementations must be thread safe.
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
   * @return the number of events in the data store.
   */
  int getNumEvents();
	
  /**
   * Retrieve events from the data store starting with the oldest event
   * in the data store.
   * 
   * @param limit  the maximum number of events to return.
   * @return a JSONArray of events, or nil if an error occurs.
   */
  JSONArray getEvents(int limit);
	
  /**
   * Remove events from the data store starting with the oldest event in the
   * data store.
   * 
   * @param limit  the maximum number of events to remove.
   * @return the number of events in the data store after removing.
   */
  int removeEvents(int limit);
}
