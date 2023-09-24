package fi.ni.statistics;

import java.util.Iterator;
import java.util.Map.Entry;

import fi.ni.ExpressReader;
import fi.ni.vo.EntityVO;

public class PrintMaxEntityAttributes {


    public static void main(String[] args) {
	ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	int max=Integer.MIN_VALUE;
	Iterator<Entry<String, EntityVO>> it = er.getEntities().entrySet().iterator();
	try {
	    while (it.hasNext()) {
		Entry<String, EntityVO> pairs = it.next();
		EntityVO evo = pairs.getValue();	
	    int numofattrs=0;
	    int numofinverses=0;	

	    if(evo.getAttributes()!=null)
	      numofattrs=evo.getAttributes().size();
	    if(evo.getInterfaces()!=null)
	       numofinverses=evo.getInterfaces().size();
	    
	    if(max<(numofattrs+numofinverses))
		max=(numofattrs+numofinverses);
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	System.out.println("max ai:"+max);
	 
    }

}
