package fi.ni.nodenamer.stats;

import java.util.Map;
import java.util.TreeMap;

public class ClassLiteralCksumBag {

    public class ClassValues {
	Map<String, Integer> values = new TreeMap<String, Integer>();
	double csum_values_count = 0;

	public void add(String value,int weight) {
	    Integer count = values.get(value);
	    if (count == null) {
		values.put(value, weight);
	    } else {
		values.put(value, count + weight);
	    }
	    csum_values_count+=weight;
	}

	public double test(String value) {
	    Integer count = values.get(value);
	    if (count == null) {
		return 1.0;
	    }
	    
	    double val=count/csum_values_count;  
	    return val;
	}


    }

    Map<String, ClassValues> bag = new TreeMap<String, ClassValues>();

    public void add(String class_name, String value,int weight) {
	ClassValues class_value = bag.get(class_name);
	if (class_value == null) {
	    class_value = new ClassValues();
	    bag.put(class_name, class_value);
	}

	class_value.add(value,weight);
    }

    public double test(String class_name, String value) {
	ClassValues class_value = bag.get(class_name);
	if (class_value == null) {
	    class_value = new ClassValues();
	    bag.put(class_name, class_value);
	    System.err.println("ClassLiteralCksumBag  class does not exist at the bag!");
	}
        double val=class_value.test(value);
	return val;
    }

    public static void main(String[] args) {
	ClassLiteralCksumBag bag=new ClassLiteralCksumBag();
	bag.add("c", "1",1);
	bag.add("c", "2",2);
	bag.add("c", "2",2);
	bag.add("b", "2",2);
	System.out.println(bag.test("c", "1"));
	System.out.println(bag.test("c", "2"));
	System.out.println(bag.test("b", "1"));
    }
}
