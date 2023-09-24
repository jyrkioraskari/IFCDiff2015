package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.Link;


public class OnlyInComingLinks {

    
    public OnlyInComingLinks(String filename) {
	ExpressReader er;
	System.out.println("----------------------------------------");

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");

	
	model.checkUniques();
	
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
            if(IfcOwnerHistory.class.isInstance(t1))
        	continue;
	    if(t1.is_grounded)
		  continue;
	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2=l.getTheOtherEnd(t1);
	        if(IfcOwnerHistory.class.isInstance(t2))
	        	continue;
		t2.incoming_count++;
	    }
	}

	Set<String> shared_classes=new HashSet<String>();
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    if(IfcOwnerHistory.class.isInstance(t1))
	        	continue;
	    if(t1.is_grounded)
		  continue;
	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    if(ifcs.size()==0)
	    {
		if(t1.incoming_count>1)
		    shared_classes.add(t1.getClass().getSimpleName());
	    }
	}
        System.out.println(filename+":");
	Iterator<String> it=shared_classes.iterator();
	while(it.hasNext())
	{
	    String s=it.next();
	    System.out.println("  "+s);
	}

   }

    public static void tiny_testset() {	
	new OnlyInComingLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new OnlyInComingLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Crane.ifc");
    }

    
    public static void complete_testset() {	
	new OnlyInComingLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Crane.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new OnlyInComingLinks("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\OfficeBuilding.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\SMC Building - modified.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\SMC Building.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new OnlyInComingLinks("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\Planer 4B Full.ifc");
	new OnlyInComingLinks("C:\\jo\\models\\HiB_DuctWork.ifc");
	new OnlyInComingLinks("C:\\jo\\IFCtest_data\\inp3.ifc");
    }

    public static String f(double d)
    {
	DecimalFormat formatter = new DecimalFormat("#0.00");
	DecimalFormatSymbols ds=new DecimalFormatSymbols();
	ds.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(ds);
	return formatter.format(d);
    }

    public static void main(String[] args) {
	/*
      System.out.print("filename; application; entities; has_duplicate_guids");
      System.out.print("; max_outgoing_links; avg_outgoing_links; max outgoing links class");
      System.out.print("; max_incoming_links;  avg_incoming_links; max incoming links class\"");
      System.out.println();*/

      //GroundablesByLinks.complete_testset();
      OnlyInComingLinks.complete_testset();
    }

}