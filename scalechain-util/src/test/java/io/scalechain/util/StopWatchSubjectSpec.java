package io.scalechain.util;

import io.kotlintest.KTestJUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit 4 Test Case
 */
public class StopWatchSubjectSpec {
  StopWatchSubject subject = null;

  @Before
  public void setUp() {
    subject = new StopWatchSubject("test");
    // set up the test case
  }

  @After
  public void tearDown() {
    subject = null;
    // tear down the test case
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