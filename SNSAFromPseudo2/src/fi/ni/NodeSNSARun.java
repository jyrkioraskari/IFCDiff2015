package fi.ni;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fi.ni.model.datastructure.Connection;
import fi.ni.model.datastructure.Node;


public class NodeSNSARun extends Thread {
	static Map<String,Node> statement_set1=new HashMap<String,Node>();
	static Map<String,Node> statement_set2=new HashMap<String,Node>();
	final String filename;
	final String datatype;
	NodeSNSA run = null;

	public NodeSNSARun(String filename, String type) {
		this.filename = filename;
		this.datatype = type;
	}

	public void run() {
		run = new NodeSNSA();
		run.analyzeSNSA(filename, datatype);
		
		
	}

	public NodeSNSA getRun() {
		return run;
	}

	static public void test(String filename1,String filename2, String type) {
		NodeSNSARun tr1 = new NodeSNSARun(filename1, type);
		try {
			tr1.start();
			tr1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		NodeSNSARun tr2 = new NodeSNSARun(filename2, type);
		try {
			tr2.start();
			tr2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<String> statements1=new LinkedList<String>();
		List<String> statements2=new LinkedList<String>();

		for (Node n1 : tr1.getRun().getNodes()) {
			List<String> stms=listStatements(n1);
			statements1.addAll(stms);
			for(String s:stms)
				statement_set1.put(s, n1);
	    }
	    for (Node n1 : tr2.getRun().getNodes()) {
			List<String> stms=listStatements(n1);
			statements2.addAll(stms);
			for(String s:stms)
				statement_set2.put(s, n1);
	    }

		for(String s1:statements1)
		{
			boolean found=false;
			for(String s2:statements2)
			{
				if(s1.equals(s2))
					found=true;
			}	
			if(!found)
				statement_set1.get(s1).setChanged(true);
		}

		for(String s1:statements2)
		{
			boolean found=false;
			for(String s2:statements1)
			{
				if(s1.equals(s2))
					found=true;
			}	
			if(!found)
				statement_set2.get(s1).setChanged(true);
		}
		tr1.getRun().reAnalyzeSNSA();
		tr2.getRun().reAnalyzeSNSA();
		statements1.clear();
		statements2.clear();

		for (Node n1 : tr1.getRun().getNodes()) {
			List<String> stms=listStatements(n1);
			statements1.addAll(stms);
			for(String s:stms)
				statement_set1.put(s, n1);
	    }
	    for (Node n1 : tr2.getRun().getNodes()) {
			List<String> stms=listStatements(n1);
			statements2.addAll(stms);
			for(String s:stms)
				statement_set2.put(s, n1);
	    }


	    System.out.println("statements 1: "+statements1.size());
		System.out.println("statements 2: "+statements2.size());
		int removed=0;
		for(String s1:statements1)
		{
			boolean found=false;
			for(String s2:statements2)
			{
				if(s1.equals(s2))
					found=true;
			}	
			if(!found)
				removed++;
		}
		System.out.println("removed: "+removed);

		int added=0;
		for(String s1:statements2)
		{
			boolean found=false;
			for(String s2:statements1)
			{
				if(s1.equals(s2))
					found=true;
			}	
			if(!found)
				added++;
		}
		System.out.println("added: "+added);
		
	}
	
	static public List<String> listStatements(Node node)
	{
	    	List<String> statements = new LinkedList<String>();
	    	
	    	
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

	static public void testset() {
		// test("HelloWall.ifc");
		// test("c:/2014_testdata/tiny.n3");
		test("c:/2014_testdata/drum_a.ttl","c:/2014_testdata/drum_b.ttl", "Turtle");
		//test("c:/2014_testdata/persons2A.n3","c:/2014_testdata/persons2B.n3", "N3");
		//test("c:/2014_testdata/persons1.n3","N3");
	}

	public static void main(String[] args) {

		testset();
		System.out.println("Done");
	}
}
