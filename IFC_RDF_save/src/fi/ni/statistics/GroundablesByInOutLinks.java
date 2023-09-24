package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.InOutBag;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.GroundSettings;
import fi.ni.vo.InOutLinks;
import fi.ni.vo.Link;



public class GroundablesByInOutLinks {

    
    public GroundablesByInOutLinks(String filename) {
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

	InOutBag inout_bag=new InOutBag();
       
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if(IfcOwnerHistory.class.isInstance(t))
	        	continue;
	    if(t.is_grounded)
		  continue;

	    int in_links  = t.incoming_count;
	    int out_links = t.i.drum_getIfcClassAttributes_notInverses().size();
	    inout_bag.add(in_links, out_links, t.getClass().getSimpleName());            
	}

	Map<String,List<InOutLinks>> ones=inout_bag.getOnes();
	String application = "";
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    int in_links=t.incoming_count;
	    int out_links=t.i.drum_getIfcClassAttributes_notInverses().size();
	    InOutLinks chk=new InOutLinks(in_links, out_links);
	    List<InOutLinks> cls_ones=ones.get(t.getClass().getSimpleName());
	    if(cls_ones!=null)
	    if(cls_ones.contains(chk))		
	    {
		t.is_grounded=true;
	    }
	    
	    if (IfcApplication.class.isInstance(t)) {
		IfcApplication appl = (IfcApplication) t;
		application = appl.getApplicationFullName();
	    }
	    
	}

	for(Map.Entry<String,List<InOutLinks>> entry : ones.entrySet()) {
	           String class_name = entry.getKey();
	           List<InOutLinks> value = entry.getValue();
	           System.out.println(class_name+", "+value);
	}

	System.out.println("----------------------------------------");
	System.gc();
	Map<String, Thing> guids1 = model.getGIDThings(); // gid, thing	
	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	GroundSettings gs=new GroundSettings(true, true, false, true, true, true, true,false);	       
	model.groundFromGUIDs(common_gids,gs);
	model.createHugeLinksMap();
	System.gc();
	model.deduceMSGs();
	int max=Integer.MIN_VALUE;
	for(int i=0;i<model.msgs.size();i++)
	{
	    if(max<model.msgs.get(i).size())
		max=model.msgs.get(i).size();
	}
	System.gc();
	System.out.println(application+";"+filename+";"+model.msgs.size()+";"+max+";"+model.object_buffer.size()+";"+model.gid_map.size());
        
   }

    public static void tiny_testset() {	
	new GroundablesByInOutLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new GroundablesByLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	//new GroundablesByInOutLinks("C:\\jo\\IFCtest_data\\talo_testi.ifc");
    }

    
    public static void complete_testset() {	
	new GroundablesByInOutLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\Crane.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new GroundablesByInOutLinks("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\OfficeBuilding.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\SMC Building - modified.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\SMC Building.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\Planer 4B Full.ifc");
	new GroundablesByInOutLinks("C:\\jo\\models\\HiB_DuctWork.ifc");
	new GroundablesByInOutLinks("C:\\jo\\IFCtest_data\\inp3.ifc");
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
	
      System.out.print("filename; application; entities; has_duplicate_guids");
      System.out.print("; max_outgoing_links; avg_outgoing_links; max outgoing links class");
      System.out.print("; max_incoming_links;  avg_incoming_links; max incoming links class\"");
      System.out.println();

      //GroundablesByLinks.complete_testset();
      GroundablesByInOutLinks.tiny_testset();
    }

}
