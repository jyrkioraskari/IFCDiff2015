package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import fi.ni.ExpressReader;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.Link;


public class StatisticsIncomingLinks {

    /**
     * @param args
     */
    public StatisticsIncomingLinks(String filename) {
	ExpressReader er;
	System.out.println("----------------------------------------");

	er = new ExpressReader("c:\\jo\\IFC2X3_TC1.exp");
	IFC_ClassModel model = new IFC_ClassModel(filename, er.getEntities(), er.getTypes(), "model");
	String application = "";

	double outgoing_links=0f;
	int max_outgoing_links=Integer.MIN_VALUE;
	String max_outgoing_class = "";
	SortedMap<Integer,SortedMap<String,Integer>> outgoing_histogram=new TreeMap<Integer,SortedMap<String,Integer>>();  // links count <class name, number or individual entities>
	
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
            if(IfcOwnerHistory.class.isInstance(t1))
        	continue;
	    List<Link> ifcs = t1.i.drum_getIfcClassAttributes_notInverses();
	    if(ifcs.size()>max_outgoing_links)
	    {
		max_outgoing_links=ifcs.size();
		max_outgoing_class=t1.getClass().getSimpleName();
		//max_outgoing_class+="L_"+t1.line_number;
	    }
	    for (int n = 0; n < ifcs.size(); n++) {
		Link l = (Link) ifcs.get(n);
		Thing t2=l.getTheOtherEnd(t1);
	        if(IfcOwnerHistory.class.isInstance(t2))
	        	continue;
		t2.incoming_count++;
		outgoing_links++;
	    }
	    SortedMap<String,Integer> current_map=outgoing_histogram.get(ifcs.size());
	    if(current_map==null)
	    {
		current_map=new TreeMap<String,Integer>();
		outgoing_histogram.put(ifcs.size(),current_map);
	    }
	    Integer current_count=current_map.get(t1.getClass().getSimpleName());
	    if(current_count==null)
		current_count=new Integer(0);
	    current_count=current_count+1;
	    current_map.put(t1.getClass().getSimpleName(),current_count);
	}

	double incoming_links=0f;
	String max_incoming_class = "";
	int max_incoming_links=Integer.MIN_VALUE;
	SortedMap<Integer,SortedMap<String,Integer>> incoming_histogram=new TreeMap<Integer,SortedMap<String,Integer>>();  // links count <class name, number or individual entities>
	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t1 = (Thing) entry.getValue();
	    if(IfcOwnerHistory.class.isInstance(t1))
	        	continue;
	    if(t1.incoming_count>max_incoming_links)
	    {
		max_incoming_links=t1.incoming_count;
		max_incoming_class=t1.getClass().getSimpleName();
		//max_incoming_class+="L_"+t1.line_number;

	    }
	    
	    
	    SortedMap<String,Integer> current_map=incoming_histogram.get(t1.incoming_count);
	    if(current_map==null)
	    {
		current_map=new TreeMap<String,Integer>();
		incoming_histogram.put(t1.incoming_count,current_map);
	    }
	    Integer current_count=current_map.get(t1.getClass().getSimpleName());
	    if(current_count==null)
		current_count=new Integer(0);
	    current_count=current_count+1;
	    current_map.put(t1.getClass().getSimpleName(),current_count);
	}

	for (Map.Entry<Long, Thing> entry : model.object_buffer.entrySet()) {
	    Thing t = (Thing) entry.getValue();
	    if (IfcApplication.class.isInstance(t)) {
		IfcApplication appl = (IfcApplication) t;
		application = appl.getApplicationFullName();
	    }
	}
	
       double avg_outcoming_links=outgoing_links/((double)model.object_buffer.size());
       double avg_incoming_links=outgoing_links/((double)model.object_buffer.size());

       
       System.out.print(filename+";"+application+";"+model.object_buffer.size()+";"+model.has_duplicate_guids);
       System.out.print(";"+max_outgoing_links+";"+ f(avg_outcoming_links)+";"+max_outgoing_class);
       System.out.print(";"+max_incoming_links+";"+f(avg_incoming_links)+";"+max_incoming_class);
       System.out.println();
       

       System.out.println("Outgoing links histogram:");
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : outgoing_histogram.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
           
	   System.out.print(key + ": " );
	       for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           System.out.print(class_name + "="+class_value+" ");
	       }
	   System.out.println();
	 }
	System.out.println("----------------------------------------");

       System.out.println("Incoming links histogram:");
       for(Map.Entry<Integer,SortedMap<String,Integer>> entry : incoming_histogram.entrySet()) {
           Integer key = entry.getKey();
           SortedMap<String,Integer> value = entry.getValue();
	   System.out.print(key + ": ");
	       for(Map.Entry<String,Integer> cls_entry : value.entrySet()) {
	           String class_name = cls_entry.getKey();
	           Integer class_value = cls_entry.getValue();
	           System.out.print(class_name + "="+class_value+" ");
	       }
	   System.out.println();
	 }

   }

    public static void tiny_testset() {	
	new StatisticsIncomingLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
    }

    
    public static void complete_testset() {	
	new StatisticsIncomingLinks("C:\\jo\\IFCtest_data\\drum_10.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\First_Floor_Vent.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\Second_Floor_Vent.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\Ground_Floor_Vent.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\Crane.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\AC11-Institute-Var-2-IFC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc");
	new StatisticsIncomingLinks("C:\\jo\\IFCtest_data\\talo_testi.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\AC14-FZK-Haus.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\OfficeBuilding.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\2\\FJK-Project-Final.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\HITOS_Electrical_Update_Storey_3_2006-10-11.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\SMC Building - modified.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\SMC Building.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20101209.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\ARK_Helmisimpukka_20110920.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\Planer 4B Full.ifc");
	new StatisticsIncomingLinks("C:\\jo\\models\\HiB_DuctWork.ifc");
	new StatisticsIncomingLinks("C:\\jo\\IFCtest_data\\inp3.ifc");
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

      StatisticsIncomingLinks.complete_testset();
    }

}
