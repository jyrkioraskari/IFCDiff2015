/*
 * IFC_ClassModel is a class to parse any IFC STEP coded file.
 * - creates RDF of the internal representation
 * - creates internal java class representation of the IFC file
 * - outputs explanation of IFC model differences
 * - calculates crc32 for a subtree of an element
 * - returns nearest matching element based on similarity measure
 * - returns tree of the building elements
 * 
 * @author Jyrki Oraskari
 * 
 * @license This work is licensed under a Creative Commons Attribution 3.0
 * Unported License.
 * http://creativecommons.org/licenses/by/3.0/
 */
package fi.ni;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.CRC32;

import org.Tree;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.rdfcontext.signing.RDFC14Ner;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import fi.ni.ifc2x3.IfcCartesianPoint;
import fi.ni.ifc2x3.IfcClassification;
import fi.ni.ifc2x3.IfcColourRgb;
import fi.ni.ifc2x3.IfcConversionBasedUnit;
import fi.ni.ifc2x3.IfcCurveStyleFont;
import fi.ni.ifc2x3.IfcDirection;
import fi.ni.ifc2x3.IfcDraughtingPreDefinedCurveFont;
import fi.ni.ifc2x3.IfcFillAreaStyle;
import fi.ni.ifc2x3.IfcGeometricRepresentationContext;
import fi.ni.ifc2x3.IfcMaterial;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.ifc2x3.IfcPolyLoop;
import fi.ni.ifc2x3.IfcPolyline;
import fi.ni.ifc2x3.IfcPresentationLayerAssignment;
import fi.ni.ifc2x3.IfcProduct;
import fi.ni.ifc2x3.IfcProject;
import fi.ni.ifc2x3.IfcRoot;
import fi.ni.ifc2x3.IfcSIUnit;
import fi.ni.ifc2x3.IfcTextStyle;
import fi.ni.vo.AttributeVO;
import fi.ni.vo.EntityVO;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.IFC_X3_VO;
import fi.ni.vo.Link;
import fi.ni.vo.Triple;
import fi.ni.vo.TypeVO;
import fi.ni.vo.ValuePair;

/**
 * The Class IFC_ClassModel.
 * 
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0
 *          Unported License.
 *          http://creativecommons.org/licenses/by/3.0/
 */
public class IFC_ClassModel {

    
    GroundingPathRegistry grounding_paths=new GroundingPathRegistry();
    /** The DRU m_ prefix. */
    final static String DRUM_PREFIX = "http://drum.cs.hut.fi";

    /** The IF c_ predicat e_ prefi x_ name. */
    final static String IFC_PREDICATE_PREFIX_NAME = "ifc";

    /** The ifc_filename. */
    final String ifc_filename;

    public final String ifc_model_name;

    /** The linemap. */
    private Map<Long, IFC_X3_VO> linemap = new HashMap<Long, IFC_X3_VO>();

    /** The entities. */
    final Map<String, EntityVO> entities;

    /** The types. */
    final Map<String, TypeVO> types;

    /** The object_buffer. */
    public final Map<Long, Thing> object_buffer = new HashMap<Long, Thing>(); // line_number,

    /** The gid_map. */
    public final Map<String, Thing> gid_map = new HashMap<String, Thing>(); // GID,
    // Thing
    /** The root. */
    IfcProject root; // Thing

    /**
     * Gets the linemap.
     * 
     * @return the linemap
     */
    public Map<Long, IFC_X3_VO> getLinemap() {
	return linemap;
    }

    public boolean has_duplicate_guids=false;

