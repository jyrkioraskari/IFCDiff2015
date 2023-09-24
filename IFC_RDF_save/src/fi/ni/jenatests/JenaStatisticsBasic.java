package fi.ni.jenatests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
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
import fi.ni.vo.Link;
import fi.ni.vo.Triple;
import fi.ni.vo.ValuePair;


public class JenaStatisticsBasic {

    
    private Model drum_statistics_model;
    Resource test_file;
    Resource test_results;
    static String ns = "http://drum/diff#";
    static String test_uri="test/tbasic";
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

    Property statistics_avg_outgoing_links;
    Property statistics_avg_incoming_links;

    Property statistics_max_outgoing_links;
    Property statistics_max_incoming_links;

    Property statistics_max_outgoing_links_class;
    Property statistics_max_incoming_links_class;
    
    Property statistics_has_dublicate_guids;
    Property statistics_literals;
    Property statistics_iris;
    Property statistics_types;
    Property statistics_blanks;
    Property statistics_nodes;

    Property statistics_application_name;
    
    Property statistics_avg_outgoing_links_inclLiterals;
    Property statistics_max_outgoing_links_inclLiterals;

    static String rdf_filename="c:\\jo/DRUM/statistics_tbasic.ttl";

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
        drum_statistics_model.add(test,test_name,"basic msgs. No grounding.");
	
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
        
        statistics_avg_outgoing_links = drum_statistics_model.createProperty( ns + "avg_outgoing_links");
        statistics_avg_incoming_links  = drum_statistics_model.createProperty( ns + "avg_incoming_links");

        statistics_max_outgoing_links = drum_statistics_model.createProperty( ns + "max_outgoing_links");
        statistics_max_incoming_links  = drum_statistics_model.createProperty( ns + "max_incoming_links");

        statistics_max_outgoing_links_class = drum_statistics_model.createProperty( ns + "max_outgoing_links_class");
        statistics_max_incoming_links_class  = drum_statistics_model.createProperty( ns + "max_incoming_links_class");
        

        statistics_has_dublicate_guids =  drum_statistics_model.createProperty( ns + "has_dublicate_guids");
        statistics_application_name =  drum_statistics_model.createProperty( ns + "application");

        statistics_avg_outgoing_links_inclLiterals = drum_statistics_model.createProperty( ns + "avg_outgoing_links_inclLiterals");
        statistics_max_outgoing_links_inclLiterals = drum_statistics_model.createProperty( ns + "max_outgoing_links_inclLiterals");

