package fi.ni;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;



public class GeometryNamer {
    
    static public void testrun() {
	GNamer gs1 = new GNamer(5);
	gs1.analyze("c:/2014_testdata/rm/nogeomo_sms.ifc", "IFC");
	gs1.makeUnique();
	
	GNamer gs2 = new GNamer(5);
	gs2.analyze("c:/2014_testdata/rm/nogeomo_sms2.ifc", "IFC");
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
	for(String s1:statements1)
	{
		if(!statements2.contains(s1))
		{
			removed++;
			if(removed<10)
				System.out.println(s1);
		}
	}
	System.out.println("removed: "+removed);

	int added=0;
	for(String s1:statements2)
	{
		if(!statements1.contains(s1))
		{
			added++;
			if(added<10)
				System.out.println(s1);

		}
	}
	System.out.println("added: "+added);

    }

	static public Set<String> listStatements(Node node)
	{
	    	Set<String> statements = new HashSet<String>();
	    	
	    	statements.add(node.getURI()+" type "+node.getRDFClass_name());
	    	
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

    
    public static void main(String[] args) {
	testrun();
    }

}
