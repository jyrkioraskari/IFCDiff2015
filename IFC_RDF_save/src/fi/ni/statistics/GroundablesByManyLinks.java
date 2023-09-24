package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.Link;


public class GroundablesByManyLinks {

    
    private class AccComparator implements Comparator<Integer>
    {

	public int compare(Integer o1, Integer o2) {	    
	    return o1-o2;
	}
	
    }
    AccComparator acc_comparator=new AccComparator();
    private class DecComparator implements Comparator<Integer>
    {

	public int compare(Integer o1, Integer o2) {	    
	    return o2-o1;
	}
	
    }
    DecComparator dec_comparator=new DecComparator();
    
    public GroundablesByManyLinks(String filename) {
	ExpressReader er;

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	String application = "";

	double outgoing_links=0f;
	int max_outgoing_links=Integer.MIN_VALUE;
	String max_outgoing_class = "";
	
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram_acc=new TreeMap<Integer,SortedMap<String,Integer>>(this.acc_comparator);  // links count <class name, number or individual entities>
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparator);  // links count <class name, number or individual entities>
	
	model.checkUniques();
	
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
            if(IfcOwnerHistory.class.isInstance(t1))
        	continue;
	    if(t1.is_grounded)
		  continue;
	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    if(ifcs.size()>max_outgoing_links)
	    {
		max_outgoing_links=ifcs.size();
		max_outgoing_class=t1.getClass().getSimpleName();
	    }
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2=l.getTheOtherEnd(t1);
	        if(IfcOwnerHistory.class.isInstance(t2))
	        	continue;
		t2.incoming_count++;
		outgoing_links++;
	    }
	    SortedMap<String,Integer> current_map=outgoing_histogram_acc.get(ifcs.size());
	    if(current_map==null)                                    //!
	    {                                                        //!
		current_map=new TreeMap<String,Integer>();           //!
		outgoing_histogram_acc.put(ifcs.size(),current_map); //!
		outgoing_histogram_dec.put(ifcs.size(),current_map); //!
	    }                                                        //!
	    Integer current_count=current_map.get(t1.getClass().getSimpleName());
	    if(current_count==null)
		current_count=new Integer(0);
	    current_count=current_count+1;
	    current_map.put(t1.getClass().getSimpleName(),current_count);
	}

	double incoming_links=0f;
	String max_incoming_class = "";
	int max_incoming_links=Integer.MIN_VALUE;
	SortedMap<Integer,SortedMap<String,Integer>> incoming_histogram_acc=new TreeMap<Integer,SortedMap<String,Integer>>(this.acc_comparator);  // links count <class name, number or individual entities>
	SortedMap<Integer,SortedMap<String,Integer>> incoming_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparator);  // links count <class name, number or individual entities>
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    if(IfcOwnerHistory.class.isInstance(t1))
	        	continue;
	    if(t1.is_grounded)
		  continue;
	    if(t1.incoming_count>max_incoming_links)
	    {
		max_incoming_links=t1.incoming_count;
		max_incoming_class=t1.getClass().getSimpleName();
	    }
	    
	    
	    SortedMap<String,Integer> current_map=incoming_histogram_acc.get(t1.incoming_count);
	    if(current_map==null)                                                     //!
	    {                                                                         //!
		current_map=new TreeMap<String,Integer>();                            //!
		incoming_histogram_acc.put(t1.incoming_count,current_map);            //!
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
	
       double avg_outcoming_links=outgoing_links/((double)model.object_buffer.size());
       double avg_incoming_links=outgoing_links/((double)model.object_buffer.size());

       
     @SuppressWarnings("deprecation")
    /*MultiMap small_candidates_out=new MultiHashMap();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : outgoing_histogram_acc.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Collection<Integer> i=(Collection<Integer>)small_candidates_out.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	        	 small_candidates_out.put(class_name, key);
	             else
	        	 if(i.size()<5)
	        	     small_candidates_out.put(class_name, key);
	        	     
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
	              if((key-intval)<5)
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
       */
       /*
       MultiMap small_candidates_in=new MultiHashMap();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : incoming_histogram_acc.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Collection<Integer> i=(Collection<Integer>)small_candidates_in.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	        	 small_candidates_in.put(class_name, key);
	             else
	        	 if(i.size()<5)
	        	     small_candidates_in.put(class_name, key);
	        	     
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
	              if((key-intval)<5)
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
       */
       MultiMap big_candidates_out=new MultiHashMap();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : outgoing_histogram_dec.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Collection<Integer> i=(Collection<Integer>)big_candidates_out.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
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
	              if((key-intval)<5)
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
	              if((key-intval)<5)
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
			    t1.is_grounded=true;
			}

	             }
	    }

	    if(t1.is_grounded)
		continue;
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
			    t1.is_grounded=true;
			}

	             }
	    }

	    if(t1.is_grounded)
		continue;

	    /*
	    c=(Collection<Integer>)small_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
	             Iterator<Integer> it=c.iterator(); 
	             while(it.hasNext())  
	             {
	                Integer intval=(Integer)it.next();
	                if(intval<0) continue;
			if(intval.intValue()==out_links)
			{
			    t1.is_grounded=true;
			}

	             }
	    }
	    
	    if(t1.is_grounded)
		continue;

	    c=(Collection<Integer>)small_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
	             Iterator<Integer> it=c.iterator(); 
	             while(it.hasNext())  
	             {
	                Integer intval=(Integer)it.next();
	                if(intval<0) continue;
			if(intval.intValue()==in_links)
			{
			    t1.is_grounded=true;
			}

	             }
	    }
	    */
	    
	}

	System.gc();
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	GroundSettings gs=new GroundSettings(true, true, true, true, true, false, true,false);	       
	model.groundFromGUIDs(common_gids,gs);
	model.createHugeLinksMap();
	System.gc();
	model.deduceMSGs();
	long triples=0;
	int max=Integer.MIN_VALUE;
	for(int i=0;i<model.msgs.size();i++)
	{
	    if(max<model.msgs.get(i).size())
		max=model.msgs.get(i).size();
	    triples+=model.msgs.get(i).size();
	}
	double pers=((double)max)/((double)triples);
	System.gc();
	System.out.println(application+";"+filename+";"+max+";"+f(pers));//";"+model.gid_map.size()+";"+triples);

   }

    public static void tiny_testset() {	
	new GroundablesByManyLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Crane.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new GroundablesByManyLinks("C:\\jo\\IFCtest_data\\talo_testi.ifc");
    }

    
    public static void complete_testset() {	
	new GroundablesByManyLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Crane.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new GroundablesByManyLinks("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\OfficeBuilding.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\SMC Building - modified.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\SMC Building.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new GroundablesByManyLinks("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\Planer 4B Full.ifc");
	new GroundablesByManyLinks("C:\\jo\\models\\HiB_DuctWork.ifc");
	new GroundablesByManyLinks("C:\\jo\\IFCtest_data\\inp3.ifc");
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
	/*
      System.out.print("filename; application; entities; has_duplicate_guids");
      System.out.print("; max_outgoing_links; avg_outgoing_links; max outgoing links class");
      System.out.print("; max_incoming_links;  avg_incoming_links; max incoming links class\"");
      System.out.println();*/

      //GroundablesByLinks.complete_testset();
      GroundablesByManyLinks.complete_testset();
    }

}
