package io.scalechain.util;

import io.kotlintest.KTestJUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit 4 Test Case
 */
public class StopWatchSpec {
  private StopWatch watch = null;
  @Before
  public void setUp() {
    // set up the test case
    watch = new StopWatch();
  }

  @After
  public void tearDown() {
    // tear down the test case
    watch = null;
  }

  @Test
  public void testReset() {

  }

  @Test
  public void testStart() {

  }

  @Test
  public void testStop() {

  }

  @Test
  public void testToString() {
  }
}