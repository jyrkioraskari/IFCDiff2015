package fi.ni;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.ni.model.RDFHandler;
import fi.ni.model.datastructure.Connection;
import fi.ni.model.datastructure.Node;

public class NodeAlgSign {
	List<Node> blank_nodes = new ArrayList<Node>();
	Set<Node> nodes = new HashSet<Node>();


	public NodeAlgSign() {
	}

	public String AlgSign(Node node)
	{
	    	List<String> l_in = new ArrayList<String>();
	    	List<String> l_class = new ArrayList<String>();
	    	List<String> l_out = new ArrayList<String>();
	    	
	    	l_class.add("type"+node.getRDFClass_name());
	    	
	    	
	    	// IN: OSOITTAVAT LUOKAT tyypin mukaan
	    	List<Connection> cons_in = node.getEdges_in();
	    	for (Connection c : cons_in) {
	    	    if(!c.getPointedNode().isAnon())
	    	      l_in.add(c.getPointedNode().getNode().asResource().getURI().toString()+c.getProperty());
	    	    else	
	    	      l_in.add("&"+c.getProperty());
	    	}

	    	// LITERAALIT
	    	List<Connection> cons_lit = node.getEdges_literals();
	    	for (Connection c : cons_lit) {
	    	    l_out.add(c.getProperty() + c.getPointedNode().node.asLiteral().getLexicalForm());
	    	}

	    	// OUT: OSOITETUT LUOKAT tyypin mukaan
	    	List<Connection> cons_out = node.getEdges_out();
	    	for (Connection c : cons_out) {
	    	    if(!c.getPointedNode().isAnon())
	    		      l_out.add(c.getProperty() + c.getPointedNode().getNode().asResource().getURI().toString());
	    		    else	
	    		      l_out.add(c.getProperty() + "&");
	    	}
	    	
	    	
	    	Collections.sort(l_in);
	    	Collections.sort(l_class);
	    	Collections.sort(l_out);

	    	StringBuffer sb=new StringBuffer();
	    	boolean first=true;
	    	for(String s:l_in)
	    	{
	    		if(first)
	    		{
	    			sb.append(s);
	    		    first=false;
	    		}
	    		else 
	    			sb.append("*"+s);
	    		
	    	}
	    	for(String s:l_class)
	    	{
	    		if(first)
	    		{
	    			sb.append(s);
	    		    first=false;
	    		}
	    		else 
	    			sb.append("*"+s);
	    		
	    	}
	    	for(String s:l_out)
	    	{
	    		if(first)
	    		{
	    			sb.append(s);
	    		    first=false;
	    		}
	    		else 
	    			sb.append("*"+s);
	    		
	    	}
	    	return sb.toString();
	}
	
	
	public void analyzeAlgSign(String filename,String datatype) {
		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
        for (Node node : nodes) {        	
		    if (!node.isLiteral())
			{
		    	if(node.isAnon())
		    	{
                 String signature=AlgSign(node);		    		
		    	 node.setURI(signature);
		    	}
			}
		}
        makeUnique();
        
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
	public Set<Node> getNodes() {
		return nodes;
	}

}
