package fi.ni;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcCartesianPoint;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcLengthMeasure;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcProject;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcRoot;
import openifctools.com.openifcjavatoolbox.ifcmodel.IfcModel;

public class PerformanceTest1UNIX {

	Random rand=null;
	public PerformanceTest1UNIX(String filename) {
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
						   //System.out.println("change:"+ca.stepLineNumber+" "+ca.toString());
						   int point=rand.nextInt(ca.getCoordinates().size());
						   muutospiste=ca.getCoordinates().get(point);
						   muutospiste.setValue(8881221f);
						}
						//ca.destruct();
						//removelist.add((IfcClass)oo);
					    }
					}
				}
			}
		}
		if(modcount==0)
		    return;
		File file1=new File("m0_p.ifc");
		try {
			ifcModel.writeStepfile(file1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		muutospiste.setValue(8881222f);

		File file2=new File("m1_p.ifc");
		try {
			ifcModel.writeStepfile(file2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new IFC_CompareDRUM("IFC2X3_TC1.exp", changed_line,"m0_p.ifc","m1_p.ifc");
	}

	public static void run(String file)
	{
	    System.out.println(file+":");
	    for(int n=0;n<3;n++)
		new PerformanceTest1UNIX(file);
	    
	}
	
	public static void basic_testset() {	
	    /*PerformanceTest1UNIX.run("drum_10.ifc");
	    PerformanceTest1UNIX.run("DC_Riverside_Bldg-LOD_100.ifc");
	    PerformanceTest1UNIX.run("PART03_Buderus_200406_20070209_ifc.ifc");
	    PerformanceTest1UNIX.run("PART02_Wilfer_200302_20070209_IFC.ifc");
	    PerformanceTest1UNIX.run("PART06_Kermi_200405_20070401_IFC.ifc");*/
	    PerformanceTest1UNIX.run("Duplex_A_20110907_optimized.ifc");
	    PerformanceTest1UNIX.run("AC11-Institute-Var-2-IFC.ifc");
	    PerformanceTest1UNIX.run("AC-11-Smiley-West-04-07-2007.ifc");
	    
	    }

	public static void main(String[] args) {
	     System.out.print("Grouding: added found;Grouding: removal found; Grouding: added triples; Grouding: removed triples;");
	   System.out.println("No grouding: added found;No grouding: removal found; No grouding: added triples; No grouding: removed triples;");
	   PerformanceTest1UNIX.basic_testset();

	}

}
