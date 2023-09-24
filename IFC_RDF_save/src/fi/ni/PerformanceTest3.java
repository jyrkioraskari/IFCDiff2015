package fi.ni;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcCartesianPoint;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcLengthMeasure;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcPolyLoop;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcPolyline;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcProject;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.LIST;
import openifctools.com.openifcjavatoolbox.ifcmodel.IfcModel;

public class PerformanceTest3 {

	Random rand=null;
	public PerformanceTest3(String filename) {
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
		File file1=new File("m00.ifc");
		try {
			ifcModel.writeStepfile(file1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int changed_line=Integer.MIN_VALUE;
		int modcount=0;
		Object modification_point=null;
		Collection o = ifcModel.getIfcObjects();
		for (Iterator iter = o.iterator(); iter.hasNext();) {
			Object oo = iter.next();
				if(!IfcProject.class.isInstance(oo))
				{
				    int num; 
				    num = rand.nextInt(50);
				    if(num==1)
				    {
					if(IfcPolyline.class.isInstance(oo))
					{
					    IfcPolyline ca=(IfcPolyline)oo;
						if(modcount==0)
						{
						   modcount++;
						   modification_point=ca;
						   changed_line=ca.stepLineNumber;
						}
					}
			            }
				    num = rand.nextInt(50);
				    if(num==1)
				    {
					if(IfcPolyLoop.class.isInstance(oo))
					{
					    IfcPolyLoop ca=(IfcPolyLoop)oo;
						if(modcount==0)
						{
						   modcount++;
						   modification_point=ca;
						   changed_line=ca.stepLineNumber;
						}
					}
			            }

				    
				}
		}
		if(modcount==0)
		    return;
		if(IfcPolyline.class.isInstance(modification_point))
		{
		           IfcPolyline ca=(IfcPolyline) modification_point;		           
			   LIST cartesianpoint=new LIST();
			   cartesianpoint.add(0,new IfcLengthMeasure(8881222f));
			   cartesianpoint.add(1,new IfcLengthMeasure(0f));
			   cartesianpoint.add(2,new IfcLengthMeasure(0f));
			   
			   IfcCartesianPoint cp=new IfcCartesianPoint(cartesianpoint);
			   ifcModel.addIfcObject(cp);
			   ca.getPoints().add(cp);
			   ca.addPoints(cp);
		}		
		if(IfcPolyLoop.class.isInstance(modification_point))
		{
		           IfcPolyLoop ca=(IfcPolyLoop) modification_point;
			   LIST cartesianpoint=new LIST();          
			   cartesianpoint.add(0,new IfcLengthMeasure(8881222f));
			   cartesianpoint.add(1,new IfcLengthMeasure(0f));
			   cartesianpoint.add(2,new IfcLengthMeasure(0f));
			   IfcCartesianPoint cp=new IfcCartesianPoint(cartesianpoint);
			   ifcModel.addIfcObject(cp);
			   ca.addPolygon(cp);
		}		
		File file2=new File("m01.ifc");
		try {
			ifcModel.writeStepfile(file2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new IFC_CompareDRUM("c:\\jo\\IFC2X3_TC1.exp", changed_line,"m00.ifc","m01.ifc");
	}

	public static void run(String file)
	{
	    System.out.println(file+":");
	    for(int n=0;n<10;n++)
		new PerformanceTest3(file);
	    
	}
	
	public static void basic_testset() {	
		PerformanceTest3.run("C:\\jo\\IFCtest_data\\drum_10.ifc");
		/*PerformanceTest1.run("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
		PerformanceTest1.run("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
		PerformanceTest1.run("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
		PerformanceTest1.run("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");*/
	        //Duplex_A_20110907_optimized.ifc
		//PerformanceTest1.run("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	    
	    }

	public static void main(String[] args) {
	     System.out.print("Grouding: added found;Grouding: removal found; Grouding: added triples; Grouding: removed triples;");
	   System.out.println("No grouding: added found;No grouding: removal found; No grouding: added triples; No grouding: removed triples;");
	   PerformanceTest3.basic_testset();

	}

}
