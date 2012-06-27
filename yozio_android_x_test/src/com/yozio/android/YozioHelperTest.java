package com.yozio.android;

import android.test.AndroidTestCase;

public class YozioHelperTest extends AndroidTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  /**
   * YozioHelper test cases
   * - Only 1 flush event should happen
   * - Flush fail, next flush should try to batch the data
   * - Data being added while flushing (less than FLUSH_BATCH_MAX), the flush should not remove new data
   * - Data store exceptional return values (null, -1, etc)
   */
}
