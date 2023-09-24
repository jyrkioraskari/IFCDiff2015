package fi.ni;

import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class GenStats {
	OntModel model;

    public GenStats() {
    }


    public void analyze(String filename,String type) {
    	model = ModelFactory
				.createOntologyModel(OntModelSpec.RDFS_MEM);
    	InputStream in = FileManager.get().open( filename );
    	if (in == null) {
    	    throw new IllegalArgumentException("File: " + filename + " not found");
    	}
    	
    	model.read(in, null,type);
    }


	public OntModel getModel() {
		return model;
	}

    
}
