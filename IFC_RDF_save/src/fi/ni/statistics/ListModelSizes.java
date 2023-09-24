package fi.ni.statistics;

import java.util.HashSet;
import java.util.Set;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;

public class ListModelSizes {

    /**
     * @param args
     */
    public ListModelSizes(String filename) {
	ExpressReader er;

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	String application = "";
	Set<String> classnameSet = new HashSet<String>();
	/*
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if (IfcApplication.class.isInstance(t)) {
		IfcApplication appl = (IfcApplication) t;
		application = appl.getApplicationFullName();
	    }
	}*/
       System.out.println(filename+";"+model.has_duplicate_guids+";"+model.object_buffer.size());
    }

    public static void complete_testset() {
	new ListModelSizes("C:\\jo\\IFCtest_data\\sample.ifc");

	new ListModelSizes("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new ListModelSizes("C:\\jo\\IFCtest_data\\door.ifc");
	new ListModelSizes("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new ListModelSizes("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new ListModelSizes("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new ListModelSizes("C:\\jo\\models\\HiB_DuctWork.Ifc");
	new ListModelSizes("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new ListModelSizes("C:\\jo\\models\\Planer 4B Full.IFC");

	new ListModelSizes("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");

	new ListModelSizes("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new ListModelSizes("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new ListModelSizes("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new ListModelSizes("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new ListModelSizes("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new ListModelSizes("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");

	new ListModelSizes("C:\\jo\\IFCtest_data\\inp3.ifc");

	new ListModelSizes("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new ListModelSizes("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new ListModelSizes("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new ListModelSizes("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new ListModelSizes("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new ListModelSizes("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new ListModelSizes("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new ListModelSizes("C:\\jo\\models2\\RAK_Helmisimpukka_20101217.ifc");
	new ListModelSizes("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new ListModelSizes("C:\\jo\\models2\\Crane.ifc");
	new ListModelSizes("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new ListModelSizes("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new ListModelSizes("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new ListModelSizes("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new ListModelSizes("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new ListModelSizes("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new ListModelSizes("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new ListModelSizes("C:\\jo\\models2\\OfficeBuilding.ifc");
	new ListModelSizes("C:\\jo\\models2\\SMC Building - modified.ifc");
	new ListModelSizes("C:\\jo\\models2\\SMC Building.ifc");
	new ListModelSizes("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
    }

    public static void main(String[] args) {
	ListModelSizes.complete_testset();
    }

}
