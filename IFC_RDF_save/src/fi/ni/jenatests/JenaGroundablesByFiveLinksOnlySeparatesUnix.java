package fi.ni.jenatests;

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


public class JenaGroundablesByFiveLinksOnlySeparatesUnix {
    
    private Model drum_statistics_model;
    Resource test_file;
    Resource test_results;
    static String ns = "http://drum/diff#";
    static String test_uri="test/t5_max_only";
    
    Resource test;    
    Property testset;

    
    Property test_result;
    Property timestamp;

    Property statistics_entity_count;

    Property statistics_testset_name;
    Property statistics_max_msg_size;
    Property statistics_max_msg_size_percentage;
    Property statistics_msg_count;
    
    Property statistics_triplecount;

    Property statistics_groudable_by_big_outs;
    Property statistics_groudable_by_big_ins;

    static String rdf_filename="c:\\jo/DRUM/statistics_t5_max.ttl";

    
    static public void set_test_properties()
    {
	
	Property test_name;
        
        Model drum_statistics_model = ModelFactory.createDefaultModel();
	try {
		drum_statistics_model.read(FileManager.get().open(rdf_filename), "","TTL");
	} catch (Exception e) {
	}

	
	drum_statistics_model.setNsPrefix( "ns", ns);
	Resource test =  drum_statistics_model.createResource(ns+test_uri);
	Property timestamp = drum_statistics_model.createProperty( ns + "timestamp" );

        test_name = drum_statistics_model.createProperty( ns + "testset_name");
	drum_statistics_model.add(test,test_name,"grouded by 5 max in and out links, literals grounded + gs:(false, true, true, true, true, true, false, true)");
	
	Date date=new Date();
	SimpleDateFormat sdate=new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
	drum_statistics_model.add(test,timestamp,sdate.format(date));

	FileOutputStream fstream;
	try {
	    fstream = new FileOutputStream(rdf_filename);
	    drum_statistics_model.write(fstream,"TTL");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

    }


    public void properties()
    {
	drum_statistics_model.setNsPrefix( "ns", ns);
	drum_statistics_model.setNsPrefix( "xml", "http://www.w3.org/2001/XMLSchema#");
	test_result = drum_statistics_model.createProperty( ns + "test_result" );
	timestamp = drum_statistics_model.createProperty( ns + "timestamp" );
        testset =drum_statistics_model.createProperty( ns + "test" );


	statistics_entity_count = drum_statistics_model.createProperty( ns + "entity_count" );

        statistics_max_msg_size = drum_statistics_model.createProperty( ns + "max_msg_size" );
        statistics_max_msg_size_percentage = drum_statistics_model.createProperty( ns + "max_msg_size_percentage" );
	statistics_msg_count = drum_statistics_model.createProperty( ns + "msg_count" );

        statistics_triplecount = drum_statistics_model.createProperty( ns + "triplecount");
        
        statistics_testset_name = drum_statistics_model.createProperty( ns + "testset_name");

        statistics_groudable_by_big_outs = drum_statistics_model.createProperty( ns + "groudable_by_big_outs");
        statistics_groudable_by_big_ins = drum_statistics_model.createProperty( ns + "groudable_by_big_ins");

    }

    
    private class DecComparator implements Comparator<Integer>
    {

	public int compare(Integer o1, Integer o2) {	    
	    return o2-o1;
	}
	
    }
    DecComparator dec_comparator=new DecComparator();

    public JenaGroundablesByFiveLinksOnlySeparatesUnix(String filename) {
	filename=ToolkitString.strReplaceLike(filename, "\\","/");

	this.drum_statistics_model = ModelFactory.createDefaultModel();
	try {
		drum_statistics_model.read(FileManager.get().open(rdf_filename), "","TTL");
	} catch (Exception e) {
	}
	Resource test =  drum_statistics_model.createResource(ns+test_uri);
	test_file
	  = drum_statistics_model.createResource(filename);
	test_results=drum_statistics_model.createResource();

	properties();	
	drum_statistics_model.add(test_file,test_result,test_results);
	Date date=new Date();
	SimpleDateFormat sdate=new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
	drum_statistics_model.add(test_results,timestamp,sdate.format(date));
	drum_statistics_model.add(test_results,testset,test);

	runStatistics(filename);

	FileOutputStream fstream;
	try {
	    fstream = new FileOutputStream(rdf_filename);
	    drum_statistics_model.write(fstream,"TTL");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }
    public void runStatistics(String filename) {

	ExpressReader er;

	er = new ExpressReader("IFC2X3_TC1.exp");
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

	drum_statistics_model.add(test_results,statistics_groudable_by_big_ins,counter_groudable_by_big_ins+"");
	drum_statistics_model.add(test_results,statistics_groudable_by_big_outs,counter_groudable_by_big_outs+"");

       
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
	}

	System.gc();
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	// NO basic GROUNDING
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
	drum_statistics_model.add(test_results,statistics_entity_count,model.object_buffer.size()+"");
	drum_statistics_model.add(test_results,statistics_max_msg_size,max+"");
	drum_statistics_model.add(test_results,statistics_max_msg_size_percentage,f(pers)+"");
	drum_statistics_model.add(test_results,statistics_msg_count,model.msgs.size()+"");
	drum_statistics_model.add(test_results,statistics_triplecount,triples+"");

   }

    public static String f(double d)
    {
	DecimalFormat formatter = new DecimalFormat("#0.000");
	DecimalFormatSymbols ds=new DecimalFormatSymbols();
	ds.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(ds);
	return formatter.format(d);
    }

    
    public static void run(String file)
    {
	new JenaGroundablesByFiveLinksOnlySeparatesUnix(file);
    }

    public static void main(String[] args) {
      JenaGroundablesByFiveLinksOnlySeparatesUnix.rdf_filename=args[0]+".tulos_sep_5max.ttl";
      JenaGroundablesByFiveLinksOnlySeparatesUnix.run(args[0]);

    }


}
