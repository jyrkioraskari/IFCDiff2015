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

public class ExportIFC2RDF_OUT {


	public void doExport(String directory, String modelname)
			throws IOException {
		
		BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
		ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_Final.exp");
		er.outputRDFS(log);
		IFC_ClassModel m1 = new IFC_ClassModel(directory + modelname+".ifc",
				er.getEntities(), er.getTypes(), "r1");
		m1.listRDF(log);		
		

	}

	public static void main(String[] args) {
		ExportIFC2RDF_OUT e=new ExportIFC2RDF_OUT();
		try {
			e.doExport("c:/2014_testdata/", "SMC_Rakennus");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
