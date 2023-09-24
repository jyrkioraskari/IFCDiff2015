package fi.ni.nodenamer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.util.StringChecksum;

public class OutEndBranchChecksummer {

    private Node getListParentNode(Node n, int inx) {
	if (inx <= 0)
	    return null;
	if (!n.getRDFClass_name().equalsIgnoreCase("rdf:list"))
	    return n;
	for (Connection c : n.getEdges_in()) {
	    return getListParentNode(c.getPointedNode(), inx - 1);
	}
	return null;
    }

    private boolean listLiterals(StringBuffer ret, Node n, int inx) {
	if (inx > 10)
	{
	    if(n.getRDFClass_name().equals("rdf:end"))
	      return true;
	    else
	      return false;
	}
	for (Connection c : n.getEdges_out()) {
	    if (c.getProperty().equals("rest"))
		if (!listLiterals(ret, c.getPointedNode(), inx + 1))
		    return false;
	    //Joku muu kuin literaali:
	    if (c.getProperty().equals("first"))
		return false;
	}
	for (Connection c : n.getEdges_literals()) {
	    if (c.getProperty().equals("first"))
		if (c.getPointedNode().isLiteral())
		    ret.append(" " + c.getPointedNode().node.asLiteral().getLexicalForm());
	}
	return true;
    }

    
    

    public void setOutEndBranchChecksums(List<Node> blank_nodes) {
	Set<Node> parentnodes = new HashSet<Node>();
	for (Node node : blank_nodes) {
	    if (node.getEdges_out().size() == 0) {
		// Alimman tason node: literaalien tarkistussumma
		
		List<String> literals = new ArrayList<String>();
		for (Connection c : node.getEdges_literals()) {
		    literals.add(c.getProperty() + "." + c.getPointedNode().node.asLiteral().getLexicalForm());
		}
		Collections.sort(literals);

		StringChecksum lchecksum = new StringChecksum();
		for (String s : literals) {
		    lchecksum.update(s);
		}
		if(node.isAnon())
		{
		  node.setEndbranch_chksum(lchecksum.getChecksumValue());
		  node.setEndbcksum(true);
		   // System.out.println("end lit "+node.class_name+" "+lchecksum.getChecksumValue());
		   for (Connection c : node.getEdges_in())
		   {
		    c.getPointedNode().incEndb_based_on_nodecount(node);
		    parentnodes.add(c.getPointedNode());
		   }
		}

	    } else {
		if (node.getEdges_out().size() == 1) {
		    String listcksum = null;
		    // Jos t‰m‰ ei ole lista, mutta t‰m‰ osoittaa listaa lasketaan literaalit+listaliteraalit
		    // Huomaa: Lista ei saa osoittaa muuhun kuin literaaliin: muuten fail: OK
		    if (!node.getRDFClass_name().equals("rdf:list")) {
			Connection lsc = node.getEdges_out().get(0);
			if (lsc.getPointedNode().getRDFClass_name().equals("rdf:list")) {
			    Node list = lsc.getPointedNode();
			    StringBuffer sb = new StringBuffer();
			    if (!listLiterals(sb, list, 0))
				continue; // If something else than literals, skip the whole node
			                  // Huomaa rajoitettu testaus!!
			    if (sb.length() > 0) {
				listcksum = new String(sb);
			    }
			    if (listcksum == null)
				continue;
			    List<String> literals = new ArrayList<String>();
			    for (Connection lic : node.getEdges_literals()) {
				literals.add(lic.getProperty() + "." + lic.getPointedNode().node.asLiteral().getLexicalForm());
			    }
			    Collections.sort(literals);

			    StringChecksum lchecksum = new StringChecksum();
			    for (String s : literals) {
				lchecksum.update(s);
			    }
			    lchecksum.update(listcksum);
			    // System.out.println("end list lit "+node.class_name+" "+lchecksum.getChecksumValue());
			    node.setEndbranch_chksum(lchecksum.getChecksumValue());
			    node.setEndbcksum(true);
			    for (Connection c : node.getEdges_in()) {
				Node to_ins = c.getPointedNode();
				if (to_ins.getRDFClass_name().equalsIgnoreCase("rdf:list")) {
				    to_ins = getListParentNode(to_ins, 10);   // Miksi haetaan listan lista? Liittyy cartesian pointteihin?
				    if (to_ins != null)
				    {
					to_ins.incEndb_based_on_nodecount(node);
					parentnodes.add(to_ins);
				    }
				} else
				{
				    to_ins.incEndb_based_on_nodecount(node);
				    parentnodes.add(to_ins);
				}
			    }
			}
		    }
		}

	    }
	}
	// K‰yd‰‰n l‰pi muutama askel
	for (int n = 0; n < 4; n++)
	    setOutEndUpperLevelChecksums(parentnodes);

    }
    
