package fi.ni.nodenamer;

import java.io.FileOutputStream;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class ExportRDF2N3 {


	public void doExport(String directory, String modelname)
			throws IOException {
		Model m = FileManager.get().loadModel( directory+modelname+".rdf");
		try {
			FileOutputStream fout = new FileOutputStream("C:/2014_testdata/"
					+ modelname + ".n3");
			m.write(fout, "N3");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		ExportRDF2N3 e=new ExportRDF2N3();
		try {
			e.doExport("C:/2014_testdata/swedish1/", "mergeFile1");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
