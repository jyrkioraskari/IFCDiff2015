package fi.ni.nodenamer.datastructure;

import java.util.ArrayList;
import java.util.List;

import fi.ni.util.StringChecksum;

public class Path {
	final Node first_node;
	int steps_taken;
	Node last_node;
	final private List<Connection> path_links = new ArrayList<Connection>();

	public Path(Node node) {
		this.first_node=node;
		last_node = node;
		steps_taken = 0;
	}

	public Path(Path old, Connection edge) {
		for(Connection c:old.path_links)
			path_links.add(c);
		this.first_node=old.first_node;
		last_node = edge.getPointedNode();
		steps_taken = old.steps_taken + 1;
	}

	public void addEdge(Connection e) {
		
		path_links.add(e);

	}

	public Node getLast_node() {
		return last_node;
	}


	public int getSteps_taken() {
	    return steps_taken;
	}

	
	
	public String getChecksum()
	{
		StringChecksum sc=new StringChecksum();
		sc.update(first_node.getLiteralChksum());
		for(Connection c: path_links)
		{
			sc.update(c.getProperty());
			//sc.update(c.getPointedNode().getLiteralChksum());
		}
		return sc.getChecksumValue();
	}
	
	/*public String getChecksumSB()
	{
		StringBuffer sc=new StringBuffer();
		sc.append(first_node.getURI());
		for(Connection c: path_links)
		{
			sc.append(c.getProperty());
		}
		return sc.toString();
	}*/

}
