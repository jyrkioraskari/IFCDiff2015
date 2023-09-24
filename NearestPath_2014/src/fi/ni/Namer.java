package fi.ni;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.ni.nodenamer.LiteralChecksummer;
import fi.ni.nodenamer.PathNamer;
import fi.ni.nodenamer.RDFHandler;
import fi.ni.nodenamer.datastructure.Node;

public class Namer {
	List<Node> blank_nodes = new ArrayList<Node>();
	Set<Node> nodes = new HashSet<Node>();

	public LiteralChecksummer nodeliteralsummer = new LiteralChecksummer();


	final int maxsteps;

	public Namer(int maxsteps) {
		this.maxsteps = maxsteps;
	}
	

	public void analyze(String filename, String datatype) {

		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
		for (Node bn : nodes) {
			if (bn.getRDFClass_name().contains("IfcPropertySet"))
				bn.setOverwriteAnon(true);

		}
		nodeliteralsummer.setliteralChecksums(nodes);

		
		System.out.println("Paths");
		for (Node n : nodes) {
			// URIs
			if (!n.isLiteral())
			if (!n.isAnon()) {
				PathNamer rn = new PathNamer(n);
				rn.run(maxsteps);
			}

		}
		System.out.println("Give URIs");
		for (Node bn : nodes) {	
			if (!bn.isLiteral())
			if (bn.isAnon()) {
				bn.calculateURI();
			}

		}
	}



	public Set<Node> getNodes() {
		return nodes;
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
				{
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
		}
		System.out.println("Uniques : "+(nodes.size()-collidecount)+" /"+ nodes.size());
	}


}
