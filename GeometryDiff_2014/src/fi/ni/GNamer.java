package fi.ni;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.LiteralChecksummer;
import fi.ni.nodenamer.PathNamer;
import fi.ni.nodenamer.RDFHandler;
import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;

public class GNamer {
	List<Node> blank_nodes = new ArrayList<Node>();
	Set<Node> nodes = new HashSet<Node>();

	final public LiteralChecksummer nodeliteralsummer;


	final int maxsteps;

	public GNamer(int maxsteps) {
		this.maxsteps = maxsteps;
		nodeliteralsummer = new LiteralChecksummer();
	}
	

	public void analyze(String filename, String datatype) {

		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
		for (Node n : nodes) {
			if (n.getRDFClass_name().endsWith("IfcGridPlacement"))
				System.out.println("GPlacement: "+n);
			if (n.getRDFClass_name().endsWith("IfcLocalPlacement"))
			{
				System.out.println("LPlacement: "+n);
				List<Connection> cons_in = n.getEdges_in();
				for (Connection c : cons_in) {
					System.out.println(c.getProperty() +"." +c.getPointedNode());
				}


				List<Connection> cons_out = n.getEdges_out();
				for (Connection c : cons_out) {
					System.out.println("  "+c.getProperty() +"."+ c.getPointedNode());
				}
			}
		}
		
		for (Node bn : nodes) {
			if (bn.getRDFClass_name().equalsIgnoreCase("IfcPropertySet"))
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

	private String parseCoordinates(Node n)
	{
		String ret="";
		for (Connection e : n.getEdges_literals()) {
	    	if(e.getProperty().equals("first"))
		    		  ret+=" "+e.getPointedNode();
	    }
		for (Connection e : n.getEdges_out()) {
	    	if(e.getProperty().equals("rest"))
		    		  ret+=" "+parseCoordinates(e.getPointedNode());
	    	if(e.getProperty().equals("first"))
	    		if(ret.length()>0)
	    		  ret+=" "+e.getPointedNode();
	    }
		return ret;
	}
	
	public void printPlacementGraph(String filename, String datatype) {
		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
		System.out.println("digraph G { rankdir=LR "
				+ "node [shape=box, color=blue] ");

		for (Node n : nodes) {
			if (n.getRDFClass_name().endsWith("IfcLocalPlacement"))
			{
				boolean refersTo=false;
				for (Connection e : n.getEdges_out()) {
			    	if(e.getProperty().equals("placementRelTo"))
			    		refersTo=true;
			    }
				if(refersTo)
					continue;

				System.out.println("\"l" + n.hashCode() + "\" [shape=box,style=filled, label=\"" + n.getRDFClass_name()+"\"];");
				for (Connection e : n.getEdges_in()) {
		    		System.out.println("\"l" + e.getPointedNode().hashCode() + "\" [shape=box,style=filled, label=\"" + e.getPointedNode().getRDFClass_name()+"\"];");
			    	System.out.println("\"l" + n.hashCode() + "\" -> " + "\"l" + e.getPointedNode().hashCode() +"\" [color=lightgray label=\""+e.getProperty()+"\" ];");
			    }

				for (Connection e : n.getEdges_out()) {
					   if(e.getProperty().equals("relativePlacement"))
			    	   {
						  Node x=e.getPointedNode();
						  Node loc=x.getOut_map().get("location");
						  if(loc==null)
						  {
							  System.err.println("No location");
							  return;
						  }
						  Node colist=loc.getOut_map().get("coordinates");
						  if(colist==null)
						  {
							  System.err.println("No cordinates");
							  return;
						  }
						  String co=parseCoordinates(colist);
			    		  System.out.println("\"l" + e.getPointedNode().hashCode() + "\" [shape=box,style=filled, label=\"" + co +"\"];");
			    	   }
			    	    else
				    	{
				    		System.out.println("\"l" + e.getPointedNode().hashCode() + "\" [shape=box,style=filled, label=\"" + e.getPointedNode().getRDFClass_name()+"\"];");
				    	}
				    	System.out.println("\"l" + n.hashCode() + "\" -> " + "\"l" + e.getPointedNode().hashCode() +"\" [color=lightgray label=\""+e.getProperty()+"\" ];");
				}
			}
		}
		System.out.println("}");
		System.out.println();
	}


}