    /*
     * Oletetaan, ettei literaaleja, jos lˆytyy palautetaan false
     * Jos yli 10 askelta, palautetaan true
     * jos ei tarkistussummaa, palautetaan false
     * jos on, lis‰t‰‰n listaan.
     */
   private boolean listECksums(StringBuffer ret, Node n, int inx) {
	if (n.getEdges_literals().size() > 0)
	    return false;
	if (inx <= 0)  // pituus!! ent‰ rdf:end
	    return true;
	for (Connection c : n.getEdges_out()) {
	    if (c.getProperty().equals("rest"))
		if (!listECksums(ret, c.getPointedNode(), inx - 1))
		    return false;
	    if (c.getProperty().equals("first")) {
		if (!c.getPointedNode().isEndbcksum())
		    return false;
		else
		    ret.append(" " + c.getPointedNode().getEndbranch_chksum());
	    }
	}
	return true;
    }

    private void setOutEndUpperLevelChecksums(Set<Node> parentnodes) {
	Set<Node> added_modes = new HashSet<Node>();
	for (Node node : parentnodes) {
	    if (node.getRDFClass_name().equalsIgnoreCase("rdf:list"))
	    {
		//System.out.print(" list");  // Voidaanko sallia jatko?  
		continue;
	    }
	    boolean is_valid = true;
	    boolean has_list = false;
	    List<String> subcsums = new ArrayList<String>();
	    Node list = null;
	    for (Connection c : node.getEdges_out()) {
		if (!c.getPointedNode().isEndbcksum()) {
		    if (node.getEdges_out().size() == 1)
			if (c.getPointedNode().getRDFClass_name().equalsIgnoreCase("rdf:list")) {
			    has_list = true;
			    list = c.getPointedNode();   // Jos  out-edge-nodella ei ole checksummia, mutta se on lista, hyv‰ksyt‰‰n listana
			    continue; // Out of FOR loop
			}
		    is_valid = false;
		    // System.out.println("not valid: "+node.getClass_name());
		    break;
		}
		// T‰h‰n voidaan merkit‰ viitattu-leima.... kannattaako, koska haaroja voi olla useita ja mielell‰‰ onkin!
		subcsums.add(c.getPointedNode().getEndbranch_chksum());

	    }
	    if (!is_valid)
		continue;
	    StringBuffer csums = new StringBuffer();
	    if (has_list) {
		if (list == null)
		    continue;
		if (!listECksums(csums, list, 100)) {
		    // Lista, joka sis‰lt‰‰ kelvottomia arvoja: Sekalista esim. Oletetaan, ett‰ lista, jossa on vain tarkistussummia
		    //System.err.println("should not happen: " + node.getRDFClass_name());
		    continue;
		}
	    }
	    
	    // K‰sittele literaalit
	    List<String> literals = new ArrayList<String>();
	    for (Connection c : node.getEdges_literals()) {
		literals.add(c.getProperty() + "." + c.getPointedNode().node.asLiteral().getLexicalForm());
	    }
	    Collections.sort(literals);
	    Collections.sort(subcsums);

	    StringChecksum lchecksum = new StringChecksum();
	    for (String s : literals) {
		lchecksum.update(s);
	    }
	    for (String s : subcsums) {
		lchecksum.update(s);
	    }

	    if (has_list)
		lchecksum.update(new String(csums));

	    // System.out.println("end collect lit "+node.class_name+" "+lchecksum.getChecksumValue());
	    if(node.isAnon())
	    {
	       node.setEndbranch_chksum(lchecksum.getChecksumValue());
	       node.setEndbcksum(true);
		    for (Connection c : node.getEdges_in()) {
			Node to_ins = c.getPointedNode();
			if (to_ins.getRDFClass_name().equalsIgnoreCase("rdf:list")) {
			    to_ins = getListParentNode(to_ins, 10);
			    if (to_ins != null)
			    {
				to_ins.incEndb_based_on_nodecount(node);
				added_modes.add(to_ins);
			    }
			} else
			{
			    to_ins.incEndb_based_on_nodecount(node);
			    added_modes.add(to_ins);
			}
		    }
	    }
	}
	parentnodes.clear();  // poistetaan k‰sitellyt, ettei k‰sitell‰ kahdesti
	for (Node n : added_modes)
	    parentnodes.add(n);
    }

}