        statistics_literals =  drum_statistics_model.createProperty( ns + "literals");
        statistics_iris =  drum_statistics_model.createProperty( ns + "IRIs");
        statistics_types =  drum_statistics_model.createProperty( ns + "types");
        statistics_blanks =  drum_statistics_model.createProperty( ns + "blanks");
        statistics_nodes =  drum_statistics_model.createProperty( ns + "nodes");

    }
    
    public JenaStatisticsBasic(String filename) {
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

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	drum_statistics_model.add(test_results,statistics_has_dublicate_guids,model.has_duplicate_guids+"");
	
	String application = "";

	double outgoing_links=0f;
	int max_outgoing_links=Integer.MIN_VALUE;
	String max_outgoing_class = "";

	double outgoing_links_incliterals=0f;
	int max_outgoing_links_incliterals=Integer.MIN_VALUE;
	
	
	model.checkUniques(false);
	
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();	    
	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    
	    int incliterals=t1.i.drum_getParameterAttributes().size()+ifcs.size();
	    outgoing_links_incliterals+=incliterals;
	    if(incliterals>max_outgoing_links_incliterals)
		max_outgoing_links_incliterals=incliterals;
	    
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
       
       double avg_outcoming_links_incliterals=outgoing_links_incliterals/((double)model.object_buffer.size());
       drum_statistics_model.add(test_results,statistics_avg_outgoing_links_inclLiterals,f(avg_outcoming_links_incliterals));

       drum_statistics_model.add(test_results,statistics_avg_outgoing_links,f(avg_outcoming_links));
       drum_statistics_model.add(test_results,statistics_avg_incoming_links,f(avg_incoming_links));

       drum_statistics_model.add(test_results,statistics_max_outgoing_links,max_outgoing_links+"");
       drum_statistics_model.add(test_results,statistics_max_incoming_links,max_incoming_links+"");
       drum_statistics_model.add(test_results,statistics_max_outgoing_links_inclLiterals,max_outgoing_links_incliterals+"");

       drum_statistics_model.add(test_results,statistics_max_outgoing_links_class,max_outgoing_class);
       drum_statistics_model.add(test_results,statistics_max_incoming_links_class,max_incoming_class);
       
	//System.out.println("----------------------------------------");
	System.gc();
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
	


	
	
	long literals=0;
	Set<String> classnameSet=new HashSet<String>(1000000);
	Set<Object> nodes=new HashSet<Object>(10000000);
	Set<Object> iris=new HashSet<Object>(1000000);
	Set<Object> blanks=new HashSet<Object>(1000000);
	for(int i=0;i<model.msgs.size();i++)
	{
	  for(int n=0;n<model.msgs.get(i).size();n++)
	  {
            Triple t=model.msgs.get(i).get(n);
	    nodes.add(t.s);
	    classnameSet.add(t.s.getClass().getSimpleName()); 
	    if(t.s.is_grounded)
		iris.add(t.s);
	    else
		blanks.add(t.s);
	    
	    if(Thing.class.isInstance(t.o))
	    {
		nodes.add(t.o);
		Thing to=(Thing)t.o;
		    classnameSet.add(to.getClass().getSimpleName()); 
		if(to.is_grounded)
		   iris.add(to);
		else
		   blanks.add(to);
	    }
	    else
	     literals++;
	  }
	}
	drum_statistics_model.add(test_results,statistics_literals,literals+"");
	drum_statistics_model.add(test_results,statistics_types,classnameSet.size()+"");
	drum_statistics_model.add(test_file,statistics_nodes,(nodes.size()+literals)+"");
	drum_statistics_model.add(test_file,statistics_iris,iris.size()+"");
	drum_statistics_model.add(test_file,statistics_blanks,blanks.size()+"");
   }

    public static void tiny_testset() {	
	new JenaStatisticsBasic("C:\\jo\\IFCtest_data\\drum_10.ifc");
    }


    public static void basic_testset() {	
	new JenaStatisticsBasic("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Crane.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new JenaStatisticsBasic("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\OfficeBuilding.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
    }
    
    public static void complete_testset() {	
	new JenaStatisticsBasic("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Crane.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new JenaStatisticsBasic("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\OfficeBuilding.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\SMC Building - modified.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\SMC Building.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new JenaStatisticsBasic("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\Planer 4B Full.ifc");
	new JenaStatisticsBasic("C:\\jo\\models\\HiB_DuctWork.ifc");
	new JenaStatisticsBasic("C:\\jo\\IFCtest_data\\inp3.ifc");
    }

    public static void run(String file)
    {
	new JenaStatisticsBasic(file);
    }
    public static void official_run()
    {
	//run("C:\\jo\\TESTSET/Duplex_A_20110907_optimized.ifc");
	//run("C:\\jo\\TESTSET/Duplex_MEP_20110907.ifc");
	/*
	run("C:\\jo\\TESTSET/Office_A_20110811.ifc");
	run("C:\\jo\\TESTSET/Office_A_20110811_optimized.ifc");
	
	run("C:\\jo\\TESTSET/Office_S_20110811.ifc");
	run("C:\\jo\\TESTSET/Office_MEP_20110811.ifc");
	run("C:\\jo\\TESTSET/Clinic_A_20110906.ifc");
	run("C:\\jo\\TESTSET/Clinic_S_20110715.ifc");
	run("C:\\jo\\TESTSET/SMC Building.ifc");*/
	run("C:\\jo\\TESTSET/inp3.ifc");
/*	run("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	run("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	run("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");

	run("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	run("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	run("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	run("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	run("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	run("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	run("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	run("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	run("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	run("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	run("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	run("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
*/
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
	
      JenaStatisticsBasic.set_test_properties();
      //JenaStatisticsBasic.basic_testset();
      JenaStatisticsBasic.official_run();
    }

}
