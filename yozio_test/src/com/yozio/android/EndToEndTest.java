package com.yozio.android;

import org.apache.http.client.HttpClient;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.InstrumentationTestCase;

public class EndToEndTest extends InstrumentationTestCase
{

  private static final String TEST_BASE_URL = "http://10.0.2.2:3000";
  private static final String APP_KEY = "139b0e10-ba56-012f-e1d9-2837371df2a8";
  private static final String LINK_NAME = "testlink";

  private Context context;
  private YozioDataStore dataStore;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    context = getInstrumentation().getContext();
    context.deleteDatabase(YozioDataStoreImpl.DATABASE_NAME);
    HttpClient httpClient = Yozio.threadSafeHttpClient();
    YozioApiServiceImpl apiService = new YozioApiServiceImpl(httpClient);
    apiService.setBaseUrl(TEST_BASE_URL);
    SQLiteOpenHelper dbHelper = new YozioDataStoreImpl.DatabaseHelper(context);
    dataStore = new YozioDataStoreImpl(dbHelper, APP_KEY);
    YozioHelper helper = new YozioHelper(dataStore, apiService);
    Yozio.setHelper(helper);
  }

  public void testEndToEnd() throws Throwable {
//    // Test sending OPEN_APP event
//    Yozio.configure(context, APP_KEY, "test secret key");
//
//    // Test blocking getYozioLink
//    String url = Yozio.getYozioLink(LINK_NAME, "www.google.com");
//    Log.i("YozioTest", "getYozioLink(): " + url);
//
//    // Test non-blocking getYozioLink
//    final CountDownLatch signal = new CountDownLatch(1);
//    runTestOnUiThread(new Runnable() {
//      public void run() {
//        Yozio.getYozioLinkAsync(LINK_NAME, "www.yahoo.com", new GetYozioLinkCallback() {
//          public void handleResponse(String url) {
//            Log.i("YozioTest", "getYozioLinkAsync(): " + url);
//            signal.countDown();
//          }
//        });
//      }
//    });
//    signal.await(10, TimeUnit.SECONDS);
//
//    // Test sending LOGIN event
//    Yozio.userLoggedIn("spaceman");
//
//    // Test sending valid viewedLink event
//    Yozio.viewedLink(LINK_NAME);
//
//    // Test sending valid sharedLink event
//    Yozio.sharedLink(LINK_NAME);
//
//    try {
//      Thread.sleep(2000);
//    } catch (InterruptedException e) {
//      Log.e("YozioTest", "testEndToEnd", e);
//    }
//
//    Log.i("YozioTest", "number of events: " + dataStore.getNumEvents());
//
//    // Test sending user invalid viewedLink event to make sure it gets removed
//    // from the queue
//    Log.i("YozioTest", "# events before invalid link name: " + dataStore.getNumEvents());
//    Yozio.viewedLink("nonexistentlinkname");
//    Log.i("YozioTest", "# events right after invalid link name: " + dataStore.getNumEvents());
//    try {
//      Thread.sleep(2000);
//    } catch (InterruptedException e) {
//      Log.e("YozioTest", "testEndToEnd", e);
//    }
//    Log.i("YozioTest", "# events after invalid link name: " + dataStore.getNumEvents());
//
//    Yozio.userLoggedIn("grrrrrrrrrreat");
//    try {
//      Thread.sleep(2000);
//    } catch (InterruptedException e) {
//      Log.e("YozioTest", "testEndToEnd2", e);
//    }
  }
}
