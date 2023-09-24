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

public class PerformanceTest3UNIX {

	Random rand=null;
	public PerformanceTest3UNIX(String filename) {
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
		File file1=new File("m00_p.ifc");
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
		File file2=new File("m01_p.ifc");
		try {
			ifcModel.writeStepfile(file2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new IFC_CompareDRUM("IFC2X3_TC1.exp", changed_line,"m00_p.ifc","m01_p.ifc");
	}

	public static void run(String file)
	{
	    System.out.println(file+":");
	    for(int n=0;n<3;n++)
		new PerformanceTest3UNIX(file);
	    
	}
	
	
	public static void basic_testset() {	
	    /*PerformanceTest3UNIX.run("drum_10.ifc");
	    PerformanceTest3UNIX.run("DC_Riverside_Bldg-LOD_100.ifc");
	    PerformanceTest3UNIX.run("PART03_Buderus_200406_20070209_ifc.ifc");
	    PerformanceTest3UNIX.run("PART02_Wilfer_200302_20070209_IFC.ifc");
	    PerformanceTest3UNIX.run("PART06_Kermi_200405_20070401_IFC.ifc");*/
	    PerformanceTest3UNIX.run("Duplex_A_20110907_optimized.ifc");
	    PerformanceTest3UNIX.run("AC11-Institute-Var-2-IFC.ifc");
	    PerformanceTest3UNIX.run("AC-11-Smiley-West-04-07-2007.ifc");
	    
	    }

	public static void main(String[] args) {
	   PerformanceTest3UNIX.basic_testset();

	}

}
