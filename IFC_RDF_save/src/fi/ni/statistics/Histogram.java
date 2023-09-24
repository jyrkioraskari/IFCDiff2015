package fi.ni.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Histogram {
  String name;
  long triples;
  SortedMap<Integer,Long> msg_sizedata=new TreeMap<Integer,Long>();  // msg_size, count

  SortedMap<Integer,Long> classnameSet_count_data=new TreeMap<Integer,Long>();  // msg_size, count
  SortedMap<Integer,Long> blanks_count_data=new TreeMap<Integer,Long>();  // msg_size, count
  SortedMap<Integer,Long> guids_count_data=new TreeMap<Integer,Long>();  // msg_size, count
  SortedMap<Integer,Long> iris_count_data=new TreeMap<Integer,Long>();  // msg_size, count
  SortedMap<Integer,Long> literals_data=new TreeMap<Integer,Long>();  // msg_size, count
  SortedMap<Integer,Long> nodes_count_data=new TreeMap<Integer,Long>();  // msg_size, count
  
  public Histogram(String name) {
    super();
    this.name = name;
}

public void add_value(int msg_size)
  {
      triples+=msg_size;
      Long current_count=msg_sizedata.get(msg_size);
      if(current_count==null)
      {
	  current_count=new Long(0);
      }
      Long new_val=current_count+1l;
      msg_sizedata.put(msg_size, new_val);      
  }


private void add_dataval(SortedMap<Integer,Long> map,int msg_size,long val)
{
    Long current_count=map.get(msg_size);
    if(current_count==null)
    {
	  current_count=new Long(0);
    }
    Long new_val=current_count+val;
    map.put(msg_size, new_val);
}

public void add_value(int msg_size, int classnameSet_count,long blanks_count,long guids_count,long iris_count,long literals,long nodes_count)
{
    triples+=msg_size;
    Long current_count=msg_sizedata.get(msg_size);
    if(current_count==null)
    {
	  current_count=new Long(0);
    }
    Long new_val=current_count+1l;
    msg_sizedata.put(msg_size, new_val);
    
    add_dataval(classnameSet_count_data,msg_size,classnameSet_count);    
    add_dataval(blanks_count_data,msg_size,blanks_count);    
    add_dataval(guids_count_data,msg_size,guids_count);    
    add_dataval(iris_count_data,msg_size,iris_count);    
    add_dataval(literals_data,msg_size,literals);    
    add_dataval(nodes_count_data,msg_size,nodes_count);    
    
}

  public void listHistogram()
  {
        System.out.println(name);
	System.out.println("msg_size;count;msgsize/triples;triple share;avg class name count;avg blanks count;avg guids count;avg iris count;avg literal count;avg node count");
	for (Map.Entry<Integer,Long> entry : msg_sizedata.entrySet()) {
	    Integer msg_size=(Integer) entry.getKey();
	    Long count = (Long) entry.getValue();
	    
	    Long classnameSet_count=classnameSet_count_data.get(msg_size);   
	    Long blanks_count=blanks_count_data.get(msg_size);
	    Long guids_count=guids_count_data.get(msg_size);    
	    Long iris_count=iris_count_data.get(msg_size);    
	    Long literals=literals_data.get(msg_size);   
	    Long nodes=nodes_count_data.get(msg_size);    

	    double avg_classnameSet_count=((double)classnameSet_count)/((double)count);   
	    double avg_blanks_count=((double)blanks_count)/((double)count);
	    double avg_guids_count=((double)guids_count)/((double)count);    
	    double avg_iris_count=((double)iris_count)/((double)count);    
	    double avg_literals=((double)literals)/((double)count);  
	    double avg_nodes=((double)nodes)/((double)count);    
            /*
            System.out.println("count:"+count);
            System.out.println("literals:"+literals);
            System.out.println("avg_literals:"+avg_literals);
            System.out.println("guids_count:"+guids_count);
            System.out.println("avg_guids_count:"+avg_guids_count);

            System.out.println("nodes_count:"+nodes);
            System.out.println("avg_nodes:"+avg_nodes);
            */
	    double  value=((double)count)*((double)msg_size)/((double)triples);
	    double  key=((double)msg_size)/((double)triples);	
	    System.out.println(msg_size+";"+ count+";"+f(key) +";"+f(value)	    +";"+f(avg_classnameSet_count)   
		    +";"+f(avg_blanks_count)
		    +";"+f(avg_guids_count)    
		    +";"+f(avg_iris_count)    
		    +";"+f(avg_literals)  
		    +";"+f(avg_nodes)    );
	}
  }
  
  
  public void listSimpleHistogram()
  {
        System.out.println(name);
	System.out.println("msg_size;count;msgsize/triples;triple share;");
	
	for (Map.Entry<Integer,Long> entry : msg_sizedata.entrySet()) {
	    Integer msg_size=(Integer) entry.getKey();
	    Long count = (Long) entry.getValue();
	    
	    double  value=((double)count)*((double)msg_size)/((double)triples);
	    double  key=((double)msg_size)/((double)triples);	
	    System.out.println(msg_size+";"+ count+";"+f(key) +";"+f(value));
	}
  }

  public static String f(double d)
  {
	DecimalFormat formatter = new DecimalFormat("#0.00000");
	DecimalFormatSymbols ds=new DecimalFormatSymbols();
	ds.setDecimalSeparator(',');
      formatter.setDecimalFormatSymbols(ds);
	return formatter.format(d);
  }

  
// Tests
public static void main(String[] args) {
   Histogram his=new Histogram("testing");
   his.add_value(2);
   his.add_value(2);
   his.add_value(3);
   his.add_value(1);
   his.listSimpleHistogram();
}
  
}