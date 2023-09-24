package fi.ni.nodenamer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.util.StringChecksum;

public class LiteralChecksummer {

	public LiteralChecksummer() {
		super();
	}

	public void setliteralChecksums(Set<Node> nodes) {
		for (Node node : nodes) {
			if (node.isLiteral()) {
				continue;
			}

			if (node.isAnon())
				setLChecksum4AnonNode(node);
			else
				node.setLiteralChksum(node.getNode().asResource().getLocalName());
		}

	}

	private String literalSet(Node node) {
		List<String> l_class = new ArrayList<String>();
		List<String> l_out = new ArrayList<String>();

		l_class.add("type" + node.getRDFClass_name());

		List<Connection> cons_lit = node.getEdges_literals();
		for (Connection c : cons_lit) {
			l_out.add(c.getProperty() + c.getPointedNode().node.asLiteral().getLexicalForm());
		}

		Collections.sort(l_class);
		Collections.sort(l_out);

		StringBuffer sb = new StringBuffer();
		boolean first = true;

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

	private void setLChecksum4AnonNode(Node node) {
		String signature = literalSet(node);
		StringChecksum lchecksum = new StringChecksum();
		lchecksum.update(signature);

		node.setLiteralChksum("\"" + signature + "\"");
	}

}
