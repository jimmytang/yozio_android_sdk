package com.yozio.android;

import android.test.AndroidTestCase;
import android.util.Log;

public class YozioTest extends AndroidTestCase {

  public YozioTest() {
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testEndToEnd() {
    Yozio.configure(getContext(), "test app key", "test secret key");
    Yozio.getUrl("test link name", "www.google.com", "www.yahoo.com");
    Yozio.viewedLink("viewed link name");
    Yozio.sharedLink("shared link name");
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Log.e("YozioTest", "testEndToEnd", e);
    }
  }
  
  public void testNotConfigured() {
    // TODO
  }
}
