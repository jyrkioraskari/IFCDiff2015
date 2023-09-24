package fi.ni.nodenamer.stats;

import java.util.Map;
import java.util.TreeMap;

public class CounterBag {

    Map<String, Integer> values = new TreeMap<String, Integer>();

    public void add(String value) {
	Integer count = values.get(value);
	if (count == null) {
	    values.put(value, 1);
	} else {
	    values.put(value, count + 1);
	}
    }

    public int test(String value) {
	Integer count = values.get(value);
	if (count == null) {
	    return 1;
	}
        if(count>999)
            count=999;
	return count;
    }

    public static void main(String[] args) {
	CounterBag bag = new CounterBag();
	bag.add("c");
	bag.add("c");
	bag.add("c");
	bag.add("b");
	System.out.println(bag.test("c"));
	System.out.println(bag.test("b"));
    }
}