    /**
     * Instantiates a new iF c_ class model.
     * 
     * @param model_file
     *            the name of the IFC file to be read in
     * @param entities
     *            the entities
     * @param types
     *            the types
     */
    public IFC_ClassModel(String model_file, Map<String, EntityVO> entities, Map<String, TypeVO> types, String ifc_model_name) {
	this.entities = entities;
	this.types = types;
	this.ifc_filename = filter_spaces((new File(model_file)).getName());
	this.ifc_model_name = ifc_model_name;
	readModel(model_file);
	mapEntries();
	calculateTheLongestsPathsToTheNode_and_setGlobalIDs();
	createObjectTree();

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing gobject = entry.getValue();
	    if (IfcRoot.class.isInstance(gobject)) {
		IfcRoot t = (IfcRoot) gobject;
		Thing tmp=gid_map.put(t.getGlobalId(), t);
		if(tmp!=null)
		{
		    has_duplicate_guids=true;
		    //System.err.println("Duplicate name:"+tmp.line_number+" - "+t.line_number);
		}
		
	    }
	    if (IfcProject.class.isInstance(gobject)) {
		root = (IfcProject) gobject;
	    }
	}
	// Save memory
	linemap.clear();
	linemap=null;
	System.gc();

    }

    // for MSG calculation

    final Map<Thing, LinkedList<Link>> linksmap = new HashMap<Thing, LinkedList<Link>>();
    LinkedList<Link> linksList = new LinkedList<Link>();

    public Map<Thing, ArrayList<Link>> createUnlimitedHugeLinksMap() {
        Map<Thing, ArrayList<Link>> ulinksmap = new HashMap<Thing, ArrayList<Link>>();
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();

	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2 = (Thing) l.t2;

		    ArrayList<Link> s1 = ulinksmap.get(t1);
		    if (s1 == null) {
			s1 = new ArrayList<Link>();
			ulinksmap.put(t1, s1);
		    }

			s1.add(l);

			ArrayList<Link> s2 = ulinksmap.get(t2);
		    if (s2 == null) {
			s2 = new ArrayList<Link>();
			ulinksmap.put(t2, s2);
		    }
			s2.add(l);

	    }
	}
	return ulinksmap;
    }

    public Map<Thing, ArrayList<Link>> createOnewayUnlimitedHugeLinksMap() {
        Map<Thing, ArrayList<Link>> ulinksmap = new HashMap<Thing, ArrayList<Link>>();
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();

	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2 = (Thing) l.t2;

		    ArrayList<Link> s1 = ulinksmap.get(t1);
		    if (s1 == null) {
			s1 = new ArrayList<Link>();
			ulinksmap.put(t1, s1);
		    }

			s1.add(l);
	    }
	}
	return ulinksmap;
    }


    
    // Create data structure for MSG and groundings
    public void createLinksMap() {
	linksList.clear();
	linksmap.clear();
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();

	    List<ValuePair> ifcs = t1.i.drum_getIfcClassAttributes();
	    for (int n = 0; n < ifcs.size(); n++) {
		ValuePair vp = (ValuePair) ifcs.get(n);
		Thing t2 = (Thing) vp.getValue();

		Link l = new Link(t1, t2, vp.getName());
		if(!t1.is_grounded)
		if (!IfcRoot.class.isInstance(t1))
		{
		    // Follow only blank nodes
		    LinkedList<Link> s1 = linksmap.get(t1);
		    if (s1 == null) {
			s1 = new LinkedList<Link>();
			linksmap.put(t1, s1);
		    }

		    if (!s1.contains(l))
			s1.add(l);

		}
		if(!t2.is_grounded)
		if (!IfcRoot.class.isInstance(t2))
		{
		    // Follow only blank nodes
		    LinkedList<Link> s2 = linksmap.get(t2);
		    if (s2 == null) {
			s2 = new LinkedList<Link>();
			linksmap.put(t2, s2);
		    }
		    if (!s2.contains(l))
			s2.add(l);
		}

		linksList.add(l);
	    }
	}
    }

    
    // Used in MSG calculation to get nondirectional arrows
    // note: ground first!!
    public void createHugeLinksMap() {
	linksList.clear();
	linksmap.clear();
	//System.out.println("links map");
	long inx=0;
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    inx++;
	    if((inx%10000)==0)
	    {
		//System.out.println(inx);
		System.gc();
	    }

	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2 = (Thing) l.t2;

		 if(!t1.is_grounded)
		 if (!IfcRoot.class.isInstance(t1))
		{
		    // Follow only blank nodes
		    LinkedList<Link> s1 = linksmap.get(t1);
		    if (s1 == null) {
			s1 = new LinkedList<Link>();
			linksmap.put(t1, s1);
		    }

		    //if (!s1.contains(l))
			s1.add(l);

		}

		if(!t2.is_grounded)
		if (!IfcRoot.class.isInstance(t2))
		{
		    // Follow only blank nodes
		    LinkedList<Link> s2 = linksmap.get(t2);
		    if (s2 == null) {
			s2 = new LinkedList<Link>();
			linksmap.put(t2, s2);
		    }
		    //if (!s2.contains(l))
			s2.add(l);
		}

		linksList.add(l);
	    }
	}
    }

    public List<List<Triple>> msgs = new ArrayList<List<Triple>>();
    int[] hg = new int[5000000];

    static String ns = "http://drum/diff#";

    public long gettriplesInMSGs() {
        long ret=0;
        for (int i = 0; i < msgs.size(); i++) {            
            ret+=msgs.get(i).size();
	}
	return ret;
    }

    
    public TreeMap<Long,MSG_CRC> calculateCanonical_CRC32s4MSGs() {

	TreeMap<Long,MSG_CRC> return_list = new  TreeMap<Long,MSG_CRC>();
        for (int i = 0; i < msgs.size(); i++) {            
	    long checksum = getCanonicalCrc32(msgs.get(i));
	    MSG_CRC msgcrc=new MSG_CRC(msgs.get(i), checksum);
	    return_list.put(checksum, msgcrc);
	}
	return return_list;
    }


    public long getCanonicalCrc32(List<Triple> triples)
    {
	Map<String,Resource> blanks=new HashMap<String,Resource>();
        Model m = ModelFactory.createDefaultModel();
	m.setNsPrefix( "ns", ns);
	
	for(int n=0;n<triples.size();n++)
	{
	        Triple t=triples.get(n);	        
	        Resource re_s=null;
	        Resource re_o=null;
	        Property p=m.createProperty(ns+t.p);
	        boolean is_literal=false;
		if (IfcRoot.class.isInstance(t.s))
		{
		   IfcRoot rs=(IfcRoot) t.s;
		   re_s =  m.createResource(ns+rs.getGlobalId());		    
		}
		else
		{
		   if(t.s.is_grounded)
		   {
		       re_s=m.createResource(ns+t.s.grounding_name);
		   }
		   else
		   {
		   re_s=blanks.get(t.s.i.drum_getLine_number()+"");
		   
		   if(re_s==null)
		   {
	             re_s=  m.createResource();	
	             blanks.put(t.s.i.drum_getLine_number()+"", re_s);
		   }
		   }
		}
		
		if (IfcRoot.class.isInstance(t.o))
		{
		   IfcRoot ro=(IfcRoot) t.o;
		   re_o =  m.createResource(ns+ro.getGlobalId());		    
		}
		else
		{
		   if (Thing.class.isInstance(t.o)) 
		   {
		    Thing to=(Thing)t.o;
		    if(to.is_grounded)
		    {
			       re_o=m.createResource(ns+t.s.grounding_name);
		    }
	            else	
	            {
		    re_o=blanks.get(to.i.drum_getLine_number()+"");
		    if(re_o==null)
		    {
	             re_o=  m.createResource();	
	             blanks.put(to.i.drum_getLine_number()+"", re_o);
		    }
	            }
		   }
		   else
		   {
		       // Literal
		       is_literal=true;
		   }
		}
		if(is_literal)
		    m.add(re_s,p,t.o.toString());
		else
		    m.add(re_s,p,re_o);
	}
	RDFC14Ner rdfc=new RDFC14Ner(m);
	ArrayList<String> canonical_strings=rdfc.getCanonicalStringsArray();
	CRC32 crc32=new CRC32();
	for(int i=0;i<canonical_strings.size();i++)
	{
	    crc32.update(canonical_strings.get(i).getBytes());
	}
	return crc32.getValue();
    }

    
    public long showCanonicalCrc32(List<Triple> triples)
    {
	Map<String,Resource> blanks=new HashMap<String,Resource>();
        Model m = ModelFactory.createDefaultModel();
	m.setNsPrefix( "ns", ns);
	
	for(int n=0;n<triples.size();n++)
	{
	        Triple t=triples.get(n);	        
	        Resource re_s=null;
	        Resource re_o=null;
	        Property p=m.createProperty(ns+t.p);
	        boolean is_literal=false;
		if (IfcRoot.class.isInstance(t.s))
		{
		   IfcRoot rs=(IfcRoot) t.s;
		   re_s =  m.createResource(ns+rs.getGlobalId());		    
		}
		else
		{
		   if(t.s.is_grounded)
		   {
		       re_s=m.createResource(ns+t.s.grounding_name);
		   }
		   else
		   {
		   re_s=blanks.get(t.s.i.drum_getLine_number()+"");
		   
		   if(re_s==null)
		   {
	             re_s=  m.createResource();	
	             blanks.put(t.s.i.drum_getLine_number()+"", re_s);
		   }
		   }
		}
		
		if (IfcRoot.class.isInstance(t.o))
		{
		   IfcRoot ro=(IfcRoot) t.o;
		   re_o =  m.createResource(ns+ro.getGlobalId());		    
		}
		else
		{
		   if (Thing.class.isInstance(t.o)) 
		   {
		    Thing to=(Thing)t.o;
		    if(to.is_grounded)
		    {
			       re_o=m.createResource(ns+t.s.grounding_name);
		    }
	            else	
	            {
		    re_o=blanks.get(to.i.drum_getLine_number()+"");
		    if(re_o==null)
		    {
	             re_o=  m.createResource();	
	             blanks.put(to.i.drum_getLine_number()+"", re_o);
		    }
	            }
		   }
		   else
		   {
		       // Literal
		       is_literal=true;
		   }
		}
		if(is_literal)
		    m.add(re_s,p,t.o.toString());
		else
		    m.add(re_s,p,re_o);
	}
	RDFC14Ner rdfc=new RDFC14Ner(m);
	ArrayList<String> canonical_strings=rdfc.getCanonicalStringsArray();
	CRC32 crc32=new CRC32();
	for(int i=0;i<canonical_strings.size();i++)
	{
	    System.out.println(" "+canonical_strings.get(i));
	    crc32.update(canonical_strings.get(i).getBytes());
	}
	return crc32.getValue();
    }

    private class GThingComparator implements Comparator<Thing>
    {

	public int compare(Thing o1, Thing o2) {	    
	    return o1.grounding_name.compareTo(o2.grounding_name);
	}
	
    }
    GThingComparator t_comparator=new GThingComparator();

    private void nameBlanksInMSG(List<Triple> triples)
    {
	Set<Thing> bnodes=new HashSet<Thing>();
	SortedSet<Thing> gnodes=new TreeSet<Thing>(t_comparator);
	for(int i=0;i<triples.size();i++)
	{
	    Triple t=triples.get(i);

	    if(!t.literal)
	    {
	      if(t.s.is_grounded)
	        gnodes.add(t.s);
	      else
		bnodes.add(t.s);  
	      
	      Thing o=(Thing)t.o;
	      if(o.is_grounded)
	        gnodes.add(o);
	      else
		bnodes.add(o);
	    }   
	}
	Queue<Thing> arguments=new LinkedList<Thing>();
	arguments.addAll(gnodes);
	while(!arguments.isEmpty())
	{
	    Thing f=arguments.poll();
	    if(f!=null)
	    {
		List<Link> ifcs = f.i.drum_getIfcClassAttributes_notInverses();
		    for (int n = 0; n < ifcs.size(); n++) {
			Link l = (Link) ifcs.get(n);
			if(bnodes.contains(l.t1))
			{
			    l.t1.grounding_name=f.grounding_name+"."+l.property;
			    arguments.add(l.t1);
			}
		    }
	    }
	}
	
    }
     
    public void nameBlanks()
    {
	for(int i=0;i<msgs.size();i++)
	{
	    nameBlanksInMSG(msgs.get(i));
	}
    }
    
    
    public void deduceMSGs() {
	long links_count = linksList.size();
	for (int n = 0; n < 5000000; n++)
	    hg[n] = 0;
	msgs.clear();
	int maxcount = 0;

	long count = 0;
	while (!linksList.isEmpty()) {
	    Link l = linksList.removeLast();
	    if (!l.used) {
		count++;
		List<Triple> msg = new LinkedList<Triple>();
		Queue<Link> arguments = new LinkedList<Link>();
		arguments.add(l);
		while (!arguments.isEmpty()) {
		    count++;
		    createMSG(arguments, msg);
		}
		if (msg.size() > 0)
		  msgs.add(msg);
		if (msg.size() > maxcount)
		    maxcount = msg.size();
		hg[msg.size()]++;
	    }
	}
	
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if((t.is_grounded)||(IfcRoot.class.isInstance(t)))
	    {
		
		List<Triple> msg1 = new LinkedList<Triple>();
	        msg1.add(new Triple( t,"a",t.getClass().getSimpleName()));
		msgs.add(msg1);
	        
		List<ValuePair> palist=t.i.drum_getParameterAttributes();
		for(int i=0;i<palist.size();i++)
		{
		    ValuePair vp=palist.get(i);
			List<Triple> msg = new LinkedList<Triple>();
			if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
				continue;
			if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
				continue;

		        msg.add(new Triple( t,vp.getName(),vp.getValue()));
			if (msg.size() > 0)
				  msgs.add(msg);
		}
	    }
	}

	Link.printed.clear();
    }


    public TreeMap<Long,MSG_CRC> calculateMSG_CRCs(List<String> common_gids ) {

	TreeMap<Long,MSG_CRC> return_list = new  TreeMap<Long,MSG_CRC>();
        for (int i = 0; i < msgs.size(); i++) {            
	    long checksum = calculateCRC4_OneMSG(common_gids, i);
	    MSG_CRC msgcrc=new MSG_CRC(msgs.get(i), checksum);
	    return_list.put(checksum, msgcrc);
	}
	return return_list;
    }


    private long calculateCRC4_OneMSG(List<String> common_gids, int i) {
	CRC32 crc32=new CRC32();
	long  checksum=0;
	for (int j = 0; j < msgs.get(i).size(); j++)
	{
	Triple l=msgs.get(i).get(j);

	calcucalateCRCForNode(l.s, crc32,common_gids);
	crc32.update(l.p.getBytes());

	if(l.literal)
	  calcucalateCRCForLiteral(l.o, crc32);
	else
	  calcucalateCRCForNode(l.o, crc32,common_gids);
	checksum=checksum ^ crc32.getValue();
	crc32.reset();
	}
	return checksum;
    }
    /*
    public TreeMap<Long,MSG_CRC> calculateMSG_CRCs(List<String> common_gids ) {

	TreeMap<Long,MSG_CRC> return_list = new  TreeMap<Long,MSG_CRC>();
	CRC32 crc32=new CRC32();
	long  checksum=0;
        for (int i = 0; i < msgs.size(); i++) {
	    //System.out.println(" msg"+i);
	    for (int j = 0; j < msgs.get(i).size(); j++)
	    {
		Triple l=msgs.get(i).get(j);

		calcucalateCRCForNode(l.s, crc32,common_gids);
		crc32.update(l.p.getBytes());
		//sSystem.out.print(" "+l.p);

		//System.out.print(" "+l.property);
		if(l.literal)
		  calcucalateCRCForLiteral(l.o, crc32);
		else
		  calcucalateCRCForNode(l.o, crc32,common_gids);
		checksum=checksum ^ crc32.getValue();
		crc32.reset();
	    }
	    //System.out.println();
	    //System.out.println("cks:"+checksum);
	    MSG_CRC msgcrc=new MSG_CRC(msgs.get(i), checksum);
	    return_list.put(checksum, msgcrc);
	    checksum=0;
	}
	return return_list;
    }*/

    private void printMSG() {

        for (int i = 0; i < msgs.size(); i++) {
            if(msgs.get(i).size()>1000)
	    for (int j = 0; j < msgs.get(i).size(); j++)
	    {
		Triple l=msgs.get(i).get(j);
		if(Thing.class.isInstance(l.o))
		  System.out.println(" "+l.s.getClass().getSimpleName()+":"+l.s.line_number+"."+l.s.is_grounded+"->"+l.p+"->"+l.o.getClass().getSimpleName()+":"+((Thing)l.o).line_number+"."+((Thing)l.o).is_grounded);
		else
	          System.out.println(" "+l.s.getClass().getSimpleName()+":"+l.s.line_number+"."+l.s.is_grounded+"->"+l.p+"->"+l.o.getClass().getSimpleName()+":"+l.o);
	    }
	}

	System.out.println();
    }

    private void calcucalateCRCForNode(Object pointer,CRC32 crc32,List<String> common_gids ) {

	crc32.update(pointer.getClass().getSimpleName().getBytes());
	//System.out.print(" "+pointer.getClass().getSimpleName());

	if(((Thing)pointer).grounding_name!=null)
	  crc32.update(((Thing)pointer).grounding_name.getBytes());
	
	//if(((Thing)pointer).grounding_name!=null)
	//  System.out.print(" "+((Thing)pointer).grounding_name);
        /*if(IfcRoot.class.isInstance(pointer)) {
	    String sgid = ((IfcRoot) pointer).getGlobalId();
	    if (common_gids.contains(sgid)) 
	    {
		//System.out.print(" "+sgid);
		crc32.update(sgid.getBytes());
	    }
	}*/
        /*
	List<ValuePair> l = pointer.drum_getParameterAttributes();
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
		continue;
	    if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
		continue;
	    System.out.print(" "+vp.getName());
	    System.out.print(" "+vp.getValue().toString());
	    crc32.update(vp.getName().getBytes());
	    crc32.update(vp.getValue().toString().getBytes());
	}
       */

    }

    private void calcucalateCRCForLiteral(Object pointer,CRC32 crc32) {

	crc32.update(pointer.getClass().getSimpleName().getBytes());
	//System.out.print(" "+pointer.getClass().getSimpleName());
	
	crc32.update(pointer.toString().getBytes());
	//System.out.print(" "+pointer.toString());
    }

    private void printGrafwizGraph() {
	System.out.println("digraph G { rankdir=LR " + "node [shape=box, color=blue] ");

        for (int i = 0; i < msgs.size() ; i++) {
	    // System.out.println("subgraph cluster_msg"+i
	    // +"{\nlabel = \"msg"+i+"\";\ncolor=red;\n");
            if(msgs.get(i).size()>490)
	    for (int j = 0; j < msgs.get(i).size(); j++)
	    {
		Link l=new Link(msgs.get(i).get(j));
		if(!msgs.get(i).get(j).literal)
		System.out.println(l.nodes2String(entities));
	    }
	    // System.out.println("}");
	}
	System.out.println();

	for (int i = 0; i < msgs.size() ; i++) {

	        if(msgs.get(i).size()>490)
		for (int j = 0; j < msgs.get(i).size(); j++)
		{
			Link l=new Link(msgs.get(i).get(j));
			if(!msgs.get(i).get(j).literal)
		           System.out.println(l.toString());
		}
	}
	System.out.println("}");
	System.out.println();
    }

    private void createMSG(Queue<Link> arguments, List<Triple> msg) {
	Link l = arguments.remove();
	if (l.used)
	    return;
	l.used = true;
	msg.add(new Triple(l));
	
	Thing t1 = l.t1;
	Thing t2 = l.t2;
	
	if(!t1.is_grounded)
		if(!t1.literal_msg_done)
		{
		        msg.add(new Triple( t1,"a",t1.getClass().getSimpleName()));

			List<ValuePair> palist=t1.i.drum_getParameterAttributes();
			for(int i=0;i<palist.size();i++)
			{
			    ValuePair vp=palist.get(i);
				if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
					continue;
				if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
					continue;

			        msg.add(new Triple( t1,vp.getName(),vp.getValue()));
			}

		    
		    t1.literal_msg_done=true;
		}

	if(!t2.is_grounded)
		if(!t2.literal_msg_done)
		{
		        msg.add(new Triple( t2,"a",t2.getClass().getSimpleName()));
			List<ValuePair> palist=t2.i.drum_getParameterAttributes();
			for(int i=0;i<palist.size();i++)
			{
			    ValuePair vp=palist.get(i);
				if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
					continue;
				if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
					continue;

			        msg.add(new Triple( t2,vp.getName(),vp.getValue()));
			}
		    
		    t2.literal_msg_done=true;
		}

	if (!t1.is_grounded)
	    if (!IfcRoot.class.isInstance(t1)) {
		{
		    LinkedList<Link> followlist = linksmap.get(t1);
		    if (followlist == null)
			return;
		    while (!followlist.isEmpty()) {
			Link ll = followlist.removeLast();
			if (!ll.used)
			    if (!arguments.contains(ll))
				arguments.add(ll);
		    }
		}
	    }

	if (!t2.is_grounded)
	    if (!IfcRoot.class.isInstance(t2)) {
		{
		    LinkedList<Link> followlist = linksmap.get(t2);
		    if (followlist == null)
			return;
		    while (!followlist.isEmpty()) {
			Link ll = followlist.removeLast();

			if (!ll.used)
			    if (!arguments.contains(ll))
				arguments.add(ll);
		    }
		}

	    }

    }

    // ==============================================================================================================================
    // ==============================================================================================================================

    private void deduce_if_unique_node(Thing pointer, List<Thing> check_list) {

	List<ValuePair> ifcs = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    EntityVO evo = entities.get(pointer.getClass().getSimpleName().toUpperCase());
	    if (evo != null) {
		List<AttributeVO> aolist = evo.getDerived_attribute_list();
		for (int j = 0; j < aolist.size(); j++) {
		    AttributeVO ao = aolist.get(j);
		    if (ao.getName().equalsIgnoreCase(vp.getName())) {
			if (ao.isUnique()) {
			    // System.out.println(pointer.line_number + ":" +
			    // pointer.getClass().getName() + " unique:" +
			    // ao.getName());
			    pointer.is_grounded = true;
			    check_list.add(pointer);
		            pointer.grounding_name=ao.getName()+"."+vp.getValue().toString();
			}
		    }
		}
	    }
	}

    }

    private void deduce_if_unique_node(Thing pointer) {

	List<ValuePair> ifcs = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    EntityVO evo = entities.get(pointer.getClass().getSimpleName().toUpperCase());
	    if (evo != null) {
		List<AttributeVO> aolist = evo.getDerived_attribute_list();
		for (int j = 0; j < aolist.size(); j++) {
		    AttributeVO ao = aolist.get(j);
		    if (ao.getName().equalsIgnoreCase(vp.getName())) {
			if (ao.isUnique()) {
			    pointer.is_grounded = true;
		            pointer.grounding_name=ao.getName()+"."+vp.getValue().toString();
			}
		    }
		}
	    }
	}

    }

