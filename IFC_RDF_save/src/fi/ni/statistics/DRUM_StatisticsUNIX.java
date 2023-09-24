package fi.ni.statistics;


public class DRUM_StatisticsUNIX {

    public static void main(String[] args) {
	       System.out.println("Without grounding:");
	       System.out.println(" Application; file;msgs;entities;max;triples;guids;literals;nodes; used_class_names; avgmsgsize; min_msg_guids; max_msg_guids; avg_msg_guids; min_rdfn_guids; max_rdfn_guids; avg_rdfn_guids");
	       new IFC_StatisticsWOGrounding(args[0]+"IFC2X3_TC1.exp", args[0]+args[1]);

    }

}
