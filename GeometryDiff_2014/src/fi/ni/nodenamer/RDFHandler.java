package fi.ni.nodenamer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;

public class RDFHandler {

	Map<String, Set<String>> resclass = new HashMap<String, Set<String>>();
	OntModel model;

	public Node handleRDF(String filename, String datatype, Set<Node> nodes, List<Node> blank_nodes) {
		try {
			if (datatype.equals("IFC"))
				model = readIFC(filename);
			else
				model = readRDF(filename, datatype);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("statements:" + model.listStatements().toList().size());
		createResClassMap(model); // Map resource names to class names
		createNodeMap(model, nodes, blank_nodes);
		return null;
	}

	private boolean isFiltered(String txt) {
		if (txt.equals("line_number"))
			return true;
		if (txt.equals("globalId"))
			return true;
		return false;
	}

	public long parseLong(String txt) {
		long ret = -1;
		try {
			int ix = txt.indexOf('^');
			if (ix > 0)
				ret = Long.parseLong(txt.substring(0, ix)); // TODO tarkista
															// indeksit!!
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	private void createNodeMap(OntModel model, Set<Node> nodes, List<Node> blank_nodes) {
		List<RDFNode> unknown_objects = new ArrayList<RDFNode>();
		Map<String, Node> nodekeymap = new HashMap<String, Node>();
		StmtIterator iter1 = model.listStatements();

		while (iter1.hasNext()) {
			String s_class_name = null;
			Statement stmt = iter1.nextStatement(); // get next statement

			if (stmt.getPredicate().getLocalName().equals("type")) {
				s_class_name = stmt.getObject().toString();

				Node ns = new Node(stmt.getSubject(), s_class_name);

				nodekeymap.put(stmt.getSubject() + "", ns);
				if (ns.getRDFClass_name().equals("IfcOwnerHistory"))
					continue;
				nodes.add(ns);

				if (ns.isAnon())
					blank_nodes.add(ns);
				continue;
			}
		}
		iter1 = model.listStatements();
		while (iter1.hasNext()) {
			String s_class_name = null;
			Statement stmt = iter1.nextStatement(); // get next statement

			if (stmt.getPredicate().getURI().contains("subClassOf"))
				continue;
			if (stmt.getPredicate().getLocalName().equals("type"))
				continue;

			if (stmt.getPredicate().getURI().contains("domain"))
				continue;

			Node subject = nodekeymap.get(stmt.getSubject() + "");

			if (subject != null) {
				s_class_name = subject.getRDFClass_name();
			} else {
				Set<String> s_oclasses = resclass.get(stmt.getSubject() + "");
				if (s_oclasses != null) {
					for (String octxt : s_oclasses) {
						if (octxt != null) {
							s_class_name = octxt;
							// break;
						}
					}
				}
				if (stmt.getPredicate().getURI().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")) {
					s_class_name = "rdf:list";
				}
				if (stmt.getPredicate().getURI().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")) {
					s_class_name = "rdf:list";
				}

				Node ns = new Node(stmt.getSubject(), s_class_name);

				nodekeymap.put(stmt.getSubject() + "", ns);
				if (ns.getRDFClass_name().equals("IfcOwnerHistory"))
					continue;
				nodes.add(ns);

				if (ns.isAnon())
					blank_nodes.add(ns);
			}

			Node object = nodekeymap.get(stmt.getObject() + "");
			if (object == null) {
				Set<String> o_oclasses = resclass.get(stmt.getObject() + "");
				String o_class_name = null;
				if (o_oclasses != null) {
					for (String octxt : o_oclasses) {
						o_class_name = octxt;
						break;
					}
				}

				if (stmt.getPredicate().getURI().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")) {
					o_class_name = "rdf:list";
				}

				if (stmt.getObject().isURIResource()) {
					if (stmt.getObject().asResource().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
						o_class_name = "rdf:nil";
				}
				if (stmt.getPredicate().getURI().contains("sameAs")) {
					if (s_class_name != null) {
						o_class_name = s_class_name;
					}
				}

				if (o_class_name == null) {
					o_class_name = null;
					if (stmt.getObject().isLiteral()) {
						o_class_name = stmt.getObject().asLiteral().getDatatypeURI();
						if (o_class_name == null)
							o_class_name = "String";
					} else {
						unknown_objects.add(stmt.getObject());
					}
				}

				if (o_class_name != null) {
					Node no = null;
					if (stmt.getPredicate().getURI().contains("sameAs"))
						no = new Node(stmt.getSubject(), o_class_name, true);
					else
						no = new Node(stmt.getObject(), o_class_name);
					nodekeymap.put(stmt.getObject() + "", no);
					if (no.getRDFClass_name().equals("IfcOwnerHistory"))
						continue;
					nodes.add(no);
					if (no.isAnon())
						blank_nodes.add(no);
				}
			}

		}
		for (RDFNode node : unknown_objects) {
			// There aren't any, but just in case
			if (nodekeymap.get(node + "") == null) {
				Node no = new Node(node, "unknown");
				nodekeymap.put(node + "", no);
			}
		}

		StmtIterator iter = model.listStatements();

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement

			if (isFiltered(stmt.getPredicate().getLocalName()))
				continue;

			if (stmt.getPredicate().getURI().contains("subClassOf"))
				continue;
			if (stmt.getPredicate().getURI().contains("type"))
				continue;
			if (stmt.getPredicate().getURI().contains("domain"))
				continue;

			if (stmt.getPredicate().getURI().contains("ownerHistory"))
				continue;

			Node subject = nodekeymap.get(stmt.getSubject() + "");
			Node object = nodekeymap.get(stmt.getObject() + "");
			if ((subject != null) && (object != null)) {
				if (subject.getRDFClass_name().equals("IfcOwnerHistory"))
					continue;
				if (object.getRDFClass_name().equals("IfcOwnerHistory"))
					continue;

				if (object.isLiteral())
					subject.addLiteralConnection(new Connection(stmt.getPredicate().getLocalName(), object));
				else
					subject.addOUTConnection(new Connection(stmt.getPredicate().getLocalName(), object));
				if (!object.getRDFClass_name().equals("rdf:nil"))
					object.addINConnection(new Connection(stmt.getPredicate().getLocalName(), subject));

			} else {
				System.err.println(stmt);
				System.err.println("s: " + subject + " " + stmt.getSubject());
				System.err.println("o: " + object + " " + stmt.getObject() + " " + stmt.getObject().isLiteral());
			}
		}
	}

	public void createResClassMap(OntModel model) {

		for (ExtendedIterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass c = (OntClass) i.next();
			for (ExtendedIterator<? extends OntResource> ii = c.listInstances(); ii.hasNext();) {
				String key = ii.next() + "";
				Set<String> set = resclass.get(key);
				if (set == null) {
					set = new HashSet<String>();
					resclass.put(key, set);

				}
				set.add(c.getLocalName());
			}
		}
	}

	public OntModel readIFC(String filename) throws IOException {

		ByteArrayOutputStream strout = new ByteArrayOutputStream();
		BufferedWriter log = new BufferedWriter(new OutputStreamWriter(strout));
		ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_Final.exp");
		er.outputRDFS(log);
		IFC_ClassModel m1 = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "r1");
		m1.listRDF(log);
		InputStream is = new ByteArrayInputStream(strout.toString().getBytes());

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);

		model.read(is, null, "N3");
		return model;

	}

	public OntModel readRDF(String filename, String type) {
		model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			throw new IllegalArgumentException("File: " + filename + " not found");
		}

		model.read(in, null, type);
		return model;
	}

}
