package fi.ni.nodenamer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.stats.ClassLiteralCksumBag;
import fi.ni.util.StringChecksum;

public class LiteralChecksummer {

    
    
    public Set<String> lchecksums = new HashSet<String>();
    
    private String getListParent(Node n, int inx, int max) {
  	if (inx <= 0)
  	    return null;
  	if (!n.getRDFClass_name().equalsIgnoreCase("rdf:list"))
  	    return n.getRDFClass_name() + "." + (max - inx);

  	for (Connection c : n.getEdges_in()) {
  	    return getListParent(c.getPointedNode(), inx - 1, max);
  	}
  	return null;
      }
      

    public void setliteralChecksums(Set<Node> nodes, ClassLiteralCksumBag class_chksums_bag ) {
	for (Node node : nodes) {
	    if (node.isLiteral()) {
		continue;
	    }

	    if (!node.isAnon()) {
		StringChecksum checksum = new StringChecksum();
		checksum.update(node.getNode().asResource().getURI().toString());
		node.setLiteralChksum(checksum.getChecksumValue());
		if(node.getRDFClass_name().equals("rdf:nil"))
		     node.setLiteral_prob(1);
		else
		     node.setLiteral_prob(0.00000001); //TODO keksitty arvo!! Oletus harvinaisuudesta
		class_chksums_bag.add(node.getRDFClass_name(), checksum.getChecksumValue(), node.getEdges_out().size() + node.getEdges_in().size());
		lchecksums.add(checksum.getChecksumValue());
	    } else {
		setLChecksum4AnonNode(class_chksums_bag, node);
	    }
	}
	
	
    }

    private void setLChecksum4AnonNode(ClassLiteralCksumBag class_chksums_bag, Node node) {
	List<String> l_in = new ArrayList<String>();
	List<String> l_class = new ArrayList<String>();
	List<String> l_out = new ArrayList<String>();
	List<String> pliterals = new ArrayList<String>();
	l_class.add(node.getRDFClass_name());
	pliterals.add(node.getRDFClass_name());
	
	// Listan isäntäsolmun luokka!
	if (node.getRDFClass_name().equals("rdf:list")) {
	    String lp = getListParent(node, 200, 200);
	    if (lp != null) {
		l_class.add("lp=" + lp);
		pliterals.add("lp=" + lp);
		node.setParent_class_name(lp);
	    }
	}

	List<Connection> cons_litp = node.getEdges_literals();
	for (Connection c : cons_litp) {
	    pliterals.add(c.getProperty() + "." + c.getPointedNode().node.asLiteral().getLexicalForm());
	}

	
	// IN: OSOITTAVAT LUOKAT tyypin mukaan
	List<Connection> cons_in = node.getEdges_in();
	for (Connection c : cons_in) {
	    if(!c.getPointedNode().isAnon())
	      l_in.add(c.getProperty() + "." + c.getPointedNode().getNode().asResource().getURI().toString());
	    else	
	      l_in.add(c.getProperty() + ".&");
	}

	// LITERAALIT
	List<Connection> cons_lit = node.getEdges_literals();
	for (Connection c : cons_lit) {
	    l_out.add(c.getProperty() + "." + c.getPointedNode().node.asLiteral().getLexicalForm());
	}

	// OUT: OSOITETUT LUOKAT tyypin mukaan
	List<Connection> cons_out = node.getEdges_out();
	for (Connection c : cons_out) {
	    if(!c.getPointedNode().isAnon())
		      l_out.add(c.getProperty() + "." + c.getPointedNode().getNode().asResource().getURI().toString());
		    else	
		      l_out.add(c.getProperty() + ".&");
	}
	
	
	Collections.sort(l_in);
	Collections.sort(l_class);
	Collections.sort(l_out);

	StringChecksum lchecksum = new StringChecksum();
	for (String s : l_in) {
	    lchecksum.update(s);
	    node.cchksumitems.add(s);
	}
	for (String s : l_class) {
	    lchecksum.update(s);
	    node.cchksumitems.add(s);
	}
	for (String s : l_out) {
	    lchecksum.update(s);
	    node.cchksumitems.add(s);
	}
	Collections.sort(pliterals);

	StringChecksum pchecksum = new StringChecksum();
	for (String s : pliterals) {
	    pchecksum.update(s);
	}
	
	node.setLiteralChksum(lchecksum.getChecksumValue());
	node.setPchksum(pchecksum.getChecksumValue());
	if (node.getParent_class_name() != null)
	    class_chksums_bag.add(node.getParent_class_name(), pchecksum.getChecksumValue(), node.getEdges_out().size() + node.getEdges_in().size());
	else
	    class_chksums_bag.add(node.getRDFClass_name(), pchecksum.getChecksumValue(), node.getEdges_out().size() + node.getEdges_in().size());
	lchecksums.add(lchecksum.getChecksumValue());
    }


    public void setNodeLiteralProbabilities(Set<Node> nodes, ClassLiteralCksumBag class_chksums_bag) {
	for (Node node : nodes) {
	    if (node.isLiteral())
		continue;
	    if (node.isAnon()) {
		double lprob;
		if (node.getEdges_literals().size() == 0)
		    lprob = 1;
		else {
		    if (node.getParent_class_name() != null)
			lprob = class_chksums_bag.test(node.getParent_class_name(), node.getPchksum());
		    else
			lprob = class_chksums_bag.test(node.getRDFClass_name(), node.getPchksum());
		}
		node.setLiteral_prob(lprob);
	    }
	}
    }

}
