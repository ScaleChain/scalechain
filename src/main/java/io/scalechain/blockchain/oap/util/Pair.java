package io.scalechain.blockchain.oap.util;

import java.util.Objects;

/**
 * Created by shannon on 16. 11. 25.
 */
public class Pair<T1, T2> {
    private T1 first;
    private T2 second;
    public Pair(T1 first, T2 second) {
        this.first  = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof  Pair)) {
            return false;
        }
        Pair<T1, T2> o = (Pair<T1, T2>)that;
        return Objects.equals(getFirst(), o.getFirst()) && Objects.equals(getSecond(), o.getSecond());
    }
    @Override
    public int hashCode() {
        // From Android SDK Pair
        //return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
        sb.append(getFirst().toString()).append(", ").append(getSecond().toString()).append(')');
        return sb.toString();
    }
}
