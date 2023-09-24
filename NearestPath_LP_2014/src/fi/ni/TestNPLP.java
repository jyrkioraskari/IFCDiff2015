package fi.ni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.RDFHandler;
import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.stats.ClassLiteralCksumBag;


public class TestNPLP {
    
    static public void testrun() {
    ClassLiteralCksumBag lpbag=new ClassLiteralCksumBag();
    get_classliterals(lpbag, "c:/2014_testdata/SMC_Rakennus.ifc", "IFC");
    get_classliterals(lpbag,  "c:/2014_testdata/SMC_RakennusMuutettu.ifc", "IFC");
    	
	Namer gs1 = new Namer(lpbag,5);
	gs1.analyze("c:/2014_testdata/SMC_Rakennus.ifc", "IFC");
	gs1.makeUnique();


	Namer gs2 = new Namer(lpbag, 5);
	gs2.analyze("c:/2014_testdata/SMC_RakennusMuutettu.ifc", "IFC");
	gs2.makeUnique();
	System.out.println("AA removed added");
	
	Set<String> statements1=new HashSet<String>();
	Set<String> statements2=new HashSet<String>();
	
	for (Node n1 : gs1.getNodes()) {	
		   if(!n1.isLiteral())
			statements1.addAll(listStatements(n1));							
	}
	for (Node n1 : gs2.getNodes()) {
		   if(!n1.isLiteral())
			statements2.addAll(listStatements(n1));				
	}

	System.out.println("statements 1: "+statements1.size());
	System.out.println("statements 2: "+statements2.size());
	int removed=0;
	for(String s:statements1)
	{
		if(!statements2.contains(s))
		{
			removed++;
			if(removed<15)
				System.out.println(s);
		}
	}
	System.out.println("removed: "+removed);

	int added=0;
	for(String s:statements2)
	{
		if(!statements1.contains(s))
		{
			added++;
			if(added<15)
				System.out.println(s);

		}
	}
	System.out.println("added: "+added);
  

    }

    static public Set<String> listStatements(Node node)
	{
	    	Set<String> statements = new HashSet<String>();
	    	
	    	//statements.add(node.getURI()+" type "+node.getRDFClass_name());
	    	
	    	// LITERAALIT
	    	List<Connection> cons_lit = node.getEdges_literals();
	    	for (Connection c : cons_lit) {
	    	    statements.add(node.getURI()+" "+c.getProperty() +" "+ c.getPointedNode().node.asLiteral().getLexicalForm());
	    	}

	    	// OUT: OSOITETUT LUOKAT tyypin mukaan
	    	List<Connection> cons_out = node.getEdges_out();
	    	for (Connection c : cons_out) {
	    	    statements.add(node.getURI()+" "+c.getProperty() +" "+ c.getPointedNode().getURI());
	    	}
	    	
	    	return statements;
	}

	static public void get_classliterals(ClassLiteralCksumBag bag, String filename, String datatype) {
		List<Node> blank_nodes = new ArrayList<Node>();
		Set<Node> nodes = new HashSet<Node>();

		nodes.clear();
		RDFHandler ifch = new RDFHandler();
		ifch.handleRDF(filename, datatype, nodes, blank_nodes);
		for (Node n : nodes) {
			if (n.getRDFClass_name().equalsIgnoreCase("IfcPropertySet"))
				n.setOverwriteAnon(true);

		}
		for (Node n : nodes) {
			if (!n.isLiteral())
				if (n.isAnon())
				{
					List<Connection> cons_lit = n.getEdges_literals();
			    	for (Connection c : cons_lit) {
			    	    bag.add(n.getRDFClass_name(),c.getProperty() +"."+ c.getPointedNode().node.asLiteral().getLexicalForm());
			    	}
				}

		}
	}


    public static void main(String[] args) {
	testrun();
    }

}
