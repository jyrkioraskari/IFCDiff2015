package fi.ni.jenatests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.bag.HashBag;

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
import fi.ni.vo.Triple;


public class JenaGroundablesByLinks_SecondOrderLiterals {

    private Model drum_statistics_model;
    Resource test_file;
    Resource test_results;
    static String ns = "http://drum/diff#";
    static String test_uri="test/tbasic_max_and_min_second_order_literals";
    Resource test;
    
    Property testset;
    Property test_result;
    Property timestamp;

    Property statistics_entity_count;

    Property statistics_max_msg_size;
    Property statistics_max_msg_size_percentage;
    Property statistics_msg_count;
    
    Property statistics_triplecount;
    
    Property statistics_guids;
    Property statistics_groudable_by_big_outs;
    Property statistics_groudable_by_big_ins;
    
    Property statistics_groudable_by_small_outs;
    Property statistics_groudable_by_small_ins;
    
    Property statistics_maxmsg_mostcommon_property;
    Property statistics_maxmsg_mostcommon_class;

    static String rdf_filename="c:\\jo/DRUM/statistics_tbasic_max_and_min_second_order_literals.ttl";

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
        drum_statistics_model.add(test,test_name,"grouded by only max and min in and out links, dist  5, literals grounded gs:(false, true, true, true, true, true, false, true), second order literals");
	
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
        

        statistics_guids = drum_statistics_model.createProperty( ns + "guids");
        statistics_groudable_by_big_outs = drum_statistics_model.createProperty( ns + "groudable_by_big_outs");
        statistics_groudable_by_big_ins = drum_statistics_model.createProperty( ns + "groudable_by_big_ins");
        
        statistics_groudable_by_small_outs = drum_statistics_model.createProperty( ns + "groudable_by_small_outs");
        statistics_groudable_by_small_ins = drum_statistics_model.createProperty( ns + "groudable_by_small_ins");

