package fi.ni.nodenamer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;

public class IFCHandler {

	OntModel model;

	public OntModel handleIFC(String filename,String output) {
		try {
			return gererate(filename,output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public OntModel gererate(String filename,String output_file) throws IOException {
		File file = new File(output_file);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter logfile = new BufferedWriter(fw);

		ByteArrayOutputStream strout = new ByteArrayOutputStream();
		BufferedWriter log = new BufferedWriter(new OutputStreamWriter(strout));
		ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_Final.exp");
		er.outputRDFS(log);
		er.outputRDFS(logfile);
		IFC_ClassModel m1 = new IFC_ClassModel(filename, er.getEntities(),
				er.getTypes(), "r1");
		m1.listRDF(log);
		m1.listRDF(logfile);

		InputStream is = new ByteArrayInputStream(strout.toString().getBytes());

		OntModel model = ModelFactory
				.createOntologyModel(OntModelSpec.RDFS_MEM);

		model.read(is, null, "N3");
		return model;

	}

	public OntModel getModel() {
		return model;
	}
	
	
}
