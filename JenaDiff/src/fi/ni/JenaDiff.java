package fi.ni;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class JenaDiff {

    static public void testrun() {

	GenStats gs1 = new GenStats();
	gs1.analyze("c:/2014_testdata/persons4A.n3", "N3");

	GenStats gs2 = new GenStats();
	gs2.analyze("c:/2014_testdata/persons4B.n3", "N3");
	
	System.out.println("is isomorphic 1: "+gs1.getModel().isIsomorphicWith(gs2.getModel()));
	System.out.println("is isomorphic 2: "+gs2.getModel().isIsomorphicWith(gs1.getModel()));
	

	showModelStats("SMS_NOGEOM_1", gs1.getModel());
	showModelStats("SMS_NOGEOM_2", gs2.getModel());
    Model d1=gs1.getModel().difference(gs2.getModel());
    Model d2=gs2.getModel().difference(gs1.getModel());
    
    showModelStats("SMS_NOGEOM_diff1_2", d1);
    showModelStats("SMS_NOGEOM_diff2_2", d2);
	
    }

    static public void writeModel(String file_name, Model m)
    {
    	try {
			OutputStream output = new FileOutputStream("C:/M/Tulokset_2014/"+file_name);
			m.write(output, "N-TRIPLE");			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
    }

    static public void showModelStats(String name,Model model)
    {
    	StmtIterator iter1 = model.listStatements();
        int i=0;
     	while (iter1.hasNext()) {	
     		iter1.nextStatement();
     		i++;
     	}
     	System.out.println(name+" statemet count: "+i);
    }

    
    public static void main(String[] args) {
	testrun();
    }

}
