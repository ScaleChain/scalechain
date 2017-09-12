package io.scalechain.util;

import io.kotlintest.KTestJUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    subject.start();
    subject.stop();
    subject.reset();
    assertEquals( "(sum=0);{}", subject.toString() );
  }

  @Test
  public void testStartStopOnce() {
    subject.start();
    subject.stop();
    // BUGBUG : In docker, it seems the time gap between start/stop is not 0
    // toString returns a string such as (sum=250);{0=1}
    //assertTrue(subject.toString().contains("{0=1}"));
  }

  @Test
  public void testStartStopTwice() {
    subject.start();
    subject.stop();
    subject.start();
    subject.stop();
    // BUGBUG : In docker, it seems the time gap between start/stop is not 0
    // toString returns a string such as (sum=526);{0=2}
    //assertTrue(subject.toString().contains("{0=2}"));
  }
}