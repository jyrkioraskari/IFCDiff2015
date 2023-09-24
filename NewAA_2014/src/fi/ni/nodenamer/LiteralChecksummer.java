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

	public void setliteralChecksums(Set<Node> nodes, ClassLiteralCksumBag class_chksums_bag) {
		for (Node node : nodes) {
			if (node.isLiteral()) {
				continue;
			}

			if (!node.isAnon()) {
				StringChecksum checksum = new StringChecksum();
				checksum.update(node.getNode().asResource().getURI().toString());
				node.setLiteralChksum(checksum.getChecksumValue());
				if (node.getRDFClass_name().equals("rdf:nil"))
					node.setLiteral_prob(1);
				else
					node.setLiteral_prob(0.001); // TODO keksitty arvo!! Oletus
													// harvinaisuudesta
				class_chksums_bag.add(node.getRDFClass_name(), checksum.getChecksumValue(), node.getEdges_out().size() + node.getEdges_in().size());
				lchecksums.add(checksum.getChecksumValue());
			} else {
				setLChecksum4AnonNode(class_chksums_bag, node);
			}
		}

	}

	public String AlgSign(Node node) {
		List<String> l_in = new ArrayList<String>();
		List<String> l_class = new ArrayList<String>();
		List<String> l_out = new ArrayList<String>();

		l_class.add("type" + node.getRDFClass_name());

		// IN: OSOITTAVAT LUOKAT tyypin mukaan
		/*List<Connection> cons_in = node.getEdges_in();
		for (Connection c : cons_in) {
			if (!c.getPointedNode().isAnon())
				l_in.add(c.getPointedNode().getNode().asResource().getURI().toString() + c.getProperty());
			else
				l_in.add("&" + c.getProperty());
		}*/

		// LITERAALIT
		List<Connection> cons_lit = node.getEdges_literals();
		for (Connection c : cons_lit) {
			l_out.add(c.getProperty() + c.getPointedNode().node.asLiteral().getLexicalForm());
		}

		// OUT: OSOITETUT LUOKAT tyypin mukaan
		/*List<Connection> cons_out = node.getEdges_out();
		for (Connection c : cons_out) {
			if (!c.getPointedNode().isAnon())
				l_out.add(c.getProperty() + c.getPointedNode().getNode().asResource().getURI().toString());
			else
				l_out.add(c.getProperty() + "&");
		}*/

		Collections.sort(l_in);
		Collections.sort(l_class);
		Collections.sort(l_out);

		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : l_in) {
			if (first) {
				sb.append(s);
				first = false;
			} else
				sb.append("*" + s);

		}
		for (String s : l_class) {
			if (first) {
				sb.append(s);
				first = false;
			} else
				sb.append("*" + s);

		}
		for (String s : l_out) {
			if (first) {
				sb.append(s);
				first = false;
			} else
				sb.append("*" + s);

		}
		return sb.toString();
	}

	private void setLChecksum4AnonNode(ClassLiteralCksumBag class_chksums_bag, Node node) {
		String signature = AlgSign(node);
		StringChecksum lchecksum = new StringChecksum();
		lchecksum.update(signature);

		node.setLiteralChksum(lchecksum.getChecksumValue());
		node.setPchksum(lchecksum.getChecksumValue());
		class_chksums_bag.add(node.getRDFClass_name(), lchecksum.getChecksumValue(), node.getEdges_out().size() + node.getEdges_in().size());
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
					lprob = class_chksums_bag.test(node.getRDFClass_name(), node.getPchksum());
				}
				node.setLiteral_prob(lprob);
			}
		}
	}

}
