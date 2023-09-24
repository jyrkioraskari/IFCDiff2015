package fi.ni.statistics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import softhema.system.toolkits.ToolkitString;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.Link;


public class ShowEmptyGroudingNames {
    
    
    private class DecComparator implements Comparator<Integer>
    {

	public int compare(Integer o1, Integer o2) {	    
	    return o2-o1;
	}
	
    }
    DecComparator dec_comparator=new DecComparator();

    public ShowEmptyGroudingNames(String filename) {
	runStatistics(filename);
    }
    public void runStatistics(String filename) {

	ExpressReader er;

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	String application = "";

	
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparator);  // links count <class name, number or individual entities>	
	model.checkUniques();
	
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
            if(IfcOwnerHistory.class.isInstance(t1))
        	continue;
	    if(t1.is_grounded)
		  continue;
	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2=l.getTheOtherEnd(t1);
	        if(IfcOwnerHistory.class.isInstance(t2))
	        	continue;
		t2.incoming_count++;
	    }
	    SortedMap<String,Integer> current_map=outgoing_histogram_dec.get(ifcs.size());
	    if(current_map==null)                                    //!
	    {                                                        //!
		current_map=new TreeMap<String,Integer>();           //!
		outgoing_histogram_dec.put(ifcs.size(),current_map); //!
	    }                                                        //!
	    Integer current_count=current_map.get(t1.getClass().getSimpleName());
	    if(current_count==null)
		current_count=new Integer(0);
	    current_count=current_count+1;
	    current_map.put(t1.getClass().getSimpleName(),current_count);
	}

	SortedMap<Integer,SortedMap<String,Integer>> incoming_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparator);  // links count <class name, number or individual entities>
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    if(IfcOwnerHistory.class.isInstance(t1))
	        	continue;
	    if(t1.is_grounded)
		  continue;
	    
	    SortedMap<String,Integer> current_map=incoming_histogram_dec.get(t1.incoming_count);
	    if(current_map==null)                                                     //!
	    {                                                                         //!
		current_map=new TreeMap<String,Integer>();                            //!
		incoming_histogram_dec.put(t1.incoming_count,current_map);            //!
	    }                                                                         //!
	    Integer current_count=current_map.get(t1.getClass().getSimpleName());
	    if(current_count==null)
		current_count=new Integer(0);
	    current_count=current_count+1;
	    current_map.put(t1.getClass().getSimpleName(),current_count);
	}

	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if (IfcApplication.class.isInstance(t)) {
		IfcApplication appl = (IfcApplication) t;
		application = appl.getApplicationFullName();
	    }
	}
	

       
       MultiMap big_candidates_out=new MultiHashMap();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : outgoing_histogram_dec.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           @SuppressWarnings("unchecked")
		Collection<Integer> i=(Collection<Integer>)big_candidates_out.get(class_name);
	           if(class_value==1)  // only one of these
	           {
	             if(i==null)  // the first one
	        	 big_candidates_out.put(class_name, key);
	             else
	        	 if(i.size()<5)
	        	     big_candidates_out.put(class_name, key);
	        	     
	           }
	           
	           // Count can be any for removal
	           if(i!=null)
	           {
	             Set<Integer> removes=new HashSet<Integer>();
	             Iterator<Integer> it=i.iterator(); 
	             while(it.hasNext()) 
	             {
	              Integer intval=(Integer)it.next();
	              if(intval!=key)
	              if(Math.abs(key-intval)<5)
	              {
	        	  removes.add(intval);
	        	  removes.add(key);
	              }
	             }
	             Iterator<Integer> rit=removes.iterator(); 
	             while(rit.hasNext())
	             {
	        	 Integer val=rit.next();
	        	 i.remove(val);
	        	 i.add(-val);
	             }
	             
	           }
	   }
	 }

       MultiMap big_candidates_in=new MultiHashMap();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : incoming_histogram_dec.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Collection<Integer> i=(Collection<Integer>)big_candidates_in.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	        	 big_candidates_in.put(class_name, key);
	             else
	        	 if(i.size()<5)
	        	     big_candidates_in.put(class_name, key);
	        	     
	           }
	           
	           // Count can be any for removal
	           if(i!=null)
	           {
	             Set<Integer> removes=new HashSet<Integer>();
	             Iterator<Integer> it=i.iterator(); 
	             while(it.hasNext()) 
	             {
	              Integer intval=(Integer)it.next();
	              if(intval!=key)
	    
	              if(Math.abs(key-intval)<5)
	              {
	        	  removes.add(intval);
	        	  removes.add(key);
	              }
	             }
	             Iterator<Integer> rit=removes.iterator(); 
	             while(rit.hasNext())
	             {
	        	 Integer val=rit.next();
	        	 i.remove(val);
	        	 i.add(-val);
	             }
	             
	           }
	   }
	 }


	long counter_groudable_by_big_ins=0;
	long counter_groudable_by_big_outs=0;

	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    int out_links=t1.i.drum_getIfcClassAttributes_notInverses().size();
	    int in_links=t1.incoming_count;

	    
	    Collection<Integer> c;
	    c=(Collection<Integer>)big_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
	             Iterator<Integer> it=c.iterator(); 
	             while(it.hasNext())  
	             {
	                Integer intval=(Integer)it.next();
	                if(intval<0) continue;
			if(intval.intValue()==out_links)
			{
			    counter_groudable_by_big_outs++;
			}

	             }
	    }

	    c=(Collection<Integer>)big_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
	             Iterator<Integer> it=c.iterator(); 
	             while(it.hasNext())  
	             {
	                Integer intval=(Integer)it.next();
	                if(intval<0) continue;
			if(intval.intValue()==in_links)
			{
			    counter_groudable_by_big_ins++;
			}

	             }
	    }

	    
	}
       
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    int out_links=t1.i.drum_getIfcClassAttributes_notInverses().size();
	    int in_links=t1.incoming_count;

	    
	    Collection<Integer> c;
	    c=(Collection<Integer>)big_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		     long bout_count=1;
	             Iterator<Integer> it=c.iterator(); 
	             while(it.hasNext())  
	             {
	                Integer intval=(Integer)it.next();
	                if(intval<0) continue;
			if(intval.intValue()==out_links)
			{
			    t1.is_grounded=true;
			    t1.grounding_name=model.getRoot().grounding_name+".big_outs."+bout_count++;
			}

	             }
	    }

	    if(t1.is_grounded)
		continue;
	    c=(Collection<Integer>)big_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		     long bin_count=1;
	             Iterator<Integer> it=c.iterator(); 
	             while(it.hasNext())  
	             {
	                Integer intval=(Integer)it.next();
	                if(intval<0) continue;
			if(intval.intValue()==in_links)
			{
			    t1.is_grounded=true;
			    t1.grounding_name=model.getRoot().grounding_name+".big_ins."+bin_count++;
			}

	             }
	    }
	}

	System.gc();
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	GroundSettings gs=new GroundSettings(true, true, true, true, true, false, true, true, true);	       
	model.groundFromGUIDs(common_gids,gs);
	model.createHugeLinksMap();
	System.gc();
	model.deduceMSGs();
	int max=Integer.MIN_VALUE;
	for(int i=0;i<model.msgs.size();i++)
	{
	    if(max<model.msgs.get(i).size())
		max=model.msgs.get(i).size();
	}
	System.gc();
	System.out.println(application+";"+filename+";"+max+";");
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    if(t1.is_grounded)
	    {
		if(t1.grounding_name==null)
		{
		    System.out.println("-1 "+t1.getClass().getSimpleName());
		    continue;
		}
		if(t1.grounding_name.length()<1)
		    System.out.println("-2 "+t1.getClass().getSimpleName());
	    }

	}
   }

    public static void tiny_testset() {	
	new ShowEmptyGroudingNames("C:\\jo\\models2\\First_Floor_Vent.ifc");
    }

    public static void basic_testset() {	
	new ShowEmptyGroudingNames("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Crane.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\OfficeBuilding.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
    }
    
    public static void complete_testset() {	
	new ShowEmptyGroudingNames("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Crane.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\OfficeBuilding.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\SMC Building - modified.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\SMC Building.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\Planer 4B Full.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\models\\HiB_DuctWork.ifc");
	new ShowEmptyGroudingNames("C:\\jo\\IFCtest_data\\inp3.ifc");
    }

    public static String f(double d)
    {
	DecimalFormat formatter = new DecimalFormat("#0.000");
	DecimalFormatSymbols ds=new DecimalFormatSymbols();
	ds.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(ds);
	return formatter.format(d);
    }

    public static void main(String[] args) {
      ShowEmptyGroudingNames.tiny_testset();

    }

}
