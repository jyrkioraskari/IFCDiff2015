package fi.ni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.rdfcontext.signing.RDFC14Ner;

public class CanonizationDiff {

    static public void testrun() {

	GenStats gs1 = new GenStats();
	gs1.analyze("c:/2014_testdata/o/SMC_Rakennus_o.ifc", "IFC");

	GenStats gs2 = new GenStats();
	gs2.analyze("c:/2014_testdata/o/SMC_RakennusMuutettu_o.ifc", "IFC");
	
	RDFC14Ner r1=new RDFC14Ner(gs1.getModel());
	RDFC14Ner r2=new RDFC14Ner(gs2.getModel());
	
	System.out.println("strings array gs1 size:"+r1.getCanonicalStringsArray().size());
	System.out.println("strings array gs2 size:"+r2.getCanonicalStringsArray().size());
	
	ArrayList<String> s1=(ArrayList<String>)r1.getCanonicalStringsArray().clone();
	ArrayList<String> s2=(ArrayList<String>)r2.getCanonicalStringsArray().clone();
	Set<String> statements1=new HashSet<String>();
	Set<String> statements2=new HashSet<String>();
	
	
	statements1.addAll(s1);
	statements2.addAll(s2);
	
	System.out.println("statements 1: "+statements1.size());
	System.out.println("statements 2: "+statements2.size());
	int removed=0;
	for(String s:statements1)
	{
		if(!statements2.contains(s))
			removed++;
	}
	System.out.println("removed: "+removed);

	int added=0;
	for(String s:statements2)
	{
		if(!statements1.contains(s))
			added++;
	}
	System.out.println("added: "+added);

    }
    
    public static void main(String[] args) {
	testrun();
    }

}
