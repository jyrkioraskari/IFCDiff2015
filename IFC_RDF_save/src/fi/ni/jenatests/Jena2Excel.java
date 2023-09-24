package fi.ni.jenatests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import softhema.system.toolkits.ToolkitString;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class Jena2Excel {

    
    
    private void list_results(Model model)
	{
	
	          Map<String, Map<String,String>> resultmap=new HashMap<String, Map<String,String>>();   // <file, Map<test,value>>
		  String queryString =				
					"PREFIX ns: <http://drum/diff#> " +
					"PREFIX xml: <http://www.w3.org/2001/XMLSchema#> " +
					"SELECT * " +
					"WHERE " +
					"{" +
					   "?file ns:test_result ?result ." +
					   "?result ns:max_msg_size_percentage ?pers ." +
					   "?result ns:test ?testname ."+ 					
					"}";
			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();
			while(results.hasNext())
			{
	                    QuerySolution qs=results.next();
	                    String file=qs.get("file").toString();
	                    int last=file.lastIndexOf('/');
	                    if(last>0)
	                	file=file.substring(last+1);
	         	    file=ToolkitString.strReplaceLike(file, ".ifc","");
	         	    file=ToolkitString.strReplaceLike(file, "_"," ");
	         	    file=ToolkitString.strReplaceLike(file, "-"," ");
	         	    Map<String,String> filemap=resultmap.get(file);
	         	    if(filemap==null)
	         	    {
	         		filemap=new TreeMap<String,String>();
	         		resultmap.put(file, filemap);
	         	    }

	                    String testname=qs.get("testname").toString();

	                    last=testname.lastIndexOf('/');
	                    if(last>0)
	                	testname=testname.substring(last+1);
	                    String pers=qs.get("pers").toString();
	                    filemap.put(testname,pers);
			}
			qe.close();		

			    System.out.println("File;tbasic;tbasic_grounding;tbasic_max_and_Min;t5_max;t30_max_and_Min;tbasic_max_and_min_second_order_literals;t5_max_second_order_literals;t5_max_second_order_literalsPLA;t5_max_second_order_literals_unique_global_naming");

			for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
			    Map<String,String> filemap = entry.getValue();
			    StringBuffer sb=new StringBuffer();
			    sb.append(entry.getKey());
			    sb.append(";"+filemap.get("tbasic"));
			    sb.append(";"+filemap.get("tbasic_grounding"));
			    sb.append(";"+filemap.get("tbasic_max_and_min"));
			    sb.append(";"+filemap.get("t5_max"));
			    sb.append(";"+filemap.get("t30_max_and_min"));
			    sb.append(";"+filemap.get("tbasic_max_and_min_second_order_literals"));
			    sb.append(";"+filemap.get("t5_max_second_order_literals"));
			    sb.append(";"+filemap.get("t5_max_second_order_literalsPLA"));
			    sb.append(";"+filemap.get("t5_max_second_order_literals_unique_global_naming"));
			    
			    
			    System.out.println(sb.toString());
			}
	}
    
    
    private void list_results2(Model model)
	{
	
	          Map<String, Map<String,String>> resultmap=new HashMap<String, Map<String,String>>();   // <file, Map<test,value>>
		  String queryString =				
					"PREFIX ns: <http://drum/diff#> " +
					"PREFIX xml: <http://www.w3.org/2001/XMLSchema#> " +
					"SELECT * " +
					"WHERE " +
					"{" +
					   "?file ns:test_result ?result ." +
					   "?result ns:max_msg_size  ?max ." +
					   "?result ns:test ?testname ."+ 					
					"}";
			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();
			while(results.hasNext())
			{
	                    QuerySolution qs=results.next();
	                    String file=qs.get("file").toString();
	                    int last=file.lastIndexOf('/');
	                    if(last>0)
	                	file=file.substring(last+1);
	         	    file=ToolkitString.strReplaceLike(file, ".ifc","");
	         	    file=ToolkitString.strReplaceLike(file, "_"," ");
	         	    file=ToolkitString.strReplaceLike(file, "-"," ");
	         	    Map<String,String> filemap=resultmap.get(file);
	         	    if(filemap==null)
	         	    {
	         		filemap=new TreeMap<String,String>();
	         		resultmap.put(file, filemap);
	         	    }

	                    String testname=qs.get("testname").toString();

	                    last=testname.lastIndexOf('/');
	                    if(last>0)
	                	testname=testname.substring(last+1);
	                    String pers=qs.get("max").toString();
	                    filemap.put(testname,pers);
			}
			qe.close();		

			    System.out.println("File;tbasic;tbasic_grounding;tbasic_max_and_Min;t5_max;t30_max_and_Min;tbasic_max_and_min_second_order_literals;t5_max_second_order_literals;t5_max_second_order_literalsPLA;t5_max_second_order_literals_unique_global_naming");

			for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
			    Map<String,String> filemap = entry.getValue();
			    StringBuffer sb=new StringBuffer();
			    sb.append(entry.getKey());
			    sb.append(";"+filemap.get("tbasic"));
			    sb.append(";"+filemap.get("tbasic_grounding"));
			    sb.append(";"+filemap.get("tbasic_max_and_min"));
			    sb.append(";"+filemap.get("t5_max"));
			    sb.append(";"+filemap.get("t30_max_and_min"));
			    sb.append(";"+filemap.get("tbasic_max_and_min_second_order_literals"));
			    sb.append(";"+filemap.get("t5_max_second_order_literals"));
			    sb.append(";"+filemap.get("t5_max_second_order_literalsPLA"));
			    sb.append(";"+filemap.get("t5_max_second_order_literals_unique_global_naming"));
			    System.out.println(sb.toString());
			}
	}

    
    
    private void list_results3(Model model)
	{
	
	          Map<String, Map<String,String>> resultmap=new HashMap<String, Map<String,String>>();   // <file, Map<test,value>>
		  String queryString =				
					"PREFIX ns: <http://drum/diff#> " +
					"PREFIX xml: <http://www.w3.org/2001/XMLSchema#> " +
					"SELECT * " +
					"WHERE " +
					"{" +
					   "?file ns:test_result ?result ." +
					   "?result ns:msg_count ?msg_count ." +
					   "?result ns:test ?testname ."+ 					
					"}";
			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();
			while(results.hasNext())
			{
	                    QuerySolution qs=results.next();
	                    String file=qs.get("file").toString();
	                    int last=file.lastIndexOf('/');
	                    if(last>0)
	                	file=file.substring(last+1);
	         	    file=ToolkitString.strReplaceLike(file, ".ifc","");
	         	    file=ToolkitString.strReplaceLike(file, "_"," ");
	         	    file=ToolkitString.strReplaceLike(file, "-"," ");
	         	    Map<String,String> filemap=resultmap.get(file);
	         	    if(filemap==null)
	         	    {
	         		filemap=new TreeMap<String,String>();
	         		resultmap.put(file, filemap);
	         	    }

	                    String testname=qs.get("testname").toString();

	                    last=testname.lastIndexOf('/');
	                    if(last>0)
	                	testname=testname.substring(last+1);
	                    String pers=qs.get("msg_count").toString();
	                    filemap.put(testname,pers);
			}
			qe.close();		

			    System.out.println("File;tbasic;tbasic_grounding;tbasic_max_and_Min;t5_max;t30_max_and_Min;tbasic_max_and_min_second_order_literals;t5_max_second_order_literals;t5_max_second_order_literalsPLA;t5_max_second_order_literals_unique_global_naming");

			for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
			    Map<String,String> filemap = entry.getValue();
			    StringBuffer sb=new StringBuffer();
			    sb.append(entry.getKey());
			    sb.append(";"+filemap.get("tbasic"));
			    sb.append(";"+filemap.get("tbasic_grounding"));
			    sb.append(";"+filemap.get("tbasic_max_and_min"));
			    sb.append(";"+filemap.get("t5_max"));
			    sb.append(";"+filemap.get("t30_max_and_min"));
			    sb.append(";"+filemap.get("tbasic_max_and_min_second_order_literals"));
			    sb.append(";"+filemap.get("t5_max_second_order_literals"));
			    sb.append(";"+filemap.get("t5_max_second_order_literalsPLA"));
			    sb.append(";"+filemap.get("t5_max_second_order_literals_unique_global_naming"));
			    System.out.println(sb.toString());
			}
	}

    private void list_basic(Model model)
	{
	
	          Map<String, Map<String,String>> resultmap=new HashMap<String, Map<String,String>>();   // <file, Map<variable,value>>
	          SortedSet<String> variables=new TreeSet<String>();
		  String queryString =				
					"PREFIX ns: <http://drum/diff#> " +
					"PREFIX xml: <http://www.w3.org/2001/XMLSchema#> " +
					"SELECT * " +
					"WHERE " +
					"{" +
					   "?file ns:test_result ?result ." +
					   "optional {?file ns:crossing_path_link ?maxmsg_cross_link } ."+
					   "?file ns:msg_max_node_of_largest_neighborhood ?msg_max_node_of_largest_neighborhood ."+
					   "?file ns:msg_max_node_of_largest_neighborhood_not_yet_grounded ?msg_max_node_of_largest_neighborhood_not_yet_grounded ."+					   
					   "?result ns:max_outgoing_links  ?max_outgoing_links_from_entity_nodes ." +
					   "?result ns:max_incoming_links  ?max_incoming_links_to_entity_nodes ." +
					   "?result ns:max_outgoing_links_inclLiterals  ?max_outgoing_inc_literals ." +
					   "?result ns:avg_incoming_links  ?avg_incoming_links_to_entity_nodes ." +
					   "?result ns:avg_outgoing_links  ?avg_outgoing_links_from_entity_nodes ." +
					   "?result ns:avg_outgoing_links_inclLiterals  ?avg_outgoing_inc_literals ." +
					   "?result ns:max_outgoing_links_class  ?max_outgoing_links_class ." +
					   "?result ns:max_incoming_links_class  ?max_incoming_links_class ." +
                                           "?file ns:nodes ?nodes ."+
					   "?result ns:triplecount  ?triples ." +
					   "?result ns:guids  ?guids ." +
					   "?result ns:entity_count  ?entities ." +
					   "?result ns:literals  ?literals ."+
					"}";
			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();
			while(results.hasNext())
			{
	                    QuerySolution qs=results.next();
	                    String file=qs.get("file").toString();
	                    int last=file.lastIndexOf('/');
	                    if(last>0)
	                	file=file.substring(last+1);
	         	    file=ToolkitString.strReplaceLike(file, ".ifc","");
	         	    file=ToolkitString.strReplaceLike(file, "_"," ");
	         	    file=ToolkitString.strReplaceLike(file, "-"," ");
	         	    Map<String,String> filemap=resultmap.get(file);
	         	    if(filemap==null)
	         	    {
	         		filemap=new TreeMap<String,String>();
	         		resultmap.put(file, filemap);
	         	    }
                            Iterator it=qs.varNames();
                            while(it.hasNext())
                            {
                        	 String var=(String)it.next();
     	                         String value=qs.get(var).toString();
     	                         filemap.put(var,value);
     	                         if(var.equals("result"))
     	                         		continue;
     	                         if(var.equals("file"))
	                         		continue;
     	                         variables.add(var);
                            }
			}
			qe.close();		

			    StringBuffer sb1=new StringBuffer();
			    sb1.append("File");
			    Iterator<String> it1=variables.iterator();
			    while(it1.hasNext())
			    {
				String variable=it1.next();
				sb1.append(";"+variable);
			    }
			    System.out.println(sb1.toString());

			for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
			    Map<String,String> filemap = entry.getValue();
			    StringBuffer sb=new StringBuffer();
			    sb.append(entry.getKey());
			    Iterator<String> it=variables.iterator();
			    while(it.hasNext())
			    {
				String variable=it.next();
				String value=filemap.get(variable);
				if(value==null)
				    value="-";
					
				sb.append(";"+value);
			    }
			    System.out.println(sb.toString());
			}
	}

    private void list_grounding(Model model)
	{
	
	          Map<String, Map<String,String>> resultmap=new HashMap<String, Map<String,String>>();   // <file, Map<variable,value>>
	          SortedSet<String> variables=new TreeSet<String>();
		  String queryString =				
					"PREFIX ns: <http://drum/diff#> " +
					"PREFIX xml: <http://www.w3.org/2001/XMLSchema#> " +
					"SELECT * " +
					"WHERE " +
					"{" +
					   "?file ns:test_result ?result ." +
					   "?result ns:test <http://drum/diff#test/tbasic_max_and_min> ." + 

					   "?result ns:groudable_by_big_ins  ?groudable_by_big_ins ." +
					   "?result ns:groudable_by_big_outs ?groudable_by_big_outs ." +
					   "?result ns:groudable_by_small_ins ?groudable_by_small_ins ." +
					   "?result ns:groudable_by_small_outs ?groudable_by_small_outs ." +
					   "?result ns:maxmsg_mostcommon_class ?maxmsg_mostcommon_class ." +
					   "?result ns:maxmsg_secondcommon_property ?maxmsg_secondcommon_property ." +
					   
					"}";
			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();
			while(results.hasNext())
			{
	                    QuerySolution qs=results.next();
	                    String file=qs.get("file").toString();
	                    int last=file.lastIndexOf('/');
	                    if(last>0)
	                	file=file.substring(last+1);
	         	    file=ToolkitString.strReplaceLike(file, ".ifc","");
	         	    file=ToolkitString.strReplaceLike(file, "_"," ");
	         	    file=ToolkitString.strReplaceLike(file, "-"," ");
	         	    Map<String,String> filemap=resultmap.get(file);
	         	    if(filemap==null)
	         	    {
	         		filemap=new TreeMap<String,String>();
	         		resultmap.put(file, filemap);
	         	    }
                            Iterator it=qs.varNames();
                            while(it.hasNext())
                            {
                        	 String var=(String)it.next();
     	                         String value=qs.get(var).toString();
     	                         filemap.put(var,value);
     	                         if(var.equals("result"))
     	                         		continue;
     	                         if(var.equals("file"))
	                         		continue;
     	                         variables.add(var);
                            }
			}
			qe.close();		

			    StringBuffer sb1=new StringBuffer();
			    sb1.append("File");
			    Iterator<String> it1=variables.iterator();
			    while(it1.hasNext())
			    {
				String variable=it1.next();
				sb1.append(";"+variable);
			    }
			    System.out.println(sb1.toString());

			for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
			    Map<String,String> filemap = entry.getValue();
			    StringBuffer sb=new StringBuffer();
			    sb.append(entry.getKey());
			    Iterator<String> it=variables.iterator();
			    while(it.hasNext())
			    {
				String variable=it.next();
				sb.append(";"+filemap.get(variable));
			    }
			    System.out.println(sb.toString());
			}
	}
    
    public Jena2Excel()
    {
        Model model = ModelFactory.createDefaultModel();
	try {
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_t5_max.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_t30_max_and_min.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_tbasic.ttl"), "","TTL");
		
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_tbasic_grounding.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_tbasic_max_and_min.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_tbasic_max_and_min_second_order_literals.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_t5_max_second_order_literals.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/OK/statistics_t5_max_second_order_literalsPLA.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/statistics_t5_max_second_order_literals_unique_global_naming.ttl"), "","TTL");
		
		
		model.read(FileManager.get().open("c:\\jo/DRUM/statistics_crossing_paths.ttl"), "","TTL");
		model.read(FileManager.get().open("c:\\jo/DRUM/statistics_msg_max_node_of_largest_neighborhood.ttl"), "","TTL");

		System.out.println("File statistics:");
		list_basic(model);
		System.out.println();
		System.out.println("Max MSG size/triplecount:");
		list_results(model);
		System.out.println();
		System.out.println("Max MSG size:");
		list_results2(model);
		System.out.println();
		System.out.println("MSG count:");
		list_results3(model);
		System.out.println();
		System.out.println("Test: tbasic max and min, grounding counts:");		
		list_grounding(model);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
    
    public static void main(String[] args) {
	new Jena2Excel();
    }

}
