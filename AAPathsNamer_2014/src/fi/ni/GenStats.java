package fi.ni;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.ni.nodenamer.LiteralChecksummer;
import fi.ni.nodenamer.PathsNamer;
import fi.ni.nodenamer.RDFHandler;
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

	final int maxsteps;

	public GenStats(int maxsteps) {
		this.maxsteps = maxsteps;
	}

	public void analyze(String filename, String datatype) {

		nameListsClasses();
		an1(filename, datatype);

		for (Node bn : nodes) {
			if (bn.isAnon()) {
				PathsNamer rn = new PathsNamer(bn, classbag);
				rn.run(this.maxsteps);
			}

		}
		
	}

	private void an1(String filename, String datatype) {
		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
		for (Node node : nodes) {
			classbag.add(node.getRDFClass_name());
		}
		for (Node bn : nodes) {
			if (bn.getRDFClass_name().contains("IfcPropertySet"))
				bn.setOverwriteAnon(true);
		}

		nodeliteralsummer.setliteralChecksums(nodes, class_chksums_bag);
		nodeliteralsummer.setNodeLiteralProbabilities(nodes, class_chksums_bag);
	}

	public void makeUnique() {
		Map<String, Integer> class_inx = new HashMap<String, Integer>();
		Map<String, Node> lchecksums = new HashMap<String, Node>();
		for (Node bn : nodes) {
			bn.setCollided(false);
		}
		for (Node bn : nodes) {
			Node ex = lchecksums.put(bn.getURI(), bn);
			if (ex != null) {
				ex.setCollided(true);
				bn.setCollided(true);
			}
		}
        long collidecount=0;
		for (Node bn : nodes) {
			if (bn.isCollided()) {
					collidecount++;
					Integer count = class_inx.get(bn.getURI());
					if (count == null)
						count = 0;
					class_inx.put(bn.getURI(), count + 1);
					if (count != 0) // to be comparable with 0 count, when 1
									// removed from list of 2 items
						bn.setURI(bn.getURI() + ".#" + count);
				}
		}
		System.out.println("Uniques : "+(nodes.size()-collidecount)+" /"+ nodes.size());
	}
	
	public Set<Node> getNodes() {
		return nodes;
	}


	
	private void nameListClasses(Node bn, Node start, int inx, String prop) {
		start.setRDFClass_name(bn.getRDFClass_name() + "." + prop + "." + inx);

		for (Connection e : start.getEdges_out()) {
			if (e.getProperty().endsWith("rest")) {
				nameListClasses(bn, e.getPointedNode(), inx + 1, prop);
			}

		}
	}

	private void nameListsClasses() {
		for (Node node : nodes) {
			if (node.isLiteral()) {
				continue;
			}
            if(node.getRDFClass_name().equals("rdf:list"))
            	continue;
			String local_uri = node.getRDFClass_name() + "." + node.getLiteralChksum();
			node.setURI(local_uri);
			for (Connection e : node.getEdges_out()) {

				if (e.getPointedNode().getRDFClass_name().equals("rdf:list")) {
					nameListClasses(node, e.getPointedNode(), 1, e.getProperty());
				}

			}
		}

	}

}
