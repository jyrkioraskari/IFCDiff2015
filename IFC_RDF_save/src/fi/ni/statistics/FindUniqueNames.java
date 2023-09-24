package fi.ni.statistics;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcRoot;

public class FindUniqueNames {

    static public Set<String>  global_nameclasses=new HashSet<String>();
    static public Set<String>  global_nameclasses_hasdublicates=new HashSet<String>();
    
    public FindUniqueNames(String filename) {
	ExpressReader er;

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	System.out.println(filename);       
	Set<String>  nameclasses=new HashSet<String>();
	Set<String>  nameclasses_hasdublicates=new HashSet<String>();
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	Map<String,String> map=new HashMap<String,String>();  //class_name, value
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if(IfcRoot.class.isInstance(t))
		continue;
	    boolean hasname=false;
	    Method m=null;
	    try {
		m = t.getClass().getMethod("getName");
		hasname=true;
	    } catch (Exception e) {
	    }
	    if(hasname)
	    {
		try
		{
		    String val=(String)m.invoke(t, null);
		    if(val==null)
			continue;
		    if(val.equals(""))
			continue;
		    if(val.equalsIgnoreCase("Name"))
			continue;
		    nameclasses.add(t.getClass().getSimpleName());
		    global_nameclasses.add(t.getClass().getSimpleName());
		    String oldval=map.put(t.getClass().getSimpleName()+"."+val,val);
		    if(oldval!=null)
		    {
			// duplicate
			global_nameclasses_hasdublicates.add(t.getClass().getSimpleName());
			nameclasses_hasdublicates.add(t.getClass().getSimpleName());
			//System.out.println("  -"+ t.getClass().getSimpleName()+" is not, since value: "+oldval+" = "+val);
		    }
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		    
	    }
	}
	
       nameclasses.removeAll(nameclasses_hasdublicates);
       Iterator it=nameclasses.iterator();
       while(it.hasNext())
       {
	   String s=(String) it.next();
	     System.out.println("  "+s);   
       }
       
    }

    public static void tiny_testset() {
	new FindUniqueNames("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	
    }

    public static void complete_testset() {
	new FindUniqueNames("C:\\jo\\IFCtest_data\\sample.ifc");

	new FindUniqueNames("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new FindUniqueNames("C:\\jo\\IFCtest_data\\door.ifc");
	new FindUniqueNames("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new FindUniqueNames("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new FindUniqueNames("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new FindUniqueNames("C:\\jo\\models\\HiB_DuctWork.Ifc");
	new FindUniqueNames("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new FindUniqueNames("C:\\jo\\models\\Planer 4B Full.IFC");

	new FindUniqueNames("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");

	new FindUniqueNames("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new FindUniqueNames("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new FindUniqueNames("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new FindUniqueNames("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new FindUniqueNames("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new FindUniqueNames("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");

	new FindUniqueNames("C:\\jo\\IFCtest_data\\inp3.ifc");

	new FindUniqueNames("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new FindUniqueNames("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new FindUniqueNames("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new FindUniqueNames("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new FindUniqueNames("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new FindUniqueNames("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new FindUniqueNames("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new FindUniqueNames("C:\\jo\\models2\\RAK_Helmisimpukka_20101217.ifc");
	new FindUniqueNames("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new FindUniqueNames("C:\\jo\\models2\\Crane.ifc");
	new FindUniqueNames("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new FindUniqueNames("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new FindUniqueNames("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new FindUniqueNames("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new FindUniqueNames("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new FindUniqueNames("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new FindUniqueNames("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new FindUniqueNames("C:\\jo\\models2\\OfficeBuilding.ifc");
	new FindUniqueNames("C:\\jo\\models2\\SMC Building - modified.ifc");
	new FindUniqueNames("C:\\jo\\models2\\SMC Building.ifc");
	new FindUniqueNames("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
    }

    public static void main(String[] args) {
	FindUniqueNames.complete_testset();
	System.out.println("Global unique list:");
	FindUniqueNames.global_nameclasses.removeAll(FindUniqueNames.global_nameclasses_hasdublicates);
	Iterator it=FindUniqueNames.global_nameclasses.iterator();
	while(it.hasNext())
	{
		   String s=(String) it.next();
		     System.out.println("  "+s);   
	}
    }

}
