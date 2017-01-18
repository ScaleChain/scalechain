package io.scalechain.blockchain.oap.util;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by shannon on 16. 12. 16.
 */
public class PairTest {
  static {
    System.out.println(PairTest.class.getName() + "." + " LOADED.");
  }

  @Test
  public void equalsTest() {
    Pair<String, String> sp1 = new Pair<String, String>("1", "2");
    Pair<String, String> sp2 = new Pair<String, String>("1", "2");
    assertEquals("Pair<String, String>", sp1, sp2);
    Pair<String, String> sp3 = new Pair<String, String>("1", "2");
    Pair<String, String> sp4 = new Pair<String, String>("2", "2");
    assertNotEquals("Pair<String, String>", sp3, sp4);
  }

  @Test
  public void toStringTest() {
    Pair<String, String> pair = new Pair<String, String>("<First>", "<Second>");
    String s = pair.toString();

    assertTrue("toString() should contain first.toString()", s.contains(pair.getFirst().toString()));
    assertTrue("toString() should contain send.toString()", s.contains(pair.getSecond().toString()));

  }

  @Test
  public void equalsDifferectTypeTest() {
    Pair<String, String> sp1 = new Pair<String, String>("1", "2");
    Pair<String, String> sp2 = new Pair<String, String>("1", "2");
    assertEquals("Pair<String, String>", sp1, sp2);

    assertNotEquals("WITH DIFFERENT TYPE", sp1, new Object());

    Pair<String, Integer> sp3 = new Pair<String, Integer>("1", 2);
    Pair<String, Integer> sp4 = new Pair<String, Integer>("2", 2);
    assertNotEquals("Pair<String, String>", sp3, sp4);
    assertNotEquals("Pairs  CONTAINING DIFFERENT TYPES", sp1, sp3);
  }

  @Test
  public void hashCodeTest() {
    Pair<String, String> sp1 = new Pair<String, String>("1", "2");
    Pair<String, String> sp2 = new Pair<String, String>("1", "2");
    assertEquals("HASH", sp1.hashCode(), sp2.hashCode());

    Pair<Integer, Integer> ip1 = new Pair<Integer, Integer>(Integer.MAX_VALUE, Integer.MIN_VALUE);
    Pair<Integer, Integer> ip2 = new Pair<Integer, Integer>(Integer.MAX_VALUE, Integer.MIN_VALUE);
    Pair<Integer, Integer> ip3 = new Pair<Integer, Integer>(Integer.MIN_VALUE, Integer.MAX_VALUE);
    Pair<Integer, Integer> ip4 = new Pair<Integer, Integer>(0, Integer.MIN_VALUE);
    assertEquals("HASH", ip1.hashCode(), ip2.hashCode());
    assertNotEquals("HASH", ip1.hashCode(), ip3.hashCode());
    assertNotEquals("HASH", ip1.hashCode(), ip4.hashCode());

    Pair<String, Integer> sp3 = new Pair<String, Integer>("1", 2);
    Pair<String, Integer> sp4 = new Pair<String, Integer>("2", 2);
    assertNotEquals("HASH", sp1.hashCode(), sp3.hashCode());
    assertNotEquals("HASH", sp1.hashCode(), sp4.hashCode());
    assertNotEquals("HASH", sp3.hashCode(), sp4.hashCode());

    Pair<String, Integer> sp5 = new Pair<String, Integer>(null, 2);
    Pair<String, Integer> sp6 = new Pair<String, Integer>("2", null);
    Pair<String, Integer> sp7 = new Pair<String, Integer>("2", 2);
    assertNotEquals("HASH", sp5.hashCode(), sp6.hashCode());
    assertNotEquals("HASH", sp6.hashCode(), sp7.hashCode());
    assertNotEquals("HASH", sp7.hashCode(), sp5.hashCode());
  }
}
