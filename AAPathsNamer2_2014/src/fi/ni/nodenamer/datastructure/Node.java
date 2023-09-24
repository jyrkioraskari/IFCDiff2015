package fi.ni.nodenamer.datastructure;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class Node {
    public  long line_number=0;
    private boolean sameAs=false;
    private String local_uri="";
    private boolean collided=false;
    private boolean has_local_uri=false;
    
    private String literal_chksum = "  ";
    
    private String endbranch_chksum = "  ";
    private boolean endbcksum=false;
    private int   endb_based_on_nodecount=0;
    private double endb_bits=0;
    
    
    private String pchksum = "  ";  // for probability calculation
    final List<String> crossings = new ArrayList<String>();  // PathName.inx
    public final List<String> cchksumitems = new ArrayList<String>();  

    private double literal_prob=1;
    public RDFNode node;
    public String class_name;
    boolean list=false;
    boolean overwriteAnon=false;
    
    final List<Connection> edges_in = new ArrayList<Connection>();
    final List<Connection> edges_out = new ArrayList<Connection>();
    final List<Connection> edges_literals = new ArrayList<Connection>();
    

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

    public double getLiteral_prob() {
	return literal_prob;
    }

    public void setLiteral_prob(double literal_prob) {
	
	this.literal_prob = literal_prob;
	if(this.literal_prob<1)
	   this.endb_bits=getBits(this.literal_prob);
	else
	    this.endb_bits=0; 
    }

    public void addINConnection(Connection c) {
	edges_in.add(c);
    }
    public void addOUTConnection(Connection c) {
	edges_out.add(c);
    }

    public void addLiteralConnection(Connection c) {
	edges_literals.add(c);
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

    public String xorStrings(String txt1, String txt2) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < txt1.length(); i++)
	    sb.append((char) (txt1.charAt(i) ^ txt2.charAt(i % txt2.length())));
	return sb.toString();
    }


    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }


    public String getPchksum() {
        return pchksum;
    }


    public void setPchksum(String pchksum) {
        this.pchksum = pchksum;
    }


    public String getEndbranch_chksum() {
        return endbranch_chksum;
    }


    public void setEndbranch_chksum(String endbrannch_chksum) {
        this.endbranch_chksum = endbrannch_chksum;
    }


    public boolean isEndbcksum() {
        return endbcksum;
    }


    public void setEndbcksum(boolean endbcksum) {
        this.endbcksum = endbcksum;
    }


    public List<String> getCrossings() {
        return crossings;
    }


   
    private String getLocal_uri() {
	if(this.sameAs)
           return local_uri+"_sameAs";
	else
	   return local_uri;
    }


    public void setURI(String local_uri) {
        this.local_uri = local_uri;
        has_local_uri=true;
    }


    public boolean has_local_uri() {
        return has_local_uri;
    }
    
    public boolean isCollided() {
        return collided;
    }


    public void setCollided(boolean collided) {
        this.collided = collided;
    }


  

    public int getEndb_based_on_nodecount() {
        return endb_based_on_nodecount;
    }


    public void setEndb_based_on_nodecount(int endb_based_on_nodecount) {
        this.endb_based_on_nodecount = endb_based_on_nodecount;
    }
    
    
    public void incEndb_based_on_nodecount(Node subnode) {
        this.endb_based_on_nodecount++;
        this.endb_bits+=subnode.getEndb_bits();
    }

    
    public double getEndb_bits() {
        return endb_bits;
    }
   
    private double getBits(double probability) {
  	return ((-Math.log(probability) / Math.log(2)) );
      }
    
	public String getURI() {
		if(getNode().isLiteral())
			return getNode().asLiteral().getString();
		if(!isAnon())
			return getNode().asResource().getURI().toString();
		return getLocal_uri();
	}

}
