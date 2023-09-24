package fi.ni;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.Frequency;

import fi.ni.nodenamer.IFCHandler;
import fi.ni.nodenamer.LiteralChecksummer;
import fi.ni.nodenamer.OutEndBranchChecksummer;
import fi.ni.nodenamer.ReducedNamer;
import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.stats.ClassLiteralCksumBag;
import fi.ni.nodenamer.stats.ClassPropertyStatsBag;

public class GenStats {
    List<Node> blank_nodes = new ArrayList<Node>();
    Set<Node> nodes = new HashSet<Node>();

    ClassPropertyStatsBag classbag = new ClassPropertyStatsBag();
    ClassLiteralCksumBag class_chksums_bag = new ClassLiteralCksumBag();
    public LiteralChecksummer nodeliteralsummer = new LiteralChecksummer();
    BufferedWriter lokifile;

    long nodecount = 0;
    long literals_count = 0;
    long named_nodes_count = 0;
    long anonymous_nodes_count = 0;

    final int maxsteps;
    final int bittarget;
    final int limit;
    boolean warming = false;

    public GenStats(int maxsteps, int bittarget, int limit) {
	System.out.println("===============================");
	this.maxsteps = maxsteps;
	this.bittarget = bittarget;
	this.limit = limit;
    }

    public GenStats(int maxsteps, int bittarget, int limit, boolean warming) {
	System.out.println("===============================");
	this.maxsteps = maxsteps;
	this.bittarget = bittarget;
	this.limit = limit;
	this.warming = warming;
    }

    public void analyze(String filename, boolean removeOne, BufferedWriter lokifile) {
	if (!warming) {
	    File file;

	    file = new File("C:/M/tulokset_2014/tulokset.txt");

	    FileWriter fw = null;
	    if (!file.exists()) {
		try {
		    file.createNewFile();
		    fw = new FileWriter(file.getAbsoluteFile());
		    lokifile = new BufferedWriter(fw);
		} catch (Exception e) {
		    e.printStackTrace();
		    
		    System.out.println("C:/M/tulokset_2014/tulokset.txt");
		    
		}
	    }
	}
	an1(filename, removeOne, lokifile);
	long maxheap = 0;

	for (Node bn : nodes) {
	    if (bn.isAnon()) {
		ReducedNamer rn = new ReducedNamer(bn, classbag);
		rn.setLimit(limit);
		rn.run(this.maxsteps, this.bittarget);

		long curheap = (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()).getUsed() / (1024 * 1014);
		if (curheap > maxheap)
		    maxheap = curheap;
	    }

	}
	println("Max heap memory memory (bytes): " + maxheap);

    }

    private void an1(String filename, boolean removeOne, BufferedWriter lokifile) {
	nodes.clear();
	IFCHandler ifch = new IFCHandler();
	Node changed = ifch.handleIFC(filename, nodes, blank_nodes, removeOne);
	if (changed != null)
	    println("   - Changed node: " + changed.getRDFClass_name(), lokifile);
	for (Node node : nodes) {
	    classbag.add(node.getRDFClass_name());
	}
	this.nodecount = nodes.size();

	nodeliteralsummer.setliteralChecksums(nodes, class_chksums_bag);
	nodeliteralsummer.setNodeLiteralProbabilities(nodes, class_chksums_bag);

	OutEndBranchChecksummer oebck = new OutEndBranchChecksummer();
	oebck.setOutEndBranchChecksums(blank_nodes);
	calculate_statistics(nodes);
    }

    private void calculate_statistics(Set<Node> nodes) {
	for (Node node : nodes) {
	    if (node.isLiteral()) {
		literals_count++;
		continue;
	    }

	    if (!node.isAnon()) {
		named_nodes_count++;
	    } else {
		// if(!node.isList())
		if (!node.isLiteral())
		    anonymous_nodes_count++;
	    }
	}
    }

    private long testBlankURICheksums() {
	long collision_count = 0;
	for (Node bn : blank_nodes) {
	    bn.setCollided(false);
	}
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	for (Node bn : blank_nodes) {
	    Node ex = lchecksums.put(bn.getLocal_uri(), bn);
	    if (ex != null) {
		// Equals!!
		if (ex.isEndbcksum())
		    if (ex.getEndbranch_chksum().equals(bn.getEndbranch_chksum()))
			continue;
		ex.setCollided(true);
		bn.setCollided(true);
	    }
	}

	for (Node bn : blank_nodes) {
	    if (bn.isCollided()) {
		collision_count++;
	    }
	}
	return collision_count;
    }

