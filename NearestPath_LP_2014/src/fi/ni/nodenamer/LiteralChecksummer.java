package fi.ni.nodenamer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.stats.ClassLiteralCksumBag;
import fi.ni.util.StringChecksum;

public class LiteralChecksummer {

	final ClassLiteralCksumBag lpbag;

	public LiteralChecksummer(ClassLiteralCksumBag lpbag) {
		super();
		this.lpbag = lpbag;
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
		boolean hasIRI=false;
		List<String> l_class = new ArrayList<String>();
		List<String> l_out = new ArrayList<String>();

		l_class.add("type" + node.getRDFClass_name());

		List<Connection> cons_lit = node.getEdges_literals();
		for (Connection c : cons_lit) {
			double pval = lpbag.test(node.getRDFClass_name(), c.getProperty() + "." + c.getPointedNode().node.asLiteral().getLexicalForm());
			if (pval == 0) {
				l_out.add(c.getProperty() + c.getPointedNode().node.asLiteral().getLexicalForm());
			}
		}
		
        if(l_out.size()==0)
		{
			for (Connection c : cons_lit) {
				l_out.add(c.getProperty() + c.getPointedNode().node.asLiteral().getLexicalForm());
			}

		}
        else
        	hasIRI=true;

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
		if(hasIRI)
			node.setURI(sb.toString());
		return sb.toString();
	}

	private void setLChecksum4AnonNode(Node node) {
		String signature = literalSet(node);
		StringChecksum lchecksum = new StringChecksum();
		lchecksum.update(signature);

		node.setLiteralChksum("\"" + signature + "\"");
	}

}
