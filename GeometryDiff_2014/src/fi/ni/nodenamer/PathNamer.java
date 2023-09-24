package fi.ni.nodenamer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.datastructure.Path;

public class PathNamer {
	Node node;

	public PathNamer(Node node) {
		this.node = node;
	}

	public void run(int steps) {
		setTheReachedNodes(node, steps);
	}

	private void setTheReachedNodes(Node node, int maxpath) {

		Queue<Path> q = new LinkedList<Path>();
		Path p0 = new Path(node);

		q.add(p0);
		while (!q.isEmpty()) {
			Path p1 = q.poll();
            
			if (p1.getSteps_taken() > maxpath) {
				continue;
			}

			handleCandidateLinks(q, p1, p1.getLast_node().getEdges_in());
			handleCandidateLinks(q, p1, p1.getLast_node().getEdges_out());
		}
	}

	Map<Node, Integer> nodes = new HashMap<Node, Integer>();

	private int handleCandidateLinks(Queue<Path> q, Path p1, List<Connection> edges) {
		
		for (Connection e : edges) {

			Node u = e.getPointedNode();
			Path p2 = new Path(p1, e);
			Integer i = nodes.get(u);
			if (i == null) {
				i = new Integer(p2.getSteps_taken());
				nodes.put(u, i);
			}
			
			if (p2.getSteps_taken() == i) {
				p2.addEdge(e);
				if(u.addPath(p2))
				   q.add(p2);

			} 

		}
		return 1;
	}

}
