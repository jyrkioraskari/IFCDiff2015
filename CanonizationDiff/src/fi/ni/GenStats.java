package fi.ni;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class GenStats {
	OntModel model;

    public GenStats() {
    }

	public OntModel readIFC(String filename) throws IOException {

		ByteArrayOutputStream strout = new ByteArrayOutputStream();
		BufferedWriter log = new BufferedWriter(new OutputStreamWriter(strout));
		ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_Final.exp");
		er.outputRDFS(log);
		IFC_ClassModel m1 = new IFC_ClassModel(filename, er.getEntities(),
				er.getTypes(), "r1");
		m1.listRDF(log);
		InputStream is = new ByteArrayInputStream(strout.toString().getBytes());

		OntModel model = ModelFactory
				.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);

		model.read(is, null, "N3");
		return model;

	}


    public void analyze(String filename,String type) {
    	if (type.equals("IFC"))
			try {
				model = readIFC(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			model = readRDF(filename, type);
    }
    
	public OntModel readRDF(String filename, String type) {
		model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			throw new IllegalArgumentException("File: " + filename
					+ " not found");
		}

		model.read(in, null, type);
		return model;
	}


	public OntModel getModel() {
		return model;
	}

    
}
