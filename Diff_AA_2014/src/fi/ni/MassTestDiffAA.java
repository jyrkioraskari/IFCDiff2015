package fi.ni;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;

import fi.ni.nodenamer.datastructure.Node;

public class MassTestDiffAA {

    static BufferedWriter lokifile;
    static FileWriter fw = null;

    static public void start() {

	try {
	    fw = new FileWriter("C:\\M/tulokset_2014/mars_2014_diff_test_nogeom.txt", true);
	    lokifile = new BufferedWriter(fw);
	    if (lokifile == null) {
		System.err.println("No file!!!");
		System.exit(1);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public HashSet<String> getGIDList(GenStats gs) {
	HashSet<String> ret=new HashSet<String>();
	for(Node x:gs.nodes)
	{
	    if(x.isLiteral())
		continue;
	    if(!x.isAnon())
	    {
		String guid=x.getNode().asResource().getLocalName();
		if(guid.startsWith("GUID"))
		{
		  ret.add(guid);		}
	    }
	}
	return ret;
    }

    static public void testrun() {
	println(" ");

	
	
	GenStats gs1 = new GenStats(4, 25, 2, false);
	gs1.analyze("c:/jo/smc_test/NOGEOM_SMC_Rakennus_optimized.ifc", false, lokifile);
	gs1.showStats(lokifile);
	gs1.makeUnique();
	gs1.testUnique();

	GenStats gs2 = new GenStats(4, 25, 2, false);
	gs2.analyze("c:/jo/smc_test/NOGEOM_SMC_Rakennus_optimized.ifc", false, lokifile);
	gs2.showStats(lokifile);
	gs2.makeUnique();
	gs2.testUnique();
	System.out.println("AA removed added");
	
	HashSet<String> l1=getGIDList(gs1);
	System.out.println("guid count g1: "+l1.size());

	l1.removeAll(getGIDList(gs2));
	HashSet<String> l2=getGIDList(gs2);
	System.out.println("guid count g2: "+l2.size());
	l2.removeAll(getGIDList(gs1));
	
	int gc1=l1.size();
	int gc2=l2.size();
	System.out.println("guid change1: "+gc1);
	System.out.println("guid change2: "+gc2);
	
	//Handle URIs
	Map<String, Node> map1=gs1.getURIMap();
	Map<String, Node> map2=gs2.getURIMap();
	
	gs1.printDiff(map2, lokifile); 
	gs2.printDiff(map1, lokifile);

    }

    static private void println(String txt) {
	if (txt == null) {
	    System.err.println("txt null");
	    return;
	}
	if (lokifile == null) {
	    System.err.println("lokifile null");
	    return;
	}
	try {
	    lokifile.write(txt);
	    lokifile.newLine();
	    lokifile.flush();
	} catch (Exception e) {
	    System.err.println(txt);
	    System.err.println(lokifile == null);
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	start();
	testrun();
    }

}
