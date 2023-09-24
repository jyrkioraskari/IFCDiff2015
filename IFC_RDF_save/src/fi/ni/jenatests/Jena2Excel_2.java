package fi.ni.jenatests;

import java.io.File;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.util.FileManager;

public class Jena2Excel_2 {

    
    
    private void list_basic1(Model model)
	{
	
	          Map<String, Map<String,String>> resultmap=new HashMap<String, Map<String,String>>();   // <file, Map<variable,value>>
	          SortedSet<String> variables=new TreeSet<String>();
		  String queryString =				
					"PREFIX ns: <http://drum/diff#> " +
					"PREFIX xml: <http://www.w3.org/2001/XMLSchema#> " +
					"SELECT * " +
					"WHERE " +
					"{" +
					   "?file ns:short_name ?name ." +
					   "optional {?file ns:test_result ?result.}" +
					   "optional {?file ns:created_by ?source }." +
					   "optional {?file ns:www ?www }."+					   
					   "optional {?file ns:type ?type } ." +
					   "optional {?result ns:types ?class_types } ." +
					   "optional {?result ns:triplecount ?triplecount } ." +
					   "?result ns:literals_new ?literals_in_triples  ." +
					   "optional {?result ns:guids ?guids } ." +
					   "optional {?result ns:entity_count ?ifc_entities } ." +
					   "optional {?result ns:application ?application } ." +					    
					   "?file ns:nodes ?uniq_nodes  ." +
					   "optional {?file ns:blanks ?blanks } ." +
					   "optional {?file ns:IRIs ?IRIs } ." +
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
				    value="\"-\"";
					
				sb.append(";"+value);
			    }
			    System.out.println(sb.toString());
			}
	}

    
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
    				   "optional{?result ns:test ?testname }."+
    				   "optional {?result ns:triplecount ?triplecount } ."+
    				"}";
    		Query query = QueryFactory.create(queryString);
    
    		QueryExecution qe = QueryExecutionFactory.create(query, model);
    		ResultSet results = qe.execSelect();
    		String triples;
    		while(results.hasNext())
    		{
                        QuerySolution qs=results.next();
                        triples=qs.get("triplecount").toString();
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
                        if(triples!=null)
                            filemap.put("triples",triples);
    		}
    		qe.close();		
    
		System.out.println("File;Triples; Without Grounding;Only ifcOwnerHistory grounded;Nodes with no links out grouded;Unique loops grounded;ifcGeometricRepresentationContext grounded;One-to-one links for grounding; Second_order_literals;Globally named  grounded;5Max only;All");
    
    		for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
    		    Map<String,String> filemap = entry.getValue();
    		    StringBuffer sb=new StringBuffer();
    		    sb.append(entry.getKey());
    		    String tm;
    		    tm=filemap.get("triples");    	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("tbasic");    	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#ifcOwnerHistory_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#No_output_nodes_grouded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Unique_loops_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#ifcGeometricRepresentationContext_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Use_One-to-one_links-for_grounding");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Second_order_literals");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);
    		        		
		    tm=filemap.get("diff#Globally_unique_naming");   	             		    
		    sb.append(";"); if(tm!=null) sb.append(tm);

		    tm=filemap.get("t5_max_only");   	             		    
		    sb.append(";"); if(tm!=null) sb.append(tm);

		    
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
    				   "optional {?result ns:triplecount ?triplecount } ."+
    				"}";
    		Query query = QueryFactory.create(queryString);
    
    		QueryExecution qe = QueryExecutionFactory.create(query, model);
    		ResultSet results = qe.execSelect();
    		String triples;
    		while(results.hasNext())
    		{
                        QuerySolution qs=results.next();
                        triples=qs.get("triplecount").toString();
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
                        if(triples!=null)
                         filemap.put("triples",triples);
    		}
    		qe.close();		
    
		System.out.println("File;Triples; Without Grounding;Only ifcOwnerHistory grounded;Nodes with no links out grouded;Unique loops grounded;ifcGeometricRepresentationContext grounded;One-to-one links for grounding; Second_order_literals;Globally named  grounded;5Max only;All");
		    
    		for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
    		    Map<String,String> filemap = entry.getValue();
    		    StringBuffer sb=new StringBuffer();
    		    sb.append(entry.getKey());
    		    String tm;
    		    tm=filemap.get("triples");    	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);
    		    
    		    tm=filemap.get("tbasic");    	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#ifcOwnerHistory_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#No_output_nodes_grouded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Unique_loops_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#ifcGeometricRepresentationContext_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Use_One-to-one_links-for_grounding");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Second_order_literals");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);
    		        		
		    tm=filemap.get("diff#Globally_unique_naming");   	             		    
		    sb.append(";"); if(tm!=null) sb.append(tm);

		    tm=filemap.get("t5_max_only");   	             		    
		    sb.append(";"); if(tm!=null) sb.append(tm);

		    
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
    				   "optional {?result ns:triplecount ?triplecount } ."+    				   
    				"}";
    		Query query = QueryFactory.create(queryString);
    
    		QueryExecution qe = QueryExecutionFactory.create(query, model);
    		ResultSet results = qe.execSelect();
    		String triples;
    		while(results.hasNext())
    		{
                        QuerySolution qs=results.next();
                        triples=qs.get("triplecount").toString();
                        
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
                        if(triples!=null)
                            filemap.put("triples",triples);
                        
    		}
    		qe.close();		
    
		System.out.println("File;Triples; Without Grounding;Only ifcOwnerHistory grounded;Nodes with no links out grouded;Unique loops grounded;ifcGeometricRepresentationContext grounded;One-to-one links for grounding; Second_order_literals;Globally named  grounded;5Max only;All");
		    
    		for (Map.Entry<String, Map<String,String>> entry : resultmap.entrySet()) {
    		    Map<String,String> filemap = entry.getValue();
    		    StringBuffer sb=new StringBuffer();
    		    sb.append(entry.getKey());
    		    String tm;
    		    tm=filemap.get("triples");    	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("tbasic");    	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#ifcOwnerHistory_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#No_output_nodes_grouded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Unique_loops_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#ifcGeometricRepresentationContext_grounded");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Use_One-to-one_links-for_grounding");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);

    		    tm=filemap.get("diff#Second_order_literals");   	             		    
    		    sb.append(";"); if(tm!=null) sb.append(tm);
    		        		
		    tm=filemap.get("diff#Globally_unique_naming");   	             		    
		    sb.append(";"); if(tm!=null) sb.append(tm);

		    tm=filemap.get("t5_max_only");   	             		    
		    sb.append(";"); if(tm!=null) sb.append(tm);

		    
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
    				   "optional {?file ns:msg_max_node_of_largest_neighborhood ?msg_max_node_of_largest_neighborhood }."+
    				   "optional {?file ns:msg_max_node_of_largest_neighborhood_not_yet_grounded ?msg_max_node_of_largest_neighborhood_not_yet_grounded }."+					   
    				   "?result ns:max_outgoing_links  ?max_outgoing_links_from_entity_nodes ." +
    				   "?result ns:max_incoming_links  ?max_incoming_links_to_entity_nodes ." +
    				   "?result ns:max_outgoing_links_inclLiterals  ?max_outgoing_inc_literals ." +
    				   "?result ns:avg_incoming_links  ?avg_incoming_links_to_entity_nodes ." +
    				   "?result ns:avg_outgoing_links  ?avg_outgoing_links_from_entity_nodes ." +
    				   "?result ns:avg_outgoing_links_inclLiterals  ?avg_outgoing_inc_literals ." +
    				   "?result ns:max_outgoing_links_class  ?max_outgoing_links_class ." +
    				   "?result ns:max_incoming_links_class  ?max_incoming_links_class ." +
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
    			   value="\"-\"";
    				
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


    public Jena2Excel_2()
    {
	
        Model model = ModelFactory.createDefaultModel();
	try {
	        File dir = new File("c:\\jo/DRUM/results/");
	        String[] chld = dir.list();
 	        if(chld == null)
 	        {
	          System.out.println("Specified directory does not exist or is not a directory.");
	          System.exit(0);
	        }
	        else
	        {
	          for(int i = 0; i < chld.length; i++)
	          {
	           String fileName = chld[i];
	           //System.out.println(fileName);
	           model.read(FileManager.get().open("c:\\jo/DRUM/results/"+fileName), "","TTL");
	          }
	        }
		//System.out.println("File statistics:");
		//list_basic1(model);
		
 	        
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
		
		/*System.out.println();
		System.out.println("Test: tbasic max and min, grounding counts:");		
		list_grounding(model);*/

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
    
    public static void main(String[] args) {
	new Jena2Excel_2();
    }

}
