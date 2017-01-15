package io.scalechain.util;

import io.kotlintest.KTestJUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 Test Case
 */
public class StopWatchSpec {
  private int countOccurrence(String source, String pattern) {
    return (source.length() - source.replace(pattern, "").length()) / pattern.length();
  }
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
    watch.start("test block 1");
    watch.stop("test block 1");
    watch.reset();
    assertEquals("",  watch.toString());
  }

  @Test(expected=AssertionError.class)
  public void testStopShouldHitAnAssertionIfAnInvalidSubjectIsGiven() {
    watch.stop("invalid subject");
  }

  @Test
  public void testStartStopOnce() {
    watch.start("test block 1");
    watch.stop("test block 1");
    /* watch.toString returns a string such as :
        subject : test block 1
        perf : (sum=1043);{0=1}
     */
    assertEquals(1, countOccurrence(watch.toString(), "subject : test block 1") );
    assertEquals(1, countOccurrence(watch.toString(), "{0=1}"));
  }

  @Test
  public void testStartStopTwice() {
    watch.start("test block 1");
    watch.stop("test block 1");
    watch.start("test block 1");
    watch.stop("test block 1");

    /* watch.toString returns a string such as :
        subject : test block 1
        perf : (sum=1127);{0=2}
     */
    assertEquals( 1, countOccurrence(watch.toString(), "subject : test block 1") );
    assertEquals( 1, countOccurrence(watch.toString(), "{0=2}") );
  }

  @Test
  public void testStartStopWithDifferentSubject() {
    watch.start("test block 1");
    watch.stop("test block 1");
    watch.start("test block 2");
    watch.stop("test block 2");
    /* watch.toString returns a string such as :
        subject : test block 1
        perf : (sum=828);{0=1}

        subject : test block 2
        perf : (sum=434);{0=1}
     */
    assertEquals( 1, countOccurrence(watch.toString(), "subject : test block 1") );
    assertEquals( 1, countOccurrence(watch.toString(), "subject : test block 2") );
    assertEquals( 2, countOccurrence(watch.toString(), "{0=1}") );
  }

  @Test
  public void testStartStopWithDifferentSubjectTwice() {
    watch.start("test block 1");
    watch.stop("test block 1");
    watch.start("test block 2");
    watch.stop("test block 2");
    watch.start("test block 1");
    watch.stop("test block 1");
    watch.start("test block 2");
    watch.stop("test block 2");
    /* watch.toString returns a string such as :
        subject : test block 1
        perf : (sum=9721);{0=2}

        subject : test block 2
        perf : (sum=967);{0=2}
     */
    assertEquals( 1, countOccurrence(watch.toString(), "subject : test block 1") );
    assertEquals( 1, countOccurrence(watch.toString(), "subject : test block 2") );
    assertEquals( 2, countOccurrence(watch.toString(), "{0=2}") );
  }

  @Test
  public void testStartStopWithNestedSubject() {
    watch.start("outer block");
    watch.start("inner block");
    watch.stop("inner block");
    watch.stop("outer block");
    /* watch.toString returns a string such as :
        subject : inner block
        perf : (sum=727);{0=1}

        subject : outer block
        perf : (sum=4886);{0=1}
     */
    assertEquals( 1, countOccurrence(watch.toString(), "subject : outer block") );
    assertEquals( 1, countOccurrence(watch.toString(), "subject : inner block") );
    assertEquals( 2, countOccurrence(watch.toString(), "{0=1}") );
  }
}