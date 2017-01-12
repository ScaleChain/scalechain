package io.scalechain.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 Test Case
 */
class StopWatchSubjectSpec {
  StopWatchSubject subject = null;

  @Before
  void setUp() {
    subject = new StopWatchSubject("test");
    // set up the test case
  }

  @After
  void tearDown() {
    subject = null;
    // tear down the test case
  }

  @Test
  void testReset() {

  }

  @Test
  void testStart() {

  }

  @Test
  void testStop() {

  }

  @Test
  void testToString() {
  }
}