        statistics_maxmsg_mostcommon_property = drum_statistics_model.createProperty( ns + "maxmsg_secondcommon_property");
        statistics_maxmsg_mostcommon_class = drum_statistics_model.createProperty( ns + "maxmsg_mostcommon_class");


    }

    
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
    DecComparator dec_comparetor=new DecComparator();
    
    public JenaGroundablesByLinks_SecondOrderLiterals(String filename) {
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
	//System.out.println("----------------------------------------");

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	String application = "";

	double outgoing_links=0f;
	int max_outgoing_links=Integer.MIN_VALUE;
	String max_outgoing_class = "";
	
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram_acc=new TreeMap<Integer,SortedMap<String,Integer>>(this.acc_comparator);  // links count <class name, number or individual entities>
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparetor);  // links count <class name, number or individual entities>
	
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
	    if(current_map==null)
	    {
		current_map=new TreeMap<String,Integer>();
		outgoing_histogram_acc.put(ifcs.size(),current_map);
		outgoing_histogram_dec.put(ifcs.size(),current_map);
	    }
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
	SortedMap<Integer,SortedMap<String,Integer>> incoming_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparetor);  // links count <class name, number or individual entities>
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
	    if(current_map==null)
	    {
		current_map=new TreeMap<String,Integer>();
		incoming_histogram_acc.put(t1.incoming_count,current_map);
		incoming_histogram_dec.put(t1.incoming_count,current_map);
	    }
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
	

       Map<String,Integer> small_candidates_out=new HashMap<String,Integer>();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : outgoing_histogram_acc.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Integer i=small_candidates_out.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	        	 small_candidates_out.put(class_name, key);
	           }
	           
	           // Count can be any for removal
	           if(i!=null)
	           {
	             if(Math.abs(key-i)<5)
	        	     small_candidates_out.put(class_name,-1);
	           }
	           
	   }
	 }

       Map<String,Integer> small_candidates_in=new HashMap<String,Integer>();  // Class name, num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : incoming_histogram_acc.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Integer i=small_candidates_in.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	             {
	        	 small_candidates_in.put(class_name, key);
	             }
	           }
	           
	           // Count can be any for removal
	           if(i!=null)
	           {
	             if(Math.abs(key-i)<5)
	        	     small_candidates_in.put(class_name,-1);
	           }
	   }
	 }

       Map<String,Integer> big_candidates_out=new HashMap<String,Integer>();  // Class name and num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : outgoing_histogram_dec.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Integer i=big_candidates_out.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	        	 big_candidates_out.put(class_name, key);
	           }
	           
	           // Count can be any for removal
	           if(i!=null)
	           {
	             if(Math.abs(i-key)<5)
	        	     big_candidates_out.put(class_name,-1);
	           }
	   }
	 }

       Map<String,Integer> big_candidates_in=new HashMap<String,Integer>();  // Class name and num of links
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : incoming_histogram_dec.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           Integer i=big_candidates_in.get(class_name);
	           if(class_value==1)
	           {
	             if(i==null)
	        	 big_candidates_in.put(class_name, key);
	           }
	           
	           // Count can be any for removal
	           if(i!=null)
	           {
	             if(Math.abs(i-key)<5)
	        	     big_candidates_in.put(class_name,-1);
	           }
	   }
	 }

       
	long counter_groudable_by_big_ins=0;
	long counter_groudable_by_big_outs=0;
	long counter_groudable_by_small_ins=0;
	long counter_groudable_by_small_outs=0;

	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    int out_links=t1.i.drum_getIfcClassAttributes_notInverses().size();
	    int in_links=t1.incoming_count;

	    Integer c;
	    c=big_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==out_links)
		{
		    counter_groudable_by_big_outs++;
		}
	    }

	    c=big_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==in_links)
		{
		    counter_groudable_by_big_ins++;
		}
	    }


	    c=small_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==out_links)
		{
		    counter_groudable_by_small_outs++;
		}
	    }
	    

	    c=small_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==in_links)
		{
		    counter_groudable_by_small_ins++;
		}
	    }
	    
	    
	}

	drum_statistics_model.add(test_results,statistics_groudable_by_big_ins,counter_groudable_by_big_ins+"");
	drum_statistics_model.add(test_results,statistics_groudable_by_big_outs,counter_groudable_by_big_outs+"");
	drum_statistics_model.add(test_results,statistics_groudable_by_small_ins,counter_groudable_by_small_ins+"");
	drum_statistics_model.add(test_results,statistics_groudable_by_small_outs,counter_groudable_by_small_outs+"");

	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    int out_links=t1.i.drum_getIfcClassAttributes_notInverses().size();
	    int in_links=t1.incoming_count;

	    Integer c;
	    c=big_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==out_links)
		{
		    //System.out.println("found: "+t1.line_number+" "+t1.getClass().getSimpleName()+" out."+c);
		    t1.is_grounded=true;
		}
	    }

	    if(t1.is_grounded)
		continue;
	    c=big_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==in_links)
		{
		    //System.out.println("found: "+t1.line_number+" "+t1.getClass().getSimpleName()+" in."+c);
		    t1.is_grounded=true;
		}
	    }

	    if(t1.is_grounded)
		continue;

	    c=small_candidates_out.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==out_links)
		{
		    //System.out.println("found: "+t1.line_number+" "+t1.getClass().getSimpleName()+" out."+c);
		    t1.is_grounded=true;
		}
	    }
	    
	    if(t1.is_grounded)
		continue;

	    c=small_candidates_in.get(t1.getClass().getSimpleName());
	    if(c!=null)
	    {
		if(c.intValue()==in_links)
		{
		    //System.out.println("found: "+t1.line_number+" "+t1.getClass().getSimpleName()+" in."+c);
		    t1.is_grounded=true;
		}
	    }
	    
	    
	}

	//System.out.println("----------------------------------------");
	System.gc();
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	GroundSettings gs=new GroundSettings(true, true, true, true, true, false, true,true);	       
	model.groundFromGUIDs(common_gids,gs);
	model.createHugeLinksMap();
	System.gc();
	model.deduceMSGs();
	long triples=0;
	int max=Integer.MIN_VALUE;
	int maxi=-1;
	for(int i=0;i<model.msgs.size();i++)
	{
	    if(max<model.msgs.get(i).size())
	    {
		max=model.msgs.get(i).size();
		maxi=i;
	    }
	    triples+=model.msgs.get(i).size();
	}
	
	HashBag maxmsg_properties=new HashBag();
	HashBag maxmsg_classes=new HashBag();
	for(int n=0;n<model.msgs.get(maxi).size();n++)
	{
	    Triple t=model.msgs.get(maxi).get(n);
	    if(!t.p.equals("a"))
	      maxmsg_properties.add(t.p);
	    maxmsg_classes.add(t.s.getClass().getSimpleName());
	    if(!t.literal)
		maxmsg_classes.add(t.o.getClass().getSimpleName());
	}
	
	Iterator<String> ip=maxmsg_properties.uniqueSet().iterator();
	String maxmsg_mostcommon_property="";
	int maxval=Integer.MIN_VALUE;
	while(ip.hasNext())
	{
	    String p=ip.next();
	    int count=maxmsg_properties.getCount(p);
	    if(count>maxval)
	    {
		maxval=count;
		maxmsg_mostcommon_property=p;
	    }
	}
	
	Iterator<String> ic=maxmsg_classes.uniqueSet().iterator();
	String maxmsg_mostcommon_class="";
	maxval=Integer.MIN_VALUE;
	while(ic.hasNext())
	{
	    String c=ic.next();
	    int count=maxmsg_properties.getCount(c);
	    if(count>maxval)
	    {
		maxval=count;
		maxmsg_mostcommon_class=c;
	    }
	}

	drum_statistics_model.add(test_results,statistics_maxmsg_mostcommon_property ,maxmsg_mostcommon_property);
	drum_statistics_model.add(test_results,statistics_maxmsg_mostcommon_class,maxmsg_mostcommon_class);
        
	double pers=((double)max)/((double)triples);
	System.gc();
	System.out.println(application+";"+filename+";"+max+";"+f(pers));//";"+model.gid_map.size()+";"+triples);
	drum_statistics_model.add(test_results,statistics_entity_count,model.object_buffer.size()+"");
	drum_statistics_model.add(test_results,statistics_max_msg_size,max+"");
	drum_statistics_model.add(test_results,statistics_max_msg_size_percentage,f(pers)+"");
	drum_statistics_model.add(test_results,statistics_msg_count,model.msgs.size()+"");
	drum_statistics_model.add(test_results,statistics_triplecount,triples+"");

	drum_statistics_model.add(test_results,statistics_guids,model.gid_map.size()+"");

   }

    public static void tiny_testset() {	
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\IFCtest_data\\drum_10.ifc");
    }

    public static void basic_testset() {	
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Crane.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\OfficeBuilding.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
    }
    
    public static void complete_testset() {	
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Crane.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\OfficeBuilding.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\SMC Building - modified.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\SMC Building.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\Planer 4B Full.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\models\\HiB_DuctWork.ifc");
	new JenaGroundablesByLinks_SecondOrderLiterals("C:\\jo\\IFCtest_data\\inp3.ifc");
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
       JenaGroundablesByLinks_SecondOrderLiterals.set_test_properties();
	/*
      System.out.print("filename; application; entities; has_duplicate_guids");
      System.out.print("; max_outgoing_links; avg_outgoing_links; max outgoing links class");
      System.out.print("; max_incoming_links;  avg_incoming_links; max incoming links class\"");
      System.out.println();*/

      //GroundablesByLinks.complete_testset();
      //JenaGroundablesByLinks.basic_testset();
       JenaGroundablesByLinks_SecondOrderLiterals.basic_testset();
    }

}
