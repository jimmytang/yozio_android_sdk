package com.yozio.android;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.yozio.android.YozioDataStoreImpl.RemoveListener;

public class TestHelper {

  private static final String LOGTAG = "TestHelper";

  /**
   * Waits until the previous event is sent by waiting for the dataStore
   * remove event.
   */
  public static void waitUntilEventSent(YozioDataStoreImpl dataStore) {
    final CountDownLatch signal = new CountDownLatch(1);
    dataStore.setRemoveListener(new RemoveListener() {
      public void onRemove() {
        signal.countDown();
      }
    });
    try {
      signal.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Log.e(LOGTAG, "waitUntilEventSent", e);
      System.exit(1);
    }
  }
}
