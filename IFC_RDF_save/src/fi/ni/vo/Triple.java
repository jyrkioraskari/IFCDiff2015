package fi.ni.vo;

import java.util.HashSet;
import java.util.Set;

import fi.ni.Thing;

public class Triple {
    static Set<Thing> printed = new HashSet<Thing>();

    boolean used = false;
    public Thing  s;
    public String p;
    public Object o;
    
    public boolean literal=false;
    
    public Triple(Link l) {
	this.s = l.t1;
	this.p = l.property;
	this.o = l.t2;
    }

    public Triple(Thing subject, String property,Object object) {
	this.s = subject;
	this.p = property;
	this.o = object;
	literal=true;
    }

    public String toString()
    {
	String s_name=s.toString();
	String o_name=o.toString();
	
	if(s.is_grounded)
	    s_name="Grounded:"+s.grounding_name;
	if(Thing.class.isInstance(o))
	{
	    if(((Thing)o).is_grounded)
	    {
		o_name="Grounded:"+((Thing)o).grounding_name;
	    }
	}
	return s.getClass().getSimpleName()+"("+s_name+")."+p+"."+o.getClass().getSimpleName()+"("+o_name+")";
    }

}
