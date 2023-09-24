package fi.ni;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcLabel;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcProject;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcPropertySingleValue;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcRepresentation;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcRoot;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcText;
import openifctools.com.openifcjavatoolbox.ifcmodel.IfcModel;

public class PerformanceTest2 {

	Random rand=null;
	public PerformanceTest2(String filename) {
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
		int modcount=0;
		Object muutospiste=null;
		Collection o = ifcModel.getIfcObjects();
		for (Iterator iter = o.iterator(); iter.hasNext();) {
			Object oo = iter.next();
				if(!IfcProject.class.isInstance(oo))
				{
				    int num; 
				    num = rand.nextInt(50);
				    if(num==1)
				    {
					if(IfcRoot.class.isInstance(oo))
					{
					    IfcRoot ca=(IfcRoot)oo;
						if(modcount==0)
						{
						   modcount++;
						   changed_line=ca.stepLineNumber;
						   muutospiste=ca;
						   ca.setDescription(new IfcText("testing_8881221f",false));
						}
					}
			            }
				    num = rand.nextInt(50);
				    if(num==1)
				    {
					if(IfcRepresentation.class.isInstance(oo))
					{
					    IfcRepresentation ca=(IfcRepresentation)oo;
					    if(modcount==0)
					    {
						   modcount++;
						   changed_line=ca.stepLineNumber;
						   muutospiste=ca;
						   ca.setRepresentationIdentifier(new IfcLabel("testing_8881221f",false));
					   }
					}
			          }
				    num = rand.nextInt(50);
				    if(num==1)
				    {
					if(IfcPropertySingleValue.class.isInstance(oo))
					{
					    IfcPropertySingleValue ca=(IfcPropertySingleValue)oo;
					    if(modcount==0)
					    {
						   modcount++;
						   changed_line=ca.stepLineNumber;
						   muutospiste=ca;
						   ca.setDescription(new IfcText("testing_8881221f",false));
					   }
					}
			          }
				}
		}
		if(modcount==0)
		    return;
		File file1=new File("m00.ifc");
		try {
			ifcModel.writeStepfile(file1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
		    Thread.sleep(3000l);
		} catch (InterruptedException e1) {
		    e1.printStackTrace();
		}
		if(IfcRepresentation.class.isInstance(muutospiste))
		{
		  ((IfcRepresentation)muutospiste).setRepresentationIdentifier(new IfcLabel("testing_8881222f",false));
		}
		else
			if(IfcPropertySingleValue.class.isInstance(muutospiste))
				  ((IfcPropertySingleValue)muutospiste).setDescription(new IfcText("testing_8881222f",false));
			else
		                  ((IfcRoot)muutospiste).setDescription(new IfcText("testing_8881222f",false));
		File file2=new File("m01.ifc");
		try {
			ifcModel.writeStepfile(file2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new IFC_CompareDRUM_String("c:\\jo\\IFC2X3_TC1.exp", "m00.ifc","m01.ifc");
	}

	public static void run(String file)
	{
	    System.out.println(file+":");
	    for(int n=0;n<3;n++)
		new PerformanceTest2(file);
	    
	}
	
	public static void basic_testset() {	
		PerformanceTest2.run("C:\\jo\\IFCtest_data\\drum_10.ifc");
		PerformanceTest2.run("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
		PerformanceTest2.run("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
		PerformanceTest2.run("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
		PerformanceTest2.run("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	        //Duplex_A_20110907_optimized.ifc
		//PerformanceTest1.run("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	    
	    }

	public static void main(String[] args) {
	     System.out.print("Grouding: added found;Grouding: removal found; Grouding: added triples; Grouding: removed triples;");
	   System.out.println("No grouding: added found;No grouding: removal found; No grouding: added triples; No grouding: removed triples;");
	   PerformanceTest2.basic_testset();

	}

}
