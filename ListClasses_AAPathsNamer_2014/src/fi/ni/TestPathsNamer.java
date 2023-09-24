package fi.ni;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;

public class TestPathsNamer {
    
    static public void testrun() {
	
	GenStats gs1 = new GenStats(75);
	gs1.analyze("c:/2014_testdata/a_testset/A1.ifc", "IFC");
	gs1.makeUnique();

	GenStats gs2 = new GenStats(75);
	gs2.analyze("c:/2014_testdata/a_testset/A2.ifc", "IFC");
	gs2.makeUnique();
	System.out.println("AA removed added");
	
	Set<String> statements1=new HashSet<String>();
	Set<String> statements2=new HashSet<String>();
	
	Map<String,Node> statements_map1=new HashMap<String,Node>();   // Statement, Subject node
	Map<String,Node> statements_map2=new HashMap<String,Node>();
	
	for (Node n1 : gs1.getNodes()) {	
		   if(!n1.isLiteral())
		   {
			   Set<String> statements=listStatements(n1);
			   statements1.addAll(statements);
			   for(String s:statements)
				   statements_map1.put(s, n1);
		   }
	}
	for (Node n1 : gs2.getNodes()) {
		   if(!n1.isLiteral())
		   {
			   Set<String> statements=listStatements(n1);
			   statements2.addAll(statements);
			   for(String s:statements)
				   statements_map2.put(s, n1);
		   }
	}

	
	Map<String,Integer> add_classes=new HashMap<String,Integer>();
	Map<String,Integer> remove_classes=new HashMap<String,Integer>();
	
	System.out.println("statements 1: "+statements1.size());
	System.out.println("statements 2: "+statements2.size());
	int removed=0;
	for(String s1:statements1)
	{
		if(!statements2.contains(s1))
		{
			removed++;
			Node n=statements_map1.get(s1);
			if(n!=null)
			{
				Integer count=remove_classes.get(n.getRDFClass_name());
				if(count==null)
					count=0;
				remove_classes.put(n.getRDFClass_name(),count+1);
			}
			//if(removed<50)
				//System.out.println(s1);
		}
	}
	System.out.println("removed: "+removed);

	for(String sc:remove_classes.keySet())
		System.out.println("    removed class: "+sc+" "+remove_classes.get(sc));
		
	
	int added=0;
	for(String s1:statements2)
	{
		if(!statements1.contains(s1))
		{
			added++;
			Node n=statements_map2.get(s1);
			if(n!=null)
			{
				Integer count=add_classes.get(n.getRDFClass_name());
				if(count==null)
					count=0;
				add_classes.put(n.getRDFClass_name(),count+1);
			}
			//if(added<50)
				//System.out.println(s1);

		}
	}
	System.out.println("added: "+added);

	for(String sc:add_classes.keySet())
		System.out.println("    added class: "+sc+" "+add_classes.get(sc));

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

    public static void main(String[] args) {
	testrun();
    }

}
