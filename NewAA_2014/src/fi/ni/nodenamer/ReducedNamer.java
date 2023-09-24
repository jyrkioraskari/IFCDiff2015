package fi.ni.nodenamer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.datastructure.Path;
import fi.ni.nodenamer.filters.FilterLinks;
import fi.ni.nodenamer.stats.ClassPropertyStatsBag;
import fi.ni.util.StringChecksum;

public class ReducedNamer {
	Node bn;
	FilterLinks filter_links = new FilterLinks();
	final ClassPropertyStatsBag classbag;
	private int limit = 5;

	public ReducedNamer(Node blank_node, ClassPropertyStatsBag classbag) {
		bn = blank_node;
		this.classbag = classbag;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void run(int steps, int bittarget) {
		try {
			List<Path> cpaths = getCandidatepathsFull(bn, steps, bittarget);

			Set<Node> nodes = new HashSet<Node>();
			for (Path p : cpaths) {
				nodes.addAll(p.getNodes());
			}
			List<String> csums = new ArrayList<String>();
			for (Path p : cpaths) {
				csums.add(p.getChecksum());
			}
			List<String> crosssings = null;
			crosssings = findCrossings(cpaths, nodes);
			csums.addAll(crosssings);
			Collections.sort(csums);

			StringChecksum checksum = new StringChecksum();
			checksum.update(bn.getRDFClass_name());
			checksum.update(bn.getLiteralChksum());
			for (String s : csums) {
				checksum.update(s);
			}

			String local_uri = checksum.getChecksumValue();
			bn.setURI(local_uri);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private List<String> findCrossings(List<Path> cpaths, Set<Node> nodes) {
		List<String> ret = new ArrayList<String>();
		for (Path path : cpaths) {
			for (int i = 0; i < path.getNodes().size(); i++) {
				Node node = path.getNodes().get(i);
				node.getCrossings().add(path.getChecksum() + "." + i);

			}
		}
		for (Node node : nodes) {
			if (node.getCrossings().size() > 1) {
				Collections.sort(node.getCrossings());
				StringChecksum checksum = new StringChecksum();

				for (String val : node.getCrossings()) {
					checksum.update(val);
				}
				ret.add(node.getLiteralChksum() + "=" + checksum.getChecksumValue());
			}
			node.getCrossings().clear();
		}
		return ret;
	}

	double node_pathbits = 0;
	int node_prob_steps_taken = 0;

	private List<Path> getCandidatepathsFull(Node bn, int maxpath, int bittarget) {

		List<Path> candidate_paths = new ArrayList<Path>();
		Queue<Path> q = new LinkedList<Path>();
		Path p0 = new Path(bn);
		node_pathbits = getBits(classbag.test(bn.getRDFClass_name())) * 0.4;
		node_pathbits += getBits(bn.getLiteral_prob()) * 0.4;

		int path_length_limit = maxpath;

		q.add(p0);
		while (!q.isEmpty()) {
			Path p1 = q.poll();

			if (node_pathbits > bittarget && node_prob_steps_taken < path_length_limit) {
				path_length_limit = node_prob_steps_taken;
			}

			// yhtäsuusuus ei toimi.. pitää olla > !!
			if (p1.getSteps_taken() > path_length_limit) {
				candidate_paths.add(p1);
				continue;
			}

			handleCandidateLinksFull("IN", candidate_paths, q, p1, p1.getLast_node().getEdges_in(), limit);
			handleCandidateLinksFull("OUT", candidate_paths, q, p1, p1.getLast_node().getEdges_out(), limit);
		}
		return candidate_paths;
	}

	public double getBits(double probability) {
		return ((-Math.log(probability) / Math.log(2)));
	}

	public double fixDistribution(double x) {
		return 0.50 * x; // valittu korjauskerroin
	}

	public double log2(double probability) {
		return ((Math.log(probability) / Math.log(2)));
	}

	Map<Node, Integer> nodes = new HashMap<Node, Integer>();

	private int handleCandidateLinksFull(String label, List<Path> candidate_paths, Queue<Path> q, Path p1, List<Connection> edges, int max_number) {
		p1.update(label);
		if (edges.size() > max_number) {
			edges = filter_links.filterLowestChecksumValues(edges, max_number);
		}
		if ((edges == null) || (edges.size() == 0)) {
			// p1.update("termine");
			candidate_paths.add(p1);
			return 0;
		}
		node_prob_steps_taken = p1.getSteps_taken();
		for (Connection e : edges) {
			node_pathbits += 0.04; // edge probability

			Node u = e.getPointedNode();
			Path p2 = new Path(p1, e);

			Integer i = nodes.get(u);
			if (i == null) {
				i = new Integer(p2.getSteps_taken());
				nodes.put(u, i);
				{
					if (u.getLiteral_prob() != 1) {
						double bits = getBits(u.getLiteral_prob());
						node_pathbits += (bits) * 0.40;
					}
				}
			}
			p2.addEdge(e);

			p2.updateProbability(u.getLiteral_prob());
			if (p2.getSteps_taken() == i) {
				q.add(p2);
			} else {
				p2.update(u.getLiteralChksum());
				p2.update("gcycle");
				candidate_paths.add(p2);
			}

		}
		return 1;
	}

}
