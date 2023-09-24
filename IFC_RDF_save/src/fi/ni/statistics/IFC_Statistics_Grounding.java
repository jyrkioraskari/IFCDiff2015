package fi.ni.statistics;

import java.util.ArrayList;
import java.util.HashSet;
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
import fi.ni.vo.GroundSettings;
import fi.ni.vo.ValuePair;

/*
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/
 */

public class IFC_Statistics_Grounding {

    ExpressReader er;

    public IFC_Statistics_Grounding(String express_file, String ifc_file,GroundSettings gs) {

	try
	{
	er = new ExpressReader(express_file);
	System.gc();
	Long smem1=Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
	Long start=System.currentTimeMillis();
	IFC_ClassModel model = new IFC_ClassModel(ifc_file, er.getEntities(), er.getTypes(), "model");
	
	
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());

	// DIFFERENCE IS HERE
	model.groundFromGUIDs(common_gids,gs);

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
	double avgmsgsize=((double)triples)/ ((double)model.msgs.size());
	Long smem2=Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
	System.out.println(application+","+ifc_file+",\t"+model.msgs.size()+",\t"+model.object_buffer.size()+",\t"+max+ ",\t"+triples+ ",\t"+model.gid_map.size()+", "+literals+", "+(model.object_buffer.size()+classnameSet.size()+literals)+","+classnameSet.size()+", "+avgmsgsize); //"+(end-start)+", "+(smem2-smem1));
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

    public static void fast_test_set(String testmodelfile)
    {
	    boolean ifcOwnerHistory_handle_as_literal=false;
	    boolean ifcOwnerHistory_handle_as_grounded=false;

	    boolean set_literals=false;
	    boolean test_unique_loops=false;
	    boolean ifcGeometricRepresentationContext_ground=false;
	    boolean ground_On_One2OneLinks_on=false;
	    boolean ground_AnyLinks_on=false;
	    boolean use_distance_from_core=false;

	    
	    ifcOwnerHistory_handle_as_grounded=false;
	    for(int i=0;i<64;i++)
	    {
		    // RESET
		    System.out.print("-");
		    set_literals=false;
		    test_unique_loops=false;
		    ifcGeometricRepresentationContext_ground=false;
		    ground_On_One2OneLinks_on=false;
		    ground_AnyLinks_on=false;
		    use_distance_from_core=false;
		
		    if (((i >>> 0) & 1) != 0) {
                        System.out.print(" L");
                        set_literals=true;
		    }
		    if (((i >>> 1) & 1) != 0) {
                           System.out.print(" UL");
                           test_unique_loops=true;
		    }
		    if (((i >>> 2) & 1) != 0) {
                        System.out.print(" GPG");
			ifcGeometricRepresentationContext_ground=true;
		    }
		    if (((i >>> 3) & 1) != 0) {
                        System.out.print(" O2O");
    		        ground_On_One2OneLinks_on=true;
		    }
		    if (((i >>> 4) & 1) != 0) {
                        System.out.print(" ANY");
    		        ground_AnyLinks_on=true;
		    }
		    if (((i >>> 5) & 1) != 0) {
                        System.out.print(" CD");
                        use_distance_from_core=true;
		    }
	            System.out.print(",");
	            GroundSettings gs=new GroundSettings(ifcOwnerHistory_handle_as_grounded, set_literals, test_unique_loops, ifcGeometricRepresentationContext_ground, ground_On_One2OneLinks_on, ground_AnyLinks_on, use_distance_from_core);
		    new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", testmodelfile,gs);
	    }

	    ifcOwnerHistory_handle_as_grounded=true;
	
	    for(int i=0;i<64;i++)
	    {
		    // RESET
		    System.out.print("OHL");
		    set_literals=false;
		    test_unique_loops=false;
		    ifcGeometricRepresentationContext_ground=false;
		    ground_On_One2OneLinks_on=false;
		    ground_AnyLinks_on=false;
		    use_distance_from_core=false;
		
		    if (((i >>> 0) & 1) != 0) {
                        System.out.print(" L");
                        set_literals=true;
		    }
		    if (((i >>> 1) & 1) != 0) {
                           System.out.print(" UL");
                           test_unique_loops=true;
		    }
		    if (((i >>> 2) & 1) != 0) {
                        System.out.print(" GPG");
			ifcGeometricRepresentationContext_ground=true;
		    }
		    if (((i >>> 3) & 1) != 0) {
                        System.out.print(" O2O");
    		        ground_On_One2OneLinks_on=true;
		    }
		    if (((i >>> 4) & 1) != 0) {
                        System.out.print(" ANY");
    		        ground_AnyLinks_on=true;
		    }
		    if (((i >>> 5) & 1) != 0) {
                        System.out.print(" CD");
                        use_distance_from_core=true;
		    }
	            System.out.print(",");
	            GroundSettings gs=new GroundSettings(ifcOwnerHistory_handle_as_grounded, set_literals, test_unique_loops, ifcGeometricRepresentationContext_ground, ground_On_One2OneLinks_on, ground_AnyLinks_on, use_distance_from_core);
		    new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", testmodelfile,gs);
	    }


    }

    public static void fast_testsetMaxGrounding()
    {
	       System.out.println("Max grounding");
	       GroundSettings gs=new GroundSettings(true, true, true, true, true, true, true);	       
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\sample.ifc",gs);
	       
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc",gs);
	       //new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\door.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\talo_testi.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\First_Floor_Vent.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc",gs);
    }
    
    public  static void complete_testset()
    {
	       GroundSettings gs=new GroundSettings(true, true, false, true, true, true, true);	       
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\sample.ifc",gs);
	       
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc",gs);
	       //new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\door.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\talo_testi.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\First_Floor_Vent.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\HiB_DuctWork.Ifc",gs);
	       //new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\HiB_PipeWork.Ifc");
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc",gs);
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\Planer 4B Full.IFC",gs);
		
	       new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc",gs);
		
	       
		new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc",gs);
		new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc",gs);
		new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\FJK-Project-Final.ifc",gs);
		new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc",gs);
		new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc",gs);
		new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc",gs);

	        new IFC_Statistics_Grounding("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\inp3.ifc",gs);  	
	        
    }

    public static void main(String[] args) {
       System.out.println(" Ground based on:, Application, file:,\tmsgs:,\tentities:,\tmax:,\ttriples:,\tguids:,\tliterals:,\tnodes:, used_class_names:, avgmsgsize");
       IFC_Statistics_Grounding.fast_test_set("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	System.out.println("done successfully");
	
       
    }

}
