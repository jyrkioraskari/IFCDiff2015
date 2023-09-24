package fi.ni;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import fi.ni.ifc2x3.IfcBuildingStorey;

import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcCartesianPoint;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcLengthMeasure;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcProject;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcRoot;
import openifctools.com.openifcjavatoolbox.ifcmodel.IfcModel;

public class PerformanceTest1 {

    
	Random rand=null;
	public PerformanceTest1(String filename) {
	    rand=new Random(System.currentTimeMillis());
		readModel(filename);
	}

	public void readModel(String filename) {
		IfcModel ifcModel = new IfcModel();
		try {
			ifcModel.readStepFile(new File(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}

		int changed_line=Integer.MIN_VALUE;
		//List<IfcClass> removelist=new LinkedList<IfcClass>();
		int modcount=0;
		IfcLengthMeasure muutospiste=null;
		Collection o = ifcModel.getIfcObjects();
		for (Iterator iter = o.iterator(); iter.hasNext();) {
			Object oo = iter.next();
			if (!IfcRoot.class.isInstance(oo)) 
			{
				//IfcRoot or = (IfcRoot) oo;
				//System.out.println(or.getGlobalId());
				if(!IfcProject.class.isInstance(oo))
				{
					if(IfcCartesianPoint.class.isInstance(oo))
					{
					    IfcCartesianPoint ca=(IfcCartesianPoint)oo;
					    int num = rand.nextInt(50);
					    if(num==1)
					    {
						if(modcount==0)
						if(ca.getCoordinates().size()>0)
						{
						   modcount++;
						   changed_line=ca.stepLineNumber;
						   int point=rand.nextInt(ca.getCoordinates().size());
						   muutospiste=ca.getCoordinates().get(point);
						   muutospiste.setValue(8881221f);
						}
					    }
					}
				}
			}
		}
		if(modcount==0)
		    return;
		File file1=new File("c:\\jo\\small_diff\\m0.ifc");
		try {
			ifcModel.writeStepfile(file1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		muutospiste.setValue(8881222f);
		/*for(int i=0;i<removelist.size();i++)
		{
			IfcClass oo=removelist.get(i);
			ifcModel.removeIfcObject(oo);	
                        System.out.println("removed line:"+oo.getStepLine());
		}*/
		File file2=new File("c:\\jo\\small_diff\\m1.ifc");
		try {
			ifcModel.writeStepfile(file2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new IFC_Compare("c:\\jo\\IFC2X3_TC1.exp", changed_line);
	}

	public static void run(String file)
	{
	    System.out.println(file+":");
	    for(int n=0;n<30;n++)
		new PerformanceTest1(file);
	    
	}
	
	public static void basic_testset() {	
		//PerformanceTest1.run("C:\\jo\\IFCtest_data\\drum_10.ifc");
		//PerformanceTest1.run("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
		/*PerformanceTest1.run("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
		PerformanceTest1.run("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
		PerformanceTest1.run("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");*/
	        //Duplex_A_20110907_optimized.ifc
		PerformanceTest1.run("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	    
	    }

	public static void main(String[] args) {
	     System.out.print("Grouding: added found;Grouding: removal found; Grouding: added triples; Grouding: removed triples;");
	   System.out.println("No grouding: added found;No grouding: removal found; No grouding: added triples; No grouding: removed triples;");
	   PerformanceTest1.basic_testset();

	}

}
