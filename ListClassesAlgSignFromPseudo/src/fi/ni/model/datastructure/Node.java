package fi.ni.model.datastructure;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class Node {

	private boolean sameAs = false;
	private String literal_chksum = "  ";
    public  String URI;
    private boolean collided=false;
	public RDFNode node;
	public String class_name;

	final List<Connection> edges_in = new ArrayList<Connection>();
	final List<Connection> edges_out = new ArrayList<Connection>();
	final List<Connection> edges_literals = new ArrayList<Connection>();

	public Node(RDFNode node, String class_name) {
		super();
		this.node = node;
		this.class_name = class_name;
	}

	public Node(RDFNode node, String class_name, boolean sameAs) {
		super();
		this.node = node;
		this.class_name = class_name;
		this.sameAs = sameAs;
	}

	public boolean isAnon() {
		return node.isAnon();
	}

	public boolean isLiteral() {
		return node.isLiteral();
	}

	public RDFNode getNode() {
		return node;
	}

	public String getRDFClass_name() {
		if (class_name.equals("list"))
			return "rdf:list";
		return class_name;
	}

	public String toString() {
		if (node.isLiteral())
			return node.asLiteral().getLexicalForm();
		else
			if(isAnon())
				return "id:"+this.hashCode();
			else
				return "guid:"+getNode().asNode().getLocalName();
	}

	public String togwString() {
		if (node.isLiteral())
			return node.asLiteral().getLexicalForm();
		else
			return this.class_name + "." + this.getLiteralChksum();
	}

	public String getLiteralChksum() {
		return literal_chksum;
	}

	public void setLiteralChksum(String chksum) {
		this.literal_chksum = chksum;
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

	public boolean isSameAs() {
		return sameAs;
	}

	public String getURI() {
		if(getNode().isLiteral())
			return getNode().asLiteral().getString();
		if(!getNode().isAnon())
			return getNode().asResource().getURI().toString();
		return URI;
	}

	public void setURI(String uRI) {
		URI = uRI;
	}

	public boolean isCollided() {
		return collided;
	}

	public void setCollided(boolean collided) {
		this.collided = collided;
	}

}
