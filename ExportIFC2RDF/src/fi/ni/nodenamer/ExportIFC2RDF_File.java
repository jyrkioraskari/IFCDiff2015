package fi.ni.nodenamer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;

public class ExportIFC2RDF_File {


	public void doExport(String directory, String modelname)
			throws IOException {
		
		File file = new File("c:/t/SMC_Rakennus.n3");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter log = new BufferedWriter(fw);
		
		ExpressReader er = new ExpressReader("c:\\jo\\IFC2X3_Final.exp");
		er.outputRDFS(log);
		IFC_ClassModel m1 = new IFC_ClassModel(directory + modelname+".ifc",
				er.getEntities(), er.getTypes(), "r1");
		m1.listRDF(log);		
		

	}

	public static void main(String[] args) {
		ExportIFC2RDF_File e=new ExportIFC2RDF_File();
		try {
			e.doExport("c:/2014_testdata/", "SMC_Rakennus");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
