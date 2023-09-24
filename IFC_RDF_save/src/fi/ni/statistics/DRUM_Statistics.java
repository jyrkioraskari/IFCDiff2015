package fi.ni.statistics;


public class DRUM_Statistics {

    public static void main(String[] args) {
	       System.out.println("Without grounding:");
	       System.out.println(" Application, file:,\tmsgs:,\tentities:,\tmax:,\ttriples:,\tguids:,\tliterals:,\tnodes:, used_class_names:, avgmsgsize");
		IFC_StatisticsWOGrounding.mikro_testset();
		

		System.out.println("Grounding used:");
		System.out.println(" Ground based on:, Application, file:,\tmsgs:,\tentities:,\tmax:,\ttriples:,\tguids:,\tliterals:,\tnodes:, used_class_names:, avgmsgsize");
		IFC_Statistics_Grounding.fast_test_set("C:\\jo\\IFCtest_data\\drum_10.ifc");
		System.out.println("done successfully");

    }

}
