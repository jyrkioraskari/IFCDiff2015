package fi.ni;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import fi.ni.nodenamer.IFCHandler;

public class GenRDFfromIFC {

    static public void testrun() {

    	IFCHandler ifch = new IFCHandler();
    	OntModel model=ifch.handleIFC("c:\\jo/remove_geom/NOGEOM_AC-11-Smiley-West-04-07-2007.ifc","C:/2014_testdata/NOGEOM_AC-11-Smiley-West-04-07-2007.n3");
    }

  
    
    public static void main(String[] args) {
	testrun();
    }

}
