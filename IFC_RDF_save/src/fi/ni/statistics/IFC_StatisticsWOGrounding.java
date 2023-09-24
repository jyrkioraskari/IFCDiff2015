package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.Tree;

import fi.ni.ExpressReader;
import fi.ni.IFC_CLassModelConstants;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcRoot;
import fi.ni.vo.DiffDescription;
import fi.ni.vo.Triple;
import fi.ni.vo.ValuePair;

/*
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/
 */

public class IFC_StatisticsWOGrounding {

    ExpressReader er;

    public IFC_StatisticsWOGrounding(String express_file, String ifc_file) {

	try{
	er = new ExpressReader(express_file);
	System.gc();
	Long smem1=Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
	Long start=System.currentTimeMillis();
	IFC_ClassModel model = new IFC_ClassModel(ifc_file, er.getEntities(), er.getTypes(), "model");
	
	
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());

	// DIFFERENCE IS HERE
	model.checkUniques();

	System.gc();
	model.createHugeLinksMap();
	System.gc();
	model.deduceMSGs();
	System.gc();
	Long end=System.currentTimeMillis();
	long triples=0;
	int max=Integer.MIN_VALUE;
	for(int i=0;i<model.msgs.size();i++)
	{
	    if(max<model.msgs.get(i).size())
		max=model.msgs.get(i).size();
	    triples+=model.msgs.get(i).size();
	}
	long literals=0;
	String application="";
	Set<String> classnameSet=new HashSet<String>();
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if(IfcApplication.class.isInstance(t))
	    {
		IfcApplication appl=(IfcApplication)t;
		application=appl.getApplicationFullName();
	    }
	    classnameSet.add(t.getClass().getSimpleName()); 
		List<ValuePair> palist=t.i.drum_getParameterAttributes();
		for(int i=0;i<palist.size();i++)
		{
		    ValuePair vp=palist.get(i);
		    classnameSet.add(vp.getName().getClass().getSimpleName()); 
		if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
			continue;
		if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
			continue;
		literals++;
		}

	}
	
	
	long min_msg_guids=Long.MAX_VALUE;
	long max_msg_guids=Long.MIN_VALUE;
	double avg_msg_guids=0;
	double msg_guids_count=0;
        List<List<IfcRoot>> guids_in_msgs=new ArrayList<List<IfcRoot>>();
	
	for(int i=0;i<model.msgs.size();i++)
	{
	    
	        List<IfcRoot> ml=new ArrayList<IfcRoot>();
	        guids_in_msgs.add(ml);

	        List<Triple> msg=model.msgs.get(i);
	        Set<IfcRoot> gset=new HashSet<IfcRoot>();
		for(int j=0;j<msg.size();j++)
		{
	             if(IfcRoot.class.isInstance(msg.get(j).s))
	             {
	        	 IfcRoot x1=(IfcRoot)msg.get(j).s;
		         gset.add(x1);
	             }
		     if(IfcRoot.class.isInstance(msg.get(j).o))
		     {
	        	 IfcRoot x2=(IfcRoot)msg.get(j).o;
		         gset.add(x2);
		     }
		}
		msg_guids_count+=gset.size();
	        Iterator it=gset.iterator();
                while (it.hasNext()) {
                        ml.add((IfcRoot)it.next());
                } 		
                if(gset.size()<min_msg_guids)
		    min_msg_guids=gset.size();
		if(gset.size()>max_msg_guids)
		    max_msg_guids=gset.size();
		
	}
	avg_msg_guids=msg_guids_count/((double)model.msgs.size());
	
	
	long min_rdfn_guids=Long.MAX_VALUE;
	long max_rdfn_guids=Long.MIN_VALUE;
	double avg_rdfn_guids=0;
	double rdfn_guids_count=0;

	for (Map.Entry<String, Thing> entry : model.gid_map.entrySet()) {
	    IfcRoot t1 = (IfcRoot) entry.getValue();
	        long count=0;
	        
		for(int i=0;i<guids_in_msgs.size();i++)
		{
		    boolean is_there=false;
		    List<IfcRoot> msg=guids_in_msgs.get(i);
		    if(msg.contains(t1))
		    {
			count++;
			rdfn_guids_count++;
		    }
		}
		if(count<min_rdfn_guids)
		    min_rdfn_guids=count;
		if(count>max_rdfn_guids)
		    max_rdfn_guids=count;
	    
	}
	avg_rdfn_guids=rdfn_guids_count/((double)model.gid_map.size());

