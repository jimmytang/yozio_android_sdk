package com.yozio.android;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.yozio.android.YozioDataStoreImpl.DataStoreListener;

public class TestHelper {

  private static final String LOGTAG = "TestHelper";
  private static final int TIMEOUT = 5;

  /**
   * Waits until the previous event is sent by waiting for the dataStore
   * remove event.
   */
  public static void waitUntilEventAdded(YozioDataStoreImpl dataStore) {
    final CountDownLatch signal = new CountDownLatch(1);
    dataStore.setListener(new DataStoreListener() {
      public void onAdd() {
        signal.countDown();
      }
      public void onRemove() {
      }
    });
    try {
      boolean timeout = !signal.await(TIMEOUT, TimeUnit.SECONDS);
      if (timeout) {
        Log.e(LOGTAG, "onAdd: timeout!");
        System.exit(1);
      }
    } catch (InterruptedException e) {
      Log.e(LOGTAG, "onAdd", e);
      System.exit(1);
    } finally {
      // Remove the listener.
      dataStore.setListener(null);
    }
  }

  /**
   * Waits until the previous event is sent by waiting for the dataStore
   * remove event.
   */
  public static void waitUntilEventSent(YozioDataStoreImpl dataStore) {
    final CountDownLatch signal = new CountDownLatch(1);
    dataStore.setListener(new DataStoreListener() {
      public void onRemove() {
        signal.countDown();
      }
      public void onAdd() {
      }
    });
    try {
      boolean timeout = !signal.await(TIMEOUT, TimeUnit.SECONDS);
      if (timeout) {
        Log.e(LOGTAG, "waitUntilEventSent: timeout!");
        System.exit(1);
      }
    } catch (InterruptedException e) {
      Log.e(LOGTAG, "waitUntilEventSent", e);
      System.exit(1);
    } finally {
      // Remove the listener.
      dataStore.setListener(null);
    }
  }
}
