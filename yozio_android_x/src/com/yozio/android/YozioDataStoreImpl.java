package com.yozio.android;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

// TODO: implement with backing sqllite
class YozioDataStoreImpl implements YozioDataStore {

  private List<JSONObject> events;
  
  YozioDataStoreImpl() {
    events = new ArrayList<JSONObject>();
  }
  
  public boolean addEvent(JSONObject event) {
    synchronized (this) {
      return events.add(event);
    }
  }
  
  public int getNumEvents() {
    synchronized (this) {
      return events.size();
    }
  }

  public JSONArray getEvents(int limit) {
    synchronized (this) {
      limit = Math.min(events.size(), limit);
      return new JSONArray(events.subList(0, limit));
    }
  }

  public int removeEvents(int limit) {
    synchronized (this) {
      if (limit >= events.size()) {
        events.clear();
        return 0;
      } else {
        events = events.subList(limit, events.size());
        return events.size();
      }
    }
  }
}
