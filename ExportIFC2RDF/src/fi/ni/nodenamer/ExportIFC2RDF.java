package fi.ni.nodenamer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;

public class ExportIFC2RDF {


	public OntModel doExport(String directory, String modelname)
			throws IOException {
		ByteArrayOutputStream strout = new ByteArrayOutputStream();
		BufferedWriter log = new BufferedWriter(new OutputStreamWriter(strout));
		ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_Final.exp");
		er.outputRDFS(log);
		IFC_ClassModel m1 = new IFC_ClassModel(directory + modelname+".ifc",
				er.getEntities(), er.getTypes(), "r1");
		m1.listRDF(log);		
		InputStream is = new ByteArrayInputStream(strout.toString().getBytes());
		OntModel model = ModelFactory
				.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);
		model.read(is, null, "N3");
		try {
			FileOutputStream fout = new FileOutputStream("C:/2014_testdata/"
					+ modelname + ".n3");
			model.write(fout, "N3");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return model;

	}

	public static void main(String[] args) {
		ExportIFC2RDF e=new ExportIFC2RDF();
		try {
			e.doExport("c:/t/", "h");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
