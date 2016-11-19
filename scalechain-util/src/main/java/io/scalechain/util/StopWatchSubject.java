package io.scalechain.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

class StopWatchSubject {
    long startNanoSec;
    String subject;

    Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
    long sum = 0;

    public StopWatchSubject(String subject) {
        this.subject = subject;
    }

    public void reset() {
        startNanoSec = 0;
        histogram.clear();
        sum = 0;
    }

    public void start() {
        assert( startNanoSec == 0 );
        startNanoSec = System.nanoTime();
    }

    public void stop() {
        assert( startNanoSec != 0 );
        long elapsedMillis = System.nanoTime() - startNanoSec;

//        int bucket = (int) (elapsedMillis / 1) * 1;
        int bucket = (int) ( elapsedMillis >> 15 << 15);
        Integer b = Integer.valueOf(bucket);
        Integer count = histogram.get(b);
        if (count == null) {
            histogram.put(b, Integer.valueOf(1));
        } else {
            histogram.put(b, Integer.valueOf(count + 1));
        }
        sum += elapsedMillis;
        startNanoSec = 0;
    }
    public String toString() {
        SortedMap<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>();
        for ( Map.Entry<Integer, Integer> e : histogram.entrySet()) {
            sortedMap.put(e.getKey(), e.getValue());
        }
/*
        for ( Map.Entry<Integer, Integer> e : sortedMap.entrySet()) {
        }
*/
        return "(sum="+sum+");"+sortedMap.toString();
    }
}