    private long firstTestUris() {
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	long collision_count = 0;
	for (Node bn : blank_nodes) {
	    Node ex = lchecksums.put(bn.getLocal_uri(), bn);
	    if (ex != null) {
		ex.setCollided(true);
		bn.setCollided(true);
	    }
	}

	for (Node bn : blank_nodes) {
	    if (bn.isCollided()) {
		collision_count++;
	    }
	}
	return collision_count;
    }

    private void nameListNodes(Node bn, Node start, int inx, String prop) {
	start.setLocal_uri(bn.getLocal_uri() + "." + prop + "." + inx);
	
	  //TODO katso
	 /*if (inx > 50) { System.out.println("Lista yli 50 pitkä!!"); return;
	 }*/
	 
	for (Connection e : start.getEdges_out()) {
	    if (e.getProperty().endsWith("rest")) {
		nameListNodes(bn, e.getPointedNode(), inx + 1, prop);
	    }

	}
    }

    private long testAllBNUris() {
	for (Node node : nodes) {
	    if (node.isLiteral()) {
		continue;
	    }

	    if (!node.isAnon()) {
		boolean ok = true;
		String local_uri = node.getRDFClass_name() + "." + node.getLiteralChksum();
		node.setLocal_uri(local_uri);
		for (Connection e : node.getEdges_out()) {

		    Set<String> pn = new HashSet<String>();
		    if (e.getPointedNode().getRDFClass_name().equals("rdf:list")) {
			if (!pn.add(e.getProperty())) {
			    System.out.println("NO!!RDFLIST!!");
			    ok = false;
			}

		    }

		}
		if (ok) {
		    for (Connection e : node.getEdges_out()) {

			if (e.getPointedNode().getRDFClass_name().equals("rdf:list")) {
			    nameListNodes(node, e.getPointedNode(), 1, e.getProperty()); // TODO
											 // hae
											 // aakkosissa
											 // ensimmäinen
											 // propertyarvo
			}
		    }
		}

	    }
	}
	for (Node bn : blank_nodes) {
	    boolean ok = true;
	    for (Connection e : bn.getEdges_out()) {

		Set<String> pn = new HashSet<String>();
		if (e.getPointedNode().getRDFClass_name().equals("rdf:list")) {
		    if (!pn.add(e.getProperty())) {
			System.out.println("NO!!RDFLIST!!");
			ok = false;
		    }

		}

	    }
	    if (ok) {
		for (Connection e : bn.getEdges_out()) {

		    if (e.getPointedNode().getRDFClass_name().equals("rdf:list")) {
			nameListNodes(bn, e.getPointedNode(), 1, e.getProperty()); // TODO
										   // hae
										   // aakkosissa
										   // ensimmäinen
										   // propertyarvo
		    }
		}
	    }

	}

	for (Node bn : nodes) {
	    bn.setCollided(false);
	}
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	long collision_count = 0;

	for (Node bn : nodes) {
	    if (bn.isAnon()) {
		if (!bn.has_local_uri()) {
		    System.out.println("No URI");
		    System.out.println(bn);
		    System.exit(1);
		}
		Node ex = lchecksums.put(bn.getLocal_uri(), bn);
		if (ex != null) {
		    // System.out.println("collision! "+bn+" "+bn.getLocal_uri());
		    ex.setCollided(true);
		    bn.setCollided(true);
		}

	    }

	}

	for (Node bn : nodes) {
	    if (bn.isAnon()) {
		if (bn.isCollided()) {
		    collision_count++;
		}
	    }

	}

	return collision_count;
    }

    private double agvNodeCount() {
	double total = 0;
	for (Node bn : blank_nodes) {
	    total += bn.getBased_on_nodecount();
	}
	return total / blank_nodes.size();
    }

    Frequency pathLenstats = new Frequency();
    Frequency C_pathLenstats = new Frequency();
    Frequency NC_pathLenstats = new Frequency();

    Frequency bitstats = new Frequency();

    private double agvBitsCount() {
	double total = 0;
	for (Node bn : blank_nodes) {
	    int bits = bn.getActual_bits() + 1; // ei nollaa

	    bitstats.addValue(bits); // diskretisointi
	    total += bits;
	}
	return total / blank_nodes.size();
    }

    Frequency coll_uri_stats = new Frequency();
    Frequency no_coll_uri_stats = new Frequency();

