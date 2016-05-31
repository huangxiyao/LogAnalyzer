package util;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<Map.Entry<String, MutableInt>> {
    public int compare(Map.Entry<String, MutableInt> mp1, Map.Entry<String, MutableInt> mp2) {
        return mp2.getValue().get() - mp1.getValue().get();
    }
}
