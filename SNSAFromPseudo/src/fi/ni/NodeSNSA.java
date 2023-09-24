package fi.ni;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import fi.ni.model.RDFHandler;
import fi.ni.model.datastructure.Connection;
import fi.ni.model.datastructure.Node;
import fi.ni.model.datastructure.Path;
import fi.ni.util.StringChecksum;

public class NodeSNSA {
	List<Node> blank_nodes = new ArrayList<Node>();
	Set<Node> nodes = new HashSet<Node>();


	public NodeSNSA() {
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
	
	final int MAXPATH=2;
	public void SNSA(Node n)
	{
		List<String> paths = new ArrayList<String>();
		Queue<Path> q = new LinkedList<Path>();
		q.add(new Path(n));
		while (!q.isEmpty()) {
			Path p1 = q.poll();
			Node node=p1.getNodelist().getLast();
			boolean handled=false;
			if(p1.getNodelist().size()>= MAXPATH)
				paths.add(p1.getChecksum());			
			else
			{
				for(Connection c:node.getEdges_in())
				{
					Node in=c.getPointedNode();
					if(!p1.contains(in))
					{
						String sc=p1.getChecksum() + in.getLiteralChksum();
				    	StringChecksum lchecksum = new StringChecksum();
				    	lchecksum.update(sc+"-");
				    	q.add(new Path(p1,in,lchecksum.getChecksumValue()));
				    	handled=true;
					}
				}
				
				for(Connection c:node.getEdges_out())
				{
					Node out=c.getPointedNode();
					if(!p1.contains(out))
					{
						String sc=p1.getChecksum() + out.getLiteralChksum();
				    	StringChecksum lchecksum = new StringChecksum();
				    	lchecksum.update(sc+"+");
				    	q.add(new Path(p1,out,lchecksum.getChecksumValue()));
				    	handled=true;
					}
				}
				if(!handled)
				{
					paths.add(p1.getChecksum());	
				}
				
			}
		}
		Collections.sort(paths);
		StringChecksum lchecksum = new StringChecksum();    	
		for(String s:paths)
		{
	    	lchecksum.update(s);
		}
		n.setURI(lchecksum.getChecksumValue());
	}
	
	public void analyzeSNSA(String filename,String datatype) {
		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
		
        for (Node node : nodes) {        	
		    if (!node.isLiteral())
			{
		    	if(node.isAnon())
		    	{
		    		
                String signature=AlgSign(node);		    		                
		    	StringChecksum lchecksum = new StringChecksum();
		    	lchecksum.update(signature);
		    	node.setLiteralChksum(lchecksum.getChecksumValue());
		    	}
		    	else
		    		node.setLiteralChksum(node.getNode().asResource().getURI().toString());
			}
		}
        System.out.println("Algsign tehty");
        int inx=0;
        for (Node node : nodes) {        	
		    if (!node.isLiteral())
			{
		    	if(node.isAnon())		    		
		    	{
		    		inx++;
		    		System.out.println(inx);
		    		SNSA(node);
		    	}
		    	else
		    		node.setURI(node.getNode().asResource().getURI().toString());
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

	for (Node bn : nodes) {
	    if (bn.isCollided()) {
		{
		    Integer count = class_inx.get(bn.getURI());
		    if (count == null)
			count = 0;
		    class_inx.put(bn.getURI(), count + 1);
		    if(count!=0)  // to be comparable with 0 count, when 1 removed from list of 2 items
		      bn.setURI(bn.getURI() + ".#" + count);
		}
	    }
	}
    }

	public Set<Node> getNodes() {
		return nodes;
	}

}
