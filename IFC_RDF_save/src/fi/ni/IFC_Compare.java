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

import com.hp.hpl.jena.sparql.function.library.now;

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

public class IFC_Compare {

    ExpressReader er;
    static long ed_time=-1;


    public IFC_Compare(String express_file,int changed_line) {

        ed_time=System.currentTimeMillis();
	er = new ExpressReader(express_file);
	long now_time;
	now_time=System.currentTimeMillis();	
	System.out.println("Time, Parse express:"+(now_time-ed_time));
	ed_time=now_time;
	
	DiffResult msg_grounding_result=diffMSGGrounding();
	now_time=System.currentTimeMillis();	
	System.out.println("Time, diff MSG Grouning:"+(now_time-ed_time));
	ed_time=now_time;
	DiffResult msg_wogrounding_result=diffMSGWOGrounding();
	now_time=System.currentTimeMillis();	
	System.out.println("Time, diff MSG WO Grounding:"+(now_time-ed_time));
	ed_time=now_time;
	boolean a_pos=false;
	boolean r_pos=false;
	for(int i=0;i<msg_grounding_result.getTriples_added().size();i++)
	{
	    Triple t=msg_grounding_result.getTriples_added().get(i);
	    if(t.s.line_number==changed_line)
		a_pos=true;
	    if(t.literal)
	    if(Double.class.isInstance(t.o))	
	    if(((Double)t.o).doubleValue()==8881222f)
		a_pos=true;
	}
	for(int i=0;i<msg_grounding_result.getTriples_removed().size();i++)
	{
	    Triple t=msg_grounding_result.getTriples_removed().get(i);
	    if(t.s.line_number==changed_line)
		r_pos=true;
	    if(t.literal)
	    if(Double.class.isInstance(t.o))	
	    if(((Double)t.o).doubleValue()==8881221f)
		r_pos=true;
	}
	System.out.print(a_pos+" ; "+r_pos+" ; "+msg_grounding_result.getTriples_added().size()+";"+msg_grounding_result.getTriples_removed().size()+";");

	
	a_pos=false;
	r_pos=false;
	for(int i=0;i<msg_wogrounding_result.getTriples_added().size();i++)
	{
	    Triple t=msg_wogrounding_result.getTriples_added().get(i);
	    if(t.s.line_number==changed_line)
		a_pos=true;
	    if(t.literal)
	    if(Double.class.isInstance(t.o))	
	    if(((Double)t.o).doubleValue()==8881222f)
		a_pos=true;
	}
	for(int i=0;i<msg_wogrounding_result.getTriples_removed().size();i++)
	{
	    Triple t=msg_wogrounding_result.getTriples_removed().get(i);
	    if(t.s.line_number==changed_line)
		r_pos=true;
	    if(t.literal)
	    if(Double.class.isInstance(t.o))	
	    if(((Double)t.o).doubleValue()==8881221f)
		r_pos=true;
	}
	System.out.println(a_pos+" ; "+r_pos+" ; "+msg_wogrounding_result.getTriples_added().size()+";"+msg_wogrounding_result.getTriples_removed().size()+";");

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

    public void diffHeuristic() {
	System.out.println("Heuristic:");
	
	System.out.println("Parse model 0:");
	IFC_ClassModel drum1 = new IFC_ClassModel("c:\\jo\\small_diff\\m0.ifc", er.getEntities(), er.getTypes(), "drum"); // addition
	System.out.println("Parse model 1:");
	IFC_ClassModel drum2 = new IFC_ClassModel("c:\\jo\\small_diff\\m1.ifc", er.getEntities(), er.getTypes(), "drum");
	System.out.println("parse done");

	DiffDescription diff_description = showModifications(drum1, drum2);
    }

    public DiffResult diffMSGGrounding() {
	 List<Triple> triples_added=new ArrayList<Triple>() ;
	 List<Triple> triples_removed =new ArrayList<Triple>() ;
	 DiffResult retval=new DiffResult(triples_added,triples_removed);
         Long now_time, ed_time;		
         ed_time=System.currentTimeMillis();	

	IFC_ClassModel drum1 = new IFC_ClassModel("c:\\jo\\small_diff\\m0.ifc", er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel("c:\\jo\\small_diff\\m1.ifc", er.getEntities(), er.getTypes(), "drum");
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, models parsed:"+(now_time-ed_time));
	ed_time=now_time;
	
	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();
	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());

	
	Describe.out("Check changed identifiers of one-to-one links");
	Map<String, LinkedList<Thing>> o2o_drum1=drum1.createOne2OneRootLinksMap();
	Map<String, LinkedList<Thing>> o2o_drum2=drum2.createOne2OneRootLinksMap();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, preprosessing done:"+(now_time-ed_time));
	ed_time=now_time;

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
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, changed identifier notification by functional mapping:"+(now_time-ed_time));
	ed_time=now_time;
	
	GroundSettings gs=new GroundSettings(true, true, true, true, true, false, false, true, true);	       
	drum1.ground5MaxSOGL();
        drum1.groundFromGUIDs(common_gids, gs);
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, grouning model 1:"+(now_time-ed_time));
	ed_time=now_time;
	drum1.createLinksMap();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, create links map 1:"+(now_time-ed_time));
	ed_time=now_time;

	drum1.deduceMSGs();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, deduce MSGs for model 1:"+(now_time-ed_time));
	ed_time=now_time;
	drum1.nameBlanks();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, name blanks in MSGs for model 1:"+(now_time-ed_time));
	ed_time=now_time;
	TreeMap<Long,MSG_CRC> crcs1=drum1.calculateMSG_CRCs(common_gids);
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, calculate MSGs CRCfor model 1:"+(now_time-ed_time));
	ed_time=now_time;

	drum2.ground5MaxSOGL();
	drum2.groundFromGUIDs(common_gids, gs);
	drum2.createLinksMap();
	drum2.deduceMSGs();
	drum2.nameBlanks();
	TreeMap<Long,MSG_CRC> crcs2=drum2.calculateMSG_CRCs(common_gids);

	
 	now_time=System.currentTimeMillis();	
	ed_time=now_time;
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
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 for(int i=0;i<triples.size();i++)
		 {		     
		     Triple l=triples.get(i);
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
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, deduce removed MSGs:"+(now_time-ed_time));
	ed_time=now_time;
	
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
	         MSG_CRC msgcrcvo=(MSG_CRC)me.getValue();

		 List<Triple> triples=msgcrcvo.getMsg();
		 for(int i=0;i<triples.size();i++)
		 {
		     Triple l=triples.get(i);
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
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, deduce added MSGs:"+(now_time-ed_time));
	ed_time=now_time;
       return retval;
    }

    public DiffResult diffMSGWOGrounding() {
	List<Triple> triples_added=new ArrayList<Triple>() ;
	List<Triple> triples_removed =new ArrayList<Triple>() ;
	DiffResult retval=new DiffResult(triples_added,triples_removed);

	Long now_time, ed_time;		
        ed_time=System.currentTimeMillis();
        
	IFC_ClassModel drum1 = new IFC_ClassModel("c:\\jo\\small_diff\\m0.ifc", er.getEntities(), er.getTypes(), "drum"); // change
	IFC_ClassModel drum2 = new IFC_ClassModel("c:\\jo\\small_diff\\m1.ifc", er.getEntities(), er.getTypes(), "drum");
	now_time=System.currentTimeMillis();	
	System.out.println("Time, models parsed:"+(now_time-ed_time));
	ed_time=now_time;
	
	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();
	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());

	
	Map<String, LinkedList<Thing>> o2o_drum1=drum1.createOne2OneRootLinksMap();
	Map<String, LinkedList<Thing>> o2o_drum2=drum2.createOne2OneRootLinksMap();
	Describe.out("Check changed identifiers of one-to-one links");
	now_time=System.currentTimeMillis();	
	System.out.println("Time, preprosessing done:"+(now_time-ed_time));
	ed_time=now_time;
	
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
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, changed identifier notification by functional mapping:"+(now_time-ed_time));
	ed_time=now_time;

	drum1.createLinksMap();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, create links map 1:"+(now_time-ed_time));
	ed_time=now_time;


	drum1.checkUniques(false);	
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, check uniques model 1:"+(now_time-ed_time));
	ed_time=now_time;
	drum1.deduceMSGs();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, deduce MSGs for model 1:"+(now_time-ed_time));
	ed_time=now_time;

	drum1.nameBlanks();
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, name blanks in MSGs for model 1:"+(now_time-ed_time));
	ed_time=now_time;
	TreeMap<Long,MSG_CRC> crcs1=drum1.calculateMSG_CRCs(common_gids);
 	now_time=System.currentTimeMillis();	
	System.out.println("Time, calculate MSGs CRCfor model 1:"+(now_time-ed_time));
	ed_time=now_time;

	drum2.createLinksMap();
	drum2.checkUniques(false);	
	drum2.deduceMSGs();
	drum2.nameBlanks();
	TreeMap<Long,MSG_CRC> crcs2=drum2.calculateMSG_CRCs(common_gids);

	
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
        
        
        new IFC_Compare("c:\\jo\\IFC2X3_TC1.exp",1);
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\drum_10.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\door.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\talo_testi.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");
        //new IFC_Converter("c:\\jo\\IFC2X3_TC1.exp", "C:\\jo\\IFCtest_data\\inp3.ifc", "c:\\jo\\rdf_out\\tmp.rdf", "model");  
               
    }

}