    private void collisionStats() {
	for (Node bn : blank_nodes) {
	    if (bn.isCollided()) {
		coll_uri_stats.addValue(bn.getActual_bits());
		C_pathLenstats.addValue(bn.getUrlPathLengtgh());
	    } else {
		no_coll_uri_stats.addValue(bn.getActual_bits());
		NC_pathLenstats.addValue(bn.getUrlPathLengtgh());
	    }
	    pathLenstats.addValue(bn.getUrlPathLengtgh());
	}
    }

    private void printlMemoryUsage() {
	long heap = (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()).getUsed() / (1024 * 1014);
	long nonheap = (ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()).getUsed() / (1024 * 1014);
	println("Heap memory memory (bytes): " + heap);
	println("Non-Heap memory (bytes): " + nonheap);
    }

    CalcCollisionEstimateAP2 ce = new CalcCollisionEstimateAP2();

    // CPU-time vaatii sen, että kutsutaan yhdessä säikeessä
    public void showStats(BufferedWriter lokifile) {
	long collisions = firstTestUris();
	collisionStats();
	long dcollisions = testBlankURICheksums();

	println("----------------------------");
	println("Max number of steps: " + (this.maxsteps + 1));
	// println("Target bits: "+this.bittarget);
	println("nodes:" + nodecount);
	println("named nodes:" + named_nodes_count);
	println("anonymous nodes:" + blank_nodes.size());
	println("anonymous nodes:" + anonymous_nodes_count);
	println("literals:" + literals_count);
	println("URI collisions:" + collisions);
	println("URI collisions no dup:" + dcollisions);
	println("AVG nodecount: " + agvNodeCount());
	println("AVG actual bits: " + agvBitsCount());

	println("Histogram:");
	for (int n = 5; n < 1000; n++)
	    println("" + n + ": " + bitstats.getCount(n));

	println("");
	println("Current thread CPU time:" + (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1000000000l));
	println("Current thread User time:" + (ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() / 1000000000l));
	println("Collision estimate based on bits: " + ce.calc(bitstats));
	long acolls = testAllBNUris();
	println("AllBN collisions:" + acolls);
	println("AllBN collisions:" + acolls, lokifile);
    }

    public long testSimilarity(GenStats gs) {
	long difference = 0;
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	for (Node bn : blank_nodes) {
	    lchecksums.put(bn.getLocal_uri(), bn);
	}

	for (Node bn : gs.blank_nodes) {
	    if (lchecksums.get(bn.getLocal_uri()) == null)
		difference++;
	}
	return difference;
    }

    public long testSimilarityLong(GenStats gs) {
	System.out.println("Nodes: " + nodes.size());
	long difference = 0;
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	for (Node n : nodes) {
	    if (n.isLiteral())
		continue;
	    if (n.isAnon())
		lchecksums.put(n.getLocal_uri(), n);
	}

	for (Node n : gs.nodes) {
	    if (n.isLiteral())
		continue;
	    if (n.isAnon())
		if (lchecksums.get(n.getLocal_uri()) == null)
		    difference++;
	}
	return difference;
    }

    public long testSimilarityFlevelChecksums(GenStats gs) {
	System.out.println("Nodes: " + nodes.size());
	long difference = 0;
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	for (Node n : nodes) {
	    if (n.isLiteral())
		continue;
	    if (n.isAnon())
		lchecksums.put(n.getLiteralChksum(), n);
	}

	for (Node n : gs.nodes) {
	    if (n.isLiteral())
		continue;
	    if (n.isAnon())
		if (lchecksums.get(n.getLiteralChksum()) == null)
		    difference++;
	}
	return difference;
    }

