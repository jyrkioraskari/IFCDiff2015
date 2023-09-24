package fi.ni;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.ni.model.datastructure.Connection;
import fi.ni.model.datastructure.Node;
import fi.ni.util.StringChecksum;

public class NodeAlgSignRun extends Thread {
	final String filename;
	final String datatype;
	NodeAlgSign run = null;

	public NodeAlgSignRun(String filename, String type) {
		this.filename = filename;
		this.datatype = type;
	}

	public void run() {
		run = new NodeAlgSign();
		run.analyzeAlgSign(filename, datatype);
	}

	public NodeAlgSign getRun() {
		return run;
	}
	
	// Muistinkulutuksen hillitsemiseksi
	static void getNodes1(String filename1, String type, Set<String> statements1)
	{
		NodeAlgSignRun tr1 = new NodeAlgSignRun(filename1, type);
		try {
			tr1.start();
			tr1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Node n1 : tr1.getRun().getNodes()) 
		{
			   if(!n1.isLiteral())
				statements1.addAll(listStatements(n1));							
		}

	}

	static void getNodes2(String filename2, String type,Set<String> statements2)
	{
		NodeAlgSignRun tr2 = new NodeAlgSignRun(filename2, type);
		try {
			tr2.start();
			tr2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Node n1 : tr2.getRun().getNodes()) {
			  if(!n1.isLiteral())
				statements2.addAll(listStatements(n1));				
		}
		
	}

	static public void test(String filename1,String filename2, String type) {

		
		Set<String> statements1=new HashSet<String>();
		Set<String> statements2=new HashSet<String>();
		getNodes1(filename1, type, statements1);
		System.gc();
		getNodes2(filename2, type, statements2);
		System.gc();
		
		System.out.println("statements 1: "+statements1.size());
		System.out.println("statements 2: "+statements2.size());
		int removed=0;
		for(String s1:statements1)
		{
			if(!statements2.contains(s1))
				removed++;
		}
		System.out.println("removed: "+removed);

		int added=0;
		for(String s1:statements2)
		{
			if(!statements1.contains(s1))
				added++;
		}
		System.out.println("added: "+added);
		
	}
	static public Set<String> listStatements(Node node)
	{
	    	Set<String> statements = new HashSet<String>();
	    	
    		StringChecksum sc1=new StringChecksum();
    		sc1.update(node.getURI()+" type "+node.getRDFClass_name());
    	    statements.add(sc1.getChecksumValue());

	    	//TODO remove checksum!
	    	// LITERAALIT
	    	List<Connection> cons_lit = node.getEdges_literals();
	    	for (Connection c : cons_lit) {
	    		StringChecksum sc=new StringChecksum();
	    		sc.update(node.getURI()+" "+c.getProperty() +" "+ c.getPointedNode().node.asLiteral().getLexicalForm());	    		
	    	    statements.add(sc.getChecksumValue());
	    	}

	    	// OUT: OSOITETUT LUOKAT tyypin mukaan
	    	List<Connection> cons_out = node.getEdges_out();
	    	for (Connection c : cons_out) {
	    		StringChecksum sc=new StringChecksum();
	    		sc.update(node.getURI()+" "+c.getProperty() +" "+ c.getPointedNode().getURI());	    		
	    	    statements.add(sc.getChecksumValue());
	    	}
	    	
	    	return statements;
	}

	static public Set<String> listStatementsORG(Node node)
	{
	    	Set<String> statements = new HashSet<String>();
	    	
	    	statements.add(node.getURI()+" type "+node.getRDFClass_name());
	    	//TODO remove checksum!
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
	   // test("c:/2014_testdata/Swedish1/mergeFile1.rdf","c:/2014_testdata/Swedish1/mergeFile1.rdf", "RDF/XML");
		//test("c:/2014_testdata/drum_a.ttl","c:/2014_testdata/drum_b.ttl", "Turtle");
		//test("c:/2014_testdata/o/SMC_Rakennus_o.ifc","c:/2014_testdata/o/SMC_RakennusMuutettu_o.ifc", "IFC");
		test("c:/2014_testdata/a_testset/A1.ifc","c:/2014_testdata/a_testset/A2.ifc", "IFC");
		//test("c:/2014_testdata/rm/nogeomo_sms.ifc","c:/2014_testdata/rm/nogeomo_sms2.ifc", "IFC");
		//test("c:/2014_testdata/a/NOGEOM_Amiraali#20140428-135003_optimized.ifc","c:/2014_testdata/a/NOGEOM_Amiraali#20140428-141008_optimized.ifc", "IFC");
	
		//test("c:/2014_testdata/drumoA.ttl","c:/2014_testdata/drumoB.ttl", "Turtle");
		//test("c:/2014_testdata/persons5A.n3","c:/2014_testdata/persons5B.n3", "N3");
		//test("c:/2014_testdata/persons1.n3","N3");

	}

	public static void main(String[] args) {

		testset();
		System.out.println("Done");
	}
}
