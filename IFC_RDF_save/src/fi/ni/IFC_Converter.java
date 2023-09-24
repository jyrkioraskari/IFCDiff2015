package fi.ni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.Tree;

import fi.ni.ifc2x3.IfcRoot;
import fi.ni.vo.DiffDescription;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.ReturnPair;
import fi.ni.vo.Triple;
import fi.ni.vo.ValuePair;

/*
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/
 */

public class IFC_Converter {

    ExpressReader er;

    public IFC_Converter(String express_file, String ifc_file, String output_file, String model_name) {

	er = new ExpressReader(express_file);
	
	IFC_ClassModel model = new IFC_ClassModel(ifc_file, er.getEntities(), er.getTypes(), model_name);
	/*System.out.println("file: "+ifc_file);
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing

	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids1.keySet());
	Map<Long, String> chksums1 = model.commonGIDSet_CalculateCRC32ForGuidAreas(common_gids);
        */

	//model.listRDF(output_file);
	
	/*System.out.println("---------------------------------------------------------------------------------");
	System.out.println("file: "+ifc_file);
	//model.listElements();
	//System.out.println("start done");
	//
	//System.out.println("grounding done");

	model.createLinksMap();  // general routine for MSG and groundings
	model.groundFromGUIDs();
	
	//System.out.println("links map done");
	model.deduceMSGs();
	TreeMap<Long,MSG_CRC> crcs=model.calculateMSG_CRCs();
	Set set = crcs.entrySet();
	// Get an iterator
	Iterator i = set.iterator();
	// Display elements
	while(i.hasNext()) {
	    Map.Entry me = (Map.Entry)i.next();
	    System.out.print(me.getKey() + ": ");
	    //System.out.println(me.getValue());
	} 
	//System.out.println(guid_tree2String(model.getCompleteElementsTree()));
	// rotate();
	// addWall();
	
*/
	addWallMSG();
	//addWall();
	//sample_remove();
	System.out.println("done successfully");
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
	//if (IfcRoot.class.isInstance(vp.getValue())) 
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

    public void addWall() {
	IFC_ClassModel drum1 = new IFC_ClassModel("C:\\export\\drum_10_A.ifc", er.getEntities(), er.getTypes(), "drum"); // addition
	IFC_ClassModel drum2 = new IFC_ClassModel("C:\\export\\drum_10_B.ifc", er.getEntities(), er.getTypes(), "drum");

	DiffDescription diff_description = showModifications(drum1, drum2);

	/*
	System.out.println("========================================");

	Tree<Thing> element_tree1 = drum1.getElementsTree();
	System.out.println("ELEMENTS alkutilanne:");
	System.out.println(tree2String(element_tree1, diff_description));

	Tree<Thing> element_tree2 = drum2.getElementsTree();
	System.out.println("ELEMENTS muutoksen jälkeen:");
	System.out.println(tree2String(element_tree2, diff_description));
        */
    }

    public void rotate() {
	IFC_ClassModel drum1 = new IFC_ClassModel("C:\\export\\drum1_3.ifc", er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel("C:\\export\\drum1_4.ifc", er.getEntities(), er.getTypes(), "drum");

	DiffDescription diff_description = showModifications(drum1, drum2);

	System.out.println("========================================");

	Tree<Thing> element_tree1 = drum1.getElementsTree();
	System.out.println("ELEMENTS alkutilanne:");
	System.out.println(tree2String(element_tree1, diff_description));

	Tree<Thing> element_tree2 = drum2.getElementsTree();
	System.out.println("ELEMENTS muutoksen jälkeen:");
	System.out.println(tree2String(element_tree2, diff_description));

    }

    public void sample_remove() {
	IFC_ClassModel drum1 = new IFC_ClassModel("C:\\jo\\small_diff\\sample1.ifc", er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel("C:\\jo\\small_diff\\sample2.ifc", er.getEntities(), er.getTypes(), "drum");

	DiffDescription diff_description = showModifications(drum1, drum2);

	System.out.println("========================================");

	Tree<Thing> element_tree1 = drum1.getElementsTree();
	System.out.println("ELEMENTS alkutilanne:");
	System.out.println(tree2String(element_tree1, diff_description));

	Tree<Thing> element_tree2 = drum2.getElementsTree();
	System.out.println("ELEMENTS muutoksen jälkeen:");
	System.out.println(tree2String(element_tree2, diff_description));

    }
    
    
    public void addWallMSG() {
	//IFC_ClassModel drum1 = new IFC_ClassModel("C:\\export\\drum_10_A.ifc", er.getEntities(), er.getTypes(), "drum"); // change
	//IFC_ClassModel drum2 = new IFC_ClassModel("C:\\export\\drum_10_B.ifc", er.getEntities(), er.getTypes(), "drum");
	IFC_ClassModel drum1 = new IFC_ClassModel("C:\\jo\\small_diff\\sample1.ifc", er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel("C:\\jo\\small_diff\\sample2.ifc", er.getEntities(), er.getTypes(), "drum");
	
	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();
	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());

	
	Map<String, LinkedList<Thing>> o2o_drum1=drum1.createOne2OneRootLinksMap();
	Map<String, LinkedList<Thing>> o2o_drum2=drum2.createOne2OneRootLinksMap();
	for (Map.Entry<String, LinkedList<Thing>> entry : o2o_drum1.entrySet()) {
	    String tkey = entry.getKey();
	    LinkedList<Thing> linkset1=entry.getValue();
	    LinkedList<Thing> linkset2=o2o_drum2.get(tkey);
	    if(linkset2!=null)
	    {
		for(int n=0;n<linkset1.size();n++)
		{
		    Thing t1=linkset1.get(n);
		    if(IfcRoot.class.isInstance(t1))
		    if(o2o_drum2.get(((IfcRoot)t1).getGlobalId())==null)
		    {
			IfcRoot tr1=(IfcRoot)t1;
			System.out.print("m1:"+tr1.getGlobalId()+" sameAs: ");
			boolean is_ok=true;
			for(int i=0;i<linkset2.size();i++)
			{
			   IfcRoot t2=(IfcRoot)linkset2.get(n);			   
			   if(tr1.getGlobalId().equals(t2.getGlobalId()))
			      is_ok=false;
			}
			if(is_ok)
			for(int i=0;i<linkset2.size();i++)
			{
			   IfcRoot t2=(IfcRoot)linkset2.get(n);			   
			   System.out.print(" m2:"+t2.getGlobalId());
			   t2.setGlobalId(tr1.getGlobalId());  // set to the same
			   common_gids.add(tr1.getGlobalId()); 
			}
			System.out.println();
		    }
		}
	    }
	}
	

	drum1.createLinksMap();  // general routine for MSG and groundings

	GroundSettings gs=new GroundSettings(true, true, false, true, true, true, true, false);	       
	drum1.groundFromGUIDs(common_gids,gs);	
	drum1.deduceMSGs();
	System.out.println("drum1:");
	TreeMap<Long,MSG_CRC> crcs1=drum1.calculateMSG_CRCs(common_gids);

	drum2.createLinksMap();  // general routine for MSG and groundings
	drum2.groundFromGUIDs(common_gids,gs);	
	drum2.deduceMSGs();
	System.out.println("drum2:");
	TreeMap<Long,MSG_CRC> crcs2=drum2.calculateMSG_CRCs(common_gids);

	
        // REMOVED	
	System.out.println("removed msgs:");
	Set set1 = crcs1.entrySet();
	Iterator i1 = set1.iterator();
	// Display elements
	int rcount=0;
	 Set<String> removedSet=new HashSet<String>();
	while(i1.hasNext()) {
	    Map.Entry me = (Map.Entry)i1.next();
	    Long key=(Long)me.getKey();
	    if(crcs2.get(key)==null)
	    {
		System.out.println(key);
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 for(int i=0;i<triples.size();i++)
		 {
		     Triple l=triples.get(i);
		     if(IfcRoot.class.isInstance(l.s))
			 removedSet.add(l.s.grounding_name);
		     else
		     if(l.s.is_grounded)
		      if(l.s.i.drum_getGroudingPath()!=null)
		      {
			  removedSet.add(l.s.i.drum_getGroudingPath().grounded_by.grounding_name);
		      }
		     
		     if(IfcRoot.class.isInstance(l.o))
			 removedSet.add(((Thing)l.o).grounding_name);
		     else
	             if(Thing.class.isInstance(l.o))
		     if(((Thing)l.o).is_grounded)
		       if(((Thing)l.o).i.drum_getGroudingPath()!=null)
			   removedSet.add(((Thing)l.o).i.drum_getGroudingPath().grounded_by.grounding_name);
		 }		 	      
	      rcount++;
	    }
	} 
	
        // ADDED
	System.out.println("added msgs:");
	Set set2 = crcs2.entrySet();
	Iterator i2 = set2.iterator();
	// Display elements
	int acount=0;
	 Set<String> addSet=new HashSet<String>();
	while(i2.hasNext()) {
	    Map.Entry me = (Map.Entry)i2.next();
	    Long key=(Long)me.getKey();
	    if(crcs1.get(key)==null)
	    {
		 System.out.println(key);
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 for(int i=0;i<triples.size();i++)
		 {
		     Triple l=triples.get(i);
		     if(IfcRoot.class.isInstance(l.s))
			 addSet.add(l.s.grounding_name);
		     else
		     if(l.s.is_grounded)
		      if(l.s.i.drum_getGroudingPath()!=null)
			  addSet.add(l.s.i.drum_getGroudingPath().grounded_by.grounding_name);
		     
		     if(IfcRoot.class.isInstance(l.o))
			 addSet.add(((Thing)l.o).grounding_name);
		     else
	             if(Thing.class.isInstance(l.o))
		     if(((Thing)l.o).is_grounded)
		       if(((Thing)l.o).i.drum_getGroudingPath()!=null)
			   addSet.add(((Thing)l.o).i.drum_getGroudingPath().grounded_by.grounding_name);
		 }		 	      
	      acount++;	    	      	      
	    }	    
	} 

	
        System.out.println("Removed:");

	 Iterator rit = removedSet.iterator();
	 while(rit.hasNext())
	 {
	     String t=(String)rit.next();
	     if(!addSet.contains(t))
	     {
	     System.out.println(" -- "+t);
	     }
	 }


         System.out.println("Added:");

	 Iterator ait = addSet.iterator();
	 while(ait.hasNext())
	 {
	     String t=(String)ait.next();
	     if(!removedSet.contains(t))
	     {
	     System.out.println(" -- "+t);
	     }
	 }

         System.out.println("Changed:");

	 Iterator ait2 = addSet.iterator();
	 while(ait2.hasNext())
	 {
	     String t=(String)ait2.next();
	     if(removedSet.contains(t))
	     {
	     System.out.println(" -- "+t);
	     }
	 }
	
	System.out.println("removed count: "+rcount+" from:"+crcs1.size());
	System.out.println("added count: "+acount+" from:"+crcs2.size());
	
    }

    
    private DiffDescription showModifications(IFC_ClassModel drum1, IFC_ClassModel drum2) {
	DiffDescription diff_description = new DiffDescription();
	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();

	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());
	Map<Long, String> chksums1 = drum1.commonGIDSet_CalculateCRC32ForGuidAreas(common_gids);
	Map<Long, String> chksums2 = drum2.commonGIDSet_CalculateCRC32ForGuidAreas(common_gids);

	Map<String, Long> id_chksums1 = drum1.commonGIDSet_calculateCRC32ForGuidAreas2(common_gids);
	Map<String, Long> id_chksums2 = drum2.commonGIDSet_calculateCRC32ForGuidAreas2(common_gids);

	List<String> change_set = new LinkedList<String>();
	List<String> removed_set = new LinkedList<String>();
	List<String> add_set = new LinkedList<String>();

	for (Map.Entry<String, Thing> entry : guids1.entrySet()) {
	    String key = entry.getKey();
	    if (guids2.get(key) == null) {
		if (chksums2.get(id_chksums1.get(key)) == null) {
		    removed_set.add(key); // add to the removed list to be
					  // checked
		}
	    }

	}

	for (Map.Entry<String, Thing> entry : guids2.entrySet()) {
	    String key = entry.getKey();
	    if (guids1.get(key) == null) {
		// Checksum does not match:
		if (chksums1.get(id_chksums2.get(key)) == null) {
		    add_set.add(key);
		    // Line number from the model 2
		    ReturnPair ret = IFC_ClassModelLibrary.calculate_nearestAddFromRemoveSet(drum1, drum2, entry.getValue(), common_gids, removed_set);
		    if (ret.difference > 8) {
			System.out.println();
			System.out.println("ADDED: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
			//drum2.listGID_Area(entry.getValue().drum_getLine_number());
			diff_description.getAdd_set().add(key);
		    } else {
			System.out.println();
			System.out.println("REPLACED: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
			diff_description.getReplace_set().add(key);
			String ret_guid = ((IfcRoot) ret.getT()).getGlobalId();
			System.out.println("SAME AS: " + key + " = " + ret_guid);
			removed_set.remove(ret_guid); // estetään tuplakäyttö
			change_set.add(ret_guid);

			// IFC_ClassModel.explain_differences(drum1,drum2,
			// ret.getT().drum_getLine_number(),entry.getValue().drum_getLine_number(),common_gids);

		    }
		}
	    }
	}

	for (Map.Entry<String, Thing> entry : guids1.entrySet()) {
	    String key = entry.getKey();
	    if (!change_set.contains(key)) {
		if (guids2.get(key) == null) {
		    // If any other has the same checksum... assume that the
		    // guid is changed.
		    if (chksums2.get(id_chksums1.get(key)) == null) {
			System.out.println("key not found in change set:" + key);
			System.out.println("change set contains:");
			for (int j = 0; j < change_set.size(); j++) {
			    System.out.println("- " + change_set.get(j));
			}
			System.out.println();
			System.out.println("REMOVED: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
			diff_description.getRemoved_set().add(key);
		    }
		}
	    }
	}

	for (Map.Entry<String, Thing> entry : guids1.entrySet()) {
	    String key = entry.getKey();
	    if (guids2.get(key) != null) {
		if (chksums2.get(id_chksums1.get(key)) == null) {
		    System.out.println();
		    System.out.println("CHANGED: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
		    diff_description.getChange_set().add(key);
		    //IFC_ClassModelLibrary.explain_differences(drum1, drum2, guids1.get(key).drum_getLine_number(), guids2.get(key).drum_getLine_number(), common_gids);

		}
	    }
	}
	return diff_description;
    }

    public void generic() {
	IFC_ClassModel drum1 = new IFC_ClassModel("C:\\export\\DRUM1.ifc", er.getEntities(), er.getTypes(), "drum");
	IFC_ClassModel drum2 = new IFC_ClassModel("C:\\export\\DRUM2.ifc", er.getEntities(), er.getTypes(), "drum");
	// drum1.listElements(24l);
	// sample.listElements(24l);

	showModifications(drum1, drum2);

    }

    public static void main(String[] args) {
	/*
	 * if(args.length!=4)
	 * System.out.println(
	 * "Usage:  java  IFC_Converter  express_filename  ifc_filename  output_filename  model_name \nExample: java  IFC_Converter  c:\\jo\\IFC2X3_TC1.exp  C:\\jo\\sample.ifc  c:\\jo\\output_rdf.txt sample"
	 * );
	 * else
	 */

	// Convert automatically
	// new
	// IFC_Converter("c:\\jo\\IFC2X3_TC1.exp","C:\\export\\drum_10.ifc","c:\\jo\\rdf_out\\drum10.rdf","model");
	//new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\export\\drum_11.ifc", "c:\\jo\\rdf_out\\drum11.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\sample.ifc", "c:\\jo\\rdf_out\\sample.rdf", "model");
	//new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\inp3.ifc", "c:\\jo\\rdf_out\\inp3.rdf", "model");
        
        
        new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\sample.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\door.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\talo_testi.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\inp3.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");  
               
    }

}