/*
 * Informal proposition

 No two or more elements (subtypes of IfcProduct) shall share the same instance of IfcObjectPlacement. 

 */

    public void groundbyObjectPlacement()
    {
	Describe.out("Ground by Object Placement");

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();


	    if (IfcProduct.class.isInstance(t1)) {
		List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
		for (int n = 0; n < ifcs.size(); n++) {
		    Link l = (Link) ifcs.get(n);
		    Thing t2 = l.getTheOtherEnd(t1);
		    if(!t2.is_grounded)
		    {
			if(l.property.equalsIgnoreCase("ObjectPlacement"))
			{
			    t2.is_grounded=true;
			    t2.grounding_name=t1.grounding_name+".ObjectPlacement";
			}
		    }
		}
	    }
	}

    }

    
    private class DecComparator implements Comparator<Integer>
    {

	public int compare(Integer o1, Integer o2) {	    
	    return o2-o1;
	}
	
    }
    DecComparator dec_comparator=new DecComparator();

    public void ground5MaxSOGL()
    {
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram_dec=new TreeMap<Integer,SortedMap<String,Integer>>(this.dec_comparator);  // links count <class name, number or individual entities>	
	//checkUniques();
	
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
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
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
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
	               // liian lähellä
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


       
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
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
			if(intval.intValue()==out_links)  // kaikki joilla vastaava märää linkkejä
			{
			    if(out_links>0)
			    {
			     t1.is_grounded=true;
			     t1.grounding_name=root.grounding_name+".big_outs."+t1.getClass().getSimpleName()+"."+out_links;
			     //System.out.println(root.grounding_name+".big_outs."+t1.getClass().getSimpleName()+"."+out_links);
			    }
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
			    if(in_links>0)
			    {
			     t1.is_grounded=true;
			     t1.grounding_name=root.grounding_name+".big_ins."+t1.getClass().getSimpleName()+"."+in_links;
			     //System.out.println(root.grounding_name+".big_ins."+t1.getClass().getSimpleName()+"."+in_links);
			    }			    
			}

	             }
	    }
	}

	System.gc();
	/*   boolean ifcOwnerHistory_handle_as_grounded
	 *   boolean set_literals
	 *   boolean test_unique_loops
	 *   boolean ifcGeometricRepresentationContext_ground
	 *   boolean ground_On_One2OneLinks_on, 
	 *   boolean ground_AnyLinks_on,
             boolean use_distance_from_core,
             boolean second_order_literals,
             boolean global_unique_naming) */

	//GroundSettings gs=new GroundSettings(true, true, true, true, true, false, false, true, true);	       
	//groundFromGUIDs(common_gids,gs);
	System.gc();

    }
    
    public void basic_ground(GroundSettings gs) {

	
        createOne2OneLinksMap();
	LinkedList<Thing> core_elements=getCoreElements();
	
	LinkedList<Thing> check_list = new LinkedList<Thing>();
	Map<Long,List<Thing>> uniqueloops=new HashMap<Long,List<Thing>>();

	preparationEntitiesScanForGroundingSecondOrder(gs, check_list, uniqueloops);

	//if(gs.isTest_unique_loops())
	for (Map.Entry<Long, List<Thing>> entry : uniqueloops.entrySet()) {
	    List<Thing> lt = (List<Thing>) entry.getValue();
	    if(lt.size()==1)
	    {
		Describe.out("Ground one valued lists");
                if(!lt.get(0).is_grounded)
                {
		 lt.get(0).is_grounded=true;
		 lt.get(0).grounding_name=root.grounding_name+"."+entry.getKey().toString();
                }
	    }
	}

	//TODO use later
	groundbyObjectPlacement();
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    t.incoming_count=0;
	}

	if(gs.isGround_On_One2OneLinks_on())
	while (!check_list.isEmpty()) {
	    Thing t = check_list.removeFirst();
	    collect_groundingsetBasedOn_One2OneLinks(t, check_list);
	}

	
	if(gs.isGround_On_One2OneLinks_on())
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    
	    if(t.i.drum_getGroudingPath()!=null)
	    if(t.i.drum_getGroudingPath().same_score_count>1)
	    {
		t.is_grounded=false;
	    }
	    else
	    {
	      GroundingPath gp=t.i.drum_getGroudingPath();
	      if(!gp.disabled)
	      if(gp.same_score_count==1)
	      {
		 if(!t.is_grounded)
		 {
	          Describe.out("One-to-one- grounding");
		  t.is_grounded=true;
		  t.grounding_name=gp.getGroundingName();
		 }
	      }
	    }
	}

	// RESET the model
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    
            if(t.is_grounded)
        	check_list.add(t);
            else
            {
             t.min_path_score=Integer.MAX_VALUE;	    
             t.linkcrcs.clear();
             t.incoming_groundLinks_lists.clear();
             t.last_paths_crc=Long.MAX_VALUE; 
            }
	}
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	// Free memory
	one2onelinksmap.clear();
        System.gc();
	createAnywayGroundingLinksMap();	    
        

	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	

	long groundables = 0;
	long more = 0;

	
	// Free memory
	anywayGroundingLinksmap.clear();
	System.gc();
    }

    
    
    public void groundFromGUIDs(List<String> common_gids,GroundSettings gs) {

	
        createOne2OneLinksMap();
	LinkedList<Thing> core_elements=getCoreElements();
	// The first run:

	// Calculate distance from the core tree
	for(int i=0;i<10;i++)
	for(int n=0;n<core_elements.size();n++) 
	{
	    Thing t = core_elements.removeFirst();
	    t.distance_from_element_tree=i*10;
	    get_the_nearest_elements(t, core_elements);
	}
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	
	
	LinkedList<Thing> check_list = new LinkedList<Thing>();
	Map<Long,List<Thing>> uniqueloops=new HashMap<Long,List<Thing>>();

	preparationEntitiesScanForGroundingSecondOrder(common_gids, gs, check_list, uniqueloops);

	//if(gs.isTest_unique_loops())
	for (Map.Entry<Long, List<Thing>> entry : uniqueloops.entrySet()) {
	    List<Thing> lt = (List<Thing>) entry.getValue();
	    if(lt.size()==1)
	    {
		Describe.out("Ground one valued lists");

		lt.get(0).is_grounded=true;
		lt.get(0).grounding_name=root.grounding_name+"."+entry.getKey().toString();
	    }
	}

	//TODO use later
	groundbyObjectPlacement();
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    t.incoming_count=0;
	}

	if(gs.isGround_On_One2OneLinks_on())
	while (!check_list.isEmpty()) {
	    Thing t = check_list.removeFirst();
	    collect_groundingsetBasedOn_One2OneLinks(t, check_list);
	}

	
	if(gs.isGround_On_One2OneLinks_on())
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    
	    if(t.i.drum_getGroudingPath()!=null)
	    if(t.i.drum_getGroudingPath().same_score_count>1)
	    {
		t.is_grounded=false;
	    }
	    else
	    {
	      GroundingPath gp=t.i.drum_getGroudingPath();
	      if(!gp.disabled)
	      if(gp.same_score_count==1)
	      {
		 if(!t.is_grounded)
		 {
	          Describe.out("One-to-one- grounding");
		  t.is_grounded=true;
		  t.grounding_name=gp.getGroundingName();
		 }
	      }
	    }
	}

	// RESET the model
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    
            if(t.is_grounded)
        	check_list.add(t);
            else
            {
             t.min_path_score=Integer.MAX_VALUE;	    
             t.linkcrcs.clear();
             t.incoming_groundLinks_lists.clear();
             t.last_paths_crc=Long.MAX_VALUE; 
            }
	}
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	// Free memory
	one2onelinksmap.clear();
        System.gc();
	createAnywayGroundingLinksMap();	    
        
	if(gs.isGround_AnyLinks_on())
	while (!check_list.isEmpty()) {
	    Thing t = check_list.removeFirst();
	    collect_groundingsetBasedOn_AnywayLinks(t,check_list,gs.isUse_distance_from_core());
	}
	

	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	

	long groundables = 0;
	long more = 0;

	if(gs.isGround_AnyLinks_on())
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    Describe.out("Any link grounding");
	    
	    if(t.i.drum_getGroudingPath()!=null)
	    if(t.i.drum_getGroudingPath().same_score_count>1)
	    {
		t.is_grounded=false;
		more++;
	    }
	    else
	    {
		GroundingPath gp=t.i.drum_getGroudingPath();
		if(!gp.disabled)
		if(gp.same_score_count==1)
		{
			 if(!t.is_grounded)
			 {
			  t.is_grounded=true;
			  t.grounding_name=gp.getGroundingName();
			 }
		}
	     }
		
	    if(t.is_grounded)
		groundables++;
	}
	
	// Free memory
	anywayGroundingLinksmap.clear();
	System.gc();
	/*
	System.out.println();
	System.out.println("- Groundables: " + (groundables-common_gids.size()));
	System.out.println("- More than one shortest path of the same lengt: "+more);
	System.out.println("- GUIDs: " + gid_map.size());
	System.out.println("- Object count in object buffer: " + object_buffer.size());
        */
	
	// Print out groundings:
	/*for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    t.printOutIncomingLinks();
	}*/

    }

    private void preparationEntitiesScanForGrounding(List<String> common_gids, GroundSettings gs, LinkedList<Thing> check_list, Map<Long, List<Thing>> uniqueloops) {
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    t.incoming_count=0;
	}
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();

	    if (IfcRoot.class.isInstance(t)) {
		if(common_gids.contains(((IfcRoot)t).getGlobalId()))
		{
		    // Muuttuneet groundataan ympäristön mukaan
		    check_list.add(t);
		    t.is_grounded = true;
		    t.grounding_name=((IfcRoot)t).getGlobalId();
		}
	    } else
		// Check, if the object is unique:
		deduce_if_unique_node(t, check_list);

	    // ----------------------------------------------------
	    if(gs.isTest_unique_loops())
	    if (IfcPolyLoop.class.isInstance(t)) {
		IfcPolyLoop pl=(IfcPolyLoop)t;
		// get context id
		// crc coordinates
		CRC32 tcrc=new CRC32();
		if(pl.getPolygon().size()>2)
		for(int j=0;j<pl.getPolygon().size();j++)
		{
		   for(int k=0;k<pl.getPolygon().get(j).getCoordinates().size();k++)
		   {
		       tcrc.update(pl.getPolygon().get(j).getCoordinates().get(k).toString().getBytes());
		   }
		}
		Long key=Long.valueOf(tcrc.getValue());
		List<Thing> uvalues=uniqueloops.get(key);
		if(uvalues==null)
		{
		    uvalues=new ArrayList<Thing>();		    
		    uniqueloops.put(key, uvalues);
		}
		uvalues.add(pl);
	    }

	    if(gs.isTest_unique_loops())
	    if (IfcPolyline.class.isInstance(t)) {
		IfcPolyline pl=(IfcPolyline)t;
		// crc coordinates
		CRC32 tcrc=new CRC32();
		if(pl.getPoints().size()>0)
		for(int j=0;j<pl.getPoints().size();j++)
		{
		   for(int k=0;k<pl.getPoints().get(j).getCoordinates().size();k++)
		   {
		       tcrc.update(pl.getPoints().get(j).getCoordinates().get(k).toString().getBytes());
		   }
		}
		Long key=Long.valueOf(tcrc.getValue());
		List<Thing> uvalues=uniqueloops.get(key);
		if(uvalues==null)
		{
		    uvalues=new ArrayList<Thing>();
		    uniqueloops.put(key, uvalues);
		}
		uvalues.add(pl);
	    }
	    if (IfcOwnerHistory.class.isInstance(t)) {
		if(gs.isIfcOwnerHistory_handle_as_grounded())
		{
		  t.is_grounded = true;
 		  t.grounding_name=root.getGlobalId()+"."+IfcOwnerHistory.class.getSimpleName();
		}		
	    }
	    
	    // These are literal like element.. shared
	    //-----------------------------------------
	    /*
	     IfcCartesianPoint
             IfcDirection
             IfcSIUnit
             IfcMaterial
             IfcDraughtingPreDefinedCurveFont
             IfcColourRgb
             IfcClassification
	     */
	    
	    if(gs.isSet_literals())
	    {
	     if (IfcCartesianPoint.class.isInstance(t)) {
		IfcCartesianPoint tp= (IfcCartesianPoint)t;
		t.is_grounded=true;
		StringBuffer sb=new StringBuffer();
		for(int n=0;n<tp.getCoordinates().size();n++)
		{
		    if(n>0)
		      sb.append('.');
		    sb.append(tp.getCoordinates().get(n));
			  
		}
		t.grounding_name=root.grounding_name+"."+sb.toString();
	     }

	     if (IfcDirection.class.isInstance(t)) {
		 IfcDirection td= (IfcDirection)t;
		t.is_grounded=true;
		StringBuffer sb=new StringBuffer();
		for(int n=0;n<td.getDirectionRatios().size();n++)
		{
		    if(n>0)
		      sb.append('.');
		    sb.append(td.getDirectionRatios().get(n));
			  
		}
		t.grounding_name=root.grounding_name+"."+sb.toString();
	     }

             
	     if (IfcSIUnit.class.isInstance(t)) {
		 IfcSIUnit tv= (IfcSIUnit)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getUnitType()+"."+tv.getPrefix()+"."+tv.getName();
	     }

	     if (IfcMaterial.class.isInstance(t)) {
		 IfcMaterial tv= (IfcMaterial)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getName();
	     }

             
	     if (IfcDraughtingPreDefinedCurveFont.class.isInstance(t)) {
		 IfcDraughtingPreDefinedCurveFont tv= (IfcDraughtingPreDefinedCurveFont)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getName();
	     }
             

	     if (IfcColourRgb.class.isInstance(t)) {
		 IfcColourRgb tr= (IfcColourRgb)t;
		t.is_grounded=true;
		t.grounding_name=root.grounding_name+"."+tr.getRed()+"."+tr.getGreen()+"."+tr.getBlue();		 
	     }
	     
             
	     if (IfcClassification.class.isInstance(t)) {
		 IfcClassification tv= (IfcClassification)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getSource()+"."+tv.getEdition()+"."+tv.getName();		 
	     }
	     
	    }
	    
	    if(gs.isIfcGeometricRepresentationContext_ground())
	    if (IfcGeometricRepresentationContext.class.isInstance(t)) {
		t.is_grounded = true;
		t.grounding_name=root.getGlobalId()+"."+IfcGeometricRepresentationContext.class.getSimpleName()+"."+((IfcGeometricRepresentationContext)t).getContextType();
	    }

	    // ----------------------------------------------------
	}
    }

    private void preparationEntitiesScanForGroundingSecondOrder(List<String> common_gids, GroundSettings gs, LinkedList<Thing> check_list, Map<Long, List<Thing>> uniqueloops) {
	for (Map.Entry<Long, Thing> entrys : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entrys.getValue();
	    t1.incoming_count=0;
	}
	for (Map.Entry<Long, Thing> entrys : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entrys.getValue();
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
	}

	Describe.out("Uniques are grounded");
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();

	    if (IfcRoot.class.isInstance(t)) {
	    Describe.out("IfcRoot is grounded, if in common_guids");
		if(common_gids.contains(((IfcRoot)t).getGlobalId()))
		{
		    // Muuttuneet groundataan ympäristön mukaan
		    check_list.add(t);
		    t.is_grounded = true;
		    t.grounding_name=((IfcRoot)t).getGlobalId();
		}
	    } else
		// Check, if the object is unique:
		deduce_if_unique_node(t, check_list);

	    // ----------------------------------------------------
	    if(gs.isTest_unique_loops())
	    {
	    Describe.out("Unique loops are grounded");
	    if (IfcPolyLoop.class.isInstance(t)) {
		IfcPolyLoop pl=(IfcPolyLoop)t;
		// get context id
		// crc coordinates
		CRC32 tcrc=new CRC32();
		if(pl.getPolygon().size()>2)
		for(int j=0;j<pl.getPolygon().size();j++)
		{
		   for(int k=0;k<pl.getPolygon().get(j).getCoordinates().size();k++)
		   {
		       tcrc.update(pl.getPolygon().get(j).getCoordinates().get(k).toString().getBytes());
		   }
		}
		Long key=Long.valueOf(tcrc.getValue());
		List<Thing> uvalues=uniqueloops.get(key);
		if(uvalues==null)
		{
		    uvalues=new ArrayList<Thing>();		    
		    uniqueloops.put(key, uvalues);
		}
		uvalues.add(pl);
	    }

	    if (IfcPolyline.class.isInstance(t)) {
		IfcPolyline pl=(IfcPolyline)t;
		// crc coordinates
		CRC32 tcrc=new CRC32();
		if(pl.getPoints().size()>0)
		for(int j=0;j<pl.getPoints().size();j++)
		{
		   for(int k=0;k<pl.getPoints().get(j).getCoordinates().size();k++)
		   {
		       tcrc.update(pl.getPoints().get(j).getCoordinates().get(k).toString().getBytes());
		   }
		}
		Long key=Long.valueOf(tcrc.getValue());
		List<Thing> uvalues=uniqueloops.get(key);
		if(uvalues==null)
		{
		    uvalues=new ArrayList<Thing>();
		    uniqueloops.put(key, uvalues);
		}
		uvalues.add(pl);
	    }
	    }
	    if (IfcOwnerHistory.class.isInstance(t)) {
		Describe.out("IfcOwnerHistory is grounded");
		if(gs.isIfcOwnerHistory_handle_as_grounded())
		{
		  t.is_grounded = true;
 		  t.grounding_name=root.getGlobalId()+"."+IfcOwnerHistory.class.getSimpleName();
		}		
	    }
	    // Shared second order literal kind of elements
            if(gs.isSecond_order_literals())
            {
		    Describe.out("Second order literal kind of elements grounded");
		    Thing t1 = (Thing) t;
		    if(IfcOwnerHistory.class.isInstance(t1))
		        	continue;
		    if(t1.is_grounded)
			  continue;
		    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
		    
		    if(ifcs.size()>0)
		    if(t1.incoming_count>1)
		    {
			    boolean no_olinks=true;
	                    for(int i=0;i<ifcs.size();i++)
	                    {
	        		Link l = (Link) ifcs.get(i);
	        		Thing t2=l.getTheOtherEnd(t1);
	        		if(t2.i.drum_getIfcClassAttributes_notInverses().size()>0)
	                	  no_olinks=false;
	                    }
	                    if(no_olinks)
	                    {
			      t1.is_grounded=true;
			      CRC32 tcrc=new CRC32();
		              for(int i=0;i<ifcs.size();i++)
		              {
		        		Link l = (Link) ifcs.get(i);
		        		Thing t2=l.getTheOtherEnd(t1);
		        		for(int n=0;n<t2.i.drum_getParameterAttributes().size();n++)
		        		    tcrc.update(t2.i.drum_getParameterAttributes().get(n).toString().getBytes());
		              }
		              t1.grounding_name=root.getGlobalId()+"."+tcrc.getValue();
	                    }
		    }
		}

	    
	    
	    // These are literal like element.. shared
	    //-----------------------------------------
	    /*
	     IfcCartesianPoint
             IfcDirection
             IfcSIUnit
             IfcMaterial
             IfcDraughtingPreDefinedCurveFont
             IfcColourRgb
             IfcClassification
	     */
	    
	    if(gs.isSet_literals())
	    {
	     if (IfcCartesianPoint.class.isInstance(t)) {
		IfcCartesianPoint tp= (IfcCartesianPoint)t;
		t.is_grounded=true;
		StringBuffer sb=new StringBuffer();
		for(int n=0;n<tp.getCoordinates().size();n++)
		{
		    if(n>0)
		      sb.append('.');
		    sb.append(tp.getCoordinates().get(n));
			  
		}
		Describe.out("literal like elements are grounded");
		t.grounding_name=root.grounding_name+"."+sb.toString();
	     }

	     if (IfcDirection.class.isInstance(t)) {
		 IfcDirection td= (IfcDirection)t;
		t.is_grounded=true;
		StringBuffer sb=new StringBuffer();
		for(int n=0;n<td.getDirectionRatios().size();n++)
		{
		    if(n>0)
		      sb.append('.');
		    sb.append(td.getDirectionRatios().get(n));
			  
		}
		t.grounding_name=root.grounding_name+"."+sb.toString();
	     }

             
	     if (IfcSIUnit.class.isInstance(t)) {
		 IfcSIUnit tv= (IfcSIUnit)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getUnitType()+"."+tv.getPrefix()+"."+tv.getName();
	     }

	     if (IfcMaterial.class.isInstance(t)) {
		 IfcMaterial tv= (IfcMaterial)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getName();
	     }

             
	     if (IfcDraughtingPreDefinedCurveFont.class.isInstance(t)) {
		 IfcDraughtingPreDefinedCurveFont tv= (IfcDraughtingPreDefinedCurveFont)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getName();
	     }
             

	     if (IfcColourRgb.class.isInstance(t)) {
		 IfcColourRgb tr= (IfcColourRgb)t;
		t.is_grounded=true;
		t.grounding_name=root.grounding_name+"."+tr.getRed()+"."+tr.getGreen()+"."+tr.getBlue();		 
	     }
	     
             
	     if (IfcClassification.class.isInstance(t)) {
		 IfcClassification tv= (IfcClassification)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getSource()+"."+tv.getEdition()+"."+tv.getName();		 
	     }
	     
	    }
	    
	    if(gs.isIfcGeometricRepresentationContext_ground())
	    if (IfcGeometricRepresentationContext.class.isInstance(t)) {
		Describe.out("IfcGeometricRepresentationContext nodes are grounded");
		t.is_grounded = true;
		t.grounding_name=root.getGlobalId()+"."+IfcGeometricRepresentationContext.class.getSimpleName()+"."+((IfcGeometricRepresentationContext)t).getContextType();
	    }

	    /*   Globally uniquely named objects:
	     * 
	     *   IfcCurveStyleFont
                 IfcClassification
                 IfcTextStyle
                 IfcPresentationLayerAssignment
                 IfcFillAreaStyle
                 IfcConversionBasedUnit
	     */
	    if(gs.isGlobal_unique_naming())
	    {		
		    Describe.out("Globally uniquely named nodes are grounded");

		     if (IfcCurveStyleFont.class.isInstance(t)) {
			 IfcCurveStyleFont v = (IfcCurveStyleFont) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcClassification.class.isInstance(t)) {
			 IfcClassification v = (IfcClassification) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcTextStyle.class.isInstance(t)) {
			 IfcTextStyle v = (IfcTextStyle) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcPresentationLayerAssignment.class.isInstance(t)) {
			 IfcPresentationLayerAssignment v = (IfcPresentationLayerAssignment) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcFillAreaStyle.class.isInstance(t)) {
			 IfcFillAreaStyle v = (IfcFillAreaStyle) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcConversionBasedUnit.class.isInstance(t)) {
			 IfcConversionBasedUnit v = (IfcConversionBasedUnit) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
	     
	    }
	    // ----------------------------------------------------
	}
    }


    private void preparationEntitiesScanForGroundingSecondOrder(GroundSettings gs, LinkedList<Thing> check_list, Map<Long, List<Thing>> uniqueloops) {
	for (Map.Entry<Long, Thing> entrys : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entrys.getValue();
	    t1.incoming_count=0;
	}
	for (Map.Entry<Long, Thing> entrys : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entrys.getValue();
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
	}

	Describe.out("Uniques are grounded");
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();

	    if (IfcRoot.class.isInstance(t)) {
		    // Muuttuneet groundataan ympäristön mukaan
		    check_list.add(t);
		    t.is_grounded = true;
		    t.grounding_name=((IfcRoot)t).getGlobalId();
	    } else
		// Check, if the object is unique:
		deduce_if_unique_node(t, check_list);

	    // ----------------------------------------------------
	    if(gs.isTest_unique_loops())
	    {
	    Describe.out("Unique loops are grounded");
	    if (IfcPolyLoop.class.isInstance(t)) {
		IfcPolyLoop pl=(IfcPolyLoop)t;
		// get context id
		// crc coordinates
		CRC32 tcrc=new CRC32();
		if(pl.getPolygon().size()>2)
		for(int j=0;j<pl.getPolygon().size();j++)
		{
		   for(int k=0;k<pl.getPolygon().get(j).getCoordinates().size();k++)
		   {
		       tcrc.update(pl.getPolygon().get(j).getCoordinates().get(k).toString().getBytes());
		   }
		}
		Long key=Long.valueOf(tcrc.getValue());
		List<Thing> uvalues=uniqueloops.get(key);
		if(uvalues==null)
		{
		    uvalues=new ArrayList<Thing>();		    
		    uniqueloops.put(key, uvalues);
		}
		uvalues.add(pl);
	    }

	    if (IfcPolyline.class.isInstance(t)) {
		IfcPolyline pl=(IfcPolyline)t;
		// crc coordinates
		CRC32 tcrc=new CRC32();
		if(pl.getPoints().size()>0)
		for(int j=0;j<pl.getPoints().size();j++)
		{
		   for(int k=0;k<pl.getPoints().get(j).getCoordinates().size();k++)
		   {
		       tcrc.update(pl.getPoints().get(j).getCoordinates().get(k).toString().getBytes());
		   }
		}
		Long key=Long.valueOf(tcrc.getValue());
		List<Thing> uvalues=uniqueloops.get(key);
		if(uvalues==null)
		{
		    uvalues=new ArrayList<Thing>();
		    uniqueloops.put(key, uvalues);
		}
		uvalues.add(pl);
	    }
	    }
	    if (IfcOwnerHistory.class.isInstance(t)) {
		Describe.out("IfcOwnerHistory is grounded");
		if(gs.isIfcOwnerHistory_handle_as_grounded())
		{
		  t.is_grounded = true;
 		  t.grounding_name=root.getGlobalId()+"."+IfcOwnerHistory.class.getSimpleName();
		}		
	    }
	    // Shared second order literal kind of elements
            if(gs.isSecond_order_literals())
            {
		    Describe.out("Second order literal kind of elements grounded");
		    Thing t1 = (Thing) t;
		    if(IfcOwnerHistory.class.isInstance(t1))
		        	continue;
		    if(t1.is_grounded)
			  continue;
		    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
		    
		    if(ifcs.size()>0)
		    if(t1.incoming_count>1)
		    {
			    boolean no_olinks=true;
	                    for(int i=0;i<ifcs.size();i++)
	                    {
	        		Link l = (Link) ifcs.get(i);
	        		Thing t2=l.getTheOtherEnd(t1);
	        		if(t2.i.drum_getIfcClassAttributes_notInverses().size()>0)
	                	  no_olinks=false;
	                    }
	                    if(no_olinks)
	                    {
			      t1.is_grounded=true;
			      CRC32 tcrc=new CRC32();
		              for(int i=0;i<ifcs.size();i++)
		              {
		        		Link l = (Link) ifcs.get(i);
		        		Thing t2=l.getTheOtherEnd(t1);
		        		for(int n=0;n<t2.i.drum_getParameterAttributes().size();n++)
		        		    tcrc.update(t2.i.drum_getParameterAttributes().get(n).toString().getBytes());
		              }
		              t1.grounding_name=root.getGlobalId()+"."+tcrc.getValue();
	                    }
		    }
		}

	    
	    
	    // These are literal like element.. shared
	    //-----------------------------------------
	    /*
	     IfcCartesianPoint
             IfcDirection
             IfcSIUnit
             IfcMaterial
             IfcDraughtingPreDefinedCurveFont
             IfcColourRgb
             IfcClassification
	     */
	    
	    if(gs.isSet_literals())
	    {
	     if (IfcCartesianPoint.class.isInstance(t)) {
		IfcCartesianPoint tp= (IfcCartesianPoint)t;
		t.is_grounded=true;
		StringBuffer sb=new StringBuffer();
		for(int n=0;n<tp.getCoordinates().size();n++)
		{
		    if(n>0)
		      sb.append('.');
		    sb.append(tp.getCoordinates().get(n));
			  
		}
		Describe.out("literal like elements are grounded");
		t.grounding_name=root.grounding_name+"."+sb.toString();
	     }

	     if (IfcDirection.class.isInstance(t)) {
		 IfcDirection td= (IfcDirection)t;
		t.is_grounded=true;
		StringBuffer sb=new StringBuffer();
		for(int n=0;n<td.getDirectionRatios().size();n++)
		{
		    if(n>0)
		      sb.append('.');
		    sb.append(td.getDirectionRatios().get(n));
			  
		}
		t.grounding_name=root.grounding_name+"."+sb.toString();
	     }

             
	     if (IfcSIUnit.class.isInstance(t)) {
		 IfcSIUnit tv= (IfcSIUnit)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getUnitType()+"."+tv.getPrefix()+"."+tv.getName();
	     }

	     if (IfcMaterial.class.isInstance(t)) {
		 IfcMaterial tv= (IfcMaterial)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getName();
	     }

             
	     if (IfcDraughtingPreDefinedCurveFont.class.isInstance(t)) {
		 IfcDraughtingPreDefinedCurveFont tv= (IfcDraughtingPreDefinedCurveFont)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getName();
	     }
             

	     if (IfcColourRgb.class.isInstance(t)) {
		 IfcColourRgb tr= (IfcColourRgb)t;
		t.is_grounded=true;
		t.grounding_name=root.grounding_name+"."+tr.getRed()+"."+tr.getGreen()+"."+tr.getBlue();		 
	     }
	     
             
	     if (IfcClassification.class.isInstance(t)) {
		 IfcClassification tv= (IfcClassification)t;
		 t.is_grounded=true;
		 t.grounding_name=root.grounding_name+"."+tv.getSource()+"."+tv.getEdition()+"."+tv.getName();		 
	     }
	     
	    }
	    
	    if(gs.isIfcGeometricRepresentationContext_ground())
	    if (IfcGeometricRepresentationContext.class.isInstance(t)) {
		Describe.out("IfcGeometricRepresentationContext nodes are grounded");
		t.is_grounded = true;
		t.grounding_name=root.getGlobalId()+"."+IfcGeometricRepresentationContext.class.getSimpleName()+"."+((IfcGeometricRepresentationContext)t).getContextType();
	    }

	    /*   Globally uniquely named objects:
	     * 
	     *   IfcCurveStyleFont
                 IfcClassification
                 IfcTextStyle
                 IfcPresentationLayerAssignment
                 IfcFillAreaStyle
                 IfcConversionBasedUnit
	     */
	    if(gs.isGlobal_unique_naming())
	    {		
		    Describe.out("Globally uniquely named nodes are grounded");

		     if (IfcCurveStyleFont.class.isInstance(t)) {
			 IfcCurveStyleFont v = (IfcCurveStyleFont) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcClassification.class.isInstance(t)) {
			 IfcClassification v = (IfcClassification) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcTextStyle.class.isInstance(t)) {
			 IfcTextStyle v = (IfcTextStyle) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcPresentationLayerAssignment.class.isInstance(t)) {
			 IfcPresentationLayerAssignment v = (IfcPresentationLayerAssignment) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcFillAreaStyle.class.isInstance(t)) {
			 IfcFillAreaStyle v = (IfcFillAreaStyle) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
		     if (IfcConversionBasedUnit.class.isInstance(t)) {
			 IfcConversionBasedUnit v = (IfcConversionBasedUnit) t;
				t.is_grounded = true;
				t.grounding_name=root.getGlobalId()+"."+t.getClass().getSimpleName()+"."+v.getName();
		             }
	     
	    }
	    // ----------------------------------------------------
	}
    }

    
    public void checkUniques() {
	    checkUniques(true);
    }
    public void checkUniques(boolean ownerHistory) {

	Describe.out("Unique elements are grounded always");


	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();

	    if(ownerHistory)
	    if (IfcOwnerHistory.class.isInstance(t)) {
		  t.is_grounded = true;
 		  t.grounding_name=root.getGlobalId()+"."+IfcOwnerHistory.class.getSimpleName();
	    }

	    if (IfcRoot.class.isInstance(t)) {
		    // Muuttuneet groundataan ympäristön mukaan
		    t.is_grounded = true;
		    t.grounding_name=((IfcRoot)t).getGlobalId();
	    } else
		// Check, if the object is unique:
		deduce_if_unique_node(t);
	}

	
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
    }
    
    
    
    private void collect_groundingsetBasedOn_AnywayLinks(Thing pointer, List<Thing> check_list,boolean use_distance_from_core) {
	Describe.out("Grounding collected based on any links");

	// Grouded nodes are visited only once
	if(pointer.is_grounded)
	{
	 if (pointer.i.isTouched())
	     return;
	 pointer.i.setTouched(true);
	 touched_set.add(pointer);
	}
	if(pointer.last_paths_crc==pointer.i.drum_getpathsCRC())
	    return;
	else	    
	    pointer.last_paths_crc=pointer.i.drum_getpathsCRC();
	
	
	LinkedList<Link> alinks=anywayGroundingLinksmap.get(pointer);
	if(alinks!=null)
	{
	    for(int n=0;n< alinks.size() ;n++)
	    {
		Link l=alinks.get(n);
		Thing t=l.getTheOtherEnd(pointer);
		if(t.is_grounded)
		    continue;

		if(pointer.incoming_groundLinks_lists.size()==0)
		{
		    GroundingPath t_path=new GroundingPath(pointer);
		    t_path.add(l);
		    if(use_distance_from_core)  // only once
		      t_path.score+=t.distance_from_element_tree;
		    if(l.isTheWay(pointer))
			t_path.score+=1;
		    else
			t_path.score+=10;
		    if(!t.linkcrcs.contains(t_path.getCRC32()))
		    {
			       if(t_path.getList().size()<50)
			       {
			        t.incoming_groundLinks_lists.add(t_path);
			        t.linkcrcs.add(t_path.getCRC32());
			       }
		    }
		    if(t_path.score<t.min_path_score)
			t.min_path_score=t_path.score;
		    if(grounding_paths.register(t_path))
		    {
			//t.min_path_score=Integer.MAX_VALUE;
		    }
		}
		else		
		// here the linkset to this node
		for(int i=0;i<pointer.incoming_groundLinks_lists.size();i++)
		{
		    GroundingPath pointer_path=pointer.incoming_groundLinks_lists.get(i);
		    GroundingPath t_path=new GroundingPath(pointer_path.grounded_by);
		    t_path.score=pointer_path.score;
		    t_path.addAll(pointer_path.getList());
		    t_path.add(l);		    

		    if(l.isTheWay(pointer))
			t_path.score+=1;
		    else
			t_path.score+=10;
		    if(t_path.score<=t.min_path_score)
		    {
			    if(!t.linkcrcs.contains(t_path.getCRC32()))
			    {
				       if(t_path.getList().size()<50)
				       {
					       t.incoming_groundLinks_lists.add(t_path);
					       t.linkcrcs.add(t_path.getCRC32());
				       }
			    }
		    }
		    
		    if(t_path.score<t.min_path_score)
			t.min_path_score=t_path.score;
		    if(grounding_paths.register(t_path))
		    {
			//t.min_path_score=Integer.MAX_VALUE;
		    }
		}
		if(!t.i.isTouched())
		   check_list.add(t);
	    }
	}

    }

    private void collect_groundingsetBasedOn_One2OneLinks(Thing pointer, List<Thing> check_list) {
	Describe.out("Grounding collected based on One-to-one links");

	if(pointer.is_grounded)
	{
	 if (pointer.i.isTouched())
	     return;
	 pointer.i.setTouched(true);
	 touched_set.add(pointer);
	}
	if(pointer.last_paths_crc==pointer.i.drum_getpathsCRC())
	    return;
	else	    
	    pointer.last_paths_crc=pointer.i.drum_getpathsCRC();
	
	LinkedList<Link> o2olinks=one2onelinksmap.get(pointer);
	if(o2olinks!=null)
	{
	    for(int n=0;n< o2olinks.size() ;n++)
	    {
		Link l=o2olinks.get(n);
		Thing t=l.getTheOtherEnd(pointer);
		if(t.is_grounded)
		    continue;

		if(pointer.incoming_groundLinks_lists.size()==0)
		{
		    GroundingPath t_path=new GroundingPath(pointer);
		    t_path.add(l);
		    if(l.isTheWay(pointer))
			t_path.score+=1;
		    else
			t_path.score+=10;
		    if(!t.linkcrcs.contains(t_path.getCRC32()))
		    {
			       t.incoming_groundLinks_lists.add(t_path);
			       t.linkcrcs.add(t_path.getCRC32());
		    }
		    if(t_path.score<t.min_path_score)
			t.min_path_score=t_path.score;
		    if(grounding_paths.register(t_path))
		    {
			//t.min_path_score=Integer.MAX_VALUE;
		    }
		}
		else		
		// here the linkset to this node
		for(int i=0;i<pointer.incoming_groundLinks_lists.size();i++)
		{
		    GroundingPath pointer_path=pointer.incoming_groundLinks_lists.get(i);
		    GroundingPath t_path=new GroundingPath(pointer_path.grounded_by);
		    t_path.score=pointer_path.score;
		    t_path.addAll(pointer_path.getList());
		    t_path.add(l);
		    if(l.isTheWay(pointer))
			t_path.score+=1;
		    else
			t_path.score+=10;
		    if(t_path.score<=t.min_path_score)
		    {
			    if(!t.linkcrcs.contains(t_path.getCRC32()))
			    {
				       t.incoming_groundLinks_lists.add(t_path);
				       t.linkcrcs.add(t_path.getCRC32());
			    }
		    }
		    
		    if(t_path.score<t.min_path_score)
			t.min_path_score=t_path.score;
		    if(grounding_paths.register(t_path))
		    {
			//t.min_path_score=Integer.MAX_VALUE;
		    }
		}
		if(!t.i.isTouched())
		   check_list.add(t);
	    }
	}

    }

    private void get_the_nearest_elements(Thing pointer, List<Thing> echeck_list) {

	    
 	 if (pointer.i.isTouched())
 	     return;
 	 pointer.i.setTouched(true);
 	 touched_set.add(pointer);

 	LinkedList<Link> alinks=anywayGroundingLinksmap.get(pointer);
 	if(alinks!=null)
 	{
 	    for(int n=0;n< alinks.size() ;n++)
 	    {
 		Link l=alinks.get(n);
 		Thing t=l.getTheOtherEnd(pointer);
 		echeck_list.add(t);
 	    }
 	}

     }
    
    // ==============================================================================================================================
    // ==============================================================================================================================

    final Map<Thing, LinkedList<Link>> one2onelinksmap = new HashMap<Thing, LinkedList<Link>>();

    // Create data structure for MSG and groundings
    public void createOne2OneLinksMap() {
	one2onelinksmap.clear();
	//System.out.println("o2o map");

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();

	    List<ValuePair> ifcs = t1.i.ifcObjectList();

	    for (int n = 0; n < ifcs.size(); n++) {
		ValuePair vp = (ValuePair) ifcs.get(n);
		Thing t2 = (Thing) vp.getValue();

		boolean is_one2one = false;

		EntityVO evo = entities.get(t1.getClass().getSimpleName().toUpperCase());
		if (evo != null) {
		    List<AttributeVO> aolist = evo.getDerived_attribute_list();
		    for (int i = 0; i < aolist.size(); i++) {
			AttributeVO ao = aolist.get(i);
			if (ao.getName().equalsIgnoreCase(vp.getName()))
			    if (ao.isOne2One()) {
				is_one2one = true;
			    }
		    }
		}

		if (is_one2one) 
		{
		    Link l = new Link(t1, t2, vp.getName());

		    // DIRECT
		    LinkedList<Link> s1 = one2onelinksmap.get(t1);
		    if (s1 == null) {
			s1 = new LinkedList<Link>();
			one2onelinksmap.put(t1, s1);
		    }

		    //if (!s1.contains(l))
			s1.add(l);

		    // BACKWARDS
		    LinkedList<Link> s2 = one2onelinksmap.get(t2);
		    if (s2 == null) {
			s2 = new LinkedList<Link>();
			one2onelinksmap.put(t2, s2);
		    }
		    //if (!s2.contains(l))
			s2.add(l);

		}
	    }
	}
    }

    
    final Map<Thing, LinkedList<Link>> anywayGroundingLinksmap = new HashMap<Thing, LinkedList<Link>>();

    // Create data structure for MSG and groundings
    
    public void createAnywayGroundingLinksMap() {
	anywayGroundingLinksmap.clear();
	//System.out.println("anyway map");
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();

	    List<Link> ifcs = t1.i.ifcAnyObjectListOfLimitedCardinality();

	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2 = (Thing) l.t2;

		    // DIRECT
		    if(!t2.is_grounded)
		    {
 		      LinkedList<Link> s1 = anywayGroundingLinksmap.get(t1);
		      if (s1 == null) {
			s1 = new LinkedList<Link>();
			anywayGroundingLinksmap.put(t1, s1);
		      }

		      //if (!s1.contains(l))
			s1.add(l);
		     }

		    // BACKWARDS
		    if(!t1.is_grounded)
		    {
		      LinkedList<Link> s2 = anywayGroundingLinksmap.get(t2);
		     if (s2 == null) {
			s2 = new LinkedList<Link>();
			anywayGroundingLinksmap.put(t2, s2);
		     }
		      //if (!s2.contains(l))
			s2.add(l);
		    }

	    }
	}

    }

    
    public Map<String, LinkedList<Thing>> createOne2OneRootLinksMap() {
	Map<String, LinkedList<Thing>> returnmap = new HashMap<String, LinkedList<Thing>>();

	for (Map.Entry<String, Thing> entry : gid_map.entrySet()) {
	    IfcRoot t1 = (IfcRoot) entry.getValue();

	    List<ValuePair> ifcs = t1.i.ifcObjectList();

	    for (int n = 0; n < ifcs.size(); n++) {
		ValuePair vp = (ValuePair) ifcs.get(n);
		Thing t2 = (Thing) vp.getValue();

		boolean is_one2one = false;

		EntityVO evo = entities.get(t1.getClass().getSimpleName().toUpperCase());
		if (evo != null) {
		    List<AttributeVO> aolist = evo.getDerived_attribute_list();
		    for (int i = 0; i < aolist.size(); i++) {
			AttributeVO ao = aolist.get(i);
			if (ao.getName().equalsIgnoreCase(vp.getName()))
			    if (ao.isOne2One()) {
				is_one2one = true;
			    }
		    }
		}

		if (is_one2one) 
		{

		    // DIRECT
		    LinkedList<Thing> s1 = returnmap.get(t1.getGlobalId());
		    if (s1 == null) {
			s1 = new LinkedList<Thing>();
			returnmap.put(t1.getGlobalId(), s1);
		    }

		    if (!s1.contains(t2))
			s1.add(t2);

		    // BACKWARDS
		    LinkedList<Thing> s2 = returnmap.get(t2);
		    if (s2 == null) {
			s2 = new LinkedList<Thing>();
			if(IfcRoot.class.isInstance(t2))
			   returnmap.put(((IfcRoot)t2).getGlobalId(), s2);
		    }
		    if (!s2.contains(t1))
			s2.add(t1);

		}
	    }
	}
	return returnmap;
    }

    // ==============================================================================================================================
    // ==============================================================================================================================

    public void listRDF(String outputFileName) {

	BufferedWriter out = null;
	try {
	    out = new BufferedWriter(new FileWriter(outputFileName));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	try {
	    out.write("@prefix drum: <" + DRUM_PREFIX + "/>.");
	    out.write("\n");
	    out.write("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
	    out.write("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
	    out.write("@prefix owl: <http://www.w3.org/2002/07/owl#> .\n");
	    out.write("@prefix ifc: <http://drum.cs.hut.fi/ontology/ifc2x3tc1#> .\n");
	    out.write("@prefix xsd: <http://www.w3.org/TR/xmlschema-2#> .\n");
	    out.write("\n");

	    for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
		Thing gobject = entry.getValue();
		generateTriples(gobject, out);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		out.close();

	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	}

    }
    
    
    
    public void listRDF(String outputFileName,String nsabr ,String ns) {

   	BufferedWriter out = null;
   	try {
   	    out = new BufferedWriter(new FileWriter(outputFileName));
   	} catch (IOException e) {
   	    e.printStackTrace();
   	}
   	try {
   	    out.write("@prefix drum: <" + DRUM_PREFIX + "/>.");
   	    out.write("\n");
   	    out.write("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
   	    out.write("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
   	    out.write("@prefix owl: <http://www.w3.org/2002/07/owl#> .\n");
   	    out.write("@prefix ifc: <http://drum.cs.hut.fi/ontology/ifc2x3tc1#> .\n");
   	    out.write("@prefix xsd: <http://www.w3.org/TR/xmlschema-2#> .\n");
   	    out.write("@prefix "+nsabr+": <"+ns+"> .\n");
   	    out.write("\n");

   	    for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
   		Thing gobject = entry.getValue();
   		generateTriples(gobject, out,nsabr);
   	    }

   	} catch (Exception e) {
   	    e.printStackTrace();
   	} finally {
   	    try {
   		out.close();

   	    } catch (Exception e2) {
   		e2.printStackTrace();
   	    }
   	}

       }
       
    
    public void listRDF(BufferedWriter out) {

    	try {
    	    out.write("@prefix drum: <" + DRUM_PREFIX + "/>.");
    	    out.write("\n");
    	    out.write("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
    	    out.write("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
    	    out.write("@prefix owl: <http://www.w3.org/2002/07/owl#> .\n");
    	    out.write("@prefix ifc: <http://drum.cs.hut.fi/ontology/ifc2x3tc1#> .\n");
    	    out.write("@prefix xsd: <http://www.w3.org/TR/xmlschema-2#> .\n");
    	    out.write("\n");

    	    for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
    		Thing gobject = entry.getValue();
    		generateTriples(gobject, out);
    	    }

    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally {
    	    try {
    		out.close();

    	    } catch (Exception e2) {
    		e2.printStackTrace();
    	    }
    	}

        }


    public void listRDF(BufferedWriter out,String nsabr ,String ns) {

    	try {
    	    out.write("@prefix drum: <" + DRUM_PREFIX + "/>.");
    	    out.write("\n");
    	    out.write("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
    	    out.write("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
    	    out.write("@prefix owl: <http://www.w3.org/2002/07/owl#> .\n");
    	    out.write("@prefix ifc: <http://drum.cs.hut.fi/ontology/ifc2x3tc1#> .\n");
    	    out.write("@prefix xsd: <http://www.w3.org/TR/xmlschema-2#> .\n");
   	    out.write("@prefix "+nsabr+": <"+ns+"> .\n");
    	    out.write("\n");

    	    for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
    		Thing gobject = entry.getValue();
    		generateTriples(gobject, out,nsabr);
    	    }

    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally {
    	    try {
    		out.close();

    	    } catch (Exception e2) {
    		e2.printStackTrace();
    	    }
    	}

        }

    
    private String deduceSubject(Thing pointer) {
	String subject;
	if (IfcRoot.class.isInstance(pointer)) {
	    byte bytecode[] = IFC_Base64.decode(((IfcRoot) pointer).getGlobalId());
	    String hex = new String(Hex.encodeHex(bytecode));
	    subject = "drum:GUID" + hex;
	} else {
	    subject = "_:" + this.ifc_model_name + "_iref_" + pointer.i.drum_getLine_number();
	}
	return subject;

    }
    
    private String deduceSubject(Thing pointer,String nsabr) {
  	String subject;
  	if (IfcRoot.class.isInstance(pointer)) {
  	    byte bytecode[] = IFC_Base64.decode(((IfcRoot) pointer).getGlobalId());
  	    String hex = new String(Hex.encodeHex(bytecode));
  	    subject = nsabr+":GUID" + hex;
  	} else {
  	    subject = "_:" + this.ifc_model_name + "_iref_" + pointer.i.drum_getLine_number();
  	}
  	return subject;

      }

    private void generateTriples(Thing pointer, BufferedWriter out) throws IOException {

	String subject = deduceSubject(pointer);
	if (IfcRoot.class.isInstance(pointer)) {
	    // No line reference allowed:
	    //out.write(subject + " owl:sameAs drum:" + this.ifc_model_name + "_iref_" + pointer.i.drum_getLine_number() + ".\n");
	    out.write(subject + " owl:sameAs " + "_:" + this.ifc_model_name + "_iref_" + pointer.i.drum_getLine_number() + ".\n");
	}
	out.write(subject + " a ifc:" + pointer.getClass().getSimpleName() + ".\n");

	List<ValuePair> l = pointer.i.drum_getParameterAttributeValues();  
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getValue() == null)
		continue; // null values allowed
	    if (IfcSet.class.isInstance(vp.getValue())) {
		List li = (List) vp.getValue();
		if (li.size() == 0)
		    continue; // empty list
		for (int j = 0; j < li.size(); j++) {
			out.write(subject + " ifc:" + ExpressReader.formatProperty(vp.getName()) + "  ");
		    Object o1 = li.get(j);
		    if (Thing.class.isInstance(o1)) {
			out.write(deduceSubject((Thing) o1) + ".\n");
			continue;
		    }
		    out.write(" \"" + o1.toString() + "\"");
		    if (o1.getClass().equals(java.lang.Long.class))
			out.write("^^xsd:integer");
		    else
		    if (o1.getClass().equals(java.lang.Double.class))
			out.write("^^xsd:double");
		    else
		    if (o1.getClass().equals(java.util.Date.class))
			out.write("^^xsd:datetime");
		    else
			out.write("^^xsd:string");
		    out.write(".\n");
		}
		continue; // Case handled
	    }
	    if (List.class.isInstance(vp.getValue())) {
		List li = (List) vp.getValue();
		if (li.size() == 0)
		    continue; // empty list
		out.write(subject + " ifc:" + ExpressReader.formatProperty(vp.getName()) + "  (\n");
		for (int j = 0; j < li.size(); j++) {
		    Object o1 = li.get(j);
		    if (Thing.class.isInstance(o1)) {
			out.write(deduceSubject((Thing) o1) + "\n");
			continue;
		    }
		    out.write(" \"" + o1.toString() + "\"");
		    if (o1.getClass().equals(java.lang.Long.class))
			out.write("^^xsd:integer");
		    else
		    if (o1.getClass().equals(java.lang.Double.class))
			out.write("^^xsd:double");
		    else
		    if (o1.getClass().equals(java.util.Date.class))
			out.write("^^xsd:datetime");
		    else
			out.write("^^xsd:string");
		    out.write("\n");
		}
		out.write(").\n");
		continue; // Case handled
	    }
	    
	    
	    out.write(subject + " ifc:" + ExpressReader.formatProperty(vp.getName()));
	    if (Thing.class.isInstance(vp.getValue())) {
		out.write(" " + deduceSubject((Thing) vp.getValue()) + ".\n");  // Modified 13rd May 2013
		continue;
	    }
	    out.write("  \"" + vp.getValue() + "\"");
	    if (vp.getValue().getClass().equals(java.lang.Long.class))
		out.write("^^xsd:integer");
	    if (vp.getValue().getClass().equals(java.lang.Double.class))
		out.write("^^xsd:decimal");
	    if (vp.getValue().getClass().equals(java.util.Date.class))
		out.write("^^xsd:datetime");
	    if (vp.getValue().getClass().equals(java.lang.String.class))
		out.write("^^xsd:string");

	    out.write(".\n");
	}
    }

    
    private void generateTriples(Thing pointer, BufferedWriter out,String nsabr) throws IOException {

	String subject = deduceSubject(pointer,nsabr);
	if (IfcRoot.class.isInstance(pointer)) {
	    // No line reference allowed:
	    //out.write(subject + " owl:sameAs drum:" + this.ifc_model_name + "_iref_" + pointer.i.drum_getLine_number() + ".\n");
	    out.write(subject + " owl:sameAs " + "_:" + this.ifc_model_name + "_iref_" + pointer.i.drum_getLine_number() + ".\n");
	}
	out.write(subject + " a ifc:" + pointer.getClass().getSimpleName() + ".\n");

	List<ValuePair> l = pointer.i.drum_getParameterAttributeValues();   
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getValue() == null)
		continue; // null values allowed
	    if (IfcSet.class.isInstance(vp.getValue())) {
		List li = (List) vp.getValue();
		if (li.size() == 0)
		    continue; // empty list
		for (int j = 0; j < li.size(); j++) {
			out.write(subject + " ifc:" + ExpressReader.formatProperty(vp.getName()) + " ");
		    Object o1 = li.get(j);
		    if (Thing.class.isInstance(o1)) {
			out.write(deduceSubject((Thing) o1,nsabr) + ".\n");
			continue;
		    }
		    out.write(" \"" + o1.toString() + "\"");
		    if (o1.getClass().equals(java.lang.Long.class))
			out.write("^^xsd:integer");
		    if (o1.getClass().equals(java.lang.Double.class))
			out.write("^^xsd:double");
		    if (o1.getClass().equals(java.util.Date.class))
			out.write("^^xsd:datetime");
		    if (o1.getClass().equals(java.lang.String.class))
			out.write("^^xsd:string");
			out.write(".\n");
		}
		continue; // Case handled
	    }
	    if (List.class.isInstance(vp.getValue())) {
		List li = (List) vp.getValue();
		if (li.size() == 0)
		    continue; // empty list
		out.write(subject + " ifc:" + ExpressReader.formatProperty(vp.getName()) + "  (\n");
		for (int j = 0; j < li.size(); j++) {
		    Object o1 = li.get(j);
		    if (Thing.class.isInstance(o1)) {
			out.write(deduceSubject((Thing) o1,nsabr) + "\n");
			continue;
		    }
		    out.write(" \"" + o1.toString() + "\"");
		    if (o1.getClass().equals(java.lang.Long.class))
			out.write("^^xsd:integer");
		    if (o1.getClass().equals(java.lang.Double.class))
			out.write("^^xsd:double");
		    if (o1.getClass().equals(java.util.Date.class))
			out.write("^^xsd:datetime");
		    if (o1.getClass().equals(java.lang.String.class))
			out.write("^^xsd:string");
		    out.write("\n");
		}
		out.write(").\n");
		continue; // Case handled
	    }

	    out.write(subject + " ifc:" + ExpressReader.formatProperty(vp.getName()));
	    if (Thing.class.isInstance(vp.getValue())) {
		out.write(" " + deduceSubject((Thing) vp.getValue(),nsabr) + ".\n");  // Modified 13rd May 2013
		continue;
	    }
	    out.write("  \"" + vp.getValue() + "\"");
	    if (vp.getValue().getClass().equals(java.lang.Long.class))
		out.write("^^xsd:integer");
	    if (vp.getValue().getClass().equals(java.lang.Double.class))
		out.write("^^xsd:decimal");
	    if (vp.getValue().getClass().equals(java.util.Date.class))
		out.write("^^xsd:datetime");
	    if (vp.getValue().getClass().equals(java.lang.String.class))
		out.write("^^xsd:string");

	    out.write(".\n");
	}
    }

    // ------------------------------------------

    /**
     * Gets the gID things.
     * 
     * @return the gID things
     */
    public Map<String, Thing> getGIDThings() {
	return gid_map;
    }

    
    /**
     * Gets the elements tree.
     * 
     * @return the elements tree
     */
    public Tree<ValuePair> getCompleteElementsTree() {
	Tree<ValuePair> ret_val = new Tree<ValuePair>(new ValuePair(".", root));
	Thing start = getLineEntry(root.i.drum_getLine_number());
	br_queue.add(new BR_ArgumentValue(start, ret_val));
	while (!br_queue.isEmpty())
	    getAllSubElementsBreathFirst(br_queue.remove());

	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	return ret_val;
    }

    private class BR_ArgumentValue {
	Thing pointer;
	Tree<ValuePair> tree;

	public BR_ArgumentValue(Thing pointer, Tree<ValuePair> tree) {
	    this.pointer = pointer;
	    this.tree = tree;
	}

    }

    private Queue<BR_ArgumentValue> br_queue = new LinkedList<BR_ArgumentValue>();

    /**
     * Gets the sub elements.
     * 
     * @param pointer
     *            the current element at the graph
     * @param tree
     *            the Tree object to be returned
     */
    private void getAllSubElementsBreathFirst(BR_ArgumentValue arguments) {

	arguments.pointer.i.setTouched(true);
	touched_set.add(arguments.pointer);

	List<ValuePair> ifcs = arguments.pointer.i.drum_getIfcClassAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    if (!t.i.isTouched()) {
		Tree<ValuePair> l = arguments.tree.addLeaf(vp);
		br_queue.add(new BR_ArgumentValue(t, l));
	    }
	}
    }

    
    // ----------------------------------------------------------------------------------------------
    public LinkedList<Thing> getCoreElements() {
	LinkedList<Thing> ret_val = new LinkedList<Thing>();
	Thing start = getLineEntry(root.i.drum_getLine_number());
	getSubElements(start, 0, false, ret_val);

	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	return ret_val;
    }

    private void getSubElements(Thing pointer, int level, boolean isElement, List<Thing> core_elements) {

	if (isElement) {
	    if (pointer.i.isTouched())
		return;
	    pointer.i.setTouched(true);
	    touched_set.add(pointer);
	}

	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    if (vp.getName().equals(IFC_CLassModelConstants.IS_DECOMPOSED_BY)) {
		core_elements.add(t);
		getSubElements(t, level + 1, false, core_elements);
	    }
	    if (vp.getName().equals(IFC_CLassModelConstants.RELATED_OBJECTS)) {
		core_elements.add(t);
		getSubElements(t, level + 1, false, core_elements);
	    }
	    if (vp.getName().equals(IFC_CLassModelConstants.RELATED_ELEMENTS)) {
		core_elements.add(t);
		getSubElements(t, level + 1, true, core_elements);
	    }
	    if (vp.getName().equals(IFC_CLassModelConstants.CONTAINS_ELEMENTS)) {
		core_elements.add(t);
		getSubElements(t, level + 1, false, core_elements);
	    }
	}
    }

    //-----------------------------------------------------------------------------------------------
    
    
    /**
     * Gets the elements tree.
     * 
     * @return the elements tree
     */
    public Tree<Thing> getElementsTree() {
	Tree<Thing> ret_val = new Tree<Thing>(root);
	Thing start = getLineEntry(root.i.drum_getLine_number());
	getSubElements(start, 0, false, ret_val);

	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	return ret_val;
    }

    /**
     * Gets the sub elements.
     * 
     * @param pointer
     *            the current element at the graph
     * @param level
     *            the iteration count in the recursive run
     * @param isElement
     *            the current element is a building element accessible from
     *            IFCPROJECT
     * @param tree
     *            the Tree object to be returned
     */
    private void getSubElements(Thing pointer, int level, boolean isElement, Tree<Thing> tree) {

	if (isElement) {
	    if (pointer.i.isTouched())
		return;
	    pointer.i.setTouched(true);
	    touched_set.add(pointer);
	}

	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    if (vp.getName().equals(IFC_CLassModelConstants.IS_DECOMPOSED_BY)) {
		Tree<Thing> l = tree.addLeaf(t);
		getSubElements(t, level + 1, false, l);
	    }
	    if (vp.getName().equals(IFC_CLassModelConstants.RELATED_OBJECTS)) {
		Tree<Thing> l = tree.addLeaf(t);
		getSubElements(t, level + 1, false, l);
	    }
	    if (vp.getName().equals(IFC_CLassModelConstants.RELATED_ELEMENTS)) {
		Tree<Thing> l = tree.addLeaf(t);
		getSubElements(t, level + 1, true, l);
	    }
	    if (vp.getName().equals(IFC_CLassModelConstants.CONTAINS_ELEMENTS)) {
		Tree<Thing> l = tree.addLeaf(t);
		getSubElements(t, level + 1, false, l);
	    }
	}
    }

    /**
     * List elements.
     * 
     * @param start_inx
     *            the IFC file line number to start iteration
     */
    public void listElements() {
	Thing start = root;
	checkSubElements(start, 0, false);

	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
    }

    /**
     * Check sub elements.
     * 
     * @param pointer
     *            the current element at the graph
     * @param level
     *            the iteration count in the recursive run
     * @param isElement
     *            the current element is a building element accessible from
     *            IFCPROJECT
     */
    private void checkSubElements(Thing pointer, int level, boolean isElement) {

	if (isElement) {
	    if (pointer.i.isTouched())
		return;
	    pointer.i.setTouched(true);
	    touched_set.add(pointer);
	}

	List<ValuePair> l = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getName().equals(IFC_CLassModelConstants.NAME)) {
		for (int i = 0; i < level; i++)
		    System.out.print("  ");
		System.out.println(pointer.getClass().getSimpleName() + ":" + vp.getName() + " = " + vp.getValue());
	    }
	}
	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();

	    if (vp.getName().equals(IFC_CLassModelConstants.IS_DECOMPOSED_BY))
		checkSubElements(t, level + 1, false);
	    if (vp.getName().equals(IFC_CLassModelConstants.RELATED_OBJECTS))
		checkSubElements(t, level + 1, false);
	    if (vp.getName().equals(IFC_CLassModelConstants.RELATED_ELEMENTS))
		checkSubElements(t, level + 1, true);
	    if (vp.getName().equals(IFC_CLassModelConstants.CONTAINS_ELEMENTS))
		checkSubElements(t, level + 1, false);
	}
    }

    // ========================================================================================================
    /**
     * Common gid set_ calculate cr c32 for guid areas.
     * 
     * @param common_gids
     *            the list of GlobalID's which are common to the two models
     *            which are to be compared
     * @return the map
     */
    Set<Thing> lapikayty=new HashSet<Thing>();
    public Map<Long, String> commonGIDSet_CalculateCRC32ForGuidAreas(List<String> common_gids) {
	Map<Long, String> return_set = new HashMap<Long, String>(); //

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing gobject = entry.getValue();
	    if (IfcRoot.class.isInstance(gobject)) {
		for (int n = 0; n < touched_set.size(); n++) {
		    Thing t = (Thing) touched_set.get(n);
		    t.i.setTouched(false);
		    lapikayty.add(t);
		}
		touched_set.clear();

		long crc32val=commonGIDSet_traverseAndCalcucalateCRCFromGUID(gobject, 0, common_gids);
		return_set.put(crc32val, ((IfcRoot) gobject).getGlobalId());
	    }
	}

	// Clean for other calls
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	    lapikayty.add(t);
	}
	touched_set.clear();
	//System.out.println("crc touched:"+lapikayty.size()+"/"+object_buffer.size()+ " commons:"+common_gids.size());
	/*Set<String> luokkasetti=new HashSet<String>();
	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing gobject = entry.getValue();
	    if(!lapikayty.contains(gobject))
		luokkasetti.add(gobject.getClass().getSimpleName());
	}
	Iterator itr = luokkasetti.iterator(); 
	while(itr.hasNext()) {

	    Object element = itr.next(); 
	    System.out.println("- "+ element );

	} */
	return return_set;
    }

    // MAP order differ

    /**
     * Common gid set_calculate cr c32 for guid areas2.
     * 
     * @param common_gids
     *            the list of GlobalID's which are common to the two models
     *            which are to be compared
     * @return the map
     */
    public Map<String, Long> commonGIDSet_calculateCRC32ForGuidAreas2(List<String> common_gids) {
	Map<String, Long> return_set = new HashMap<String, Long>(); //

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing gobject = entry.getValue();
	    if (IfcRoot.class.isInstance(gobject)) {
		for (int n = 0; n < touched_set.size(); n++) {
		    Thing t = (Thing) touched_set.get(n);
		    t.i.setTouched(false);
		}
		touched_set.clear();
		long retval=commonGIDSet_traverseAndCalcucalateCRCFromGUID(gobject, 0, common_gids);
		return_set.put(((IfcRoot) gobject).getGlobalId(), retval);
	    }
	}
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();
	return return_set;
    }

    /**
     * Common gid set_traverse and calcucalate crc from guid.
     * 
     * @param pointer
     *            the current element at the graph
     * @param level
     *            the iteration count in the recursive run
     * @param common_gids
     *            the list of GlobalID's which are common to the two models
     *            which are to be compared
     */
    private long commonGIDSet_traverseAndCalcucalateCRCFromGUID(Thing pointer, int level, List<String> common_gids) {

	long retval;
	CRC32 crc32 = new CRC32();
	crc32.update(pointer.getClass().getSimpleName().getBytes());
	if ((level > 0) && (IfcRoot.class.isInstance(pointer))) {
	    String sgid = ((IfcRoot) pointer).getGlobalId();
	    if (common_gids.contains(sgid)) {
		crc32.update(sgid.getBytes());
		return crc32.getValue();
	    }
	}

	if (pointer.i.isTouched())
	    return crc32.getValue();
	pointer.i.setTouched(true);
	touched_set.add(pointer);

	List<ValuePair> l = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
		continue;
	    if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
		continue;
	    crc32.update(vp.getName().getBytes());
	    crc32.update(vp.getValue().toString().getBytes());
	}
	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes_2_CHKSUM();
	retval=crc32.getValue();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    if (t.getClass().equals(IfcOwnerHistory.class))
		continue;
	    crc32.update(vp.getName().getBytes());
	    retval=retval^commonGIDSet_traverseAndCalcucalateCRCFromGUID(t, level + 1, common_gids);	    
	}
	return retval;
    }

    /** The touched_set. */
    List<Thing> touched_set = new LinkedList<Thing>();

    /** The crc32. */
    CRC32 crc32 = new CRC32();

    /**
     * Calculate cr c32 for guid areas.
     * 
     * @return the map
     */
    public Map<Long, String> calculateCRC32ForGuidAreas() {
	Map<Long, String> return_set = new HashMap<Long, String>(); //

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing gobject = entry.getValue();
	    if (IfcRoot.class.isInstance(gobject)) {
		crc32.reset();
		traverseAndCalcucalateCRCFromGUID(gobject, 0);
		for (int n = 0; n < touched_set.size(); n++) {
		    Thing t = (Thing) touched_set.get(n);
		    t.i.setTouched(false);
		}
		touched_set.clear();
		return_set.put(crc32.getValue(), ((IfcRoot) gobject).getGlobalId());
	    }
	}
	return return_set;
    }

    // MAP order differ
    /**
     * Calculate cr c32 for guid areas2.
     * 
     * @return the map
     */
    public Map<String, Long> calculateCRC32ForGuidAreas2() {
	Map<String, Long> return_set = new HashMap<String, Long>(); //

	for (Map.Entry<Long, Thing> entry : object_buffer.entrySet()) {
	    Thing gobject = entry.getValue();
	    if (IfcRoot.class.isInstance(gobject)) {
		for (int n = 0; n < touched_set.size(); n++) {
		    Thing t = (Thing) touched_set.get(n);
		    t.i.setTouched(false);
		}
		touched_set.clear();
		crc32.reset();
		traverseAndCalcucalateCRCFromGUID(gobject, 0);
		return_set.put(((IfcRoot) gobject).getGlobalId(), crc32.getValue());
	    }
	}
	return return_set;
    }

    /**
     * Traverse and calcucalate crc from guid.
     * 
     * @param pointer
     *            the current element at the graph
     * @param level
     *            the iteration count in the recursive run
     */
    private void traverseAndCalcucalateCRCFromGUID(Thing pointer, int level) {

	crc32.update(pointer.getClass().getSimpleName().getBytes());
	if ((level > 0) && (IfcRoot.class.isInstance(pointer))) {
	    String sgid = ((IfcRoot) pointer).getGlobalId();
	    crc32.update(sgid.getBytes());
	    return;
	}

	if (pointer.i.isTouched())
	    return;
	pointer.i.setTouched(true);
	touched_set.add(pointer);

	List<ValuePair> l = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getName().equals(IFC_CLassModelConstants.GLOBAL_ID))
		continue;
	    if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
		continue;
	    crc32.update(vp.getName().getBytes());
	    crc32.update(vp.getValue().toString().getBytes());
	}
	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes_2_CHKSUM();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    if (t.getClass().equals(IfcOwnerHistory.class))
		continue;
	    traverseAndCalcucalateCRCFromGUID(t, level + 1);
	}
    }

    // ========================================================================================================

    /**
     * List guid_ area.
     * 
     * @param line_number
     *            the line_number
     */
    public void listGID_Area(Long line_number) {
	Thing gobject = getLineEntry(line_number);
	traverseFromGUID(gobject, 0);
	for (int n = 0; n < touched_set.size(); n++) {
	    Thing t = (Thing) touched_set.get(n);
	    t.i.setTouched(false);
	}
	touched_set.clear();

    }

    /**
     * Traverse from guid.
     * 
     * @param pointer
     *            the current element at the graph
     * @param level
     *            the iteration count in the recursive run
     */
    private void traverseFromGUID(Thing pointer, int level) {

	if ((level > 0) && (IfcRoot.class.isInstance(pointer))) {
	    for (int i = 0; i < level; i++)
		System.out.print("  ");
	    System.out.println(pointer.getClass().getSimpleName() + ":" + ((IfcRoot) pointer).getGlobalId());
	    return;
	}
	if (pointer.i.isTouched())
	    return;
	pointer.i.setTouched(true);
	touched_set.add(pointer);

	List<ValuePair> l = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    if (vp.getName().equals(IFC_CLassModelConstants.LINE_NUMBER))
		continue;
	    for (int i = 0; i < level; i++)
		System.out.print("  ");
	    System.out.println(pointer.getClass().getSimpleName() + ":" + vp.getName() + " = " + vp.getValue());
	}
	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    if (t.getClass().equals(IfcOwnerHistory.class))
		continue;
	    traverseFromGUID(t, level + 1);
	}
    }

    // ========================================================================================================

    /**
     * Neat print out.
     * 
     * @param line_number
     *            the line_number
     */
    public void neatPrintOut(Long line_number) {
	Thing start = getLineEntry(line_number);
	neatPrintOut(start, 0);
    }

    /**
     * Neat print out.
     * 
     * @param pointer
     *            the current element at the graph
     * @param level
     *            the iteration count in the recursive run
     */
    private void neatPrintOut(Thing pointer, int level) {
	if (level > 5)
	    return;

	List<ValuePair> l = pointer.i.drum_getParameterAttributes();
	for (int n = 0; n < l.size(); n++) {
	    ValuePair vp = (ValuePair) l.get(n);
	    for (int i = 0; i < level; i++)
		System.out.print("  ");
	    System.out.println(vp.getName() + " = " + vp.getValue());
	}
	List<ValuePair> ifcs = pointer.i.drum_getIfcClassAttributes();
	for (int n = 0; n < ifcs.size(); n++) {
	    ValuePair vp = (ValuePair) ifcs.get(n);
	    Thing t = (Thing) vp.getValue();
	    neatPrintOut(t, level + 1);
	}
    }

    // ========================================================================================================

    /**
     * Gets the line entry.
     * 
     * @param line_number
     *            the line_number
     * @return the line entry
     */
    public Thing getLineEntry(Long line_number) {
	return getLineEntry(line_number, null);
    }

    /**
     * Gets the line entry.
     * 
     * @param line_number
     *            the line_number
     * @param class_name
     *            the class_name
     * @return the line entry
     */

    private Thing getLineEntry(Long line_number, String class_name) {
	Thing thing = object_buffer.get(line_number);
	if (thing == null) {
	    if (class_name == null)
		return null;
	    @SuppressWarnings("rawtypes")
	    Class cls = null;
	    try {
		cls = Class.forName("fi.ni.ifc2x3." + class_name);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Constructor ct = cls.getConstructor();
		thing = (Thing) ct.newInstance();
		thing.i.drum_setLine_number(line_number);
		object_buffer.put(line_number, (Thing) thing);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return thing;
    }

    /**
     * Format set method.
     * 
     * @param s
     *            the s
     * @return the string
     */
    static public String formatSetMethod(String s) {
	if (s == null)
	    return null;
	StringBuffer sb = new StringBuffer();
	sb.append("set");
	sb.append(Character.toUpperCase(s.charAt(0)));
	sb.append(s.substring(1));
	return sb.toString();
    }

    /**
     * Sets the value2 thing.
     * 
     * @param t
     *            the t
     * @param param_name
     *            the param_name
     * @param value
     *            the value
     * 
     *            This uses most of the CPU time
     */
    @SuppressWarnings("unchecked")
    private void setValue2Thing(Thing t, String param_name, Object value) {
	if (value.equals("*")) {
	    return; // No value
	}
	String set_method_name = formatSetMethod(param_name);
	Method method[] = t.getClass().getMethods();
	for (int j = 0; j < method.length; j++) {
	    try {
		if (method[j].getName().equals(set_method_name)) {
		    method[j].invoke(t, value);
		    return; // Only one invocation
		}

	    } catch (Exception e) {
		//System.err.println(t.getClass().getName()+" "+param_name+" "+value);
		//e.printStackTrace();
		try {

		    @SuppressWarnings("rawtypes")
		    Class cls = Class.forName("fi.ni.ifc2x3." + method[j].getParameterTypes()[0].getSimpleName() + "_StringValue");
		    @SuppressWarnings("rawtypes")
		    Constructor ct = cls.getConstructor();
		    Object o = ct.newInstance();
		    cls.getMethod(IFC_CLassModelConstants.SET_VALUE, String.class).invoke(o, value);
		    if (method[j].getName().equals(formatSetMethod(param_name))) {
			if (value.equals("*")) {
			    method[j].invoke(t, o);//; // No value
			} else
			    method[j].invoke(t, o);
		    }

		} catch (Exception e1) {
		    e1.printStackTrace();
		    System.err.println("ERR param:" + param_name + " object:" + value.toString() + " class:" + t.getClass().getSimpleName() + " value class:" + value.getClass().getSimpleName());
		}
	    }

	}

    }

    /**
     * Adds the literal value.
     * 
     * @param subject_line_number
     *            the subject_line_number
     * @param s
     *            the s
     * @param p
     *            the p
     * @param o
     *            the o
     */
    private void addLiteralValue(Long subject_line_number, String s, String p, String o) {

	String p_uri;

	Thing subject_line_entry = getLineEntry(subject_line_number);
	p_uri = ExpressReader.formatProperty(p);

	try {
	    setValue2Thing(subject_line_entry, filter_illegal_chars(p_uri), filter_illegal_chars(filter_extras(o)));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Adds the literal value.
     * 
     * @param subject_line_number
     *            the subject_line_number
     * @param object_line_number
     *            the object_line_number
     * @param s
     *            the s
     * @param p
     *            the p
     */
    private void addLiteralValue(Long subject_line_number, Long object_line_number, String s, String p) {

	String p_uri;

	Thing subject_line_entry = getLineEntry(subject_line_number);
	p_uri = ExpressReader.formatProperty(p);
	Thing object_line_entry = getLineEntry(object_line_number);

	try {
	    setValue2Thing(subject_line_entry, filter_illegal_chars(p_uri), object_line_entry);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Adds the ifc attribute.
     * 
     * @param vo
     *            the value object representing the IFC file line
     * @param attribute
     *            the attribute
     * @param ref_value
     *            the ref_value
     */
    private void addIFCAttribute(IFC_X3_VO vo, AttributeVO attribute, IFC_X3_VO ref_value) {
	if (attribute.isReverse_pointer()) {

	    LinkedList<IFC_X3_VO> lista = ref_value.inverse_pointer_sets.get(attribute.getPoints_from().getName());
	    if (lista == null) {
		lista = new LinkedList<IFC_X3_VO>();
		ref_value.inverse_pointer_sets.put(attribute.getPoints_from().getName(), lista);
	    }
	    lista.add(vo);
	}
    }

    /**
     * Fill java class instance values.
     * 
     * @param name
     *            the name
     * @param vo
     *            the value object representing the IFC file line
     * @param level_up_vo
     *            the IFC line pointing to this line
     * @param level
     *            the iteration count in the recursive run
     */
    private void fillJavaClassInstanceValues(String name, IFC_X3_VO vo, IFC_X3_VO level_up_vo, int level) {


	EntityVO evo = entities.get(ExpressReader.formatClassName(vo.name));
	if (evo == null)
	    System.err.println("Does not exist: " + vo.name);
	String subject = null;

	if (vo.getGid() != null) {
	    subject = "gref_" + filter_extras(vo.getGid());
	} else {
	    subject = "iref_" + ifc_filename + "_i" + vo.line_num;
	}

	// Somebody has pointed here from above:
	if (vo != level_up_vo) {
	    String level_up_subject;
	    if (level_up_vo.getGid() != null) {
		level_up_subject = "gref_" + filter_extras(level_up_vo.getGid());
	    } else {
		level_up_subject = "iref_" + ifc_filename + "_i" + level_up_vo.line_num;
	    }

	    addLiteralValue(level_up_vo.getLine_num(), vo.getLine_num(), level_up_subject, name);
	}
	if (vo.is_touched())
	    return;

	int attribute_pointer = 0;
	for (int i = 0; i < vo.list.size(); i++) {
	    Object o = vo.list.get(i);
	    if (String.class.isInstance(o)) {
		if (!((String) o).equals("$")) { // Do not print out empty
		    // values'

		    if (types.get(ExpressReader.formatClassName((String) o)) == null) {

			if ((evo != null) && (evo.getDerived_attribute_list() != null) && (evo.getDerived_attribute_list().size() > attribute_pointer)) {
			    addLiteralValue(vo.getLine_num(), subject, evo.getDerived_attribute_list().get(attribute_pointer).getName(), "\'" + filter_extras((String) o) + "'");
			}

			attribute_pointer++;
		    }
		} else
		    attribute_pointer++;
	    } else if (IFC_X3_VO.class.isInstance(o)) {
		if ((evo != null) && (evo.getDerived_attribute_list() != null) && (evo.getDerived_attribute_list().size() > attribute_pointer)) {
		    fillJavaClassInstanceValues(evo.getDerived_attribute_list().get(attribute_pointer).getName(), (IFC_X3_VO) o, vo, level + 1);
		    addIFCAttribute(vo, evo.getDerived_attribute_list().get(attribute_pointer), (IFC_X3_VO) o);
		} else {
		    fillJavaClassInstanceValues("-", (IFC_X3_VO) o, vo, level + 1);
		    System.out.println("1!" + evo);

		}
		attribute_pointer++;
	    } else if (LinkedList.class.isInstance(o)) {
		@SuppressWarnings("unchecked")
		LinkedList<Object> tmp_list = (LinkedList<Object>) o;
		StringBuffer local_txt = new StringBuffer();
		for (int j = 0; j < tmp_list.size(); j++) {
		    Object o1 = tmp_list.get(j);
		    if (String.class.isInstance(o1)) {
			if (j > 0)
			    local_txt.append(", ");
			local_txt.append(filter_extras((String) o1));
		    }
		    if (IFC_X3_VO.class.isInstance(o1)) {
			if ((evo != null) && (evo.getDerived_attribute_list() != null) && (evo.getDerived_attribute_list().size() > attribute_pointer)) {
			    fillJavaClassInstanceValues(evo.getDerived_attribute_list().get(attribute_pointer).getName(), (IFC_X3_VO) o1, vo, level + 1);
			    addIFCAttribute(vo, evo.getDerived_attribute_list().get(attribute_pointer), (IFC_X3_VO) o1);

			} else {
			    fillJavaClassInstanceValues("-", (IFC_X3_VO) o1, vo, level + 1);
			    System.out.println("2!" + evo);
			}

		    }
		}

		if (local_txt.length() > 0) {
		    if ((evo != null) && (evo.getDerived_attribute_list() != null) && (evo.getDerived_attribute_list().size() > attribute_pointer)) {
			addLiteralValue(vo.getLine_num(), subject, evo.getDerived_attribute_list().get(attribute_pointer).getName(), "'" + local_txt.toString() + "\'");
		    }

		}
		attribute_pointer++;
	    }

	}

    }

    /**
     * Creates the object tree.
     */
    private void createObjectTree() {
	for (Map.Entry<Long, IFC_X3_VO> entry : linemap.entrySet()) {
	    IFC_X3_VO vo = entry.getValue();
	    fillJavaClassInstanceValues("root", vo, vo, 0);
	}

	try {
	    for (Map.Entry<Long, IFC_X3_VO> entry : linemap.entrySet()) {
		IFC_X3_VO vo = entry.getValue();
		if (vo.inverse_pointer_sets.size() > 0) {
		    for (Map.Entry<String, LinkedList<IFC_X3_VO>> inverse_set : vo.inverse_pointer_sets.entrySet()) {
			LinkedList<IFC_X3_VO> li = inverse_set.getValue();
			String subject = filter_illegal_chars("drum:" + ifc_filename + "_i" + vo.getLine_num());
			if (vo.getGid() != null) {
			    byte bytecode[] = IFC_Base64.decode(filter_extras(vo.getGid()));
			    String hex = new String(Hex.encodeHex(bytecode));
			    subject = "drum:GID" + hex;
			}
			for (int i = 0; i < li.size(); i++) {
			    IFC_X3_VO ivo = li.get(i);
			    addLiteralValue(vo.getLine_num(), ivo.getLine_num(), subject, inverse_set.getKey());

			}

		    } // for map inverse_set

		} // if
	    } // for map linemap
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    // ===========================================================================================================================

    /*
     * Traverse through the graph to check the max levels of the entries. The
     * level refers the longest path to the node.
     * 
     * Sets the Global ID to the nodes.
     */

    /**
     * Estimate_level.
     * 
     * @param vo
     *            the value object representing the IFC file line
     * @param level
     *            the iteration count in the recursive run
     */
    private void estimate_level(IFC_X3_VO vo, int level) {
	if (level > vo.getMaxlevel())
	    vo.setMaxlevel(level);
	EntityVO evo = entities.get(vo.name);
	int pointer = 0;

	for (int i = 0; i < vo.list.size(); i++) {
	    Object o = vo.list.get(i);

	    if (String.class.isInstance(o)) {
		if (!((String) o).equals("$")) { // Do not handle empty values

		    if ((evo != null) && (evo.getDerived_attribute_list() != null) && (evo.getDerived_attribute_list().size() > pointer)) {
			if (evo.getDerived_attribute_list().get(pointer).getName().equals(IFC_CLassModelConstants.GLOBAL_ID)) {
			    vo.setGid(filter_extras((String) o));
			}
		    }
		}
		pointer++;
	    } else if (IFC_X3_VO.class.isInstance(o)) {
		estimate_level((IFC_X3_VO) o, level + 1);
	    } else if (LinkedList.class.isInstance(o)) {
		@SuppressWarnings("unchecked")
		LinkedList<Object> tmp_list = (LinkedList<Object>) o;
		for (int j = 0; j < tmp_list.size(); j++) {
		    Object o1 = tmp_list.get(j);
		    if (IFC_X3_VO.class.isInstance(o1)) {
		      estimate_level((IFC_X3_VO) o1, level + 1);
		    } else if (LinkedList.class.isInstance(o1)) {
			@SuppressWarnings("unchecked")
			LinkedList<Object> tmp2_list = (LinkedList<Object>) o1;
			for (int j2 = 0; j2 < tmp2_list.size(); j2++) {
			    Object o2 = tmp2_list.get(j2);
			    if (IFC_X3_VO.class.isInstance(o2)) {
				estimate_level((IFC_X3_VO) o2, level + 1);
			    } else if (String.class.isInstance(o2)) {
				//if (!((String) o2).equals("?"))
				//    System.err.println("ts:" + ((String) o2));
			    } else if (Character.class.isInstance(o2))
				;
			    else
				System.err.println("t:" + o2.getClass().getSimpleName());
			}
		    }

		}
	    }
	}

    }

    /**
     * Calculate the longests paths to the node.
     */
    public void calculateTheLongestsPathsToTheNode_and_setGlobalIDs() {
	for (Map.Entry<Long, IFC_X3_VO> entry : linemap.entrySet()) {
	    IFC_X3_VO vo = entry.getValue();
	    estimate_level(vo, 0);
	}

    }

    // ===========================================================================================================================

    /*
     * Sets object mapping on base of the IFC file local # references.
     */

    /**
     * Map entries.
     */
    @SuppressWarnings("unchecked")
    private void mapEntries() {
	for (Map.Entry<Long, IFC_X3_VO> entry : linemap.entrySet()) {
	    IFC_X3_VO vo = entry.getValue();

	    // Initialize the object_buffer
	    try {
		Thing thing = object_buffer.get(vo.getLine_num());
		if (thing == null) {
		    @SuppressWarnings("rawtypes")
		    Class cls = Class.forName("fi.ni.ifc2x3." + entities.get(vo.name).getName());
		    @SuppressWarnings("rawtypes")
		    Constructor ct = cls.getConstructor();
		    thing = (Thing) ct.newInstance();
		    thing.i.drum_setLine_number(vo.getLine_num());
		    object_buffer.put(vo.getLine_num(), (Thing) thing);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    for (int i = 0; i < vo.list.size(); i++) {
		Object o = vo.list.get(i);
		if (String.class.isInstance(o)) {
		    String s = (String) o;
		    if (s.length() < 1)
			continue;
		    if (s.charAt(0) == '#') {
			Object or = linemap.get(toLong(s.substring(1)));
			vo.list.set(i, or);
		    }
		}
		if (LinkedList.class.isInstance(o)) {
		    LinkedList<Object> tmp_list = (LinkedList<Object>) o;
		    for (int j = 0; j < tmp_list.size(); j++) {
			Object o1 = tmp_list.get(j);
			if (String.class.isInstance(o1)) {
			    String s = (String) o1;
			    if (s.length() < 1)
				continue;
			    if (s.charAt(0) == '#') {
				Object or = linemap.get(toLong(s.substring(1)));
				tmp_list.set(j, or);
			    }
			} else if (LinkedList.class.isInstance(o1)) {
			    LinkedList<Object> tmp2_list = (LinkedList<Object>) o1;
			    for (int j2 = 0; j2 < tmp2_list.size(); j2++) {
				Object o2 = tmp2_list.get(j2);
				if (String.class.isInstance(o2)) {
				    String s = (String) o2;
				    if (s.length() < 1)
					continue;
				    if (s.charAt(0) == '#') {
					Object or = linemap.get(toLong(s.substring(1)));
					tmp_list.set(j, or);
				    }
				}
			    }

			}

		    }
		}
	    }
	}

    }

    // ===========================================================================================================================

    /**
     * To long.
     * 
     * @param txt
     *            the txt
     * @return the long
     */
    private Long toLong(String txt) {
	try {
	    return Long.valueOf(txt);
	} catch (Exception e) {
	    return Long.MIN_VALUE;
	}
    }

    /**
     * Filter_extras.
     * 
     * @param txt
     *            the txt
     * @return the string
     */
    private String filter_extras(String txt) {
	StringBuffer sb = new StringBuffer();
	for (int n = 0; n < txt.length(); n++) {
	    char ch = txt.charAt(n);
	    switch (ch) {
	    case '\'':
		break;
	    case '=':
		break;
	    default:
		sb.append(ch);
	    }
	}
	return sb.toString();
    }

    /**
     * Filter_illegal_chars.
     * 
     * @param txt
     *            the txt
     * @return the string
     */
    private String filter_illegal_chars(String txt) {
	StringBuffer sb = new StringBuffer();
	for (int n = 0; n < txt.length(); n++) {
	    char ch = txt.charAt(n);
	    switch (ch) {
	    case '.':
		sb.append('_');
		break;
	    case '\"':
		sb.append("\\\"");
		break;
	    case '\'':
		sb.append("\\\'");
		break;
	    case '\\':
		sb.append("\\\\");
		break;
	    default:
		sb.append(ch);
	    }
	}
	return sb.toString();
    }

    /**
     * Filter_spaces.
     * 
     * @param txt
     *            the txt
     * @return the string
     */
    private String filter_spaces(String txt) {
	StringBuffer sb = new StringBuffer();
	for (int n = 0; n < txt.length(); n++) {
	    char ch = txt.charAt(n);
	    switch (ch) {
	    case '\'':
		break;
	    case ' ':
		sb.append('_');
		break;
	    default:
		sb.append(ch);
	    }
	}
	return sb.toString();
    }

    /**
     * Parse_ if c_ line statement.
     * 
     * @param line
     *            the line
     */
    private void parse_IFC_LineStatement(String line) {
	IFC_X3_VO ifcvo = new IFC_X3_VO();
	int state = 0;
	StringBuffer sb = new StringBuffer();
	int cl_count = 0;
	LinkedList<Object> current = ifcvo.getList();
	Stack<LinkedList<Object>> list_stack = new Stack<LinkedList<Object>>();
	for (int i = 0; i < line.length(); i++) {
	    char ch = line.charAt(i);
	    switch (state) {
	    case 0:
		if (ch == '=') {
		    ifcvo.setLine_num(toLong(sb.toString()));
		    sb.setLength(0);
		    state++;
		    continue;
		} else if (Character.isDigit(ch))
		    sb.append(ch);
		break;
	    case 1:  // (
		if (ch == '(') {
		    ifcvo.setName(sb.toString());
		    sb.setLength(0);
		    state++;
		    continue;
		} else if (ch == ';') {
		    ifcvo.setName(sb.toString());
		    sb.setLength(0);
		    state = Integer.MAX_VALUE;
		} else if (!Character.isWhitespace(ch))
		    sb.append(ch);
		break;
	    case 2: // (...   line  started and doing (... 
		if (ch == '\'') {
		    state++;
		}
		if (ch == '(') {
		    list_stack.push(current);
		    LinkedList<Object> tmp = new LinkedList<Object>();
		    if (sb.toString().trim().length() > 0)
			current.add(sb.toString().trim());
		    sb.setLength(0);
		    current.add(tmp); // listaan lisätään lista
		    current = tmp;
		    cl_count++;
		    // sb.append(ch);
		} else if (ch == ')') {
		    if (cl_count == 0) {
			if (sb.toString().trim().length() > 0)
			    current.add(sb.toString().trim());
			sb.setLength(0);
			state = Integer.MAX_VALUE;  // line is done
			continue;
		    } else {
			if (sb.toString().trim().length() > 0)
			    current.add(sb.toString().trim());
			sb.setLength(0);
			cl_count--;
			current = list_stack.pop();
		    }
		} else if (ch == ',') {
		    if (sb.toString().trim().length() > 0)
			current.add(sb.toString().trim());
		    current.add(Character.valueOf(ch));

		    sb.setLength(0);
		} else {
		    sb.append(ch);

		}
		break;
	    case 3: // (...
		if (ch == '\'') {
		    state--;
		} else {
		    sb.append(ch);

		}
		break;
	    default:
		// Do nothing
	    }
	}
	linemap.put(ifcvo.line_num, ifcvo);
    }

    /**
     * Read model.
     * 
     * @param model_file
     *            the name of the IFC file to be read in
     */
    /**
     * @param model_file
     */
    private void readModel(String model_file) {
	try {
	    FileInputStream fstream = new FileInputStream(model_file);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    try {
		String strLine;
		while ((strLine = br.readLine()) != null) {
		    if (strLine.length() > 0) {
			if (strLine.charAt(0) == '#') {
			    StringBuffer sb = new StringBuffer();
			    String stmp = strLine;
			    sb.append(stmp.trim());
			    while (!stmp.contains(";")) {
				stmp = br.readLine();
				if (stmp == null)
				    break;
				sb.append(stmp.trim());
			    }
			    parse_IFC_LineStatement(sb.toString().substring(1));
			}
		    }
		}
	    } finally {
		br.close();

	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Gets the object_buffer.
     * 
     * @return the object_buffer
     */
    public Map<Long, Thing> getObject_buffer() {
	return object_buffer;
    }


    public IfcProject getRoot() {
        return root;
    }


    public void setRoot(IfcProject root) {
        this.root = root;
    }

}
