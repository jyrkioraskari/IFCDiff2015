package fi.ni.nodenamer.datastructure;

import java.util.ArrayList;
import java.util.List;

import fi.ni.util.StringChecksum;

public class Path {
	private final StringChecksum checksum;
	int steps_taken;
	private double probability = 1;
	Node last_node;
	boolean value_read = false;
	List<Node> nodes = new ArrayList<Node>();

	public Path(Node blank_node) {
		checksum = new StringChecksum();
		nodes.add(blank_node);
		last_node = blank_node;
		update(blank_node.getLiteralChksum());
		update(blank_node.class_name);
		steps_taken = 0;
	}

	public Path(Path last_step, Connection edge) {
		for(Node node:last_step.nodes)
			nodes.add(node);

		last_node = edge.getPointedNode();
		checksum = last_step.checksum.copy();

		update(edge.getProperty());
		update(edge.getPointedNode().getLiteralChksum());
		update(edge.getPointedNode().getRDFClass_name());
		probability = last_step.probability;
		steps_taken = last_step.steps_taken + 1;
	}

	public boolean addEdge(Connection e) {
		Node u=e.points_to;
		if (!u.isLiteral())
		{
			if(!nodes.contains(u))
			{
			    nodes.add(e.getPointedNode());
			    return true;
			}
			else
			   return false;
		}
		return false;
	}

	public void updateProbability(double value) {
		if (value > 1)
			System.err.println("wrong prob: " + value);
		if (value < 0)
			System.err.println("wrong prob: " + value);
		probability *= value;
	}

	public void update(String txt) {
		checksum.update(txt);
	}

	public Node getLast_node() {
		return last_node;
	}

	public String getChecksum() {
		return checksum.getChecksumValue();
	}

	public int getBits() {
		return (int) ((-Math.log(probability) / Math.log(2)) + 0.5);
	}

	public double getProbability() {
		return probability;
	}
	
	

	public int getSteps_taken() {
	    return steps_taken;
	}

	
	

	public List<Node> getNodes() {
	    return nodes;
	}
	
	
	public String toString()
	{
	    StringBuffer sb=new StringBuffer();
	    for(Node node:nodes)
	    {
		  sb.append(node.getLiteralChksum()+" ");
	    }
	    
	    return sb.toString();
	}

	public static void main(String[] args) {
		Path fn = new Path(new Node(null, ""));
		fn.probability = 0.001f;
		System.out.println(fn.getBits());
		fn.probability = 0.01f;
		System.out.println(fn.getBits());
		fn.probability = 0.1f;
		System.out.println(fn.getBits());
		fn.probability = 0.5f;
		System.out.println(fn.getBits());
		fn.probability = 0.7f;
		System.out.println(fn.getBits());
		fn.probability = 0.9f;
		System.out.println(fn.getBits());

	}

}
