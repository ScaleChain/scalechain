package io.scalechain.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class StopWatch {
    Map<String, StopWatchSubject> subjects = new HashMap<String, StopWatchSubject>();
    public void reset() {
        subjects.clear();
    }

    public StopWatchSubject start(String subject) {
        StopWatchSubject watchSubject = subjects.get(subject);
        if (watchSubject == null) {
            watchSubject = new StopWatchSubject(subject);
            watchSubject.start();
            subjects.put(subject, watchSubject);
        } else {
            watchSubject.start();
        }
        return watchSubject;
    }

    public void stop(String subject) {
        StopWatchSubject watchSubject = subjects.get(subject);
        assert( watchSubject != null );
        watchSubject.stop();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, StopWatchSubject> e : subjects.entrySet()) {
            buffer.append("subject : " + e.getKey() + "\n");
            buffer.append("perf : " + e.getValue() + "\n\n");
        }
        return buffer.toString();
    }
}
