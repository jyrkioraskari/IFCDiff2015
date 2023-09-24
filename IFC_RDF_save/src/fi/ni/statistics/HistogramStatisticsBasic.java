package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Set;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcRoot;
import fi.ni.vo.Triple;


public class HistogramStatisticsBasic {
  
    static Histogram msg_histogram=new Histogram("Basic");
  
    
    public HistogramStatisticsBasic(String filename) {

	runStatistics(filename);
    }

    public void runStatistics(String filename) {
	ExpressReader er;

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	
	String application = "";

	model.checkUniques();
	
	System.gc();
	model.createHugeLinksMap();
	System.gc();
	model.deduceMSGs();
	int max=Integer.MIN_VALUE;
	
	for(int i=0;i<model.msgs.size();i++)
	{
	          
		long literals=0;
		Set<String> classnameSet=new HashSet<String>(1000000);
		Set<Object> nodes=new HashSet<Object>(10000000);
		Set<Object> iris=new HashSet<Object>(1000000);
		Set<Object> guids=new HashSet<Object>(1000000);
		Set<Object> blanks=new HashSet<Object>(1000000);
		  for(int n=0;n<model.msgs.get(i).size();n++)
		  {
	            Triple t=model.msgs.get(i).get(n);
		    nodes.add(t.s);
		    classnameSet.add(t.s.getClass().getSimpleName()); 
		    if(IfcRoot.class.isInstance(t.s))
		    {
			guids.add(t.s);
		    }
		    if(IfcRoot.class.isInstance(t.o))
		    {
			guids.add(t.o);
		    }
		    
		    if(t.s.is_grounded)
			iris.add(t.s);
		    else
			blanks.add(t.s);
		    
		    if(Thing.class.isInstance(t.o))
		    {
			nodes.add(t.o);
			Thing to=(Thing)t.o;
			    classnameSet.add(to.getClass().getSimpleName()); 
			if(to.is_grounded)
			   iris.add(to);
			else
			   blanks.add(to);
		    }
		    else
		     literals++;
		  }
		  
	    if(max<model.msgs.get(i).size())
		max=model.msgs.get(i).size();
	    msg_histogram.add_value(model.msgs.get(i).size(),classnameSet.size(),blanks.size(),guids.size(),iris.size(),literals,nodes.size()+literals);
	}
	System.gc();
	
	System.out.println(application+";"+filename+";"+max+";");


   }

    public static void tiny_testset() {	
	//new HistogramStatisticsBasic("C:\\jo\\IFCtest_data\\inp3.ifc");
	//new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	//new HistogramStatisticsBasic("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	//new HistogramStatisticsBasic("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
    }


    public static void basic_testset() {	
	new HistogramStatisticsBasic("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Crane.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new HistogramStatisticsBasic("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\OfficeBuilding.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
    }
    
    public static void complete_testset() {	
	new HistogramStatisticsBasic("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Crane.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new HistogramStatisticsBasic("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\OfficeBuilding.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\SMC Building - modified.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\SMC Building.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\Planer 4B Full.ifc");
	new HistogramStatisticsBasic("C:\\jo\\models\\HiB_DuctWork.ifc");
	new HistogramStatisticsBasic("C:\\jo\\IFCtest_data\\inp3.ifc");
    }

    public static String f(double d)
    {
	DecimalFormat formatter = new DecimalFormat("#0.00000");
	DecimalFormatSymbols ds=new DecimalFormatSymbols();
	ds.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(ds);
	return formatter.format(d);
    }

    public static void main(String[] args) {
	
      HistogramStatisticsBasic.tiny_testset();
      msg_histogram.listHistogram();

    }

}
