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
					node.setLiteral_prob(0.2);
				else
					node.setLiteral_prob(0); //TODO
				class_chksums_bag.add(node.getRDFClass_name(), checksum.getChecksumValue(), node.getEdges_out().size() + node.getEdges_in().size());
				lchecksums.add(checksum.getChecksumValue());
			} else {
				setLChecksum4AnonNode(class_chksums_bag, node);
			}
		}

	}

	public String AlgSign(Node node) {
		List<String> l_class = new ArrayList<String>();
		List<String> l_out = new ArrayList<String>();

		l_class.add("type" + node.getRDFClass_name());

		// LITERAALIT
		List<Connection> cons_lit = node.getEdges_literals();
		for (Connection c : cons_lit) {
			l_out.add(c.getProperty() + c.getPointedNode().node.asLiteral().getLexicalForm());
		}

		Collections.sort(l_class);

		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : l_class) {
			if (first) {
				sb.append(s);
				first = false;
			} else
				sb.append("*" + s);

		}
		first = true;
		for (String s : l_out) {
			if (first) {
				sb.append(s);
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
