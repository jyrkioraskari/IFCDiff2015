package fi.ni.nodenamer.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;

import fi.ni.util.StringChecksum;

public class Node {
    private boolean sameAs=false;
    private String local_uri="";
    private boolean collided=false;
    private String literal_chksum = "  ";
    
    public RDFNode node;
    public String class_name;
    boolean overwriteAnon=false;

    final Map<String,Node> in_map = new HashMap<String,Node>();
    final Map<String,Node> out_map = new HashMap<String,Node>();
    final Map<String,String> literals_map = new HashMap<String,String>();

    final List<Connection> edges_in = new ArrayList<Connection>();
    final List<Connection> edges_out = new ArrayList<Connection>();
    final List<Connection> edges_literals = new ArrayList<Connection>();
    

    int maxPathLength=Integer.MAX_VALUE;
    List<Path> paths =new ArrayList<Path>();
    
    public Node(RDFNode node, String class_name) {
	super();
	this.node = node;
	this.class_name = class_name;
    }

    public Node(RDFNode node, String class_name,boolean sameAs) {
	super();
	this.node = node;
	this.class_name = class_name;
	this.sameAs=sameAs;
    }


    public void setOverwriteAnon(boolean overwriteAnon) {
		this.overwriteAnon = overwriteAnon;
	}

	public boolean isAnon() {
		if(this.overwriteAnon)
			return true;
	    return node.isAnon();
    }

    public boolean isLiteral() {
	return node.isLiteral();
    }

    public RDFNode getNode() {
	return node;
    }

    public String getRDFClass_name() {
	if(class_name.equals("list"))
	    return "rdf:list";
	return class_name;
    }
    
    public void setRDFClass_name(String class_name) {
		this.class_name = class_name;
	}

	public String toString() {
	if (node.isLiteral())
	    return node.asLiteral().getLexicalForm();
	else
	    return this.class_name+"."+this.getLiteralChksum();
    }
    
    public String togwString() {
	if (node.isLiteral())
	    return node.asLiteral().getLexicalForm();
	else
	    return this.class_name+"."+this.getLiteralChksum();
    }

    public String getLiteralChksum() {
	return literal_chksum;
    }

    public void setLiteralChksum(String chksum) {
	this.literal_chksum = chksum;
    }

  
     public void addINConnection(Connection c) {
	edges_in.add(c);
	in_map.put(c.getProperty(), c.getPointedNode());
    }
    public void addOUTConnection(Connection c) {
	edges_out.add(c);
	out_map.put(c.getProperty(), c.getPointedNode());
    }

    public void addLiteralConnection(Connection c) {
	edges_literals.add(c);
	literals_map.put(c.getProperty(), c.getPointedNode().toString());
    }


    public List<Connection> getEdges_in() {
        return edges_in;
    }

    public List<Connection> getEdges_out() {
        return edges_out;
    }

    public List<Connection> getEdges_literals() {
        return edges_literals;
    }

   
    private String getLocal_uri() {
	if(this.sameAs)
           return local_uri+"_sameAs";
	else
	   return local_uri;
    }


    
    public boolean isCollided() {
        return collided;
    }


    public void setCollided(boolean collided) {
        this.collided = collided;
    }


	public String getURI() {
		if(getNode().isLiteral())
			return getNode().asLiteral().getString();
		if(!isAnon())
		{
			return getNode().asResource().getLocalName();
		}
		return getLocal_uri();
	}

    public void setURI(String local_uri) {
        this.local_uri = local_uri;
    }

    
   
	public boolean addPath(Path p)
    {
    	if(!isAnon())
    		return false;
    	if(p.getSteps_taken()>0)
    	{
    		if(p.getSteps_taken()<maxPathLength)
    		{
    			maxPathLength=p.getSteps_taken();
    			paths.clear();
    			paths.add(p);
    		}
    		else
        		if(p.getSteps_taken()==maxPathLength)
        		{
        			paths.add(p);
        		}
        		else
        			return false;
    	}
    	return true;
    }
    
    /*
     * Calculates URI from paths
     */
    
    public void calculateURI()
    {
    	StringBuffer sb=new StringBuffer();
    	StringChecksum sc=new StringChecksum();
    	List<String> pc=new ArrayList<String>();
    	for(Path p:paths)
    	{
    		pc.add(p.getChecksum());    		
    	}
    	Collections.sort(pc);
    	sc.update(getLiteralChksum());  // just in case there is no path
    	sb.append(getLiteralChksum());
    	for(String s:pc)
    	{
    		sc.update(s);
    		sb.append(s);
    	}
    	setURI(sc.getChecksumValue()+"");
    	//setURI("["+sb.toString()+"]");
    }

	public Map<String, Node> getIn_map() {
		return in_map;
	}

	public Map<String, Node> getOut_map() {
		return out_map;
	}

	public Map<String, String> getLiterals_map() {
		return literals_map;
	}
    
    
}