	double avgmsgsize=((double)triples)/ ((double)model.msgs.size());
	Long smem2=Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
	System.out.println(application+";"+ifc_file+";"+model.msgs.size()+";"+model.object_buffer.size()+";"+max+ ";"+triples+ ";"+model.gid_map.size()+";"+literals+";"+(model.object_buffer.size()+classnameSet.size()+literals)+";"+classnameSet.size()+";"+f(avgmsgsize)+";"+min_msg_guids+";"+max_msg_guids+";"+f(avg_msg_guids)+";"+min_rdfn_guids+";"+max_rdfn_guids+";"+f(avg_rdfn_guids)
); //"+(end-start)+", "+(smem2-smem1));
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private String guid_tree2String(Tree<ValuePair> t) {
	return guid_tree2String(t, 0).toString();
    }

    private StringBuffer guid_tree2String(Tree<ValuePair> t, int increment) {
	StringBuffer s = new StringBuffer();
	StringBuffer inc = new StringBuffer();
	for (int i = 0; i < increment; ++i) {
	    inc.append(" ");
	}
	ValuePair vp=t.getHead();
	{
	    s.append('\n');
	    s.append(inc);
	    s.append(t.getHead().getName());
	    s.append("-");
	    s.append(t.getHead().getValue());
	}
	for (int n = 0; n < t.getLeafs().size(); n++) {
	    Tree<ValuePair> child = (Tree<ValuePair>) t.getLeafs().get(n);
	    s.append(guid_tree2String(child, increment + 2));

	}
	return s;
    }

    private String tree2String(Tree<Thing> t, DiffDescription diff_description) {
	return tree2String(t, diff_description, 0).toString();
    }

    private StringBuffer tree2String(Tree<Thing> t, DiffDescription diff_description, int increment) {
	StringBuffer s = new StringBuffer();
	StringBuffer inc = new StringBuffer();
	for (int i = 0; i < increment; ++i) {
	    inc.append(" ");
	}
	s.append(inc);
	s.append(t.getHead());
	if (IfcRoot.class.isInstance(t.getHead())) {
	    String sgid = ((IfcRoot) t.getHead()).getGlobalId();
	    if (diff_description.getAdd_set().contains(sgid)) {
		s.append(' ');
		s.append("ADDED");
	    }
	    if (diff_description.getRemoved_set().contains(sgid)) {
		s.append(' ');
		s.append("REMOVED");
	    }
	    if (diff_description.getChange_set().contains(sgid)) {
		s.append(' ');
		s.append("CHANGED");
	    }
	    if (diff_description.getReplace_set().contains(sgid)) {
		s.append(' ');
		s.append("REPLACED");
	    }
	}
	for (int n = 0; n < t.getLeafs().size(); n++) {
	    Tree<Thing> child = (Tree<Thing>) t.getLeafs().get(n);
	    s.append('\n');
	    s.append(tree2String(child, diff_description, increment + 2));
	}
	return s;
    }

    public static void mikro_testset()
    {
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc");
    }

    
    public static void fast_testset()
    {
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\sample.ifc");
	       
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc");
	        //new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\door.ifc");
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "Ground_Floor_Vent.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "First_Floor_Vent.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "Second_Floor_Vent.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "Smiley-West-5-Buildings-14-10-2005.ifc"); 
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "OfficeBuilding.ifc");  	
    }
    
    
    public  static void complete_testset()
    {
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\sample.ifc");
	       
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc");
	       //new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\door.ifc");
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\talo_testi.ifc");
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\HiB_DuctWork.Ifc");
	       //new IFC_Statistics("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\HiB_PipeWork.Ifc");
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\Planer 4B Full.IFC");
		
	       new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
		
	       
		new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
		new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
		new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\FJK-Project-Final.ifc");
		new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
		new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
		new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");

	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\inp3.ifc");  	

	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\AC14-FZK-Haus.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");  	
	        //new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\RAK_Helmisimpukka_20101217.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\Crane.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\Ground_Floor_Vent.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\First_Floor_Vent.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\Second_Floor_Vent.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\OfficeBuilding.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\SMC Building - modified.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\SMC Building.ifc");  	
	        new IFC_StatisticsWOGrounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc"); 
    }
    
    public static String f(double d)
    {
	DecimalFormat formatter = new DecimalFormat("#0.00");
	DecimalFormatSymbols ds=new DecimalFormatSymbols();
	ds.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(ds);
	return formatter.format(d);
    }
    public static void main(String[] args) {
	System.out.println(" Application; file;msgs;entities;max;triples;guids;literals;nodes; used_class_names; avgmsgsize; min_msg_guids; max_msg_guids; avg_msg_guids; min_rdfn_guids; max_rdfn_guids; avg_rdfn_guids");
	System.out.println("done successfully");
	IFC_StatisticsWOGrounding.complete_testset();
    }

}
