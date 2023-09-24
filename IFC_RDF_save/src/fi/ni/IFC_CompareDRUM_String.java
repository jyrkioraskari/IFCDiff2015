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
import fi.ni.vo.DiffResult;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.ReturnPair;
import fi.ni.vo.Triple;
import fi.ni.vo.ValuePair;

/*
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/
 */

public class IFC_CompareDRUM_String {

    ExpressReader er;

    public IFC_CompareDRUM_String(String express_file,String file1,String file2) {

	er = new ExpressReader(express_file);
	
	DiffResult msg_grounding_result=diffMSGGrounding(file1,file2);
	DiffResult msg_wogrounding_result=diffMSGWOGrounding(file1,file2);
	
	
	boolean a_pos=false;
	boolean r_pos=false;
	for(int i=0;i<msg_grounding_result.getTriples_added().size();i++)
	{
	    Triple t=msg_grounding_result.getTriples_added().get(i);
	    if(t.literal)
	    if(String.class.isInstance(t.o))	
	    if(((String)t.o).equals("testing_8881222f"))
		a_pos=true;
	}
	for(int i=0;i<msg_grounding_result.getTriples_removed().size();i++)
	{
	    Triple t=msg_grounding_result.getTriples_removed().get(i);
	    if(t.literal)
		    if(String.class.isInstance(t.o))	
			    if(((String)t.o).equals("testing_8881221f"))
		r_pos=true;
	}
	System.out.print("+"+a_pos+" ; "+r_pos+" ; "+msg_grounding_result.getTriples_added().size()+";"+msg_grounding_result.getTriples_removed().size()+";"+
		msg_grounding_result.getAdded_msg_cksums().size()+";"+msg_grounding_result.getRemoved_msg_cksums().size()+";");

	
	a_pos=false;
	r_pos=false;
	for(int i=0;i<msg_wogrounding_result.getTriples_added().size();i++)
	{
	    Triple t=msg_wogrounding_result.getTriples_added().get(i);
	    if(t.literal)
		    if(String.class.isInstance(t.o))	
			    if(((String)t.o).equals("testing_8881222f"))
		a_pos=true;
	}
	for(int i=0;i<msg_wogrounding_result.getTriples_removed().size();i++)
	{
	    Triple t=msg_wogrounding_result.getTriples_removed().get(i);
	    if(t.literal)
		    if(String.class.isInstance(t.o))	
			    if(((String)t.o).equals("testing_8881221f"))
		r_pos=true;
	}
	System.out.println("+"+a_pos+" ; "+r_pos+" ; "+msg_wogrounding_result.getTriples_added().size()+";"+msg_wogrounding_result.getTriples_removed().size()+";"+
		msg_wogrounding_result.getAdded_msg_cksums().size()+";"+msg_wogrounding_result.getRemoved_msg_cksums().size()+";");

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
    public DiffResult diffMSGGrounding(String file1,String file2) {
	 List<Triple> triples_added=new ArrayList<Triple>() ;
	 List<Triple> triples_removed =new ArrayList<Triple>() ;
	 DiffResult retval=new DiffResult(triples_added,triples_removed);

	IFC_ClassModel drum1 = new IFC_ClassModel(file1, er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel(file2, er.getEntities(), er.getTypes(), "drum");

	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();
	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());

	
	Describe.out("Check changed identifiers of one-to-one links");
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
			   t2.setGlobalId(tr1.getGlobalId());  // set to the same
			   common_gids.add(tr1.getGlobalId()); 
			}
		    }
		}
	    }
	}
	GroundSettings gs=new GroundSettings(true, true, true, true, true, false, false, true, true);
	/*
	ifcOwnerHistory_handle_as_grounded,
	set_literals
	test_unique_loops,
	ifcGeometricRepresentationContext_ground, 
	ground_On_One2OneLinks_on,
	ground_AnyLinks_on,
	use_distance_from_core,
	second_order_literals,
	global_unique_naming*/
	
	//GroundSettings gs=new GroundSettings(true, false, false, false, false, false, false, false, false);
	/**/
	drum1.checkUniques();
	drum1.ground5MaxSOGL();
        drum1.basic_ground(gs);
	
	drum1.createLinksMap();
	drum1.deduceMSGs();
	TreeMap<Long,MSG_CRC> crcs1_1=drum1.calculateCanonical_CRC32s4MSGs();
	TreeMap<Long,MSG_CRC> crcs1_2=drum1.calculateCanonical_CRC32s4MSGs();	
	TreeMap<Long,MSG_CRC> crcs1 = new  TreeMap<Long,MSG_CRC>();
	Iterator it1 =crcs1_1.entrySet().iterator();
	    while (it1.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it1.next();
	        if(crcs1_2.get(pairs.getKey())!=null)
	            crcs1.put((Long)pairs.getKey(), (MSG_CRC)pairs.getValue());
	            
	    }
	
	drum2.checkUniques();
	drum2.ground5MaxSOGL();
        drum2.basic_ground(gs);
	
	drum2.createLinksMap();	
	drum2.deduceMSGs();
	TreeMap<Long,MSG_CRC> crcs2_1=drum2.calculateCanonical_CRC32s4MSGs();
	TreeMap<Long,MSG_CRC> crcs2_2=drum2.calculateCanonical_CRC32s4MSGs();
	TreeMap<Long,MSG_CRC> crcs2 = new  TreeMap<Long,MSG_CRC>();
	Iterator it2 =crcs2_1.entrySet().iterator();
	    while (it2.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it2.next();
	        if(crcs2_2.get(pairs.getKey())!=null)
	            crcs2.put((Long)pairs.getKey(), (MSG_CRC)pairs.getValue());	            
	    }
	
	System.out.println("G model1 triples: "+drum1.gettriplesInMSGs());
	System.out.println("model1 MSGs: "+drum1.msgs.size());
	System.out.println("model2 MSGs: "+drum2.msgs.size());
	
        // REMOVED	
	Set set1 = crcs1.entrySet();
	Iterator i1 = set1.iterator();
	// Display elements
	int rcount=0;
	 Set<String> removedGUIDSSet=new HashSet<String>();
	while(i1.hasNext()) {
	    Map.Entry me = (Map.Entry)i1.next();
	    Long key=(Long)me.getKey();
	    if(crcs2.get(key)==null)
	    {
		 retval.getRemoved_msg_cksums().add(key.toString());
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 /*System.out.println();
		 System.out.println();
		 System.out.println("r crc:"+msgcrcvo.getCrc32());
		 drum1.showCanonicalCrc32(triples);*/

		 for(int i=0;i<triples.size();i++)
		 {		     
		     Triple l=triples.get(i);
	             //System.out.println("t: "+l);
		     triples_removed.add(l);
		     if(IfcRoot.class.isInstance(l.s))
			 removedGUIDSSet.add(l.s.grounding_name);
		     else
		     if(l.s.is_grounded)
		      if(l.s.i.drum_getGroudingPath()!=null)
		      {
			  removedGUIDSSet.add(l.s.i.drum_getGroudingPath().grounded_by.grounding_name);
		      }
		     
		     if(IfcRoot.class.isInstance(l.o))
			 removedGUIDSSet.add(((Thing)l.o).grounding_name);
		     else
	             if(Thing.class.isInstance(l.o))
		     if(((Thing)l.o).is_grounded)
		       if(((Thing)l.o).i.drum_getGroudingPath()!=null)
			   removedGUIDSSet.add(((Thing)l.o).i.drum_getGroudingPath().grounded_by.grounding_name);
		 }		 	      
	      rcount++;
	    }
	} 
	
        // ADDED
	Set set2 = crcs2.entrySet();
	Iterator i2 = set2.iterator();
	// Display elements
	int acount=0;
	 Set<String> addedGUIDSSet=new HashSet<String>();
	while(i2.hasNext()) {
	    Map.Entry me = (Map.Entry)i2.next();
	    Long key=(Long)me.getKey();
	    if(crcs1.get(key)==null)
	    {
		 retval.getAdded_msg_cksums().add(key.toString());
                 MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

                 
                 
		 List<Triple> triples=msgcrcvo.getMsg();
		 /*System.out.println();
		 System.out.println();
		 System.out.println("a crc:"+msgcrcvo.getCrc32());
		 drum1.showCanonicalCrc32(triples);*/
		 for(int i=0;i<triples.size();i++)
		 {		     
		     Triple l=triples.get(i);
	             //System.out.println("t: "+l);

		     triples_added.add(l);

		     if(IfcRoot.class.isInstance(l.s))
			 addedGUIDSSet.add(l.s.grounding_name);
		     else
		     if(l.s.is_grounded)
		      if(l.s.i.drum_getGroudingPath()!=null)
			  addedGUIDSSet.add(l.s.i.drum_getGroudingPath().grounded_by.grounding_name);
		     
		     if(IfcRoot.class.isInstance(l.o))
			 addedGUIDSSet.add(((Thing)l.o).grounding_name);
		     else
	             if(Thing.class.isInstance(l.o))
		     if(((Thing)l.o).is_grounded)
		       if(((Thing)l.o).i.drum_getGroudingPath()!=null)
			   addedGUIDSSet.add(((Thing)l.o).i.drum_getGroudingPath().grounded_by.grounding_name);
		 }		 	      
	      acount++;	    	      	      
	    }	    
	} 
       return retval;
    }

    public DiffResult diffMSGWOGrounding(String file1,String file2) {
	List<Triple> triples_added=new ArrayList<Triple>() ;
	List<Triple> triples_removed =new ArrayList<Triple>() ;
	DiffResult retval=new DiffResult(triples_added,triples_removed);

	IFC_ClassModel drum1 = new IFC_ClassModel(file1, er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel(file2, er.getEntities(), er.getTypes(), "drum");

	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();
	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());

	
	Map<String, LinkedList<Thing>> o2o_drum1=drum1.createOne2OneRootLinksMap();
	Map<String, LinkedList<Thing>> o2o_drum2=drum2.createOne2OneRootLinksMap();
	Describe.out("Check changed identifiers of one-to-one links");

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
			   t2.setGlobalId(tr1.getGlobalId());  // set to the same
			   common_gids.add(tr1.getGlobalId()); 
			}
		    }
		}
	    }
	}
	
	drum1.createLinksMap();
	//drum1.createHugeLinksMap();
	drum1.checkUniques(false);	
	drum1.deduceMSGs();
	TreeMap<Long,MSG_CRC> crcs1=drum1.calculateCanonical_CRC32s4MSGs();

	drum2.createLinksMap();
	//drum2.createHugeLinksMap();
	drum2.checkUniques(false);	
	drum2.deduceMSGs();
	TreeMap<Long,MSG_CRC> crcs2=drum2.calculateCanonical_CRC32s4MSGs();

	System.out.println("WO model1 triples: "+drum1.gettriplesInMSGs());
	System.out.println("model1 MSGs: "+drum1.msgs.size());
	System.out.println("model2 MSGs: "+drum2.msgs.size());

	
        // REMOVED	
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
		 retval.getRemoved_msg_cksums().add(key.toString());
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 for(int i=0;i<triples.size();i++)
		 {		     
		     Triple l=triples.get(i);
		     triples_removed.add(l);
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
		 retval.getAdded_msg_cksums().add(key.toString());
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 for(int i=0;i<triples.size();i++)
		 {
		     Triple l=triples.get(i);
		     triples_added.add(l);
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
        return retval;
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
		    IFC_ClassModelLibrary.explain_differences(drum1, drum2, guids1.get(key).i.drum_getLine_number(), guids2.get(key).i.drum_getLine_number(), common_gids);

		}
	    }
	}
	return diff_description;
    }

    public void generic() {
	IFC_ClassModel drum1 = new IFC_ClassModel("C:\\export\\DRUM1.ifc", er.getEntities(), er.getTypes(), "drum");
	IFC_ClassModel drum2 = new IFC_ClassModel("C:\\export\\DRUM2.ifc", er.getEntities(), er.getTypes(), "drum");
	showModifications(drum1, drum2);

    }

    public static void main(String[] args) {
        new IFC_CompareDRUM_String("c:\\jo\\IFC2X3_TC1.exp","c:\\export\\drum_10_A.ifc","c:\\export\\drum_10_B.ifc");
               
    }

}
