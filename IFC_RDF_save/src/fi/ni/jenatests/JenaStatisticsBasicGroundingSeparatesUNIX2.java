package fi.ni.jenatests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import softhema.system.toolkits.ToolkitString;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import fi.ni.ExpressReader;
import fi.ni.IFC_CLassModelConstants;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.Link;
import fi.ni.vo.ValuePair;


public class JenaStatisticsBasicGroundingSeparatesUNIX2 {

    
    private Model drum_statistics_model;
    Resource test_file;
    static String ns = "http://drum/diff#";
    
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

    Property statistics_avg_outgoing_links;
    Property statistics_avg_incoming_links;

    Property statistics_max_outgoing_links;
    Property statistics_max_incoming_links;

    Property statistics_max_outgoing_links_class;
    Property statistics_max_incoming_links_class;
    
    Property statistics_has_dublicate_guids;
    Property statistics_literals;
    Property statistics_application_name;
    
    static String rdf_filename="c:\\jo/DRUM/statistics_tbasic_grounding.ttl";

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

        
        statistics_avg_outgoing_links = drum_statistics_model.createProperty( ns + "avg_outgoing_links");
        statistics_avg_incoming_links  = drum_statistics_model.createProperty( ns + "avg_incoming_links");

        statistics_max_outgoing_links = drum_statistics_model.createProperty( ns + "max_outgoing_links");
        statistics_max_incoming_links  = drum_statistics_model.createProperty( ns + "max_incoming_links");

        statistics_max_outgoing_links_class = drum_statistics_model.createProperty( ns + "max_outgoing_links_class");
        statistics_max_incoming_links_class  = drum_statistics_model.createProperty( ns + "max_incoming_links_class");
        

        statistics_has_dublicate_guids =  drum_statistics_model.createProperty( ns + "has_dublicate_guids");
        statistics_literals =  drum_statistics_model.createProperty( ns + "literals");
        statistics_application_name =  drum_statistics_model.createProperty( ns + "application");

    }
    
    public JenaStatisticsBasicGroundingSeparatesUNIX2(String filename) {
	filename=ToolkitString.strReplaceLike(filename, "\\","/");

	this.drum_statistics_model = ModelFactory.createDefaultModel();
	try {
		drum_statistics_model.read(FileManager.get().open(rdf_filename), "","TTL");
	} catch (Exception e) {
	}
	test_file
	  = drum_statistics_model.createResource(filename);
	/*
	 * ifcOwnerHistory_handle_as_grounded
	 * set_literals
	 * test_unique_loops
	 * ifcGeometricRepresentationContext_ground
	 * ground_On_One2OneLinks_on
	 * ground_AnyLinks_on,	   
	   use_distance_from_core
	   second_order_literals
	 */	
	single_test(filename,"Globally_unique_naming",                      new GroundSettings(true,  false, false, false,false, false, false, false,true));

	FileOutputStream fstream;
	try {
	    fstream = new FileOutputStream(rdf_filename);
	    drum_statistics_model.write(fstream,"TTL");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    private void single_test(String filename,String testname, GroundSettings gs) {
	Resource test_results;

	Resource test =  drum_statistics_model.createResource(ns+testname);
	test_results=drum_statistics_model.createResource();

	properties();	
	drum_statistics_model.add(test_file,test_result,test_results);
	Date date=new Date();
	SimpleDateFormat sdate=new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
	drum_statistics_model.add(test_results,timestamp,sdate.format(date));
	drum_statistics_model.add(test_results,testset,test);
	
	runStatistics(filename,gs,test_results);
    }

    public void runStatistics(String filename,GroundSettings gs,Resource test_results) {
	ExpressReader er;

	er = new ExpressReader("IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	drum_statistics_model.add(test_results,statistics_has_dublicate_guids,model.has_duplicate_guids+"");
	
	String application = "";

	double outgoing_links=0f;
	int max_outgoing_links=Integer.MIN_VALUE;
	String max_outgoing_class = "";
	
	
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
	}

	double incoming_links=0f;
	String max_incoming_class = "";
	int max_incoming_links=Integer.MIN_VALUE;
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
	    incoming_links+=t1.incoming_count;
	    
	}

	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if (IfcApplication.class.isInstance(t)) {
		IfcApplication appl = (IfcApplication) t;
		application = appl.getApplicationFullName();
	    }
	}
	
       double avg_outcoming_links=outgoing_links/((double)model.object_buffer.size());
       double avg_incoming_links=incoming_links/((double)model.object_buffer.size());
       drum_statistics_model.add(test_results,statistics_avg_outgoing_links,f(avg_outcoming_links));
       drum_statistics_model.add(test_results,statistics_avg_incoming_links,f(avg_incoming_links));

       drum_statistics_model.add(test_results,statistics_max_outgoing_links,max_outgoing_class+"");
       drum_statistics_model.add(test_results,statistics_max_incoming_links,max_incoming_links+"");

       drum_statistics_model.add(test_results,statistics_max_outgoing_links_class,max_outgoing_class);
       drum_statistics_model.add(test_results,statistics_max_incoming_links_class,max_incoming_class);
       
	//System.out.println("----------------------------------------");
	System.gc();
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
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
	drum_statistics_model.add(test_results,statistics_application_name,application);

	drum_statistics_model.add(test_results,statistics_entity_count,model.object_buffer.size()+"");
	drum_statistics_model.add(test_results,statistics_max_msg_size,max+"");
	drum_statistics_model.add(test_results,statistics_max_msg_size_percentage,f(pers)+"");
	drum_statistics_model.add(test_results,statistics_msg_count,model.msgs.size()+"");
	drum_statistics_model.add(test_results,statistics_triplecount,triples+"");

	drum_statistics_model.add(test_results,statistics_guids,model.gid_map.size()+"");
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
	new JenaStatisticsBasicGroundingSeparatesUNIX2(file);
    }

    public static void main(String[] args) {
      JenaStatisticsBasicGroundingSeparatesUNIX2.rdf_filename=args[0]+".tulos_sep2.ttl";
      JenaStatisticsBasicGroundingSeparatesUNIX2.run(args[0]);

    }

}