    public void closeFile() {
	if (warming)
	    return;
	try {
	    lokifile.flush();
	    lokifile.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    private void println(String txt) {
	if (warming)
	    return;
	if (txt == null) {
	    System.err.println("txt null");
	    return;
	}
	if (lokifile == null) {
	    // System.err.println("lokifile null");
	    return;
	}
	try {
	    lokifile.write(txt);
	    lokifile.newLine();
	} catch (Exception e) {
	    System.err.println(txt);
	    System.err.println(lokifile == null);
	    e.printStackTrace();
	}
    }

    public void makeUnique() {
	Map<String, Integer> class_inx = new HashMap<String, Integer>();
	Map<String, Node> lchecksums = new HashMap<String, Node>();
	for (Node bn : nodes) {
	    bn.setCollided(false);
	}
	for (Node bn : nodes) {
	    Node ex = lchecksums.put(bn.getLocal_uri(), bn);
	    if (ex != null) {
		ex.setCollided(true);
		bn.setCollided(true);
	    }
	}

	for (Node bn : nodes) {
	    if (bn.isCollided()) {
		{
		    Integer count = class_inx.get(bn.getLocal_uri());
		    if (count == null)
			count = 0;
		    class_inx.put(bn.getLocal_uri(), count + 1);
		    if(count!=0)  // to be comparable with 0 count, when 1 removed from list of 2 items
		      bn.setLocal_uri(bn.getLocal_uri() + ".#" + count);
		}
	    }
	}
    }
    
    public void testUnique() {
  	Map<String, Node> lchecksums = new HashMap<String, Node>();
  	for (Node bn : nodes) {
  	    bn.setCollided(false);
  	}
  	for (Node bn : nodes) {
  	    Node ex = lchecksums.put(bn.getLocal_uri(), bn);
  	    if (ex != null) {
  		ex.setCollided(true);
  		bn.setCollided(true);
  	    }
  	}

  	for (Node bn : nodes) {
  	    if (bn.isCollided()) {
  		{
  		    System.err.println("Not labelled uniquely!!"+bn.getLocal_uri());
  		}
  	    }
  	}
      }

    public Map<String, Node> getURIMap() {
	Map<String, Node> ret = new HashMap<String, Node>();
	for (Node n : nodes) {
	    if (n.isLiteral())
		continue;
	    if (!n.isAnon())
		n.setLocal_uri(n.getNode().asResource().getURI().toString());
	    if (n.has_local_uri())
		ret.put(n.getLocal_uri(), n);
	}
	return ret;
    }

    public void printDiff(Map<String, Node> dataset2_urimap, BufferedWriter lokifile1) {
	int muuttunut = 0;
	int kaikki = 0;
	int cnodecount = 0;
	int cnodecount_total=0;;
	for (Node n : nodes) {
	    if (n.isLiteral())
		continue;
	    cnodecount_total++;
	    kaikki++; // RDF Ontology class
	    kaikki += n.getEdges_out().size();
	    kaikki += n.getEdges_literals().size();
	    if (!n.isAnon())
		n.setLocal_uri(n.getNode().asResource().getURI().toString());

	    Node theOther = dataset2_urimap.get(n.getLocal_uri());
	    if (theOther != null) {
		for (Connection e : n.getEdges_literals()) {
		    boolean iexist = false;
		    for (Connection eo : theOther.getEdges_literals()) {
			if (eo.getProperty().equals(e.getProperty()))
			    if (eo.getPointedNode().getNode().asLiteral().getLexicalForm().equals(e.getPointedNode().getNode().asLiteral().getLexicalForm())) {
				iexist = true;
			    }
		    }
		    if (!iexist) {
			muuttunut++;
		    }
		}

		for (Connection e : n.getEdges_out()) {
		    boolean oexist = false;
		    for (Connection eo : theOther.getEdges_out()) {
			if (eo.getProperty().equals(e.getProperty())) {
			    if (eo.getPointedNode().getLocal_uri().equals(e.getPointedNode().getLocal_uri())) {
				oexist = true;
			    }
			    
			    if (eo.getPointedNode().isEndbcksum() && e.getPointedNode().isEndbcksum())
				if (eo.getPointedNode().getEndbranch_chksum().equals(e.getPointedNode().getEndbranch_chksum())) {
				    oexist = true;
		            }
			}
		    }
		    if (!oexist) {
			muuttunut++;
		    }
		}

	    } else {
		cnodecount++;
		muuttunut++; // RDF Ontology class
		muuttunut += n.getEdges_out().size();
		muuttunut += n.getEdges_literals().size();
	    }

	}

	System.out.println("");
	System.out.println("muuttunut/kaikki: " + muuttunut + "/" + kaikki);
	System.out.println("muuttuneet nodet /kaikki " + cnodecount+"/"+cnodecount_total);
	println("muuttunut/kaikki: " + muuttunut + "/" + kaikki,lokifile1);


    }

    private void println(String txt, BufferedWriter lokifile) {
   	if (txt == null) {
   	    System.err.println("txt null");
   	    return;
   	}
   	if (lokifile == null) {
   	    System.err.println("lokifile null");
   	    return;
   	}
   	try {
   	    lokifile.write(txt);
   	    lokifile.newLine();
   	    lokifile.flush();
   	} catch (Exception e) {
   	    System.err.println(txt);
   	    System.err.println(lokifile == null);
   	    e.printStackTrace();
   	}
       }


}
