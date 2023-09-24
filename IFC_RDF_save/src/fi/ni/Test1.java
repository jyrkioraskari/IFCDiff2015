package fi.ni;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class Test1 {

    public Test1() {
	getJenaMSG();
    }
    static String ns = "http://drum/diff#";

    public Model getJenaMSG()
    {
	Set<Resource> resources=new HashSet<Resource>();
        Model m = ModelFactory.createDefaultModel();
	m.setNsPrefix( "ns", ns);
	Resource test1 =  m.createResource(ns+"test");
	Resource test2 =  m.createResource(ns+"test");
        System.out.println(resources.add(test1));
        System.out.println(resources.add(test1));
	return m;
    }
    public static void main(String[] args) {
	new Test1();

    }

}
