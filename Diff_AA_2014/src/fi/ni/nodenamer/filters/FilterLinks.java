package fi.ni.nodenamer.filters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.stats.CounterBag;

public class FilterLinks {

    // Expectation: No literal connections at the connections parameter

    private List<Connection> filterLowestChecksumValue(List<Connection> connections) {
	List<Connection> retval = new ArrayList<Connection>();
	String lowestvalue = " ";
	double prob = 1;
	for (Connection c : connections) {
	    if (c.getPointedNode().getLiteral_prob() < prob) {
		prob = c.getPointedNode().getLiteral_prob();
	    }
	}
	for (Connection c : connections) {
	    if (c.getPointedNode().getLiteral_prob() == prob)
		if (c.getPointedNode().getLiteralChksum().compareTo(lowestvalue) < 0) {
		    lowestvalue = c.getPointedNode().getLiteralChksum();
		}
	}
	for (Connection c : connections) {
	    if (c.getPointedNode().getLiteral_prob() == prob)
		if (c.getPointedNode().getLiteralChksum() == lowestvalue)
		    retval.add(c);
	}
	return retval;
    }

    DecimalFormat plainD = new DecimalFormat("0.0000");
    DecimalFormat plainI = new DecimalFormat("000");

    private class ConnectionComparator implements Comparator<Connection> {

	public int compare(Connection o1, Connection o2) {
	    String s1 = plainI.format(o1.getPropertyCount())+"."+plainD.format(o1.getPointedNode().getLiteral_prob()) + "." + o1.getPointedNode().getLiteralChksum();
	    String s2 = plainI.format(o2.getPropertyCount())+"."+plainD.format(o2.getPointedNode().getLiteral_prob()) + "." + o2.getPointedNode().getLiteralChksum();
	    if (o1.equals(o2))
		return 0;
	    return s1.compareTo(s2);
	}
    }

    private List<Connection> filterLowestChecksumValuesW(List<Connection> connections, int count) {
	List<Connection> retval = new ArrayList<Connection>();
	Collections.sort(connections, new ConnectionComparator());

	int i = 0;
	Connection last = null;
	for (Connection c : connections) {
	    if (i < count) {
		last = c;
		retval.add(c);
	    } else {
		if (last.getPointedNode().getLiteral_prob() == c.getPointedNode().getLiteral_prob() && last.getPointedNode().getLiteralChksum().equals(c.getPointedNode().getLiteralChksum())) {
		    retval.add(c);
		}
	    }
	    i++;
	}

	if (retval.size() > count)
	    return null;
	return retval;
    }

    public List<Connection> filterLowestChecksumValues(List<Connection> connections, int count) {
	CounterBag cb=new CounterBag();
	for (Connection c : connections) {
	    cb.add(c.getProperty());
	}
	for (Connection c : connections) {
	    c.setPropertyCount(cb.test(c.getProperty()));
	}
	List<Connection> retval = null;
	retval = filterLowestChecksumValuesW(connections, count);
	if (retval == null) {
	    retval = filterLowestChecksumValuesW(connections, count * 4);
	}
	if (retval == null) {
	    return filterLowestChecksumValue(connections);
	}

	if (retval.size() > count)
	    return filterLowestChecksumValue(connections);
	return retval;
    }

}
